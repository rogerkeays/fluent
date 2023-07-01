package com.sun.tools.javac.comp;

import com.sun.source.util.*;
import com.sun.source.tree.*;
import com.sun.tools.javac.api.*;
import com.sun.tools.javac.code.*;
import com.sun.tools.javac.main.JavaCompiler;
import com.sun.tools.javac.tree.JCTree.*;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.*;
import java.lang.reflect.MalformedParametersException;
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
            open.invoke(compilerModule, "com.sun.tools.javac.comp", fluentModule);
            open.invoke(compilerModule, "com.sun.tools.javac.main", fluentModule);
            open.invoke(compilerModule, "com.sun.tools.javac.tree", fluentModule);
            open.invoke(compilerModule, "com.sun.tools.javac.util", fluentModule);
            open.invoke(baseModule, "java.lang", fluentModule);

            // patch extended classes into the compiler context
            Context context = ((BasicJavacTask) task).getContext();
            Object resolve = bootstrap(FluentResolve.class, context);
            Object attr = bootstrap(FluentAttr.class, context);
            inject(JavaCompiler.class, "attr", attr, context);
            inject(ArgumentAttr.class, "attr", attr, context);
            inject(DeferredAttr.class, "attr", attr, context);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // reload an extended class using the jdk.compiler classloader
    // this is necessary to be considered part of the same package
    // otherwise we cannot override package/protected methods
    // returns the singleton instance of the reloaded class
    Object bootstrap(Class klass, Context context) throws Exception {
        InputStream is = klass.getClassLoader().getResourceAsStream(
                klass.getName().replace('.', '/') + ".class");
        byte[] bytes = new byte[is.available()];
        is.read(bytes);
        Method m = ClassLoader.class.getDeclaredMethod("defineClass", new Class[] {
                String.class, byte[].class, int.class, int.class });
        m.setAccessible(true);
        Class newKlass = (Class) m.invoke(JavaCompiler.class.getClassLoader(), 
                klass.getName(), bytes, 0, bytes.length);
        return instance(newKlass, context);
    }
    
    // get the singleton of a class for a given context
    Object instance(Class klass, Context context) throws Exception {
        return klass.getDeclaredMethod("instance", Context.class).invoke(null, context);
    }

    // use reflection to inject components into final/private fields
    void inject(Class klass, String field, Object value, Context context) throws Exception {
        Field f = klass.getDeclaredField(field);
        f.setAccessible(true);
        f.set(instance(klass, context), value);
    }

    public static class FluentResolve extends Resolve {
        protected FluentResolve(Context context) {
            super(context);
        }
        public static FluentResolve instance(Context context) {
            context.put(resolveKey, (Resolve) null); // superclass constructor will put it back
            return new FluentResolve(context);
        }

        // if we fail to find a class method, but find a static extension method,
        // throw an exception to instruct the attributor to transform the AST
        @Override
        Symbol findMethod(Env<AttrContext> env, Type site, Name name, List<Type> argtypes,
                          List<Type> typeargtypes, boolean allowBoxing, boolean useVarargs) {
            Symbol symbol = super.findMethod(env, site, name, argtypes, typeargtypes, 
                    allowBoxing, useVarargs);
            if (symbol.kind == Kinds.Kind.ABSENT_MTH && env.tree instanceof JCMethodInvocation 
                    && ((JCMethodInvocation) env.tree).getMethodSelect() instanceof JCFieldAccess) {
                throw new MalformedParametersException();
            }
            return symbol;
        }
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
        
        // transform the abstract syntax tree when instructed to by the resolver
        @Override
        public void visitApply(JCMethodInvocation tree) {
            try {
                super.visitApply(tree);
            } catch (MalformedParametersException e) {
                JCFieldAccess lhs = (JCFieldAccess) tree.getMethodSelect();
                tree.args = tree.args.prepend(lhs.getExpression());
                tree.meth = TreeMaker.instance(context).at(tree.pos).Ident(lhs.getIdentifier());
                super.visitApply(tree);
            }
        }
    }

    @Override public String getName() { return "fluent"; }
}
