package jamaica.fluent;

import com.sun.source.util.*;
import com.sun.source.tree.*;
import com.sun.tools.javac.api.*;
import com.sun.tools.javac.tree.JCTree.*;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.*;
import java.lang.reflect.*;
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
            open.invoke(compilerModule, "com.sun.tools.javac.tree", fluentModule);
            open.invoke(compilerModule, "com.sun.tools.javac.util", fluentModule);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // transform the ast when an extension method is called
    @Override public void init(JavacTask task, String... args) {
        TreeMaker make = TreeMaker.instance(((BasicJavacTask) task).getContext());
        task.addTaskListener(new TaskListener() {
            public void started(TaskEvent e) {}
            public void finished(TaskEvent e) {
                if (e.getKind() == TaskEvent.Kind.PARSE) {
                    e.getCompilationUnit().accept(new TreeScanner<Void, Void>() {
                        @Override public Void visitMethodInvocation(MethodInvocationTree node, Void x) {
                            if (node.getMethodSelect() instanceof JCFieldAccess)  {
                                JCMethodInvocation call = (JCMethodInvocation) node;
                                JCFieldAccess lhs = (JCFieldAccess) node.getMethodSelect();
                                if (lhs.getIdentifier().toString().equals("duplicate")) {
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
}
