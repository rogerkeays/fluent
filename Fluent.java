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
import com.sun.tools.javac.util.JCDiagnostic.Error;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.util.Map;

public class Fluent implements Plugin {
    @Override
    public void init(JavacTask task, String... args) {
        try {
            // open access to the compiler packages
            // requires -J--add-opens=java.base/java.lang=ALL-UNNAMED
            Module unnamedModule = Fluent.class.getModule();
            Module compilerModule = ModuleLayer.boot().findModule("jdk.compiler").get();
            Module baseModule = ModuleLayer.boot().findModule("java.base").get();
            Method opener = Module.class.getDeclaredMethod("implAddOpens", String.class, Module.class);
            opener.setAccessible(true);
            for (String packg : new String[] {
                    "com.sun.tools.javac.api",
                    "com.sun.tools.javac.code",
                    "com.sun.tools.javac.comp",
                    "com.sun.tools.javac.jvm",
                    "com.sun.tools.javac.main",
                    "com.sun.tools.javac.model",
                    "com.sun.tools.javac.parser",
                    "com.sun.tools.javac.processing",
                    "com.sun.tools.javac.util",
                    "com.sun.tools.javac.util" }) {
                opener.invoke(compilerModule, packg, unnamedModule);
            }

            // patch extended classes into the compiler context
            Context context = ((BasicJavacTask) task).getContext();
            reload(AbsentMethodException.class);
            Object resolve = instance(reload(FluentResolve.class), context);
            Object log = instance(reload(FluentLog.class), context);
            Object attr = instance(reload(FluentAttr.class), context);
            Map singletons = (Map) getProtected(context, "ht");
            for (Object component : singletons.values()) {
                if (component != null) {
                    try { setProtected(component, "attr", attr); } catch (NoSuchFieldException e) {}
                    try { setProtected(component, "rs", resolve); } catch (NoSuchFieldException e) {}
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    // reload a declared class using the jdk.compiler classloader
    // this is necessary to be considered part of the same package
    // otherwise we cannot override package/protected methods
    Class<?> reload(Class klass) throws Exception {
        java.io.InputStream is = Fluent.class.getClassLoader().getResourceAsStream(
                klass.getName().replace('.', '/') + ".class");
        byte[] bytes = new byte[is.available()];
        is.read(bytes);
        Method defineClass = ClassLoader.class.getDeclaredMethod("defineClass", new Class[] {
                String.class, byte[].class, int.class, int.class });
        defineClass.setAccessible(true);
        try {
            return (Class) defineClass.invoke(Context.class.getClassLoader(),
                    klass.getName(), bytes, 0, bytes.length);
        } catch (InvocationTargetException e) {
            return klass; // jshell hack: class already reloaded, but no way to tell
        }
    }

    // use reflection to inject components into final/private fields
    void inject(Class klass, String field, Object value, Context context) throws Exception {
        Field f = klass.getDeclaredField(field);
        f.setAccessible(true);
        f.set(instance(klass, context), value);
    }

    // get the singleton of a class for a given context
    Object instance(Class<?> klass, Context context) throws Exception {
        return klass.getDeclaredMethod("instance", Context.class).invoke(null, context);
    }

    // get a value from an inaccessible field
    private Object getProtected(Object object, String field) throws Exception {
        Field f = object.getClass().getDeclaredField(field);
        f.setAccessible(true);
        return f.get(object);
    }

    // set a value for an inaccessible field
    private void setProtected(Object object, String field, Object value) throws Exception {
        Field f = object.getClass().getDeclaredField(field);
        f.setAccessible(true);
        f.set(object, value);
    }

    public static class FluentAttr extends Attr {
        Context context;

        public FluentAttr(Context context) {
            super(context);
            this.context = context;
        }
        public static FluentAttr instance(Context context) {
            Attr current = (Attr) context.get(attrKey);
            if (current != null && current instanceof FluentAttr) {
                return (FluentAttr) current;
            } else {
                // superclass constructor will register the singleton
                context.put(attrKey, (FluentAttr) null);
                return new FluentAttr(context);
            }
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
            Resolve current = (Resolve) context.get(resolveKey);
            if (current != null && current instanceof FluentResolve) {
                return (FluentResolve) current;
            } else {
                // superclass constructor will register the singleton
                context.put(resolveKey, (Resolve) null);
                return new FluentResolve(context);
            }
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
            Log current = (Log) context.get(logKey);
            if (current instanceof FluentLog) {
                return (FluentLog) current;
            } else {
                // superclass constructor will register the singleton
                context.put(logKey, (Log) null);
                return new FluentLog(context);
            }
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
