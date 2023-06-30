package com.sun.tools.javac.comp;

import com.sun.source.util.*;
import com.sun.source.tree.*;
import com.sun.tools.javac.api.*;
import com.sun.tools.javac.code.*;
import com.sun.tools.javac.main.*;
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
            open.invoke(compilerModule, "com.sun.tools.javac.main", fluentModule);
            open.invoke(compilerModule, "com.sun.tools.javac.tree", fluentModule);
            open.invoke(compilerModule, "com.sun.tools.javac.util", fluentModule);
            open.invoke(baseModule, "java.lang", fluentModule);

            // reload extended classes using the package classloader
            // this is necessary to override methods in the extended class
            String name = FluentExtension.class.getName();
            InputStream is = Fluent.class.getClassLoader().getResourceAsStream(
                    name.replace('.', '/') + ".class");
            byte[] bytes = new byte[is.available()];
            is.read(bytes);
            Method m = ClassLoader.class.getDeclaredMethod("defineClass", new Class[] {
                    String.class, byte[].class, int.class, int.class });
            m.setAccessible(true);
            Class fluentClass = (Class) m.invoke(Attr.class.getClassLoader(), 
                    name, bytes, 0, bytes.length);

            // patch extended classes into the compiler global state
            Context context = ((BasicJavacTask) task).getContext();
            Object fluent = fluentClass.getDeclaredMethod("instance", Context.class)
                    .invoke(null, context);
            for (Object component : List.of(
                    JavaCompiler.instance(context),
                    MemberEnter.instance(context), 
                    Resolve.instance(context),
                    ArgumentAttr.instance(context),
                    DeferredAttr.instance(context),
                    Analyzer.instance(context))) {
                Field field = component.getClass().getDeclaredField("attr");
                field.setAccessible(true);
                field.set(component, fluent);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static class FluentExtension extends Attr {
        Context context;

        protected FluentExtension(Context context) {
            super(context);
            this.context = context;
        }
        public static FluentExtension instance(Context context) {
            context.put(attrKey, (FluentExtension) null); // superclass constructor will put it back
            return new FluentExtension(context);
        }
        
        @Override
        public void visitApply(JCMethodInvocation tree) {
            if (tree.getMethodSelect() instanceof JCFieldAccess) {
                JCFieldAccess lhs = (JCFieldAccess) tree.getMethodSelect();
                if (lhs.getIdentifier().toString().endsWith("EX")) {
                    tree.args = tree.args.prepend(lhs.getExpression());
                    tree.meth = TreeMaker.instance(context).at(tree.pos).Ident(lhs.getIdentifier());
                }
            }
            super.visitApply(tree);
        }
    }

    @Override public String getName() { return "fluent"; }
}
