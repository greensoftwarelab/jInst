package jInst.Instrumentation.profiler.trepn;

import com.github.javaparser.ast.expr.*;

import java.util.LinkedList;

public class TrepnLibrary {

    public static String libraryName = "com.greenlab.trepnlib";
   // public static String library = "TrepnLib";

    public static String  fulllibrary = "uminho.di.greenlab.trepnlibrary.TrepnLib";
    public static String library = "TrepnLib";
    public static String startProfiling = "startProfilingTest";
    public static String stopProfiling = "stopProfilingTest";
    public static String markMethod = "traceMethod";
    public static String markTest = "traceTest";
    public static String context = "null";


    public static MethodCallExpr getWritePermissions(){
        //xpressionStmt exp = new ExpressionStmt();
        MethodCallExpr metodo = new MethodCallExpr();
        MethodCallExpr mce = new MethodCallExpr();
        MethodCallExpr mce1 = new MethodCallExpr();
        NameExpr mce2 = new NameExpr();
        mce2.setName("InstrumentationRegistry");
        mce1.setName("getInstrumentation");
        mce.setName("getUiAutomation");
        metodo.setName("executeShellCommand");
        mce.setScope(mce1);
        mce1.setScope(mce2);
        metodo.setScope(mce);
        //exp.setExpression(metodo);
        LinkedList<Expression> list = new LinkedList<>();
        BinaryExpr bino = new BinaryExpr();
        bino.setOperator(BinaryExpr.Operator.plus);
        BinaryExpr left = new BinaryExpr();
        left.setLeft(new StringLiteralExpr("pm grant "));
        left.setOperator(BinaryExpr.Operator.plus);
        MethodCallExpr mceRight = new MethodCallExpr();
        MethodCallExpr mceRight1 = new MethodCallExpr();
        mceRight1.setScope(new NameExpr("InstrumentationRegistry"));
        mceRight1.setName("getTargetContext");
        mceRight.setScope(mceRight1);
        mceRight.setName("getPackageName");
        left.setRight(mceRight);
        bino.setLeft(left);
        bino.setRight(new StringLiteralExpr(" android.permission.WRITE_EXTERNAL_STORAGE"));
        list.add(bino);
        metodo.setArgs(list);
        return metodo;
    }

    public static MethodCallExpr getTrepnlibWritePermissions(){
        //ExpressionStmt exp = new ExpressionStmt();
        MethodCallExpr metodo = new MethodCallExpr();
        MethodCallExpr mce = new MethodCallExpr();
        MethodCallExpr mce1 = new MethodCallExpr();
        NameExpr mce2 = new NameExpr();
        mce2.setName("InstrumentationRegistry");
        mce1.setName("getInstrumentation");
        mce.setName("getUiAutomation");
        metodo.setName("executeShellCommand");
        mce.setScope(mce1);
        mce1.setScope(mce2);
        metodo.setScope(mce);
        //exp.setExpression(metodo);
        LinkedList<Expression> list = new LinkedList<>();
        BinaryExpr bino = new BinaryExpr();
        bino.setOperator(BinaryExpr.Operator.plus);
        BinaryExpr left = new BinaryExpr();
        left.setLeft(new StringLiteralExpr("pm grant "));
        left.setOperator(BinaryExpr.Operator.plus);

        left.setRight(new StringLiteralExpr("com.greenlab.trepnlib"));
        bino.setLeft(left);
        bino.setRight(new StringLiteralExpr(" android.permission.WRITE_EXTERNAL_STORAGE"));
        list.add(bino);
        metodo.setArgs(list);
        return metodo;
    }

    public static MethodCallExpr getReadPermissions(){
        // ExpressionStmt exp = new ExpressionStmt();
        MethodCallExpr metodo = new MethodCallExpr();
        MethodCallExpr mce = new MethodCallExpr();
        MethodCallExpr mce1 = new MethodCallExpr();
        NameExpr mce2 = new NameExpr();
        mce2.setName("InstrumentationRegistry");
        mce1.setName("getInstrumentation");
        mce.setName("getUiAutomation");
        metodo.setName("executeShellCommand");
        mce.setScope(mce1);
        mce1.setScope(mce2);
        metodo.setScope(mce);
        //exp.setExpression(metodo);
        LinkedList<Expression> list = new LinkedList<>();
        BinaryExpr bino = new BinaryExpr();
        bino.setOperator(BinaryExpr.Operator.plus);
        BinaryExpr left = new BinaryExpr();
        left.setLeft(new StringLiteralExpr("pm grant "));
        left.setOperator(BinaryExpr.Operator.plus);
        MethodCallExpr mceRight = new MethodCallExpr();
        MethodCallExpr mceRight1 = new MethodCallExpr();
        mceRight1.setScope(new NameExpr("InstrumentationRegistry"));
        mceRight1.setName("getTargetContext");
        mceRight.setScope(mceRight1);
        mceRight.setName("getPackageName");
        left.setRight(mceRight);
        bino.setLeft(left);
        bino.setRight(new StringLiteralExpr(" android.permission.READ_EXTERNAL_STORAGE"));
        list.add(bino);
        metodo.setArgs(list);
        return metodo;
    }

    public static MethodCallExpr getTrepnlibReadPermissions(){
        // ExpressionStmt exp = new ExpressionStmt();
        MethodCallExpr metodo = new MethodCallExpr();
        MethodCallExpr mce = new MethodCallExpr();
        MethodCallExpr mce1 = new MethodCallExpr();
        NameExpr mce2 = new NameExpr();
        mce2.setName("InstrumentationRegistry");
        mce1.setName("getInstrumentation");
        mce.setName("getUiAutomation");
        metodo.setName("executeShellCommand");
        mce.setScope(mce1);
        mce1.setScope(mce2);
        metodo.setScope(mce);
        //exp.setExpression(metodo);
        LinkedList<Expression> list = new LinkedList<>();
        BinaryExpr bino = new BinaryExpr();
        bino.setOperator(BinaryExpr.Operator.plus);
        BinaryExpr left = new BinaryExpr();
        left.setLeft(new StringLiteralExpr("pm grant "));
        left.setOperator(BinaryExpr.Operator.plus);
        left.setRight(new StringLiteralExpr("com.greenlab.trepnlib"));
        bino.setLeft(left);
        bino.setRight(new StringLiteralExpr(" android.permission.READ_EXTERNAL_STORAGE"));
        list.add(bino);
        metodo.setArgs(list);
        return metodo;
    }

}
