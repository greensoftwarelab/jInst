/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jInst.visitors;

import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import java.util.LinkedList;
import java.util.List;

import jInst.Instrumentation.InstrumentHunterDebug;
import jInst.profiler.MethodOrientedProfiler;
import jInst.profiler.Profiler;
import jInst.profiler.TestOrientedProfiler;
import jInst.Instrumentation.InstrumentHelper;

import jInst.profiler.trepn.TrepnLibrary;
import jInst.visitors.utils.ClassDefs;

/**
 *
 * @author User
 */
public class TestChangerVisitor extends VoidVisitorAdapter{


    public static boolean traceMethod;

    @Override
    public void visit(ConstructorDeclaration n, Object arg){

        ClassDefs cDef = (ClassDefs)arg;
        Expression className = new StringLiteralExpr(cDef.getDescriptor());
        Expression method = new StringLiteralExpr(n.getName());
        if(n.getBlock() == null) n.setBlock(new BlockStmt());
        List<Statement> x = n.getBlock().getStmts() != null ? n.getBlock().getStmts() : new LinkedList<Statement>();
        String metodo = ((ClassDefs) arg).getPack() + "." + ((ClassDefs) arg).getName()+"<" +n.getName() + ">";
        MethodCallExpr m = null;
        MethodCallExpr mce =  (((Profiler) InstrumentHelper.getInstrumenter())).marKTest(m,metodo);
        x.add( new ExpressionStmt(mce));
        n.getBlock().setStmts(x);

    }

    @Override
    public void visit(MethodDeclaration n, Object arg) {

        ClassDefs cDef = (ClassDefs)arg;
        Expression className = new StringLiteralExpr(cDef.getDescriptor());
        Expression method = new StringLiteralExpr(n.getName());
        if(n.getBody() == null) n.setBody(new BlockStmt());
        List<Statement> x = n.getBody().getStmts() != null ? n.getBody().getStmts() : new LinkedList<Statement>();
        if(n.getName().equals("suite") && n.getType().getClass().getName().equals("Test")){
            //is a Test Suite
            cDef.setSuite(true);
        }
        else{
            String s1 = "";
            if(n.getAnnotations()!=null && n.getAnnotations().size() > 0){
                 s1 = (String) n.getAnnotations().get(0).getName().getName();
            }
            if(cDef.isJunit4suite()){

//                if (s1.equals("BeforeClass")) {
//                    cDef.setBeforeClass(true);
//                    //... <- add the call 'startEstimator();' at the begining
//                    MethodCallExpr mcS = null;
//                    MethodCallExpr getContext = new MethodCallExpr();
//                    if (!cDef.isInstrumented()) {
//                        getContext.setName("this.getContext");
//                    }
//                    else {
//                        if (cDef.isOther()){
//                            getContext.setName("this.getContext");
//                        }
//                        else{
//                            getContext.setName("InstrumentationRegistry.getTargetContext");
//                        }
//
//                    }
//                    if(traceMethod){
////                        mcS.setName("TrepnLib.startProfilingTest");
//                          mcS = ((TestOrientedProfiler) InstrumentHelper.getProfiler()).startProfiler(getContext);
//                    }
//                    else {
////                        mcS.setName("TrepnLib.startProfiling");
//                        mcS = ((MethodOrientedProfiler) InstrumentHelper.getProfiler()).startProfiler(getContext);
//                    }
//
//
//
//                    //ASTHelper.addArgument(getUid, getPid);
//                    //ASTHelper.addArgument(mcS, new StringLiteralExpr(cDef.getAppName()));
//                    // ASTHelper.addArgument(mcConfig, getUid);
////                    ASTHelper.addArgument(mcS, getContext);
//
//                    x.add(0, new ExpressionStmt(mcS));
//                    // x.add(1, new ExpressionStmt(mcS));
//                    n.getBody().setStmts(x);
//
//                }

//                //junit 4
//                if (s1.equals("AfterClass")) {
//                    cDef.setAfterClass(true);
//                    //... <- add the call 'startEstimator();' at the begining
//                    MethodCallExpr mcS = new MethodCallExpr();
//                    MethodCallExpr getContext = new MethodCallExpr();
//                    if (!cDef.isInstrumented()) {
//                        getContext.setName("this.getContext");
//                    }
//                    else {
//                        if (cDef.isOther()){
//                            getContext.setName("this.getContext");
//                        }
//                        else{
//                            getContext.setName("InstrumentationRegistry.getTargetContext");
//                        }
//                    }
//                    if(traceMethod){
////                        mcS.setName("TrepnLib.startProfilingTest");
//                        mcS = ((TestOrientedProfiler) InstrumentHelper.getProfiler()).stopProfiler(getContext);
//                    }
//                    else {
////                        mcS.setName("TrepnLib.startProfiling");
//                        mcS = ((MethodOrientedProfiler) InstrumentHelper.getProfiler()).stopProfiler(getContext);
//                    }
//
//                    //ASTHelper.addArgument(getUid, getPid);
//                    //ASTHelper.addArgument(mcS, new StringLiteralExpr(cDef.getAppName()));
//                    // ASTHelper.addArgument(mcConfig, getUid);
//                    ASTHelper.addArgument(mcS, getContext);
//
//                    x.add(x.size(), new ExpressionStmt(mcS));
//                    // x.add(1, new ExpressionStmt(mcS));
//                    n.getBody().setStmts(x);
//
//                }
            }
            if(cDef.isJunit4()) {

                if (s1.equals("Before")) {
                    cDef.setBefore(true);
                    //... <- add the call 'startEstimator();' at the begining
                    MethodCallExpr mcS = new MethodCallExpr();
                    MethodCallExpr getContext = new MethodCallExpr();
                    if (!cDef.isInstrumented()) {
                        getContext.setName("this.getContext");
                    } else {
                        if (cDef.isOther()){
                            getContext.setName("this.getInstrumentation().getContext");
                        }
                        else{
                            getContext.setName("InstrumentationRegistry.getTargetContext");
                        }
                    }
                    if(traceMethod){
                        mcS = ((TestOrientedProfiler) InstrumentHelper.getInstrumenter()).startProfiler(getContext);
                    }
                    else {
//
                        mcS = ((MethodOrientedProfiler) InstrumentHelper.getInstrumenter()).startProfiler(getContext);
                    }

                    x.add(0, new ExpressionStmt(mcS));
                    // x.add(1, new ExpressionStmt(mcS));
                    n.getBody().setStmts(x);

                    if(InstrumentHelper.compiledSdkVersion>22) {
                        ExpressionStmt exp = new ExpressionStmt(TrepnLibrary.getReadPermissions());
                        ExpressionStmt exp1 = new ExpressionStmt(TrepnLibrary.getWritePermissions());
                        ExpressionStmt exp2 = new ExpressionStmt(TrepnLibrary.getTrepnlibReadPermissions());
                        ExpressionStmt exp3 = new ExpressionStmt(TrepnLibrary.getTrepnlibWritePermissions());
                        x.add(0,exp);
                        x.add(0,exp1);
                        x.add(0,exp2);
                        x.add(0,exp3);
                    }

                }

                //junit 4
                if (s1.equals("After")) {
                    cDef.setAfter(true);
                    //... <- add the call 'startEstimator();' at the begining
                    MethodCallExpr getContext = new MethodCallExpr();
                    if (!cDef.isInstrumented()) {
                        getContext.setName("this.getContext");
                    } else {
                        if (cDef.isOther()){
                            getContext.setName("this.getInstrumentation().getContext");
                        }
                        else{
                            getContext.setName("InstrumentationRegistry.getTargetContext");
                        }
                    }
                    MethodCallExpr mcS = new MethodCallExpr();
                    if(traceMethod){
//                        mcS.setName("TrepnLib.startProfilingTest");
                        mcS = ((TestOrientedProfiler) InstrumentHelper.getInstrumenter()).stopProfiler(getContext);
                    }
                    else {
//                        mcS.setName("TrepnLib.startProfiling");
                        mcS = ((MethodOrientedProfiler) InstrumentHelper.getInstrumenter()).stopProfiler(getContext);
                    }

                    x.add(x.size()-1 >0 ? x.size()-1:0, new ExpressionStmt(mcS));
                    // x.add(1, new ExpressionStmt(mcS));
                    n.getBody().setStmts(x);

                }
                else {
                     System.out.println("NORMAL TEST VISITOR");
                    //is normal test
                    InstrumentHunterDebug instrumentHunterDebug = new InstrumentHunterDebug();
                    instrumentHunterDebug.insertMarkerAnnotation(n,"HunterDebug");
                    String metodo = InstrumentHelper.wrapMethod(n,cDef,"");
                    MethodCallExpr mce = ((TestOrientedProfiler) InstrumentHelper.getInstrumenter()).markTest(null,metodo);
                    x.add(0, new ExpressionStmt(mce));
                    n.getBody().setStmts(x);
                }
            }
            else {
                if (n.getName().equals("setUp")) {
                    cDef.setBefore(true);
                    cDef.setSetUp(true);
                    MethodCallExpr mcS = new MethodCallExpr();
                    MethodCallExpr getContext = new MethodCallExpr();
                    
                    if (!cDef.isInstrumented()) {
                        getContext.setName("this.getContext");
                    } else {
                        if (cDef.isOther()){
                            getContext.setName("this.getInstrumentation().getContext");
                        }
                        else{
                            getContext.setName("InstrumentationRegistry.getTargetContext");
                        }
                    }
                    if(traceMethod){
                        mcS = ((TestOrientedProfiler) InstrumentHelper.getInstrumenter()).startProfiler(getContext);
                    }
                    else {
                        mcS = ((MethodOrientedProfiler) InstrumentHelper.getInstrumenter()).startProfiler(getContext);
                    }

                    x.add(0, new ExpressionStmt(mcS));
                    n.getBody().setStmts(x);


                    if(InstrumentHelper.compiledSdkVersion>22) {
                        ExpressionStmt exp = new ExpressionStmt(TrepnLibrary.getReadPermissions());
                        ExpressionStmt exp1 = new ExpressionStmt(TrepnLibrary.getWritePermissions());
                        ExpressionStmt exp2 = new ExpressionStmt(TrepnLibrary.getTrepnlibReadPermissions());
                        ExpressionStmt exp3 = new ExpressionStmt(TrepnLibrary.getTrepnlibWritePermissions());
                        x.add(0,exp);
                        x.add(0,exp1);
                        x.add(0,exp2);
                        x.add(0,exp3);
                    }

                } else if (n.getName().equals("tearDown")) {
                    cDef.setAfter(true);
                    cDef.setTearDown(true);
                    MethodCallExpr mcT = new MethodCallExpr();
                    MethodCallExpr getContext = new MethodCallExpr();
                    if (!cDef.isInstrumented()) {
                        getContext.setName("this.getContext");
                    } else {
                        if (cDef.isOther()){
                            getContext.setName("this.getInstrumentation().getContext");
                        }
                        else{
                            getContext.setName("InstrumentationRegistry.getTargetContext");
                        }
                    }
                    if(traceMethod){
//                        mcS.setName("TrepnLib.startProfilingTest");
                        mcT = ((TestOrientedProfiler) InstrumentHelper.getInstrumenter()).stopProfiler(getContext);
                    }
                    else {
//                        mcS.setName("TrepnLib.startProfiling");
                        mcT = ((MethodOrientedProfiler) InstrumentHelper.getInstrumenter()).stopProfiler(getContext);
                    }

//                    ASTHelper.addArg ument(mcT, getContext);
                    x.add(x.size()-1 >0 ? x.size()-1:0, new ExpressionStmt(mcT));
                    n.getBody().setStmts(x);
                }

                else {
                     System.out.println("NORMAL TEST VISITOR 2");
                    //normal test
                    InstrumentHunterDebug instrumentHunterDebug = new InstrumentHunterDebug();
                    instrumentHunterDebug.insertMarkerAnnotation(n,"HunterDebug");
                    String metodo = InstrumentHelper.wrapMethod(n,cDef,"");
                    MethodCallExpr mce = ((TestOrientedProfiler) InstrumentHelper.getInstrumenter()).markTest(null,metodo);
                    x.add(0, new ExpressionStmt(mce));
                    n.getBody().setStmts(x);
                }
            }
        }
    }
}
