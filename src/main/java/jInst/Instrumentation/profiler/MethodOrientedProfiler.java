package jInst.Instrumentation.profiler;

import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;

/**
 * Created by rrua on 17/06/17.
 */
public interface MethodOrientedProfiler extends Profiler{ //  product

    MethodCallExpr markMethodStart(Expression context, String method);

    MethodCallExpr marKMethodStop(Expression context, String method);

    MethodCallExpr marKTest(Expression context, String method);
}

