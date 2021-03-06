package jInst.Instrumentation.amp

import jInst.util.convertImportDeclaration
import jInst.util.convertMethodCall
import kastree.ast.Node

class AMPInstrumenterAdapter ( val actInstr : ActivityInstrumenter )  {



     fun onActivityCreate(arg: Any?): List<Node.Expr> {

        val l =  actInstr.onActivityCreate(arg)
         return l.map { x -> convertMethodCall(x ) }
    }

     fun onActivityDestroy(arg: Any?): List<Node.Expr>? {
         val l =  actInstr.onActivityDestroy(arg)
         return l.map { x -> convertMethodCall(x ) }
    }

     fun getImports(): List<Node.Import>? {
         return actInstr.imports.map { x -> convertImportDeclaration(x ) }
    }

     fun getManifestEntries(): MutableList<String> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


}


/*
fun main() {
    val amp = AMPInstrumenter()
    val ampAdapt = AMPInstrumenterAdapter(amp)
    val codeRepr = Parser.parseFile("import batata.cao \n" +
            "" +
            "val amp = AMPInstrumenter(\"random\")")
    val l = ampAdapt.onActivityCreate("batata.trace")
   for (c in l){
       println(Writer.write(c))
   }
}*/