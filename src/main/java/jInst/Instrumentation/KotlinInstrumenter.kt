package jInst.Instrumentation


import com.github.javaparser.ast.expr.Expression
import jInst.Instrumentation.Utils.*
import jInst.JInst
import jInst.Instrumentation.amp.AMPInstrumenter
import jInst.Instrumentation.amp.AMPInstrumenterAdapter
import jInst.Instrumentation.amp.ActivityInstrumenter
import jInst.Instrumentation.hunter.HunterAnnotationInstrumenter
import jInst.Instrumentation.hunter.HunterAnnotationInstrumenterAdapter
import jInst.Instrumentation.profiler.trepn.*
import jInst.util.KastreeWriterFixed
import jInst.visitors.MMutableVisitor.MMutableVisitor
import kastree.ast.Node
import kastree.ast.Visitor
import kastree.ast.Writer
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import java.io.File



class KotlinInstrumenter(val instrumentationType : JInst.InstrumentationType, val ctx : Expression ) :InstrumentHelper() {

    //val profiler : Profiler?

    companion object {
        val interest = listOf<String>(  "CLASS", "ENUM_CLASS", "INTERFACE", "COMPANION_OBJECT" ) //, "OBJECT", )
    }
    val tag = "[Kotlin]"

    init {
        if (instrumentationType == JInst.InstrumentationType.METHOD){
            instrumenter = TrepnProfilerFactory().createMethodOrientedProfiler()

        }
        else if (instrumentationType == JInst.InstrumentationType.TEST){    // else if ( instrumentationType == JInst.InstrumentationType.TEST) {
            instrumenter =  TrepnProfilerFactory().createTestOrientedProfiler()
        }
        else if (instrumentationType == JInst.InstrumentationType.ACTIVITY){    // else if ( instrumentationType == JInst.InstrumentationType.TEST) {
            instrumenter =   AMPInstrumenter()
        }
        else if (instrumentationType == JInst.InstrumentationType.ANNOTATION){    // else if ( instrumentationType == JInst.InstrumentationType.TEST) {
            instrumenter =   HunterAnnotationInstrumenter()
        }
    }


    fun getJUnitVersion(node : Node.File): Int {
        var default = 4
        for (import in node.imports){
            when{
                Writer.write(import).startsWith("org.junit.jupiter") -> {
                    default = 5
                }
                import.names.last().equals("AfterAll") -> {
                    default = 5
                }
                import.names.last().equals("BeforeAll") -> {
                    default = 5
                }
            }
        }
        return default
    }

    fun instrument(node : Node.File) : Node.File {
        return when  {
            instrumentationType == JInst.InstrumentationType.METHOD -> {
                instrumentFileMethodOriented(node)
            }
            instrumentationType == JInst.InstrumentationType.TEST  -> {
                instrumentTestOriented(node)
            }
            instrumentationType == JInst.InstrumentationType.ACTIVITY  -> {
                instrumentLaunchActivity(node)
            }
            instrumentationType == JInst.InstrumentationType.ANNOTATION  -> {
                instrumentFileAnnotationOriented(node)
            }
            else -> {
                throw UnsupportedOperationException("Unsupported Instrumentation Type")
            }
        }
    }

    private fun isOncreate(func : Node.Decl.Func) : Boolean {
        return func.name.equals("onCreate")
                && funcIsVoid(func)
                &&  func.params.size==1 && KastreeWriterFixed.write(func.params[0] as Node, null).matches(Regex(".*Bundle.*"))
    }

    private fun isOnDestroy(v : Node.Decl.Func) : Boolean {
        return v.name.equals("onDestroy")
                && (v.params==null || v.params.isEmpty() )
                &&  (v.type==null || KastreeWriterFixed.write(v.type as Node, null).contains(Regex("Unit")  ))
    }

    private fun instrumentLaunchActivity(node: Node.File): Node.File {
        val pack = if( node.pkg!=null) wrapPackage(node.pkg!!) else ""
        var hasCreate = false
        var hasDestroy = false
        val xz = addInstrumenterImport( node)
        val z = MMutableVisitor.preVisit(xz) { v, p ->
            when (v) {
                is Node.Decl.Structured -> {
                    if ( p is Node.File && v.form.name.equals("CLASS")){
                        //hasCreate = v.members.stream().filter{y -> y is Node.Decl.Func}.anyMatch{ x->isOncreate(x as Node.Decl.Func) }
                        hasCreate = v.members.any{x -> x is Node.Decl.Func && isOncreate(x)}
                        var xx = v
                        if ( !hasCreate ){
                            val listMember = v.members + listOf(createOnCreateFunction())
                            xx = v.copy(v.mods,v.form,v.name,v.typeParams,v.primaryConstructor,v.parentAnns,v.parents,v.typeConstraints,listMember)
                        }
                        hasDestroy = xx.members.any{x -> x is Node.Decl.Func && isOnDestroy(x)}
                        if ( !hasDestroy ){
                            val listMember = xx.members + listOf(createDestroyFunction())
                            xx.copy(v.mods,v.form,v.name,v.typeParams,v.primaryConstructor,v.parentAnns,v.parents,v.typeConstraints,listMember)
                        }

                        else{
                           v
                        }
                    }
                    else v
                }
                is Node.Decl.Func -> {
                    if( isOncreate(v) ){

                        if(v.body is Node.Decl.Func.Body.Block){
                            var l = (v.body as Node.Decl.Func.Body.Block).block.stmts
                            val z = AMPInstrumenterAdapter(instrumenter as ActivityInstrumenter)
                            val x: List<Node.Expr> =  z.onActivityCreate(null)
                           for (a in x.reversed()){
                               l = insertInBegin(l , Node.Stmt.Expr(a),"sadj")
                           }
                           // val rhs = Node.Expr.Call(expr=Node.Expr.Name("onCreate"), typeArgs = listOf(), args= listOf(Node.ValueArg(null,false,Node.Expr.Name("bd"))),lambda =  null )
                           // val lhs = Node.Expr.Super(null,null)
                           //val stm = Node.Stmt.Expr(Node.Expr.BinaryOp(lhs=lhs, oper = Node.Expr.BinaryOp.Oper.Token(Node.Expr.BinaryOp.Token.DOT), rhs = rhs ))
                           // val l =  insertInBegin(( v.body as Node.Decl.Func.Body.Block).block.stmts, stm  ,"jcaj" )
                            v.copy(v.mods,v.typeParams,v.receiverType,v.name,v.paramTypeParams,v.params,v.type,v.typeConstraints, Node.Decl.Func.Body.Block(Node.Block(l)))
                            //x.copy(x.mods,x.typeParams,x.receiverType,x.name,x.paramTypeParams,x.params,x.type,x.typeConstraints, Node.Decl.Func.Body.Block(Node.Block(l)))
                        }
                        else{
                            v
                        }

                       // val newBody = insertInBegin
                        // TODO
                       // v.copy(v.mods,v.typeParams,v.receiverType,v.name,v.paramTypeParams,v.params,v.type,v.typeConstraints, v.body)


                    }
                    else  if( isOnDestroy(v)  ){
                        kotlinReturnFinderAndMarker(v)
                    }
                    else{
                        v
                    }
                }
                else -> v
            }
        }
        /*
        if ( ! hasCreate ){
            // create on create func
            val mods =  listOf(Node.Modifier.Lit(Node.Modifier.Keyword.OVERRIDE))
            val fstParam = Node.Type( mods= listOf(), ref =  Node.TypeRef.Nullable(  Node.TypeRef.Simple(pieces = listOf(Node.TypeRef.Simple.Piece(name = "Bundle",typeParams = listOf())) ) ))
            val params = arrayListOf(Node.Decl.Func.Param(listOf(),false,"bd", fstParam ,null))

            // create Block
            var l = listOf<Node.Stmt>()
            val rhs = Node.Expr.Call(expr=Node.Expr.Name("onCreate"), typeArgs = listOf(), args= listOf(Node.ValueArg(null,false,Node.Expr.Name("bd"))),lambda =  null )
            val lhs = Node.Expr.Super(null,null)
            val superexpre = (Node.Expr.BinaryOp(lhs=lhs, oper = Node.Expr.BinaryOp.Oper.Token(Node.Expr.BinaryOp.Token.DOT), rhs = rhs ))
            l = insertInBegin(l,Node.Stmt.Expr( (superexpre)),"sadj" )
            val z = AMPInstrumenterAdapter(instrumenter as ActivityInstrumenter)
            val x: List<Node.Expr> =  z.onActivityCreate(null)
            for (a in x.reversed()){
                l = insertInBegin(l , Node.Stmt.Expr(a),"sadj")
            }
            TODO("add func to class")
        }
        if (! hasDestroy ){
            // create on  destroy func
            TODO("add func to class and create func")
        }*/
        return z as Node.File

    }

    fun instrumentTestCase ( node: Node.Decl.Func, funcId : String): Node.Decl.Func{
        return MMutableVisitor.preVisit(node) { v,_ ->
            when {
                v is Node.Decl.Func.Body.Block -> {
                    v.copy( Node.Block( insertInBegin(v.block.stmts, Node.Stmt.Expr (getMarkTestCall(funcId)) , "Trepn")))
                }
                else -> v
            }
        }
    }


    fun createOnCreateFunction(): Node.Decl.Func {
        // create on create func
        val mods =  listOf(Node.Modifier.Lit(Node.Modifier.Keyword.OVERRIDE))
        val fstParam = Node.Type( mods= listOf(), ref =  Node.TypeRef.Nullable(  Node.TypeRef.Simple(pieces = listOf(Node.TypeRef.Simple.Piece(name = "Bundle",typeParams = listOf())) ) ))
        val params = arrayListOf(Node.Decl.Func.Param(listOf(),false,"bd", fstParam ,null))

        // create Block
        var l = listOf<Node.Stmt>()
        val rhs = Node.Expr.Call(expr=Node.Expr.Name("onCreate"), typeArgs = listOf(), args= listOf(Node.ValueArg(null,false,Node.Expr.Name("bd"))),lambda =  null )
        val lhs = Node.Expr.Super(null,null)
        val superexpre = (Node.Expr.BinaryOp(lhs=lhs, oper = Node.Expr.BinaryOp.Oper.Token(Node.Expr.BinaryOp.Token.DOT), rhs = rhs ))
        l = insertInBegin(l,Node.Stmt.Expr( (superexpre)),"sadj" )

       /* val z = AMPInstrumenterAdapter(instrumenter as ActivityInstrumenter)
        val x: List<Node.Expr> =  z.onActivityCreate(null)
        for (a in x.reversed()){
            l = insertInBegin(l , Node.Stmt.Expr(a),"sadj")
        }*/
        return Node.Decl.Func(mods, listOf(),null,"onCreate", listOf(), params,null, listOf(), Node.Decl.Func.Body.Block(Node.Block(l))  )

    }


    fun createDestroyFunction(): Node.Decl.Func {
        // create on destroy func
        val mods =  listOf(Node.Modifier.Lit(Node.Modifier.Keyword.OVERRIDE))
        var l = listOf<Node.Stmt>()
        val rhs = Node.Expr.Call(expr=Node.Expr.Name("onDestroy"), typeArgs = listOf(), args= listOf(),lambda =  null )
        val lhs = Node.Expr.Super(null,null)
        val superexpre = (Node.Expr.BinaryOp(lhs=lhs, oper = Node.Expr.BinaryOp.Oper.Token(Node.Expr.BinaryOp.Token.DOT), rhs = rhs ))
        l = insertInBegin(l,Node.Stmt.Expr( (superexpre)),"sadj" )
        return Node.Decl.Func(mods, listOf(),null,"onDestroy", listOf(), listOf(),null, listOf(), Node.Decl.Func.Body.Block(Node.Block(l))  )

    }


    fun instrumentTestFile(node: Node.File) : Node.File {
        var xz = addInstrumenterImport( node)
        if (compiledSdkVersion>22){
            //import android.support.test.InstrumentationRegistry;
            val asd = listOf("android", "support", "test", "InstrumentationRegistry" )
            val i = Node.Import(  asd,false, null )
            xz = xz.copy(xz.anns,xz.pkg, xz.imports + listOf(i) ,xz.decls)
        }
        var scope =  if (node.pkg!=null) wrapPackage(node.pkg!!)else ""
        var hasAfterEach = false
        var hasBeforeEach = false
        var hasAfter = false
        var hasBefore = false
        return MMutableVisitor.preVisit(xz) { v,_ ->
            when{
                v is Node.Decl.Structured && v.form.name in interest -> {
                    var scope1 = scope
                    scope+= v.name
                    var newMembers: MutableList<Node.Decl> = ( v.members.map {
                        x ->
                            when (x) {
                                is Node.Decl.Func ->{
                                    if ( KtlTestsInstrumenterUtil.isTestCase(x)){
                                        val z =  instrumentTestCase(x, scope + x.name  )
                                        x.copy(z.mods,z.typeParams,z.receiverType,z.name,z.paramTypeParams,z.params,z.type,z.typeConstraints, z.body)
                                    }
                                    if(getJUnitVersion(node)==4){
                                        if(KtlTestsInstrumenterUtil.hasAnnotatedMethod(x,"After")){
                                            hasAfter=true
                                            if(x.body is Node.Decl.Func.Body.Block){
                                                val l =  insertInBegin(( x.body as Node.Decl.Func.Body.Block).block.stmts, Node.Stmt.Expr( getAppropriateTestCall(false)) ,"Trepn" )
                                                x.copy(x.mods,x.typeParams,x.receiverType,x.name,x.paramTypeParams,x.params,x.type,x.typeConstraints, Node.Decl.Func.Body.Block(Node.Block(l)))
                                                //x.copy(x.mods,x.typeParams,x.receiverType,x.name,x.paramTypeParams,x.params,x.type,x.typeConstraints, Node.Decl.Func.Body.Block(Node.Block(l)))
                                            }
                                            else x
                                        }
                                        else if(KtlTestsInstrumenterUtil.hasAnnotatedMethod(x,"Before")){
                                            hasBefore=true
                                            if(x.body is Node.Decl.Func.Body.Block){
                                                if (compiledSdkVersion > 22){
                                                    var l =  ( x.body as Node.Decl.Func.Body.Block).block.stmts
                                                    getAppropriatePermissions().forEach{
                                                        z ->  l = insertInEnd( l,z , "Trepn")
                                                    }
                                                    l = insertInEnd(  l,  Node.Stmt.Expr (getAppropriateTestCall(true)) ,"Trepn" )
                                                    x.copy(x.mods,x.typeParams,x.receiverType,x.name,x.paramTypeParams,x.params,x.type,x.typeConstraints, Node.Decl.Func.Body.Block(Node.Block(l)))
                                                }
                                                else{
                                                    val l = insertInEnd ( ( x.body as Node.Decl.Func.Body.Block).block.stmts,   Node.Stmt.Expr(getAppropriateTestCall(true)) ,"Trepn" )
                                                    x.copy(x.mods,x.typeParams,x.receiverType,x.name,x.paramTypeParams,x.params,x.type,x.typeConstraints, Node.Decl.Func.Body.Block(Node.Block(l)))
                                                }

                                                //val l =  insertInEnd(( x.body as Node.Decl.Func.Body.Block).block.stmts,  getAppropriateTestCall(true) ,"Trepn" )
                                                //x.copy(x.mods,x.typeParams,x.receiverType,x.name,x.paramTypeParams,x.params,x.type,x.typeConstraints, Node.Decl.Func.Body.Block(Node.Block(l)))
                                            }
                                            else x
                                        }
                                        else {
                                            x
                                        }
                                    }
                                    else if (getJUnitVersion(node)>4){
                                        if(KtlTestsInstrumenterUtil.hasAnnotatedMethod(x,"AfterEach")){
                                            hasAfterEach=true
                                            if(x.body is Node.Decl.Func.Body.Block){
                                                val l =  insertInBegin(( x.body as Node.Decl.Func.Body.Block).block.stmts, Node.Stmt.Expr( getAppropriateTestCall(false)) ,"Trepn" )
                                                x.copy(x.mods,x.typeParams,x.receiverType,x.name,x.paramTypeParams,x.params,x.type,x.typeConstraints, Node.Decl.Func.Body.Block(Node.Block(l)))
                                                //x.copy(x.mods,x.typeParams,x.receiverType,x.name,x.paramTypeParams,x.params,x.type,x.typeConstraints, Node.Decl.Func.Body.Block(Node.Block(l)))
                                            }
                                            else x
                                        }
                                        else if(KtlTestsInstrumenterUtil.hasAnnotatedMethod(x,"BeforeEach")){
                                            hasBeforeEach=true
                                            if(x.body is Node.Decl.Func.Body.Block){
                                                if (compiledSdkVersion > 22){
                                                    var l =  ( x.body as Node.Decl.Func.Body.Block).block.stmts
                                                    getAppropriatePermissions().forEach{
                                                        z ->  l = insertInEnd( l,z , "Trepn")
                                                    }
                                                    l = insertInEnd(  l,  Node.Stmt.Expr (getAppropriateTestCall(true)) ,"Trepn" )
                                                    x.copy(x.mods,x.typeParams,x.receiverType,x.name,x.paramTypeParams,x.params,x.type,x.typeConstraints, Node.Decl.Func.Body.Block(Node.Block(l)))
                                                }
                                                else{
                                                    val l = insertInEnd ( ( x.body as Node.Decl.Func.Body.Block).block.stmts,   Node.Stmt.Expr(getAppropriateTestCall(true)) ,"Trepn" )
                                                    x.copy(x.mods,x.typeParams,x.receiverType,x.name,x.paramTypeParams,x.params,x.type,x.typeConstraints, Node.Decl.Func.Body.Block(Node.Block(l)))
                                                }

                                                //val l =  insertInEnd(( x.body as Node.Decl.Func.Body.Block).block.stmts,  getAppropriateTestCall(true) ,"Trepn" )
                                                //x.copy(x.mods,x.typeParams,x.receiverType,x.name,x.paramTypeParams,x.params,x.type,x.typeConstraints, Node.Decl.Func.Body.Block(Node.Block(l)))
                                            }
                                            else x
                                        }
                                        else {
                                            x
                                        }
                                    }

                                    else x

                                }
                                else -> x
                            }
                    }).toMutableList()

                    if(getJUnitVersion(node)==4){
                        if ( ! hasAfter ){
                            newMembers.add(createAfter())
                            //v.copy(mods = v.mods, form = v.form, name = v.name,typeParams = v.typeParams,primaryConstructor = v.primaryConstructor,parentAnns = v.parentAnns,parents = v.parents,typeConstraints = v.typeConstraints, members = withAfter )
                        }
                        if ( ! hasBefore ){
                            newMembers.add(createBefore())
                            //v.copy(mods = v.mods, form = v.form, name = v.name,typeParams = v.typeParams,primaryConstructor = v.primaryConstructor,parentAnns = v.parentAnns,parents = v.parents,typeConstraints = v.typeConstraints, members = withBefore )
                        }
                    }
                    else if (getJUnitVersion(node)>4){
                        if ( ! hasAfterEach ){
                            newMembers.add(createAfterEach())
                            //v.copy(mods = v.mods, form = v.form, name = v.name,typeParams = v.typeParams,primaryConstructor = v.primaryConstructor,parentAnns = v.parentAnns,parents = v.parents,typeConstraints = v.typeConstraints, members = withAfter )
                        }
                        if ( ! hasBeforeEach ){
                            newMembers.add(createBeforeEach())
                            //v.copy(mods = v.mods, form = v.form, name = v.name,typeParams = v.typeParams,primaryConstructor = v.primaryConstructor,parentAnns = v.parentAnns,parents = v.parents,typeConstraints = v.typeConstraints, members = withBefore )
                        }
                    }

                    scope = scope1
                    v.copy(mods = v.mods, form = v.form, name = v.name,typeParams = v.typeParams,primaryConstructor = v.primaryConstructor,parentAnns = v.parentAnns,parents = v.parents,typeConstraints = v.typeConstraints, members = newMembers )
                }
                else -> v
            }
        }
    }

    /*
    fun createAnnotationHunter (name: String) : Node.Decl.Func{
        val annotation = Node.Modifier.AnnotationSet.Annotation(names = listOf("HunterDebug"), typeArgs = listOf(), args = listOf())
        val annSet =  Node.Modifier.AnnotationSet(target = null, anns = listOf(annotation))
        return Node.Decl.Func(mods= listOf(annSet),typeParams = listOf(),receiverType = null,name = name, paramTypeParams = listOf(), params = listOf(), type = null, typeConstraints = listOf(), body = body )
    }

     */

    fun createAfterEach ( ) : Node.Decl.Func{
        val annotation = Node.Modifier.AnnotationSet.Annotation(names = listOf("AfterEach"), typeArgs = listOf(), args = listOf())
        val annSet =  Node.Modifier.AnnotationSet(target = null, anns = listOf(annotation))
        val block =  Node.Decl.Func.Body.Block(Node.Block(listOf(Node.Stmt.Expr(getAppropriateTestCall(false)))))
        val name = "anaDroidAfterEach"
        return Node.Decl.Func(mods= listOf(annSet),typeParams = listOf(),receiverType = null,name = name, paramTypeParams = listOf(), params = listOf(), type = null, typeConstraints = listOf(), body = block )
    }

    fun createBeforeEach ( ) : Node.Decl.Func{
        val annotation = Node.Modifier.AnnotationSet.Annotation(names = listOf("BeforeEach"), typeArgs = listOf(), args = listOf())
        val annSet =  Node.Modifier.AnnotationSet(target = null, anns = listOf(annotation))
        //var block =  Node.Decl.Func.Body.Block(Node.Block(listOf(Node.Stmt.Expr(getAppropriateTestCall(true)))))
        var l =  Node.Block(listOf()).stmts
        if (compiledSdkVersion > 22){
            getAppropriatePermissions().forEach{
                z ->  l = insertInEnd( l,z , "Trepn")
            }
            l = insertInEnd(  l,  Node.Stmt.Expr (getAppropriateTestCall(true)) ,"Trepn" )
            //x.copy(x.mods,x.typeParams,x.receiverType,x.name,x.paramTypeParams,x.params,x.type,x.typeConstraints, Node.Decl.Func.Body.Block(Node.Block(l)))
        }
        else{
            val l = insertInEnd (  l,   Node.Stmt.Expr(getAppropriateTestCall(true)) ,"Trepn" )
            //x.copy(x.mods,x.typeParams,x.receiverType,x.name,x.paramTypeParams,x.params,x.type,x.typeConstraints, Node.Decl.Func.Body.Block(Node.Block(l)))
        }
        val name = "anaDroidBeforeEach"
        return Node.Decl.Func(mods= listOf(annSet),typeParams = listOf(),receiverType = null,name = name, paramTypeParams = listOf(), params = listOf(), type = null, typeConstraints = listOf(), body = Node.Decl.Func.Body.Block(  Node.Block(l) ))
    }

    fun createAfter ( ) : Node.Decl.Func{
        val annotation = Node.Modifier.AnnotationSet.Annotation(names = listOf("After"), typeArgs = listOf(), args = listOf())
        val annSet =  Node.Modifier.AnnotationSet(target = null, anns = listOf(annotation))
        val block =  Node.Decl.Func.Body.Block(Node.Block(listOf(Node.Stmt.Expr(getAppropriateTestCall(false)))))
        val name = "anaDroidAfter"
        return Node.Decl.Func(mods= listOf(annSet),typeParams = listOf(),receiverType = null,name = name, paramTypeParams = listOf(), params = listOf(), type = null, typeConstraints = listOf(), body = block )
    }

    fun createBefore ( ) : Node.Decl.Func{
        val annotation = Node.Modifier.AnnotationSet.Annotation(names = listOf("Before"), typeArgs = listOf(), args = listOf())
        val annSet =  Node.Modifier.AnnotationSet(target = null, anns = listOf(annotation))
        //var block =  Node.Decl.Func.Body.Block(Node.Block(listOf(Node.Stmt.Expr(getAppropriateTestCall(true)))))
        var l =  Node.Block(listOf()).stmts
        if (compiledSdkVersion > 22){
            getAppropriatePermissions().forEach{
                z ->  l = insertInEnd( l,z , "Trepn")
            }
            l = insertInEnd(  l,  Node.Stmt.Expr (getAppropriateTestCall(true)) ,"Trepn" )
            //x.copy(x.mods,x.typeParams,x.receiverType,x.name,x.paramTypeParams,x.params,x.type,x.typeConstraints, Node.Decl.Func.Body.Block(Node.Block(l)))
        }
        else{
            val l = insertInEnd (  l,   Node.Stmt.Expr(getAppropriateTestCall(true)) ,"Trepn" )
            //x.copy(x.mods,x.typeParams,x.receiverType,x.name,x.paramTypeParams,x.params,x.type,x.typeConstraints, Node.Decl.Func.Body.Block(Node.Block(l)))
        }
        val name = "anaDroidBefore"
        return Node.Decl.Func(mods= listOf(annSet),typeParams = listOf(),receiverType = null,name = name, paramTypeParams = listOf(), params = listOf(), type = null, typeConstraints = listOf(), body = Node.Decl.Func.Body.Block(  Node.Block(l) ))
    }


    fun getMarkTestCall(call:String) : Node.Expr {
       return if (instrumentationType == JInst.InstrumentationType.METHOD) {
           TrepnMethodOrientedProfilerAdapter(instrumenter as TrepnMethodOrientedProfiler).marKTest(ctx,call)
        }
        else if (instrumentationType == JInst.InstrumentationType.TEST) {
           TrepnTestOrientedProfilerAdapter(instrumenter as TrepnTestOrientedProfiler).marKTest(ctx,call)
       }
        else{
           TrepnTestOrientedProfilerAdapter(instrumenter as TrepnTestOrientedProfiler).marKTest(ctx,call)
       }
    }

    fun getAppropriateMethodCall( name: String,  mark: Boolean ) : Node.Expr {
        if (instrumentationType == JInst.InstrumentationType.METHOD) {
            return if (mark) {
                 TrepnMethodOrientedProfilerAdapter(instrumenter as TrepnMethodOrientedProfiler).markMethodStart(ctx, name)
            }
            else {
                 TrepnMethodOrientedProfilerAdapter(instrumenter as TrepnMethodOrientedProfiler).marKMethodStop(ctx, name)
            }
        }
        else if (instrumentationType == JInst.InstrumentationType.TEST) {
            if (mark) {
                 return TrepnTestOrientedProfilerAdapter(instrumenter as TrepnTestOrientedProfiler).markMethod(ctx, name)
            }
        }
        else if(instrumentationType == JInst.InstrumentationType.ACTIVITY){
            return AMPInstrumenterAdapter(instrumenter as ActivityInstrumenter).onActivityDestroy(null)!![0]

        }

        return  Node.Expr.Name("Instrumentation Error")
    }

    fun getAppropriateTestCall(  mark: Boolean ) : Node.Expr {
        if (instrumentationType == JInst.InstrumentationType.METHOD) {
            return if (mark) {
                TrepnMethodOrientedProfilerAdapter(instrumenter as TrepnMethodOrientedProfiler).startProfiler(ctx)
            }
            else {
                    TrepnMethodOrientedProfilerAdapter(instrumenter as TrepnMethodOrientedProfiler).stopProfiler(ctx)
            }
        }
        else if (instrumentationType == JInst.InstrumentationType.TEST) {
           return if (mark) {
                 TrepnTestOrientedProfilerAdapter(instrumenter as TrepnTestOrientedProfiler).startProfiler(ctx)
            }
            else{
                 TrepnTestOrientedProfilerAdapter(instrumenter as TrepnTestOrientedProfiler).stopProfiler(ctx)
            }
        }
        return  Node.Expr.Name("Instrumentation Error")
    }

    fun getAppropriatePermissions( ) : List<Node.Stmt> {
        return if (instrumentationType == JInst.InstrumentationType.METHOD) {
            TrepnMethodOrientedProfilerAdapter(instrumenter as TrepnMethodOrientedProfiler).getAllowPermissionCalls()
        }
        else if (instrumentationType == JInst.InstrumentationType.TEST) {
            TrepnTestOrientedProfilerAdapter(instrumenter as TrepnTestOrientedProfiler).getAllowPermissionCalls()
        }
        else  listOf()
    }

    fun instrumentBegin(node: Node, scope : String, map : MutableSet<Int> ): Node {
        var scope1 : String =  scope
        val allmet = allMethods
        return MMutableVisitor.preVisit(node) { v, p ->
            when  {
                v is Node.Expr.BinaryOp  && KastreeWriterFixed.write(v.oper).equals(".")  && v.rhs is Node.Expr.Call && ! map.contains(v.hashCode())->{
                    //scope1 += extractString(v.lhs)
                    map.add(v.rhs.hashCode())
                    //val insExpr = getAppropriateMethodCall(scope1, true)
                    val x = v.copy(v.lhs, v.oper, instrumentBegin(v.rhs,scope1, map ) as Node.Expr)
                    map.add(v.rhs.hashCode())
                    scope1 = scope
                    x
                }
                v is Node.Decl.Structured && v.form.name in interest  && ( ! map.contains(v.hashCode()) ) -> {
                    scope1+=  "."+v.name
                    //val insExpr = getAppropriateMethodCall(scope1, true)
                    val l: List<Node.Decl> = v.members.map { x -> instrumentBegin( x, scope1, map) }.filter { it -> it is Node.Decl  }.map { it -> it as Node.Decl }
                    scope1 = scope
                    val x = v.copy(v.mods,v.form,v.name,v.typeParams,v.primaryConstructor,v.parentAnns,v.parents,v.typeConstraints, l)
                    map.add(x.hashCode())
                    x
                }
                v is Node.Decl.Func  && ( ! map.contains(v.hashCode()) ) -> {
                    val args = ProjectMethods.wrapArgs(v.params)
                    scope1 += "->"+ v.name + "|"+ ProjectMethods.hashArgs(args)
                    map.add(v.hashCode())
                    val x = instrumentBegin(v, scope1,map) as Node.Decl.Func
                    val jo = JSONObject()
                    jo.put("args", JSONArray())
                    allMethods.addMethod( ProjectMethods.buildJSONObj(ProjectMethods.wrapMods(v.mods), args,scope1, "kotlin" ) )
                    if (x.body is Node.Decl.Func.Body.Block){
                        val z = (x.body as Node.Decl.Func.Body.Block).block.stmts
                        val insExpr = getAppropriateMethodCall(scope1, true)
                        if( z.isEmpty() ){
                            //println(Writer.write(v))
                            scope1 = scope
                            v
                        }
                        else if( ! KastreeWriterFixed.write(z.first()).equals(KastreeWriterFixed.write(insExpr))){

                            val xx = x.copy(x.anns,x.typeParams,x.receiverType,x.name,x.paramTypeParams,x.params,x.type,x.typeConstraints, Node.Decl.Func.Body.Block( Node.Block( insertInBegin(z, Node.Stmt.Expr(insExpr), "Trepn")) ))
                            map.add(xx.hashCode())
                            scope1 = scope
                            xx
                        }
                        else{
                            println("INSTRUMENT BEGIN KOTLIN 3.4")
                            scope1 = scope
                            v
                        }
                    }
                    else{
                        println("INSTRUMENT BEGIN KOTLIN 3.5")
                        scope1 = scope
                        v
                    }
                }
                v is Node.Decl.Constructor  && ( ! map.contains(v.hashCode()) ) -> {
                    println("INSTRUMENT BEGIN KOTLIN 4")
                    val args = ProjectMethods.wrapArgs(v.params)
                    scope1 += "-><init>"+ "|" + ProjectMethods.hashArgs(args)
                    allMethods.addMethod( ProjectMethods.buildJSONObj(ProjectMethods.wrapMods(v.mods),args,scope1 , "kotlin" ) )
                    if (v.block == null) {
                        val insExpr = Node.Stmt.Expr( getAppropriateMethodCall(scope1, true))
                        //val newRealInsExpr = addArg(insExpr, Node.Expr.Name ( scope1 ) )
                        val x = v.copy(v.mods, v.params, v.delegationCall, Node.Block(listOf(insExpr)) )   // v.block!!.copy(arrayListOf(insExpr)))
                        map.add(x.hashCode())
                        x
                    } else {
                        val insExpr = getAppropriateMethodCall(scope1, true)
                        //val newRealInsExpr = addArg(insExpr , Node.Expr.Name ( scope1 ) )
                        val x = v.copy(v.mods, v.params, v.delegationCall, v.block!!.copy(( insertInBegin(v.block!!.stmts, Node.Stmt.Expr(insExpr), "Trepn"))))
                        map.add(x.hashCode())
                        x
                    }
                }
                v  is Node.Decl.Init  && ( ! map.contains(v.hashCode()) ) -> {
                    println("INSTRUMENT BEGIN KOTLIN 5")
                    scope1+="-><init>"
                    allMethods.addMethod( ProjectMethods.buildJSONObj(listOf(),ProjectMethods.wrapArgs(listOf()),scope1 , "kotlin" ) )
                    val insExpr = getAppropriateMethodCall(scope1, true)
                   // val newRealInsExpr = addArg(insExpr, Node.Expr.Name ( scope1 ) )
                    val x =  v.copy(Node.Block( insertInBegin(v.block.stmts, Node.Stmt.Expr(insExpr), "Trepn")))
                    map.add(v.hashCode())
                    x
                }
                /*  v is Node.Expr.Call.TrailLambda   && ( (! map.containsKey(v.hashCode()))  ) -> {
                    var insExpr = getAppropriateMethodCall(scope1, true)
                    if( p is Node.Expr.Call && v.func.block != null && (! Writer.write(v.func.block!!.stmts.first()).equals(Writer.write(insExpr)))){
                        if (p.expr is Node.Expr.Name){
                            scope1 += ( p.expr as Node.Expr.Name).name
                        }
                        insExpr = getAppropriateMethodCall(scope1, true)
                        //val newRealInsExpr = addArg(insExpr, Node.Expr.Name ( scope1 ) )
                        val x= v.copy(v.anns, v.label, v.func.copy(v.func.params, Node.Block( insertInBegin(v.func.block!!.stmts, insExpr, "Trepn")) ))
                        map.put(x.hashCode(),scope1)
                        x
                    }
                     //v.copy(Node.Block(arrayListOf <Node.Stmt>( insExpr ) + v.block.stmts ))
                    else v
                }*/
                else -> v
            }
        }
    }

    fun kotlinReturnFinderAndMarker(exp: Node): Node {
            var hasReturn = false
            //var depth = 1
            var insExpr : Node.Expr
            var newfile = MMutableVisitor.preVisit(exp) { v, p ->
                when {
                    v is Node.Block -> {
                        var l = getMatchString(v)
                        if (l.equals("ERROR")){
                            l  = getMatchString(exp)
                        }
                        var newV :Node.Block = v
                        if (hasComplexReturn(v) ) {//&& retThings!=null ){
                            newV = replaceBlockWithComplexReturn(newV, null)  as Node.Block
                        }
                        insExpr =  getAppropriateMethodCall(l, false)
                        val x = arrayListOf<Int>()
                        val replaceNodes = HashMap<Node, Node>()
                        for (i in 0..newV.stmts.size - 1) {
                            if (isTerminalExpr(newV.stmts[i])) {
                               hasReturn=true
                                //x.add(i)
                                //  x.add(i)
                                x.add(i)

                            }
                            if( hasTerminalExpr(newV.stmts[i])&& isConditionalExpr(newV.stmts[i])){
                                if (isElvis(newV.stmts[i])){
                                    replaceNodes[newV.stmts[i]] = replaceElvis(newV.stmts[i])
                                }
                            }

                        }
                        val x1 = ArrayList<Node>(newV.stmts)
                        for (j in x) {
                            x1.add(j, insExpr)
                        }
                        val x2 = ArrayList<Node>()
                        for (elem in x1) {
                            if (elem in replaceNodes.keys) {
                                x2.add(replaceNodes.get(elem) as Node)
                            } else {
                                x2.add(elem)
                            }
                        }
                        val finalist = ArrayList<Node.Stmt>()
                        // gambiarra pk Node.Stmt.Expr != Node.Expr :(
                        for (x in x2) {
                            if (x !is Node.Stmt && x is Node.Expr) {
                                finalist.add(Node.Stmt.Expr(x))
                            } else {
                                finalist.add(x as Node.Stmt)
                            }
                        }
                        v.copy(finalist)
                    }
                    else -> v
                }
            }
            if (funcIsVoid(newfile)) {
                if (!hasReturn){
                    insExpr =  getAppropriateMethodCall( (getMatchString(exp)), false)  //addArg(getAppropriateMethodCall(), getMatchString(v))
                    // println("will insert ${ getMatchString(exp)} in  ${Writer.write(exp)}")
                    newfile = insertExpressionInEndOfFunction( (insExpr), exp)
                }
                else if (hasReturn && newfile is Node.Decl.Func){
                    insExpr =  getAppropriateMethodCall( (getMatchString(exp)), false)
                    newfile = ensureMainBlockIsInstrumented(newfile as Node.Decl.Func,insExpr)
                }
            }
            return newfile
        }

    private fun ensureMainBlockIsInstrumented( node: Node.Decl.Func, inserExp: Node.Expr) : Node.Decl.Func {
        if (node.body!= null && node.body is Node.Decl.Func.Body.Block){
            if ( ! KastreeWriterFixed.write((node.body as Node.Decl.Func.Body.Block).block.stmts.last()).startsWith("Trepn")){
                val l = (node.body as Node.Decl.Func.Body.Block).block.stmts.toMutableList()
                l.add(Node.Stmt.Expr(inserExp))
                return node.copy(node.mods,node.typeParams,node.receiverType,node.name,node.paramTypeParams,node.params,node.type,node.typeConstraints,Node.Decl.Func.Body.Block(Node.Block(l)))
            }
        }
        return node
    }

    private fun isThrow (node : Node.Expr): Boolean{
        var b = false
        Visitor.visit(node){ v,_ ->
            when(v){
                is Node.Expr.Throw -> {
                    b = true
                }
            }
        }
        return b
    }

    private fun extractExceptionOfThrowExpr (node : Node.Expr): String{
            var ret : String = "Exception"
            Visitor.visit(node) { v, _ ->
                when (v) {
                    is Node.Expr.Call -> {
                        if (v.expr is Node.Expr.Name) {
                            val x = ((v.expr) as Node.Expr.Name).name
                            if (x.contains("ception")) {
                                ret = x
                            }
                        }
                    }
                }
            }
            return ret
        }


    /*
    * Replaces an terminal Elvis expression like:
     *      val x =  "".trim() ?: throw IllegalArgumentException("Name required")
     *      by an equivalent expression with try catch
    * */
    fun replaceElvis(stmt: Node): Node {
        var newfile = MMutableVisitor.preVisit(stmt) { v, _ ->
            when {
                v is Node.Expr.BinaryOp && isElvis(v) -> {
                    val terminal = extractTerminalExpr(v)
                    val tryBlock = Node.Block(listOf(v.lhs).map { x -> Node.Stmt.Expr(x) })
                    val catchBlock = Node.Block(listOf(v.rhs).map { x -> Node.Stmt.Expr(x) })
                    //val catchBlock = kotlinReturnFinderAndMarker( Node.Block(listOf(v.rhs).cast()) , insertingExp).cast<Node.Block>()
                    var exceptionName = "Exception"
                    if (isThrow(v!!)) {
                        exceptionName = extractExceptionOfThrowExpr(terminal!!)
                    }
                    val exceptionToCatch = Node.TypeRef.Simple(listOf(Node.TypeRef.Simple.Piece(name = exceptionName, typeParams = listOf())))
                    val catc = Node.Expr.Try.Catch(listOf(), "elvisException", exceptionToCatch, catchBlock)
                    Node.Expr.Try(tryBlock, listOf(catc), null)
                }
                else -> v
            }

        }
        return newfile
    }

    private fun extractTerminalExpr(v: Node.Expr.BinaryOp) : Node.Expr? {
        var ret : Node.Expr?= null
        Visitor.visit(v){ v,_ ->
            when{
                v!=null &&  isTerminalExpr(v!!) ->{
                    if (v is Node.Expr.Throw ) {
                        ret = v.expr
                    }
                    else if (v is Node.Expr.Return){
                        ret= v.expr
                    }
                }
            }
        }
        return ret
    }

    fun isConditionalExpr(stmt: Node): Boolean {
        var b = false
        Visitor.visit(stmt) { v, _ ->
            when {
                v is Node.Expr.If -> b = true
                v != null && isElvis(v) -> b = true
            }
        }
        return b
    }

    fun insertExpressionInEndOfFunction(expr: Node.Expr, node: Node): Node {
        return MMutableVisitor.preVisit(node) { v, p ->
            when (v) {
                is Node.Decl.Func -> {
                    if (v.body !=null ){
                        val insExpr = getAppropriateMethodCall(getMatchString(v), false)
                        val prefix = getPrefix()
                        if ( v.body is Node.Decl.Func.Body.Block){
                                val z = Node.Decl.Func.Body.Block(Node.Block(  insertInEnd( (v.body!! as Node.Decl.Func.Body.Block).block.stmts, Node.Stmt.Expr(insExpr), prefix) ))
                            v.copy(v.mods,v.typeParams,v.receiverType,v.name,v.paramTypeParams,v.params,v.type, v.typeConstraints, z )
                        }
                        else v

                    }
                    else v
                }
            is Node.Decl.Func.Body -> {
                if (v is Node.Decl.Func.Body.Block){
                    val prefix = getPrefix()
                    val insExpr = getAppropriateMethodCall(getMatchString(v), false)
                   // val  insExpr =  getAppropriateMethodCall( (getMatchString(v)), false)  //addArg(getAppropriateMethodCall(), getMatchString(v))
                    v.copy( Node.Block( insertInEnd(v.block.stmts,  Node.Stmt.Expr(insExpr),prefix)))
                }
                else{
                    v
                }
            }
            is Node.Decl.Constructor -> {
                val insExpr = getAppropriateMethodCall(getMatchString(v), false)
                if (v.block !=null ){
                    val prefix = getPrefix()
                    v.copy ( v.mods, v.params, v.delegationCall, v.block!!.copy( (insertInEnd(v.block!!.stmts, Node.Stmt.Expr(insExpr) , prefix)))  )
                }
                else{
                    v.copy ( v.mods, v.params, v.delegationCall, Node.Block(  listOf(Node.Stmt.Expr(insExpr)) )  )
                }
            }
            is Node.Decl.Init -> {
                    val insExpr = getAppropriateMethodCall(getMatchString(v), false)
                val prefix = getPrefix()
                    v.copy( Node.Block((insertInEnd(v.block!!.stmts, Node.Stmt.Expr(insExpr) , prefix))))
            }
            /*is Node.Expr.Call.TrailLambda -> {
                val insExpr = expr
                if (v.func.block !=null ){
                    v.copy ( v.anns, v.label , v.func.copy(v.func.params, v.func.block!!.copy( (insertInEnd(v.func.block!!.stmts,insExpr , "Trepn")) ))  )
                }
                else{
                    v.copy( v.anns, v.label , v.func.copy(v.func.params,  Node.Block(  listOf(Node.Stmt.Expr(insExpr)) )))
                }
            }*/
                else -> v
            }
        }
    }

    private fun getPrefix(): String {
        return if (instrumentationType==JInst.InstrumentationType.METHOD){
            "Trepn"
        }else if (instrumentationType==JInst.InstrumentationType.TEST){
            "Trepn"
        }
        else if (instrumentationType==JInst.InstrumentationType.ACTIVITY){
            "Debug"
        }
        else{
            "Trepn"
        }
    }

    fun funcIsVoid(exp: Node): Boolean {
            var ret = false
            when (exp) {
                is Node.Decl.Func -> {
                    if (exp.type == null || KastreeWriterFixed.write(exp.type as Node, null).equals("Unit") ) {
                        ret = true

                    }
                }
                is Node.Decl.Init -> {
                    ret = true
                }
                is Node.Decl.Constructor -> {
                    ret = true

                }
            }
            return ret
        }

    fun addInstrumenterImport (node : Node.File): Node.File{
        return MMutableVisitor.preVisit(node) { v, _ ->
            when (v) {
                 is Node.File -> {
                     val l = v.imports.toMutableList()
                     for ( i in instrumenter.imports){
                         val nameList = i.name.name.split(".")
                         l.add(Node.Import(  nameList,false, null ))
                     }
                     v.copy(v.anns,v.pkg, l as List<Node.Import> ,v.decls)

                }
                else -> v
            }
        }
    }

    fun instrumentTestOriented (node: Node.File) : Node.File {
        val pack = if( node.pkg!=null) wrapPackage(node.pkg!!) else ""
        return instrumentBegin(addInstrumenterImport( node),  pack, mutableSetOf()) as Node.File
    }

    fun instrumentFileMethodOriented(node: Node.File ): Node.File {
            val pack = if( node.pkg!=null) wrapPackage(node.pkg!!) else ""
            val xz = addInstrumenterImport( node)
            val newFile =   instrumentBegin(xz , pack, mutableSetOf())
            val z = MMutableVisitor.preVisit(newFile) { v, _ ->
                when (v) {
                     is Node.Decl.Func  -> {
                        kotlinReturnFinderAndMarker(v)
                    }
                     is Node.Decl.Constructor -> {
                        kotlinReturnFinderAndMarker(v)
                    }
                     is Node.Decl.Init -> {
                        kotlinReturnFinderAndMarker(v)
                    }
                    else -> v
                }
            }
            return z as Node.File
        }

    fun instrumentFileAnnotationOriented(node: Node.File): Node.File {
        val pack = if( node.pkg!=null) wrapPackage(node.pkg!!) else ""
        return instrumentAnnotation(addInstrumenterImport( node),  pack, mutableSetOf()) as Node.File
    }


    fun wrapPackage(node : Node.Package): String{
        return node.names.foldRight(""){ y, x-> y + "." +x}.reversed().replaceFirst(".","").reversed()
    }

    fun extractString (node :Node ): String{
        if (node is Node.Expr.Name){
            return node.name
        }
        if (node is Node.Expr.BinaryOp && node.lhs is Node.Expr.Name){
            return extractString(node.lhs) + "." + extractString(node.rhs)
        }
        if(node is Node.Expr.StringTmpl){
            return (node.elems[0] as Node.Expr.StringTmpl.Elem.Regular).str
        }
        return ""
    }

    fun instrumentAnnotation(node: Node, scope : String, map : MutableSet<Int> ): Node {
        var scope1 : String =  scope
        return MMutableVisitor.preVisit(node) { v, p ->
            when  {
                v is Node.Expr.BinaryOp  && KastreeWriterFixed.write(v.oper).equals(".")  && v.rhs is Node.Expr.Call && ! map.contains(v.hashCode())->{
                    //scope1 += extractString(v.lhs)
                    map.add(v.rhs.hashCode())
                    //val insExpr = getAppropriateMethodCall(scope1, true)
                    val x = v.copy(v.lhs, v.oper, instrumentBegin(v.rhs,scope1, map ) as Node.Expr)
                    map.add(v.rhs.hashCode())
                    scope1 = scope
                    x
                }
                v is Node.Decl.Structured && v.form.name in interest  && ( ! map.contains(v.hashCode()) ) -> {
                    scope1+=  "."+v.name
                    //val insExpr = getAppropriateMethodCall(scope1, true)
                    val l: List<Node.Decl> = v.members.map { x -> instrumentAnnotation( x, scope1, map) }.filter { it is Node.Decl  }.map { it as Node.Decl }
                    scope1 = scope
                    val x = v.copy(v.mods,v.form,v.name,v.typeParams,v.primaryConstructor,v.parentAnns,v.parents,v.typeConstraints, l)
                    map.add(x.hashCode())
                    x
                }
                v is Node.Decl.Func  && ( ! map.contains(v.hashCode()) ) -> {
                    val args = ProjectMethods.wrapArgs(v.params)
                    //val scopada = scope1+ "->"+ v.name
                    scope1 += "->"+ v.name + "|"+ ProjectMethods.hashArgs(args)
                    val jo = JSONObject()
                    jo.put("args", JSONArray())
                    allMethods.addMethod( ProjectMethods.buildJSONObj(ProjectMethods.wrapMods(v.mods), args,scope1, "kotlin" ) )
                    val anns = mutableListOf<Node.Modifier>( HunterAnnotationInstrumenterAdapter(getInstrumenter() as HunterAnnotationInstrumenter).getAnnotations())
                    anns.addAll(v.mods)
                    val t = v.copy(anns,v.typeParams,v.receiverType,v.name,v.paramTypeParams,v.params,v.type,v.typeConstraints,v.body)
                    map.add(t.hashCode())
                    scope1 = scope
                    t
                }
                v is Node.Decl.Constructor  && ( ! map.contains(v.hashCode()) ) -> {
                    println("INSTRUMENT BEGIN KOTLIN 4")
                    val args = ProjectMethods.wrapArgs(v.params)
                    scope1 += "-><init>"+ "|" + ProjectMethods.hashArgs(args)
                    allMethods.addMethod( ProjectMethods.buildJSONObj(ProjectMethods.wrapMods(v.mods),args,scope1 , "kotlin" ) )
                    val anns = mutableListOf<Node.Modifier>( HunterAnnotationInstrumenterAdapter(getInstrumenter() as HunterAnnotationInstrumenter).getAnnotations())
                    anns.addAll(v.mods)
                    val t = v.copy(anns,v.params,v.delegationCall,v.block)
                    map.add(t.hashCode())
                    scope1 = scope
                    t
                }
                v  is Node.Decl.Init  && ( ! map.contains(v.hashCode()) ) -> {
                    scope1+="-><init>"
                    allMethods.addMethod( ProjectMethods.buildJSONObj(listOf(),ProjectMethods.wrapArgs(listOf()),scope1 , "kotlin" ) )
                    //val insExpr = getAppropriateMethodCall(scope1, true)
                    // val newRealInsExpr = addArg(insExpr, Node.Expr.Name ( scope1 ) )
                    //val x =  v.copy(Node.Block( insertInBegin(v.block.stmts, Node.Stmt.Expr(insExpr), "Trepn")))
                    map.add(v.hashCode())
                    v
                }
                else -> v
            }
        }
    }

}


fun readFileToString(fileName: String): String = File(fileName).inputStream().readBytes().toString(Charsets.UTF_8)

