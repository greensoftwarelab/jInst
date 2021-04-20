package jInst.Instrumentation.profiler;

import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;

/**
 * Created by rrua on 17/06/17.
 */
public interface TestOrientedProfiler extends Profiler { // abstract product

//    MethodCallExpr startProfiler(MethodCallExpr context);
//    MethodCallExpr stopProfiler(MethodCallExpr context);
    MethodCallExpr markMethod(Expression context, String method);
    MethodCallExpr markTest(Expression context, String method);


}

