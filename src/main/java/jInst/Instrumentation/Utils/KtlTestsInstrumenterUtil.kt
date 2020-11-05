package jInst.Instrumentation.Utils

import jInst.util.KastreeWriterFixed
import kastree.ast.Node
import kastree.ast.Visitor
import kastree.ast.Writer



class KtlTestsInstrumenterUtil {

    companion object {

        fun isLauncherActivity(node:Node.File, activityName: String):Boolean {
            val pack = Writer.write(node.pkg!!).replace("package ","")
            var ret = false
            Visitor.visit(node){ v,_ ->
                when(v){
                    is Node.Decl.Structured ->{
                        if (v.form == Node.Decl.Structured.Form.CLASS && ( pack+"."+ v.name).equals(activityName))
                            ret=true
                            return@visit
                    }
                    else ->v
                }
            }
           return ret
        }

        fun isTestFile(node:Node.File):Boolean {
            return !(node.imports.filter {
                x -> KastreeWriterFixed.write(x).toLowerCase().contains("org.junit")
            }).isEmpty()
        }

        fun isTestCase (node: Node):Boolean{
            var ret = false
            Visitor.visit(node) { v,_ ->
                when(v){
                    is Node.Decl.Func ->{
                        if(v.anns.any {x-> KastreeWriterFixed.Companion.write(x).contains("Test")  }){
                            ret = true
                           // println(v.name)
                            return@visit
                        }
                    }
                }
            }
            return ret
        }

        fun hasAnnotatedMethod ( node : Node, annName: String): Boolean {
            var ret = false
            Visitor.visit(node) { v,_ ->
                when(v){
                    is Node.Decl.Func ->{
                        if(v.anns.any {x-> KastreeWriterFixed.Companion.write(x).contains(annName)  }){
                            ret = true
                           // println(annName+" -> "+v.name)
                            return@visit
                        }
                    }
                }
            }
            return ret
        }
    }


}

