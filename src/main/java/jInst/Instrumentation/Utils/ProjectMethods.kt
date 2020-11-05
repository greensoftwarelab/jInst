package jInst.Instrumentation.Utils

import com.github.javaparser.ast.TypeParameter
import com.github.javaparser.ast.body.AnnotationDeclaration
import com.github.javaparser.ast.body.ModifierSet
import com.github.javaparser.ast.body.Parameter
import com.github.javaparser.ast.type.ClassOrInterfaceType
import com.github.javaparser.ast.type.PrimitiveType
import com.github.javaparser.ast.type.ReferenceType
import com.github.javaparser.ast.type.WildcardType
import com.github.javaparser.ast.visitor.VoidVisitorAdapter
import jInst.ResourceLoader
import jInst.util.KastreeWriterFixed
import jInst.visitors.utils.ClassDefs
import kastree.ast.MutableVisitor
import kastree.ast.Node
import kastree.ast.Writer
import org.jetbrains.kotlin.utils.addToStdlib.cast
import org.json.simple.JSONArray

import org.json.simple.JSONObject

public class ProjectMethods( )  {
    val allmethods : JSONArray = JSONArray()
    companion object {
        fun wrapArgs( listArgs : List<Node> ) : List<String> {
            val l = arrayListOf<String>()
            listArgs.forEach {
                when {
                    it is Node.Decl.Func.Param -> {
                        if (it.type != null) {
                            when (it.type!!.ref) {
                                is Node.TypeRef.Func -> {
                                    val argsize = ((it.type!!.ref as Node.TypeRef.Func).params).size
                                    val rs = ResourceLoader()
                                    // l.add(rs.retrieveConfigJVMConventions("lambdaFunction").replace("?","") + argsize) //TODO
                                    l.add("Lkotlin.jvm.functions.Function"+argsize)
                                }
                                else -> {
                                    val x = (KastreeWriterFixed.Companion.write(it.type!!)).replace("?","")
                                    l.add(x)
                                }
                            }
                        }
                    }
                }
            }
            return l;
        }

        fun hashArgs(listArgs: List<String>): String {
            var str:String = "";
            listArgs.forEach {
                val  x = it
                str += if(x.split(".").size>0) x.split(".")[x.split(".").size-1] else x
            }
            return str.toLowerCase().hashCode().toString()
        }

        fun wrapJavaArgs( listArgs : List<Parameter>? ) : List<String> {
            val l = arrayListOf<String>()
            if (listArgs!=null && listArgs!!.size>2)
                print("")
            listArgs?.forEach {
                    var z = ProjectMethods.ExtractParameterType("")
                    z.visit(it,  Any())
                    if ( ! z.str.equals("")){
                        l.add(z.str)
                    }
            }
            return l;
        }



        fun buildJSONObj ( listmods: List<String> ,listArgs: List<String>, key: String, language: String ) : JSONObject{
            val ja =  JSONArray()
            listArgs.forEach { ja.add(it) }
            val jo = JSONObject()
            jo.put("name", key )
            jo.put("args", ja)
            jo.put("language", language)
            jo.put("hash", hashArgs(listArgs) )
            val mods = JSONArray()
            listmods.forEach {  mods.add(it) }
            jo.put("modifiers", mods  )
            return jo
        }

        fun wrapMods(mods: List<Node.Modifier>): List<String> {
            val l = ArrayList<String>()
            mods.forEach { x ->
                when (x){
                    is Node.Modifier.Lit ->{
                        l.add(x.keyword.name)
                    }
                    is Node.Modifier.Keyword->{
                        l.add(x.name)
                    }
                }
            }
            return l
        }
        fun wrapMods(mods: Int ): List<String> {
            val l = ArrayList<String>()
            when{
                ModifierSet.isFinal(mods) ->{
                    l.add("final")
                }
                ModifierSet.isNative(mods) ->{
                    l.add("native")
                }
                ModifierSet.isPrivate(mods) ->{
                    l.add("private")
                }
                ModifierSet.isProtected(mods) ->{
                    l.add("protected")
                }
                ModifierSet.isPublic(mods) ->{
                    l.add("public")
                }
                ModifierSet.isStatic(mods) ->{
                    l.add("static")
                }
                ModifierSet.isSynchronized(mods) ->{
                    l.add("synchronized")
                }
                ModifierSet.isTransient(mods) ->{
                    l.add("transient")
                }
                ModifierSet.isVolatile(mods) ->{
                 l.add("volatile")
                }

            }
            return l
        }
    }

    fun addMethod(jo : JSONObject){
        allmethods.add(jo)
    }



    /*

    //FUN fact -> compilador nao consegue diferenciar isto

    fun xxx ( mm : (Int,Int) -> Unit ): Unit {

    }

    fun xxx ( mqm : (String,Int) -> Int ): Unit {

    }


    // NEM este
    fun xxx ( mm : Int): Unit {

    }

    fun xxx ( mqm : Int ): Int {

    }

    // MAS CONSEGUE este, porque mapeia o primeiro para int e o segundo para Integer

    fun xxx ( mm : Int): Unit {

    }

    fun xxx ( mqm : Int? ): Unit {

    }

    // MAS NESTE CASO já nao, nao consegue mapear para tipos diferentes ):

    fun xxx ( mm : Node.Expr): Unit {

    }

    fun xxx ( mqm : Node.Expr? ): Unit {

    }
    */


    class ExtractParameterType(var str : String): VoidVisitorAdapter<Any>(){
        override fun visit(n: ReferenceType?, a:Any) {
            if (n !=null){
                str = n.type.toString()
                var i = n.arrayCount
                while (i>0){
                    str = "[" + str +"]"
                    i--
                }
            }
        }
        override fun visit(n: PrimitiveType?, a:Any) {
            if (n !=null){
                str = n.type.toString()
            }
        }
        override fun visit(n: WildcardType?, a:Any) {
            if (n !=null){
                str = n.toStringWithoutComments()
            }
        }
        override fun visit(n: ClassOrInterfaceType?, a:Any) {
            if (n !=null){
                str = n.name
            }
        }

    }


    fun convertTOJSONObject (): JSONObject{
         val jo  = JSONObject()
        allmethods.forEach{ x->
           val z =  x as JSONObject
            jo.put(z.get("name"), z)
        }
        return jo
    }



}