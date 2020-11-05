package jInst;


import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.Expression;
import jInst.util.FileUtils;
import jInst.util.KastreeWriterFixed;
import jInst.util.ParserFixed;
import kastree.ast.Node;

import java.io.File;
import java.io.FileInputStream;


/**
 * Created by rrua on 30/05/17.
 */

public class tester {

/*
    public static void main(String[] args) {
        String f = "/Users/ruirua/repos/Anadroid/demoProjects/appsSauce/y20k-escapepod/v0.8.4_src/y20k-escapepod-c43457f/app/src/main/java/org/y20k/escapepod/PodcastPlayerActivity.kt";
        ParserFixed pf = new ParserFixed();
        Node.File fs =  pf.parseFile( FileUtils.stringFromFile(f),false);
        System.out.println(KastreeWriterFixed.Companion.write(fs,null));

            }
*/
    /*
    public static void main(String[] args) throws Exception{

        //checkParser();
      //  SootClass sClassNew = Scene.v().loadClassAndSupport("jInst.JInst");
      //  sClassNew.setApplicationClass();
        System.out.println("");
        String s = "TrepnLib.updateState(1,3);";

       Expression sss =  JavaParser.parseExpression(s);
        System.out.println(sss);

       // Node.Expr n =  Batata.Companion.buildTrepnLibTraceCall();
    }





    public static void checkParser() throws Exception{

        String file = "/Users/ruirua/tests/actual/27d5f1b6-d1b3-496b-b6c8-9ba25532a0b7/latest/_TRANSFORMED_/app/src/jInst.Instrumentation.main/java/com/micnubinub/materiallibrary/MaterialRadioButton.java";

        CompilationUnit cu;
       FileInputStream in = new FileInputStream(file);
       try {
           // parse the file
           cu = JavaParser.parse(in,null, false);
           System.out.println("");
       } finally {
           in.close();
       }
    }
*/


//    public static MethodCallExpr me = new MethodCallExpr();

//   public static void jInst.Instrumentation.main(String[] args) throws Exception{
//
//       String file = "/Users/ruirua/repos/GreenDroid/jInst/src/jInst.Instrumentation.main/java/jInst/tester.java";
//       CompilationUnit cu;
//       FileInputStream in = new FileInputStream(file);
//       try {
//           // parse the file
//           cu = JavaParser.parse(in,null, false);
//           System.out.println("");
//       } finally {
//           in.close();
//       }
//
//   }
//
//
//
//
//
//
//
//   public static Map<String,String> apisUsed (MethodDeclaration me ){
//       Map<String,String> returnMap = new HashMap<>();
//
//
//
//       return returnMap;
//   }
//
//
//
//    public static  int dummymethod(){
//        MethodCallExpr mce = new MethodCallExpr();
//        Integer [] batata = new Integer[10];
//        Integer xx = new Integer(10).hashCode();
//        xx.toString();
//        me.getArgs();
//        me = mce;
//        System.out.println(me);
//        int c = 0;
//        //int x = c==0? 0 :1 ;
//        ClassInfo ci = new ClassInfo();
//        APICallUtil.getClassesUsed(new MethodDeclaration());
//        //dummymethod();
//        for (int i = 0; i <10  ; i++) {
//
//            if(c > 0)
//                return 2;
//        }
//
//        return 0;
//    }

}
