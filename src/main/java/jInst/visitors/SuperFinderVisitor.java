package jInst.visitors;

import com.github.javaparser.ast.stmt.ExplicitConstructorInvocationStmt;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import jInst.visitors.utils.ReturnFlag;

public class SuperFinderVisitor extends VoidVisitorAdapter {


    @Override
    public void visit(ExplicitConstructorInvocationStmt n, Object arg) {
        ReturnFlag x = (ReturnFlag)arg;
        x.setRet(true);
    }



}
