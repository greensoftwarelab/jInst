package jInst.Instrumentation.profiler;

import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import jInst.Instrumentation.Instrumenter;

import java.util.List;
/**
 * Created by rrua on 17/06/17.
 */
public interface Profiler extends Instrumenter { // abstract Product
    MethodCallExpr startProfiler(Expression context);
    MethodCallExpr stopProfiler(Expression context);
    ImportDeclaration getLibrary();
    MethodCallExpr getContext();
    MethodCallExpr marKTest(Expression context, String method);
    List<MethodCallExpr> getAllowPermissionCalls();
}
