package jamaica.fluent;

import com.sun.source.util.*;
import com.sun.source.tree.*;
import com.sun.tools.javac.api.*;
import com.sun.tools.javac.code.*;
import com.sun.tools.javac.comp.*;
import com.sun.tools.javac.tree.JCTree.*;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.*;
import com.sun.tools.javac.util.JCDiagnostic.DiagnosticPosition;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import sun.misc.Unsafe;

public class fluent implements Plugin {
    public fluent() {
        try {
            // open access to compiler internals
            Module fluentModule = fluent.class.getModule();
            Module compilerModule = ModuleLayer.boot().findModule("jdk.compiler").get();
            Method open = Module.class.getDeclaredMethod("implAddOpens", String.class, Module.class);
            Field f = Unsafe.class.getDeclaredField("theUnsafe"); f.setAccessible(true);
            Unsafe unsafe = (Unsafe) f.get(null);
            unsafe.putBoolean(open, 12, true); // make it public
            open.invoke(compilerModule, "com.sun.tools.javac.api", fluentModule);
            open.invoke(compilerModule, "com.sun.tools.javac.code", fluentModule);
            open.invoke(compilerModule, "com.sun.tools.javac.comp", fluentModule);
            open.invoke(compilerModule, "com.sun.tools.javac.tree", fluentModule);
            open.invoke(compilerModule, "com.sun.tools.javac.util", fluentModule);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override public void init(JavacTask task, String... args) {

        // patch the compiler to use our custom resolver
        // we can only override public methods
        Context context = ((BasicJavacTask) task).getContext();
        try {
            Resolve resolver = ResolveExtensions.instance(context);
            Field field = Attr.class.getDeclaredField("rs");
            field.setAccessible(true);
            field.set(Attr.instance(context), resolver);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        // transform the ast when an extension method (ending in EX) is called
        TreeMaker make = TreeMaker.instance(context);
        task.addTaskListener(new TaskListener() {
            public void started(TaskEvent e) {
                System.out.println(">>>> " + e.getKind());
            }
            public void finished(TaskEvent e) {
                if (e.getKind() == TaskEvent.Kind.PARSE) {
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
        });
    }

    @Override public String getName() { return "fluent"; }

    public static class ResolveExtensions extends Resolve {
        protected ResolveExtensions(Context context) {
            super(context);
        }
        public static ResolveExtensions instance(Context context) {
            context.put(resolveKey, (Resolve) null);
            return new ResolveExtensions(context);
        }

        @Override
        public boolean isAccessible(Env<AttrContext> env,
              Type site,
              Symbol sym,
              boolean checkInner) {
            System.out.println("isAccessible " + sym);
            return super.isAccessible(env, site, sym, checkInner);

        }

        @Override
        public Symbol.VarSymbol resolveInternalField(DiagnosticPosition pos, Env<AttrContext> env,
                                              Type site, Name name) {
            System.out.println("resolveInternalField " + name);
            return super.resolveInternalField(pos, env, site, name);
        }

        @Override
        public Symbol.MethodSymbol resolveInternalMethod(DiagnosticPosition pos, Env<AttrContext> env,
                                        Type site, Name name,
                                        List<Type> argtypes,
                                        List<Type> typeargtypes) {
            System.out.println("resolveInternalMethod " + name);
            return super.resolveInternalMethod(pos, env, site, name, argtypes, typeargtypes);
        }
    }
}
