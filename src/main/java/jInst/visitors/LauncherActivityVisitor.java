package jInst.visitors;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.ReferenceType;
import com.github.javaparser.ast.type.VoidType;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import jInst.Instrumentation.amp.ActivityInstrumenter;

import java.util.ArrayList;

public class LauncherActivityVisitor extends VoidVisitorAdapter {

    public static boolean isOnCreateMethod(BodyDeclaration x){
        return x instanceof MethodDeclaration && ((MethodDeclaration) x).getName().equals("onCreate")
                && ((MethodDeclaration) x).getParameters()!=null
                && ((MethodDeclaration) x).getParameters().size()==1
                && ((MethodDeclaration) x).getParameters().get(0).getType().toStringWithoutComments().matches("Bundle");
    }

    public static boolean isOnDestroyMethod(BodyDeclaration x){
        return  x instanceof MethodDeclaration
                && ((MethodDeclaration) x).getName().equals("onDestroy")
                && ( ((MethodDeclaration) x).getParameters()==null || ((MethodDeclaration) x).getParameters().isEmpty() );

    }

    public static boolean activityHasOnCreate(ClassOrInterfaceDeclaration n){
       return n.getMembers()!=null
               && n.getMembers().stream().filter( x -> isOnCreateMethod(x) ).count()==1;
    }


    public static boolean activityHasOnDestroy(ClassOrInterfaceDeclaration n){
        return n.getMembers()!=null
                &&  n.getMembers().stream().filter(x -> isOnDestroyMethod(x) ).count()==1;

    }

    public static MethodDeclaration createOnDestroyMethod(){
        MethodDeclaration md = new MethodDeclaration();
        md.setName("onDestroy");
        md.setType(new VoidType());
        md.setModifiers(ModifierSet.PUBLIC);
        md.setBody(new BlockStmt());
        md.getBody().setStmts(new ArrayList<Statement>());
        try {
            Statement xx = JavaParser.parseStatement("super.onDestroy();");
           md.getBody().getStmts().add(0, xx);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return md;
    }
    public static MethodDeclaration createOnCreateMethod(){
        MethodDeclaration md = new MethodDeclaration();
        md.setName("onCreate");
        md.setType(new VoidType());
        md.setModifiers(ModifierSet.PUBLIC);
        md.setParameters(new ArrayList<Parameter>());
        Parameter pd = new Parameter();
        pd.setType(new ReferenceType( new ClassOrInterfaceType("Bundle") ));
        pd.setId(new VariableDeclaratorId("bd"));
        md.getParameters().add(0, pd );
        md.setBody(new BlockStmt());
        md.getBody().setStmts(new ArrayList<Statement>());
        try {
            Statement xx = JavaParser.parseStatement("super.onCreate(bd);");
            md.getBody().getStmts().add(0, xx);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return md;
    }

    @Override
    public void visit(ClassOrInterfaceDeclaration n, Object arg) {
        if ( ! activityHasOnCreate(n) ){
            // add import to bundle if not exists
            Node cu = n;
            while ( ! ( cu instanceof CompilationUnit) ){
                cu = cu.getParentNode();
            }
            ((CompilationUnit) cu).getImports().add(0,  new ImportDeclaration(new NameExpr("android.os.Bundle"),false,false));
            // create on create method
            // martelada
            /*try {
                MethodDeclaration md = ((MethodDeclaration) JavaParser.parseBodyDeclaration("public void onCreate(Bundle bd){ super.onCreate(bd);}"));
                n.getMembers().add(0, md );
            } catch (ParseException e) {
                e.printStackTrace();
            }*/
            n.getMembers().add(0, createOnCreateMethod());

        }
        if ( ! activityHasOnDestroy(n) ) {
            n.getMembers().add( 0, createOnDestroyMethod());
        }
        for (BodyDeclaration b : n.getMembers()){
           if (b instanceof MethodDeclaration  ){
               visit(((MethodDeclaration) b),arg);
           }
        }
    }

    @Override
    public void visit(MethodDeclaration n, Object arg) {
        ActivityInstrumenter act = ((ActivityInstrumenter) arg);
        if( isOnCreateMethod(n) ){
            int i = 0;
            for (Expression ex : act.onActivityCreate(null)){
                n.getBody().getStmts().add(i++, new ExpressionStmt(ex)   );
            }
        }
        if( isOnDestroyMethod(n) ){
            int i = n.getBody().getStmts().size();
            for (Expression ex : act.onActivityDestroy(null)){
                n.getBody().getStmts().add(i++, new ExpressionStmt(ex)   );
            }
        }
    }
}
