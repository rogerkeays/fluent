package com.sun.tools.javac.comp;

import com.sun.source.util.*;
import com.sun.source.tree.*;
import com.sun.tools.javac.api.*;
import com.sun.tools.javac.code.*;
import com.sun.tools.javac.main.JavaCompiler;
import com.sun.tools.javac.tree.JCTree.*;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.*;
import com.sun.tools.javac.util.JCDiagnostic.DiagnosticPosition;
import com.sun.tools.javac.util.JCDiagnostic.Error;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.io.InputStream;
import jdk.internal.misc.Unsafe;

public class Fluent implements Plugin {
    @Override 
    public void init(JavacTask task, String... args) {
        try {
            // open access to compiler internals, bypassing module restrictions
            Module unnamedModule = Fluent.class.getModule();
            Module compilerModule = ModuleLayer.boot().findModule("jdk.compiler").get();
            Module baseModule = ModuleLayer.boot().findModule("java.base").get();
            Method open = Module.class.getDeclaredMethod("implAddOpens", String.class, Module.class);
            Field f = Unsafe.class.getDeclaredField("theUnsafe"); f.setAccessible(true);
            Unsafe unsafe = (Unsafe) f.get(null);
            unsafe.putBoolean(open, 12, true); // make impleAddOpens public
            open.invoke(compilerModule, "com.sun.tools.javac.api", unnamedModule);
            open.invoke(compilerModule, "com.sun.tools.javac.code", unnamedModule);
            open.invoke(compilerModule, "com.sun.tools.javac.comp", unnamedModule);
            open.invoke(compilerModule, "com.sun.tools.javac.main", unnamedModule);
            open.invoke(compilerModule, "com.sun.tools.javac.tree", unnamedModule);
            open.invoke(compilerModule, "com.sun.tools.javac.util", unnamedModule);
            open.invoke(baseModule, "java.lang", unnamedModule);

            // patch extended classes into the compiler context
            Context context = ((BasicJavacTask) task).getContext();
            reload(AbsentMethodException.class, context);
            Object resolve = instance(reload(FluentResolve.class, context), context);
            Object log = instance(reload(FluentLog.class, context), context);
            inject(Attr.class, "log", log, context);
            Object attr = instance(reload(FluentAttr.class, context), context);
            inject(JavaCompiler.class, "attr", attr, context);
            inject(ArgumentAttr.class, "attr", attr, context);
            inject(DeferredAttr.class, "attr", attr, context);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // reload a class using the jdk.compiler classloader
    // this is necessary to be considered part of the same package
    // otherwise we cannot override package/protected methods
    Class reload(Class klass, Context context) throws Exception {
        InputStream is = Fluent.class.getClassLoader().getResourceAsStream(
                klass.getName().replace('.', '/') + ".class");
        byte[] bytes = new byte[is.available()];
        is.read(bytes);
        Method defineClass = ClassLoader.class.getDeclaredMethod("defineClass", new Class[] {
                String.class, byte[].class, int.class, int.class });
        defineClass.setAccessible(true);
        try {
            return (Class) defineClass.invoke(JavaCompiler.class.getClassLoader(), 
                    klass.getName(), bytes, 0, bytes.length);
        } catch (InvocationTargetException e) {
            return klass; // jshell hack: class already reloaded, but no way to tell
        }
    }

    // get the singleton of a class for a given context
    Object instance(Class<?> klass, Context context) throws Exception {
        return klass.getDeclaredMethod("instance", Context.class).invoke(null, context);
    }

    // use reflection to inject components into final/private fields
    void inject(Class klass, String field, Object value, Context context) throws Exception {
        Field f = klass.getDeclaredField(field);
        f.setAccessible(true);
        f.set(instance(klass, context), value);
    }

    public static class FluentAttr extends Attr {
        Context context;

        protected FluentAttr(Context context) {
            super(context);
            this.context = context;
        }
        public static FluentAttr instance(Context context) {
            context.put(attrKey, (FluentAttr) null); // superclass constructor will put it back
            return new FluentAttr(context);
        }
        
        // transform the abstract syntax tree when an object method is not found
        @Override
        public void visitApply(JCMethodInvocation tree) {
            try {
                super.visitApply(tree);
            } catch (AbsentMethodException e) {
                JCFieldAccess lhs = (JCFieldAccess) tree.getMethodSelect();
                tree.args = tree.args.prepend(lhs.getExpression());
                tree.meth = TreeMaker.instance(context).at(tree.pos).Ident(lhs.getIdentifier());
                super.visitApply(tree);
            }
        }
    }

    public static class FluentResolve extends Resolve {
        protected FluentResolve(Context context) {
            super(context);
        }
        public static FluentResolve instance(Context context) {
            context.put(resolveKey, (Resolve) null); // superclass constructor will put it back
            return new FluentResolve(context);
        }

        // throw an exception when an object method is not found, causing a transformation
        @Override
        Symbol findMethod(Env<AttrContext> env, Type site, Name name, List<Type> argtypes,
                          List<Type> typeargtypes, boolean boxing, boolean varargs) {
            Symbol symbol = super.findMethod(env, site, name, argtypes, typeargtypes, boxing, varargs);
            if (symbol.kind == Kinds.Kind.ABSENT_MTH
                    && env.tree instanceof JCMethodInvocation 
                    && ((JCMethodInvocation) env.tree).getMethodSelect() instanceof JCFieldAccess) {
                throw new AbsentMethodException();
            }
            return symbol;
        }
    }

    public static class FluentLog extends Log {
        Context context;

        protected FluentLog(Context context) {
            super(context);
            this.context = context;
        }
        public static FluentLog instance(Context context) {
            context.put(logKey, (Log) null); // superclass constructor will put it back
            return new FluentLog(context);
        }

        // throw an exception when a primitive is referenced, causing a transformation
        @Override
        public void report(JCDiagnostic diagnostic) {
            if (diagnostic.getCode().equals("compiler.err.cant.deref")) {
                throw new AbsentMethodException();
            }
            super.report(diagnostic);
        }
    }

    public static class AbsentMethodException extends RuntimeException {}

    @Override public String getName() { return "fluent"; }
}
