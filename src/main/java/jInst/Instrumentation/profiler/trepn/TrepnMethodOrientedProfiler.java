package jInst.Instrumentation.profiler.trepn;

import com.github.javaparser.ASTHelper;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.expr.*;
import jInst.Instrumentation.profiler.MethodOrientedProfiler;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rrua on 17/06/17.
 * Created by rrua on 17/06/17.
 */

public class TrepnMethodOrientedProfiler extends TrepnLibrary implements MethodOrientedProfiler{


    private static String markstartMethod = "updateState";
    private static String markstopMethod = "updateState";
    //public static String hunterLibrary = "com.hunter.library.debug";



    public static String getMarkstartMethod() {
        return markstartMethod;
    }

    public static String getMarkstopMethod() {
        return markstartMethod;
    }

    public TrepnMethodOrientedProfiler(){


        startProfiling = "startProfiling";
        stopProfiling = "stopProfiling";
        context = "null";
        markTest = "traceTest";
        //hunterLibrary = "com.hunter.library.debug";

    }

    @Override
    public MethodCallExpr startProfiler(Expression context) {
        MethodCallExpr mcB = new MethodCallExpr();
        mcB.setScope(new NameExpr(library));
        mcB.setName(   startProfiling);
        ASTHelper.addArgument(mcB, context);
        return mcB;
    }

    @Override
    public MethodCallExpr stopProfiler(Expression context) {
        MethodCallExpr mcB = new MethodCallExpr();
        mcB.setScope(new NameExpr(library));
        mcB.setName( stopProfiling);
        ASTHelper.addArgument(mcB, context);
        return mcB;
    }

    @Override
    public ImportDeclaration getLibrary() {
        return new ImportDeclaration(new NameExpr(fulllibrary),false,false);
    }

    @Override
    public MethodCallExpr getContext() {
        return null;
    }

    @Override
    public MethodCallExpr markMethodStart(Expression context, String method) {
        MethodCallExpr mcB = new MethodCallExpr();
        mcB.setScope(new NameExpr(library));
        mcB.setName(  markstopMethod);
        //mcB.setName(markstartMethod);
        ASTHelper.addArgument(mcB, new NullLiteralExpr());
        Expression flagE = new IntegerLiteralExpr("1");
        Expression method1 = new StringLiteralExpr( method);
        ASTHelper.addArgument(mcB, flagE);
        ASTHelper.addArgument(mcB, method1);
        return mcB;
    }

    @Override
    public MethodCallExpr marKMethodStop(Expression context, String method) {
        MethodCallExpr mcB = new MethodCallExpr();
        mcB.setScope(new NameExpr(library));
        mcB.setName(  markstopMethod);
        //ASTHelper.addArgument(mcB, context);
        ASTHelper.addArgument(mcB, new NullLiteralExpr());
        Expression flagE = new IntegerLiteralExpr("0");
        Expression method1 = new StringLiteralExpr( method);
        ASTHelper.addArgument(mcB, flagE);
        ASTHelper.addArgument(mcB, method1);
        return  mcB;
    }

    @Override
    public MethodCallExpr marKTest(Expression context, String method) {
        MethodCallExpr mcB = new MethodCallExpr();
        mcB.setScope(new NameExpr(library));
        mcB.setName(  markTest);
        Expression method1 = new StringLiteralExpr( method);
        ASTHelper.addArgument(mcB, method1);
        return  mcB;
    }

    @Override
    public List<MethodCallExpr> getAllowPermissionCalls() {
        List<MethodCallExpr> l = new ArrayList<>();
        l.add(TrepnLibrary.getReadPermissions());
        l.add(TrepnLibrary.getWritePermissions());
        l.add(TrepnLibrary.getTrepnlibReadPermissions());
        l.add(TrepnLibrary.getTrepnlibWritePermissions());
        return l;
    }

    @Override
    public List<ImportDeclaration> getImports() {
        ArrayList<ImportDeclaration> l =  new ArrayList<>();
        l.add(getLibrary());

        //ImportDeclaration importHunter = new ImportDeclaration(new NameExpr(hunterLibrary),false,true);
        //l.add(importHunter);

        return l;
    }

    @Override
    public String getLibraryName() {
        return libraryName;
    }


}
