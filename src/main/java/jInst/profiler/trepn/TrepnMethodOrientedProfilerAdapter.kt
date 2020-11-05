package jInst.profiler.trepn

import com.github.javaparser.ast.ImportDeclaration
import com.github.javaparser.ast.expr.Expression

import jInst.util.convertMethodCall

import kastree.ast.Node
import java.util.ArrayList


class TrepnMethodOrientedProfilerAdapter ( val tp : TrepnMethodOrientedProfiler ) {

    fun markMethodStart(context: Expression, method: String): Node.Expr {
        return  convertMethodCall(  tp.markMethodStart(context,method))
    }
    fun marKMethodStop(context: Expression, method: String):  Node.Expr {
        return  convertMethodCall(  tp.marKMethodStop(context,method))
    }

     fun marKTest(context: Expression, method: String): Node.Expr {
         return  convertMethodCall(  tp.marKTest(context,method))
     }

    fun startProfiler(context: Expression): Node.Expr {
        return  convertMethodCall(  tp.startProfiler(context))
    }
    fun stopProfiler(context: Expression): Node.Expr {
        return  convertMethodCall(  tp.stopProfiler(context))
    }

    fun getAllowPermissionCalls() : List<Node.Stmt> {
        return tp.allowPermissionCalls.map { x->  Node.Stmt.Expr( convertMethodCall(x) ) }
    }

    /*
    public getAllowPermissionCalls() : List<> {
        List<MethodCallExpr> l = new ArrayList<>();
        l.add(TrepnAux.getReadPermissions());
        l.add(TrepnAux.getWritePermissions());
        l.add(TrepnAux.getTrepnlibReadPermissions());
        l.add(TrepnAux.getTrepnlibWritePermissions());
        return l;
    }*/

}

/*

fun jInst.Instrumentation.main(args: Array<String>) {
    val tp  = TrepnProfilerFactory().createMethodOrientedProfiler()
    val tadpt = TrepnMethodOrientedProfilerAdapter ( tp as TrepnMethodOrientedProfiler)
    val s1 = "TrepnLib.updateState(null, \"2\", \"3\");"
    val sss = JavaParser.parseExpression(s1)
    val call = tadpt.markMethodStart( MethodCallExpr() , "ei crl" )
    println(Writer.write(call!!))

}
 */

