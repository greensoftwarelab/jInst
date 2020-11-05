package jInst.util

import com.github.javaparser.ast.ImportDeclaration
import com.github.javaparser.ast.expr.*
import kastree.ast.Node
import kastree.ast.Writer
import kastree.ast.psi.Parser
import java.util.ArrayList


//  JavaParser.Type -> kastree.ast.Node.Type
fun simpleExprConverter(x: Expression ): kastree.ast.Node.Expr? {
    return when ( x){
        is NameExpr -> nameExprConverter(x)
        is IntegerLiteralExpr -> integerLiteralConverter(x)
        is StringLiteralExpr -> stringLiteralConverter(x)
        is DoubleLiteralExpr -> doubleLiteralConverter(x)
        is NullLiteralExpr -> nullLiteralConverter(x)
        is BinaryExpr ->{
            val left = simpleExprConverter(x.left)!!
          //  println(Writer.write(left))
            val right =  simpleExprConverter(x.right)!!
            //println(Writer.write(right))
            val oper =  operatorConverter(x.operator)!!


         //   println(Writer.write(oper))
           val x =  Node.Expr.BinaryOp( simpleExprConverter(x.left)!! , operatorConverter(x.operator)!! , simpleExprConverter(x.right)!! )
            //println(KastreeWriterFixed.write(x))
            x
        }
        is MethodCallExpr -> staticMethodCallConverter(x)
        is ObjectCreationExpr -> convertObjectCreationCall(x)
        else -> null
    }
}

fun convertImportDeclaration(dec : ImportDeclaration): Node.Import {
    return Node.Import(  dec.name.name.split("."),false, null )
}

fun operatorConverter(operator: BinaryExpr.Operator?): Node.Expr.BinaryOp.Oper?? {
    return when ( operator){
        is BinaryExpr.Operator -> {
            when {
                operator == BinaryExpr.Operator.plus -> {
                     Node.Expr.BinaryOp.Oper.Token( Node.Expr.BinaryOp.Token.ADD)
                }
                operator == BinaryExpr.Operator.equals -> {
                    Node.Expr.BinaryOp.Oper.Token( Node.Expr.BinaryOp.Token.ASSN)
                }
                operator == BinaryExpr.Operator.minus -> {
                    Node.Expr.BinaryOp.Oper.Token( Node.Expr.BinaryOp.Token.SUB)
                }
                else -> null
            }
        }
        else -> null
    }
}


fun convertMethodCall(exp : Expression ): Node.Expr {
    when(exp){
        is MethodCallExpr -> {
            return  convertMethodCall(exp)
        }
        is ObjectCreationExpr -> {
            return convertObjectCreationCall(exp)!!
        }
        else->{

            return simpleExprConverter(exp)!!
        }
    }
}

fun convertObjectCreationCall(mce: ObjectCreationExpr): Node.Expr? {

    var classname =  mce.type.name
    val listArgs = ArrayList<Node.ValueArg>()
    for ( i in 0 until mce.args.size){
        // VALUEARG(name: kotlin.String?, asterisk: kotlin.Boolean, expr: kastree.ast.Node.Expr)
        val arg = simpleExprConverter(mce.args[i])

        listArgs.add(Node.ValueArg(name = null,asterisk = false, expr = arg!! ))
       // listArgs.add(  Node.ValueArg( simpleExprConverter(mce.args[i]).toString(), asterisk = false, expr = simpleExprConverter(mce.args[i])!!))

        //listArgs.add(Node.Type(listOf(), simpleExprConverter(mce.typeArgs[i])))
    }
     val rhs =  Node.Expr.Call( Node.Expr.Name(classname), listOf(), listArgs  ,null)
    return rhs
}

fun convertMethodCall (mce : MethodCallExpr): Node.Expr {
    // call (expr: kastree.ast.Node.Expr, typeArgs: kotlin.collections.List<kastree.ast.Node.Type?>, args: kotlin.collections.List<kastree.ast.Node.ValueArg>, lambda: kastree.ast.Node.Expr.Call.TrailLambda?) : kastree.ast.Node.Expr {
    if (mce.scope!=null|| mce.name.contains(".")){
        return  ( staticMethodCallConverter(mce)!! )
    }

    else{
        val listArgs = ArrayList<Node.ValueArg>()
        val typeArgs = ArrayList<Node.Type>()
        for ( i in 0 until mce.args.size){
            // VALUEARG(name: kotlin.String?, asterisk: kotlin.Boolean, expr: kastree.ast.Node.Expr)
            listArgs.add(  Node.ValueArg( simpleExprConverter(mce.args[i]).toString(), asterisk = false, expr = simpleExprConverter(mce.args[i])!!))
            //listArgs.add(Node.Type(listOf(), simpleExprConverter(mce.typeArgs[i])))
        }
        return (Node.Expr.Call(Node.Expr.Name(mce.nameExpr.name), listOf() , listArgs,null ))
    }

}


fun nullLiteralConverter(expr: NullLiteralExpr): Node.Expr.Const {
    return Node.Expr.Const(value = expr.toString(), form =Node.Expr.Const.Form.NULL )
}

fun stringLiteralConverter (x : StringLiteralExpr) : Node.Expr.StringTmpl {
    val z = Node.Expr.StringTmpl(listOf(Node.Expr.StringTmpl.Elem.Regular(x.value)), raw = false)
    return z
}

fun integerLiteralConverter (x : IntegerLiteralExpr) : Node.Expr.Const {
    return Node.Expr.Const(value = x.value, form =Node.Expr.Const.Form.INT )
}

fun nameExprConverter ( x : NameExpr ) : Node.Expr.Name{
    return Node.Expr.Name ( x.name)
}

fun doubleLiteralConverter (x : DoubleLiteralExpr) : Node.Expr.Const {
    return Node.Expr.Const(value = x.value, form =Node.Expr.Const.Form.FLOAT )
}

fun staticMethodCallConverter ( x : MethodCallExpr) : Node.Expr? {
    var ret : Node.Expr
    var bin = false

    var lhs = if (x.scope is NameExpr) nameExprConverter( x.scope as NameExpr) else if (x.scope is ObjectCreationExpr) convertObjectCreationCall(x.scope as ObjectCreationExpr) else staticMethodCallConverter(x.scope as MethodCallExpr)
    //var lhs = nameExprConverter( x.scope as NameExpr)
    val operator = Node.Expr.BinaryOp.Oper.Token (token=Node.Expr.BinaryOp.Token.DOT)
    val list =  x.args?.map { it ->  simpleExprConverter(it)}?.filter { x -> x!=null }?.map { it -> Node.ValueArg( null,false , it!!) } ?: listOf()
    val name = nameExprConverter (x.nameExpr)
    val call = Node.Expr.Call(name, listOf(),   list,null)
    val z =  Node.Expr.BinaryOp(lhs!!, operator , call)
   // val x = staticMethodCallConverter(JavaParser.parseExpression("TrepnLib.updateState(null ,3.0, x);") as MethodCallExpr)
    return z
}


