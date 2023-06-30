package com.sun.tools.javac.comp;

import com.sun.source.util.*;
import com.sun.source.tree.*;
import com.sun.tools.javac.api.*;
import com.sun.tools.javac.code.*;
import com.sun.tools.javac.tree.JCTree.*;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.*;
import com.sun.tools.javac.util.JCDiagnostic.DiagnosticPosition;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.io.InputStream;
import sun.misc.Unsafe;

public class Fluent implements Plugin {
    @Override 
    public void init(JavacTask task, String... args) {
        try {
            // open access to compiler internals, bypassing module restrictions
            Module fluentModule = Fluent.class.getModule();
            Module compilerModule = ModuleLayer.boot().findModule("jdk.compiler").get();
            Module baseModule = ModuleLayer.boot().findModule("java.base").get();
            Method open = Module.class.getDeclaredMethod("implAddOpens", String.class, Module.class);
            Field f = Unsafe.class.getDeclaredField("theUnsafe"); f.setAccessible(true);
            Unsafe unsafe = (Unsafe) f.get(null);
            unsafe.putBoolean(open, 12, true); // make it public
            open.invoke(compilerModule, "com.sun.tools.javac.api", fluentModule);
            open.invoke(compilerModule, "com.sun.tools.javac.code", fluentModule);
            open.invoke(compilerModule, "com.sun.tools.javac.comp", fluentModule);
            open.invoke(compilerModule, "com.sun.tools.javac.tree", fluentModule);
            open.invoke(compilerModule, "com.sun.tools.javac.util", fluentModule);
            open.invoke(baseModule, "java.lang", fluentModule);

            // reload extended classes using the package classloader
            // this is necessary to override methods in the extended class
            String name = ResolveExtensions.class.getName();
            InputStream is = Fluent.class.getClassLoader().getResourceAsStream(
                    name.replace('.', '/') + ".class");
            byte[] bytes = new byte[is.available()];
            is.read(bytes);
            Method m = ClassLoader.class.getDeclaredMethod("defineClass", new Class[] {
                    String.class, byte[].class, int.class, int.class });
            m.setAccessible(true);
            Class resolverClass = (Class) m.invoke(Resolve.class.getClassLoader(), 
                    name, bytes, 0, bytes.length);

            // patch extended classes into the compiler context
            Context context = ((BasicJavacTask) task).getContext();
            Resolve resolver = (Resolve) resolverClass.getDeclaredMethod("instance", Context.class)
                    .invoke(null, context);
            Field field = Attr.class.getDeclaredField("rs");
            field.setAccessible(true);
            field.set(Attr.instance(context), resolver);

            // transform the ast when an extension method (ending in EX) is called
            TreeMaker make = TreeMaker.instance(context);
            task.addTaskListener(new TaskListener() {
                public void started(TaskEvent e) {
                    System.out.println(">>>> " + e.getKind());
                    if (e.getKind() == TaskEvent.Kind.ANALYZE) {
                        e.getCompilationUnit().accept(new TreeScanner<Void, Void>() {
                            @Override public Void visitMethodInvocation(MethodInvocationTree node, Void x) {
                                if (node.getMethodSelect() instanceof JCFieldAccess)  {
                                    JCMethodInvocation call = (JCMethodInvocation) node;
                                    JCFieldAccess lhs = (JCFieldAccess) node.getMethodSelect();
                                    if (lhs.getIdentifier().toString().endsWith("EX")) {
                                        call.meth = make.at(call.pos).Ident(lhs.getIdentifier());
                                        call.args = call.args.prepend(lhs.getExpression());
                                    }
                                }
                                return super.visitMethodInvocation(node, x);
                            }
                        }, null);
                    }
                }
                public void finished(TaskEvent e) {}
            });
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override public String getName() { return "fluent"; }

    public static class ResolveExtensions extends Resolve {
        protected ResolveExtensions(Context context) {
            super(context);
        }
        public static ResolveExtensions instance(Context context) {
            context.put(resolveKey, (Resolve) null); // superclass constructor will put it back
            return new ResolveExtensions(context);
        }

        @Override
        Symbol findMethod(Env<AttrContext> env,
                      Type site,
                      Name name,
                      List<Type> argtypes,
                      List<Type> typeargtypes,
                      boolean allowBoxing,
                      boolean useVarargs) {
            System.out.println("findMethod " + name);
            return super.findMethod(env, site, name, argtypes, typeargtypes, allowBoxing, useVarargs);
        }
    }
}
