package jInst.profiler.trepn;

import com.github.javaparser.ASTHelper;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.visitor.GenericVisitor;
import com.github.javaparser.ast.visitor.VoidVisitor;
import jInst.profiler.TestOrientedProfiler;
import jdk.nashorn.internal.ir.BlockStatement;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rrua on 17/06/17.
 */
public class TrepnTestOrientedProfiler extends TrepnLibrary implements TestOrientedProfiler {

    public static String markMethod = "traceMethod";
    public static String markTest = "traceTest";
    public static String hunterLibrary = "com.hunter.library.debug";

    public TrepnTestOrientedProfiler(){
        //fulllibrary = "com.greenlab.trepnlib.TrepnLib";
        //library = "TrepnLib"
        startProfiling = "startProfilingTest";
        stopProfiling = "stopProfilingTest";
        markMethod = "traceMethod";
        markTest = "traceTest";
        hunterLibrary = "com.hunter.library.debug";
    }


    @Override
    public MethodCallExpr startProfiler(Expression context) {
        MethodCallExpr mcB = new MethodCallExpr();
        mcB.setScope(new NameExpr(library));
        mcB.setName(  startProfiling);
        ASTHelper.addArgument(mcB, context);
        //ASTHelper.addArgument(mcB, method);
        return mcB;


    }

    @Override
    public MethodCallExpr stopProfiler(Expression context) {
        MethodCallExpr mcB = new MethodCallExpr();
        mcB.setScope(new NameExpr(library));
        mcB.setName(  stopProfiling);
        ASTHelper.addArgument(mcB, context);
        //ASTHelper.addArgument(mcB, method);
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
    public MethodCallExpr marKTest(Expression context, String method) {
        MethodCallExpr mcB = new MethodCallExpr();
        mcB.setScope(new NameExpr(library));
        mcB.setName(markTest);
        Expression method1 = new StringLiteralExpr( method);
        ASTHelper.addArgument(mcB, method1);
        return  mcB;
    }

    @Override
    public MethodCallExpr markMethod(Expression context, String method) {
        MethodCallExpr mcB = new MethodCallExpr();
        mcB.setScope(new NameExpr(library));
        mcB.setName(markMethod);
        Expression method1 = new StringLiteralExpr(method);
        ASTHelper.addArgument(mcB, method1);
        return  mcB;
    }

    @Override
    public MethodCallExpr markTest(Expression context, String method) {
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

       ImportDeclaration importHunter = new ImportDeclaration(new NameExpr(hunterLibrary),false,true);
       l.add(importHunter);
       return l;
    }
}
