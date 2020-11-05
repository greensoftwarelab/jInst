package jInst.profiler.trepn

import com.github.javaparser.ast.ImportDeclaration
import com.github.javaparser.ast.expr.Expression
import jInst.util.convertMethodCall
import kastree.ast.Node


class TrepnTestOrientedProfilerAdapter ( val tt : TrepnTestOrientedProfiler )  {


     fun startProfiler(context: Expression): Node.Expr {
       return  convertMethodCall(tt.startProfiler(context))
    }

     fun stopProfiler(context: Expression):  Node.Expr {
         return  convertMethodCall(tt.stopProfiler(context))
    }

     fun getLibrary(): ImportDeclaration {
        return tt.library
    }

     fun getContext(): Expression? {
       return tt.context
    }

     fun marKTest(context: Expression, method: String):  Node.Expr {
         return  convertMethodCall(tt.marKTest(context, method))
    }

     fun markMethod(context: Expression, method: String):  Node.Expr {
         return  convertMethodCall(tt.markMethod(context, method))
    }

    fun getAllowPermissionCalls() : List<Node.Stmt> {
        return tt.allowPermissionCalls.map { x->  Node.Stmt.Expr( convertMethodCall(x) ) }
    }
}