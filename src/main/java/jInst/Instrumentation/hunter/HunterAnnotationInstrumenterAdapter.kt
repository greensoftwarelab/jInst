package jInst.Instrumentation.hunter

import jInst.util.convertAnnotation
import jInst.util.convertImportDeclaration
import kastree.ast.Node

class HunterAnnotationInstrumenterAdapter(val hai : HunterAnnotationInstrumenter){


    fun getAnnotations() : Node.Modifier.AnnotationSet{
        val annotations =  hai.annotations.map{ convertAnnotation(it) }
        return Node.Modifier.AnnotationSet(target = null, anns = annotations)
    }

    fun getImports() : List<Node.Import>{
        return hai.imports.map{ convertImportDeclaration(it) }
    }


}