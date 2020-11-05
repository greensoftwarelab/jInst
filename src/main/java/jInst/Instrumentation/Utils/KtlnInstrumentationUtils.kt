package jInst.Instrumentation.Utils

import jInst.util.KastreeWriterFixed
import jInst.visitors.MMutableVisitor.MMutableVisitor
import kastree.ast.Node
import kastree.ast.Visitor
import kastree.ast.Writer

fun isTerminalExpr(node: Node): Boolean {
    var b = false
    when (node) {
        is Node.Expr.Return -> {
            b = true
        }
        is Node.Expr.Throw -> b = true
        is Node.Stmt.Expr -> {
            b = isTerminalExpr(node.expr)
        }
    }
    return b  //&& !defnot
}

fun hasTerminalExpr( node: Node): Boolean {
    var b = false
    Visitor.visit(node){ v,p ->
        when (v) {
            is Node.Expr.Return -> {
                b = true

            }is Node.Expr.Throw -> {
            b = true
        }
        }
    }

    return b  //&& !defnot
}

fun inferRet ( node:Node): Pair<String, Node.Type?>? {
    return when (node ) {
        is Node.Decl.Func -> {
            return Pair(node.name!! , node.type  )
        }
        else  -> null
    }
}

fun insertInEndOfBlock(node : Node, expr: Node.Expr) : Node{
    return MMutableVisitor.preVisit(node) { v, p ->
        when {
            v is Node.Block -> {
                v.copy(insertInEnd(v.stmts, Node.Stmt.Expr(expr), "Trepn" ))
            }
            else -> v
        }
    }
}

fun insertInEnd( node: List<Node.Stmt>, stm : Node.Stmt , stmPrefix: String): List<Node.Stmt> {
    if (node.isEmpty()){
        return listOf((stm))
    }
    return if ( KastreeWriterFixed.write(node.last()).startsWith( stmPrefix )){
        node
    }
    else{
        val has = node.any { it -> hasTerminalExpr(it) }
        if (!has){
            (    node +  listOf( (stm) ) )
        }
        else node
        // v.copy(v.stmts + listOf(expr))
    }
}

/* fun buildTrepnLibUpdateState(state: Int, str: String, desc: String): Node.Expr {
     // ex : TrepnLib.updateState(null, 0, "")
     val lhs = Node.Expr.Name(name = "TrepnLib")
     val operator = Node.Expr.BinaryOp.Oper.Token(token = Node.Expr.BinaryOp.Token.DOT)
     val arg1 = Node.ValueArg(null, false, Node.Expr.StringTmpl(arrayListOf(Node.Expr.StringTmpl.Elem.Regular(str = str)), raw = false))
     val arg2 = Node.ValueArg(null, false, Node.Expr.Const(value = state.toString(), form = Node.Expr.Const.Form.INT))
     val arg3 = Node.ValueArg(null, false, Node.Expr.StringTmpl(arrayListOf(Node.Expr.StringTmpl.Elem.Regular(str = desc)), raw = false))
     val methodName = Node.Expr.Name(name = "updateState")
     val rhs = Node.Expr.Call(methodName, ArrayList<Node.Type>(), listOf(arg1, arg2, arg3), null)
     return Node.Expr.BinaryOp(lhs, operator, rhs)

 }*/

/*   fun buildTrepnLibTraceCall(): Node.Expr {
      // ex : TrepnLib.traceMethod("uminho.di.greenlab.n2apptest.MainActivity.onBackPressed()")
      val lhs = Node.Expr.Name(name = "TrepnLib")
      val operator = Node.Expr.BinaryOp.Oper.Token(token = Node.Expr.BinaryOp.Token.DOT)
      val arg = Node.ValueArg(null, false, Node.Expr.StringTmpl(arrayListOf(Node.Expr.StringTmpl.Elem.Regular(str = "X")), raw = false))
      val methodName = Node.Expr.Name(name = "traceMethod")
      val rhs = Node.Expr.Call(methodName, ArrayList<Node.Type>(), listOf(arg), null)
      return Node.Expr.BinaryOp(lhs, operator, rhs)

  }
*/
fun addArg( node:  Node.Expr,  insExpr: Node.Expr ) : Node.Expr    {
    return  when (node) {
        is Node.Expr.BinaryOp -> {
            node.copy( node.lhs, node.oper, addArg( node.rhs, insExpr) )
        }

        is Node.Expr.Call -> {
            node.copy(node.expr, node.typeArgs,
                    node.args + listOf(Node.ValueArg (null, false, insExpr )))
        }
        else -> node
    }
}

fun wrapCall( node : Node, prefix:String): String{
    return when(node){
        is  Node.Decl.Func -> {
            prefix
        }
        is Node.Decl.Init -> {
            prefix
        }
        is Node.Decl.Constructor -> {
            prefix
        }
        else -> prefix
    }
}
// gambiarra autentica
// admitindo que foi instrumentado o inicio do bloco de codigo, buscar  a string (arg1 primeira instrucao)



fun getMatchString( node: Node) : String {
    var ret = "ERROR"
    MMutableVisitor.preVisit(node) { v, _ ->
        when (v) {
            is Node.Block -> {
                //println(Writer.write(v.stmts[0]))
                if (v.stmts.size > 0 && KastreeWriterFixed.write(v.stmts[0]).startsWith("TrepnLib") &&  ret.equals( "ERROR")) {
                    if ( ((v.stmts[0] as Node.Stmt.Expr).expr) is Node.Expr.Call ){
                        val z =   KastreeWriterFixed.write(( ( (((v.stmts[0] as Node.Stmt.Expr).expr) as Node.Expr.Call).args[2].expr) as Node.Expr.StringTmpl).elems[0])
                        ret = z
                    }
                    else if ( (v.stmts[0] as Node.Stmt.Expr).expr is Node.Expr.BinaryOp &&  ret.equals( "ERROR")  ){
                        val z  =    KastreeWriterFixed.write( (((((v.stmts[0] as Node.Stmt.Expr).expr as Node.Expr.BinaryOp).rhs as Node.Expr.Call).args[2].expr  ) as Node.Expr.StringTmpl).elems[0])
                        ret = z
                    }
                    v

                }    //else ((node.stmts[0] as Node.Expr.BinaryOp).rhs as Node.Expr.Call).args[1].expr
                else {
                    //ret = Node.Expr.Name("ERROR")
                    v
                }
            }
            else -> v
        }
    }
    return ret
}

fun isComplexReturn(x : Node) : Boolean{
    var b = false
    when {
        x is Node.Expr.Return -> {
            if (x.expr!= null){
                Visitor.visit(x.expr!!) { k, _ ->
                    when {
                        k is Node.Block ->{
                            b=true
                        }
                    }
                }
            }
        }
        x is Node.Stmt.Expr && x.expr is Node.Expr.Return -> {
            if ((x.expr as Node.Expr.Return).expr != null){
                Visitor.visit(x.expr!!) { k, _ ->
                    when {
                        k is Node.Block ->{
                            b=true
                        }
                    }
                }
            }
        }

    }
    return b
}

fun hasComplexReturn(node : Node) : Boolean{
    var b = false
    Visitor.visit(node) { x,_ ->
        when {
            x is Node.Expr.Return -> {
                if (x.expr!= null){
                    Visitor.visit(x.expr!!) { k, _ ->
                        when {
                            k is Node.Block ->{
                                b=true
                            }
                        }
                    }
                }
            }
        }

    }
    return b
}

fun isElvis(node: Node): Boolean {
    var b = false
    Visitor.visit(node) {  isidro, pai ->
        when{
        isidro is Node.Expr.BinaryOp.Oper.Token ->{
            if (isidro.token.name.equals("ELVIS")) {
                if (pai is Node.Expr.BinaryOp && pai.oper.equals(isidro)) {
                    b = true
                    return@visit
                }
            }
        }
        }
    }
    return b
}


fun insertInBegin( node: List<Node.Stmt>, stm : Node.Stmt , stmPrefix: String): List<Node.Stmt> {
    return if (  ! node.isEmpty() &&  KastreeWriterFixed.write(node.first()).startsWith( stmPrefix )){
        node
    }
    else{
        (  listOf( stm ) + node)
        // v.copy(v.stmts + listOf(expr))
    }
}

fun transformReturnComplexExpr ( ret : Node.Stmt, retTypeParams: Node.Type? ): List<Node.Stmt> {
    val l : ArrayList<Node.Stmt> = arrayListOf()
    Visitor.visit(ret) { x,y ->
        when (x){
            is Node.Expr.Return -> {
                if( x.expr != null ){
                    //val retTypePiece = Node.TypeRef.Simple.Piece(name = retType, typeParams = retTypeParams)
                    //val x = Node.TypeRef.Simple(listOf(retTypePiece))
                    val variable = Node.Decl.Property.Var(name = "myjInstRetVar", type =  retTypeParams )
                    val varExprRet = Node.Stmt.Decl (  Node.Decl.Property ( listOf(),   false,  listOf() ,retTypeParams, listOf(variable), listOf(), false, x.expr, null   ) )
                    // val l = Node.Expr.BinaryOp()
                    val newRet  : Node.Stmt =  Node.Stmt.Expr( Node.Expr.Return ( x.label, Node.Expr.Name("myjInstRetVar")  ) )
                    l.add(varExprRet)
                    l.add (newRet)
                }
            }
        }

    }
    return l
}

fun replaceBlockWithComplexReturn (node :kastree.ast.Node, retPair : Pair<String, Node.Type?>? ) : Node {
    return MMutableVisitor.preVisit(node) { v, p ->
        when {
            v is Node.Block -> {
                val l : ArrayList<Node.Stmt>  = arrayListOf()
                val lreplace : MutableMap< Int, List<Node.Stmt>>  = mutableMapOf< Int, List<Node.Stmt>>() // list for replacement
                v.stmts.forEach { x -> l.add(x)  }
                for (i in 0 until v.stmts.size) { // find complex returns
                    if ( isComplexReturn(v.stmts[i] )){
                        lreplace[i] =  transformReturnComplexExpr(v.stmts[i], if (retPair!=null) retPair.second else null ) // index of original complex return statement,  corresponding exprs
                    }
                }
                // replace
                val finalList = mutableListOf<Node.Stmt>()
                val x = l.iterator()
                var i = 0
                while (x.hasNext()){
                    val exp = x.next()
                    if (lreplace.containsKey(i)){
                        lreplace.get(i)!!.forEach { z -> finalList.add(i,z);i++ }
                    }
                    else{
                        finalList.add(exp)
                    }
                    i++
                }
                v.copy( finalList)
            }
            else -> v
        }
    }
}