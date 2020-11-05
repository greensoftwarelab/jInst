/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jInst.visitors;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;

import jInst.Instrumentation.Instrumenter;
import jInst.Instrumentation.Utils.ProjectMethods;
import jInst.profiler.MethodOrientedProfiler;
import jInst.profiler.Profiler;
import jInst.profiler.TestOrientedProfiler;
import jInst.Instrumentation.InstrumentHelper;
import jInst.visitors.utils.ReturnFlag;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.util.ArrayList;
import java.util.List;
import jInst.visitors.utils.ClassDefs;
import jInst.util.ClassM;
import jInst.util.PackageM;
import org.json.simple.JSONObject;

import java.util.LinkedList;

/**
 *
 * @author User
 */
public class MethodChangerVisitor extends VoidVisitorAdapter {
    private static LinkedList<PackageM> packages = new LinkedList<PackageM>();

    private static final int NUMBER_OF_OPERATIONS = 0; // TODO

    public static void restartPackages(){
        packages.clear();
    }

    public static LinkedList<PackageM> getPackages(){
        return packages;
    }

    public static CompilationUnit cu;
    public static boolean tracedMethod;

    public void setTracedMethod(boolean trace){
        tracedMethod= trace;
    }

    public void setCu(CompilationUnit cus) {
        cu = cus;
    }

    private ClassM getClass(String cla, String pack){
        ClassM newC = null; PackageM newP = null;
        for(PackageM p : this.packages){
            if(p.getName().equals(pack)){
                newP = p;
                for(ClassM c : newP.getClasses()){
                    if(c.getName().equals(cla)){
                        newC = c;
                    }
                }
                if(newC == null){
                    newC = new ClassM(cla);
                    newP.getClasses().add(newC);
                }
            }
        }
        if(newP == null){
            newP = new PackageM();
            newP.setName(pack);
            newC = new ClassM(cla);
            newP.getClasses().add(newC);
            packages.add(newP);
        }
        return newC;
    }


    /*
    public static MethodDeclaration getMethod( String methodName){
        List<TypeDeclaration> tp = cu.getTypes();
        for (TypeDeclaration type : tp) {
            //System.out.println("tipozinho: " + type); // isto imprime a classe toda.
            List<BodyDeclaration> members = type.getMembers();
            for (BodyDeclaration member : members) {
               // imprime vars instancia, comentarios, metodos com a respetiva anotacao(caso tenha)
                if (member instanceof MethodDeclaration && ((MethodDeclaration) member).getName().equals(methodName)) {
                    return (MethodDeclaration) member;
                }
            }
        }
        return null;
    }*/

    public static int countOperations(Node s ) {
        return  10;
    }
/*
        if (s instanceof ExpressionStmt) {
            Expression s1 = ((ExpressionStmt) s).getExpression();
            counter += countOperations(s1);

        } else if (s instanceof WhileStmt) {
            return counter + NUMBER_OF_OPERATIONS;
        } else if (s instanceof ForStmt) {
            return counter + NUMBER_OF_OPERATIONS;
        } else if (s instanceof ForStmt) {
            return counter + NUMBER_OF_OPERATIONS;
        } else if (s instanceof DoStmt) {
            return counter + NUMBER_OF_OPERATIONS;
        } else if (s instanceof ForeachStmt) {
            return counter + NUMBER_OF_OPERATIONS;
        } else if (s instanceof SynchronizedStmt) {
            return counter + NUMBER_OF_OPERATIONS;
        } else if (s instanceof SwitchStmt) {
            // if(((SwitchStmt)s).getEntries().size()>2)
            return counter + NUMBER_OF_OPERATIONS;

        } else if (s instanceof IfStmt) {
            counter += countOperations(((IfStmt) s).getCondition()) + countOperations(((IfStmt) s).getElseStmt()) + countOperations(((IfStmt) s).getThenStmt());
        } else if (s instanceof BlockStmt) {
            if (((BlockStmt) s).getStmts() != null) {
                for (Statement e : ((BlockStmt) s).getStmts()
                        ) {
                    counter +=  countOperations(e);
                }
            }

        } else if (s instanceof ReturnStmt) {
            counter += 1 + countOperations(((ReturnStmt) s).getExpr());
        } else if (s instanceof UnaryExpr) {
            counter = counter + countOperations(((UnaryExpr) s).getExpr());
        } else if (s instanceof BinaryExpr) {
            counter = counter + 1 + countOperations(((BinaryExpr) s).getLeft()) + countOperations(((BinaryExpr) s).getRight());

        } else if (s instanceof ConditionalExpr) {
            counter = counter + 1 + countOperations(((ConditionalExpr) s).getCondition()) + countOperations(((ConditionalExpr) s).getThenExpr()) + countOperations(((ConditionalExpr) s).getElseExpr());

        } else if (s instanceof CastExpr) {
            counter += 1 + countOperations(((CastExpr) s).getExpr());
        } else if (s instanceof ArrayAccessExpr) {
            counter += countOperations(((ArrayAccessExpr) s).getIndex());
        } else if (s instanceof AssignExpr) {
            counter += 1 + countOperations(((AssignExpr) s).getTarget()) + countOperations(((AssignExpr) s).getValue());
        } else if (s instanceof StringLiteralExpr) {
            return counter;
        } else if (s instanceof EnclosedExpr) {
            counter += countOperations(((EnclosedExpr) s).getInner());
        } else if (s instanceof MethodCallExpr) {
            Expression ss = ((MethodCallExpr) s).getScope();
            if (ss instanceof SuperExpr) {
                counter += NUMBER_OF_OPERATIONS;
                return counter;
            } else {
                if (((MethodCallExpr) s).getArgs() != null) {
                    for (Expression sss : ((MethodCallExpr) s).getArgs()) {
                        if (!notCount(sss))
                            counter += countOperations(sss);
                    }
                }

                counter++;
            }
            //counter += countOperations(getMethod(((MethodCallExpr) s).getName()));
            if(counter>NUMBER_OF_OPERATIONS)
                return counter;
        } else if (s instanceof VariableDeclarationExpr) {

            for (VariableDeclarator v : ((VariableDeclarationExpr) s).getVars())
                counter += countOperations(v);
        } else if (s instanceof VariableDeclarator) {
            counter += 1 + countOperations(((VariableDeclarator) s).getInit());
        } else if (s instanceof ObjectCreationExpr) {
            counter++;
            if (((ObjectCreationExpr) s).getArgs() != null) {
                for (Expression a : ((ObjectCreationExpr) s).getArgs()) {
                    counter += countOperations(a);
                }
            }

        } else {
            counter++;
        }


            return counter;
            }


    public  static boolean notCount(Expression s){

        if(s instanceof NameExpr || s instanceof NullLiteralExpr || s instanceof StringLiteralExpr || s instanceof ThisExpr)
            return true;
        else {
            if(s instanceof EnclosedExpr)
                return notCount(((EnclosedExpr)s).getInner());
        }
        return false;
    }

    public static int countOperations(MethodDeclaration n){
        int counter = 0;
        if(n==null) return 0;
        if(n.getBody().getStmts() != null) {
            List<Statement> x = n.getBody().getStmts();
            for (Node s :
                    x) {
                if (counter >= NUMBER_OF_OPERATIONS) return counter;
                if (s instanceof ExpressionStmt) {
                    Expression s1 = ((ExpressionStmt) s).getExpression();
                    counter += countOperations(s1);

                } else if (s instanceof WhileStmt) {
                    return counter + NUMBER_OF_OPERATIONS;
                } else if (s instanceof ForStmt) {
                    return counter + NUMBER_OF_OPERATIONS;
                } else if (s instanceof ForStmt) {
                    return counter + NUMBER_OF_OPERATIONS;
                } else if (s instanceof DoStmt) {
                    return counter + NUMBER_OF_OPERATIONS;
                } else if (s instanceof ForeachStmt) {
                    return counter + NUMBER_OF_OPERATIONS;
                } else if (s instanceof SynchronizedStmt) {
                    return counter + NUMBER_OF_OPERATIONS;
                } else if (s instanceof SwitchStmt) {
                    // if(((SwitchStmt)s).getEntries().size()>2)
                    return counter + NUMBER_OF_OPERATIONS;

                } else if (s instanceof IfStmt) {
                    counter += 1+ countOperations(((IfStmt) s).getCondition()) + countOperations(((IfStmt) s).getElseStmt()) + countOperations(((IfStmt) s).getThenStmt());
                } else if (s instanceof BlockStmt) {
                    if (((BlockStmt) s).getStmts() != null) {
                        for (Statement e : ((BlockStmt) s).getStmts()
                                ) {
                            counter += countOperations(e);
                        }
                    }

                } else if (s instanceof ReturnStmt) {
                    counter += 1 + countOperations(((ReturnStmt) s).getExpr());
                } else if (s instanceof UnaryExpr) {
                    counter = counter + countOperations(((UnaryExpr) s).getExpr());
                } else if (s instanceof BinaryExpr) {
                    counter = counter + 1 + countOperations(((BinaryExpr) s).getLeft()) + countOperations(((BinaryExpr) s).getRight());

                } else if (s instanceof ConditionalExpr) {
                    counter = counter + 1 + countOperations(((ConditionalExpr) s).getCondition()) + countOperations(((ConditionalExpr) s).getThenExpr()) + countOperations(((ConditionalExpr) s).getElseExpr());

                } else if (s instanceof CastExpr) {
                    counter += 1 + countOperations(((CastExpr) s).getExpr());
                } else if (s instanceof ArrayAccessExpr) {
                    counter += countOperations(((ArrayAccessExpr) s).getIndex());
                } else if (s instanceof AssignExpr) {
                    counter += 1 + countOperations(((AssignExpr) s).getTarget()) + countOperations(((AssignExpr) s).getValue());
                } else if (s instanceof StringLiteralExpr) {
                    continue;
                } else if (s instanceof EnclosedExpr) {
                    counter += countOperations(((EnclosedExpr) s).getInner());
                } else if (s instanceof MethodCallExpr) {
                    Expression ss = ((MethodCallExpr) s).getScope();
                    if (ss instanceof SuperExpr) {
                        counter += NUMBER_OF_OPERATIONS;
                        return counter;
                    } else {
                        if (((MethodCallExpr) s).getArgs() != null) {
                            for (Expression sss : ((MethodCallExpr) s).getArgs()) {
                                if (!notCount(sss))
                                    counter += countOperations(sss);
                            }
                        }

                        counter++;
                    }
                    counter += countOperations(getMethod(((MethodCallExpr) s).getName()));
                } else if (s instanceof VariableDeclarationExpr) {

                    for (VariableDeclarator v : ((VariableDeclarationExpr) s).getVars())
                        counter += countOperations(v);
                } else if (s instanceof VariableDeclarator) {
                    counter += 1 + countOperations(((VariableDeclarator) s).getInit());
                } else if (s instanceof ObjectCreationExpr) {
                    counter++;
                    if (((ObjectCreationExpr) s).getArgs() != null) {
                        for (Expression a : ((ObjectCreationExpr) s).getArgs()) {
                            counter += countOperations(a);
                        }
                    }

                } else {
                    counter++;
                }

            }
            return counter;
        }
        else return 0;

    }
*/


    public static void registMethod(String key, MethodDeclaration n){
        List<String> l ;
        if (n.getParameters()!=null){
            l= ProjectMethods.Companion.wrapJavaArgs(n.getParameters());
        }else{
            l = new ArrayList<>();
        }
        JSONObject jo = ProjectMethods.Companion.buildJSONObj(ProjectMethods.Companion.wrapMods(n.getModifiers()),l,key, "Java");
        InstrumentHelper.allMethods.addMethod( jo );
       // InstrumentHelper.allMethods.getAllmethods().add(method); TODO
    }

    public static void registMethod(String key, ConstructorDeclaration n){
        List<String> l ;
        if (n.getParameters()!=null){
           l= ProjectMethods.Companion.wrapJavaArgs(n.getParameters());
        }else{
            l = new ArrayList<>();
        }
        JSONObject jo = ProjectMethods.Companion.buildJSONObj(ProjectMethods.Companion.wrapMods(n.getModifiers()),l,key, "Java");
        InstrumentHelper.allMethods.addMethod( jo );
        // InstrumentHelper.allMethods.getAllmethods().add(method); TODO
    }


    @Override
    public void visit(MethodDeclaration n, Object arg) {
        ClassDefs cDef = (ClassDefs)arg;
        String retType = n.getType().getClass().getName();

        String metodo = InstrumentHelper.wrapMethod(n,cDef, ProjectMethods.Companion.hashArgs(ProjectMethods.Companion.wrapJavaArgs(n.getParameters())));

        registMethod(metodo,n);

        if(n.getBody() != null){
            if(n.getBody().getStmts() != null){
                List<Statement> x = n.getBody().getStmts();
                if (tracedMethod){
                    Instrumenter p =InstrumentHelper.getInstrumenter();
                    MethodCallExpr getContext = new MethodCallExpr();
                    getContext.setName(InstrumentHelper.getApplicationFullName() + ".getAppContext");
                    MethodCallExpr mcB = ((TestOrientedProfiler) p).markMethod(getContext,metodo);
                    int insertIn = 0;
                    x.add(insertIn, new ExpressionStmt(mcB));
                }
                else {
                    //Avoid monitoring getters and setters and simple methods
                    int operations = MethodChangerVisitor.countOperations(n);
                    if(operations>=NUMBER_OF_OPERATIONS){
                        MethodCallExpr getContext = new MethodCallExpr();
                        getContext.setName(InstrumentHelper.getApplicationFullName() + ".getAppContext");
                        MethodCallExpr mcB = ((MethodOrientedProfiler) InstrumentHelper.getInstrumenter()).markMethodStart(getContext,metodo);
                        int insertIn = 0;
                        x.add(insertIn, new ExpressionStmt(mcB));
                        MethodCallExpr mcE = ((MethodOrientedProfiler) InstrumentHelper.getInstrumenter()).marKMethodStop(getContext,metodo);
                        ReturnFlag hasRet = new ReturnFlag();
                        ReturnFlag unReachable = new ReturnFlag();
                        new ReturnVisitor().visit(n, hasRet);
                        new WhileChangerVisitor().visit(n, unReachable);
                        new ForChangerVisitor().visit(n, unReachable);
                        if(hasRet.hasRet()){
                            new GenericBlockVisitor().visit(n, new ExpressionStmt(mcE));
                        }
                        String stm = x.get(x.size()-1).getClass().getName();
                        if(stm.contains("ReturnStmt") || stm.contains("ThrowStmt")){
                            x.add((x.size()-1), new ExpressionStmt(mcE));
                        }else if(retType.contains("VoidType")){
                            if(!unReachable.hasRet()){
                                x.add(new ExpressionStmt(mcE));
                            }
                        }
                        ClassM cm = this.getClass(cDef.getName(), cDef.getPack());
                        cm.getMethods().add(n.getName());
                    }
                }
            }
        }
    }

    @Override
    public void visit(ConstructorDeclaration n, Object arg) {
        String retType = "";
        ClassDefs cDef = (ClassDefs)arg;
        String metodo = InstrumentHelper.wrapMethod(n,cDef, ProjectMethods.Companion.hashArgs(ProjectMethods.Companion.wrapJavaArgs(n.getParameters())));
        registMethod(metodo, n);

        if(n.getBlock() != null){
            if(n.getBlock().getStmts() != null){
                List<Statement> x = n.getBlock().getStmts();
                // then just trace the method
                if (tracedMethod){
                    Instrumenter p =InstrumentHelper.getInstrumenter();
                    MethodCallExpr getContext = new MethodCallExpr();
                    getContext.setName(InstrumentHelper.getApplicationFullName() + ".getAppContext");
                    MethodCallExpr mcB = ((TestOrientedProfiler) p).markMethod(getContext,metodo);
                    int insertinitcall=0;
                    if ( ! n.getBlock().getStmts().isEmpty() ) {
                        // if constructor has a super call, this call has to be the first stmt in the blocl
                        // otherwise java raises a compile time error
                        ReturnFlag rf = new ReturnFlag();

                        SuperFinderVisitor sfv = new SuperFinderVisitor();
                        // create a visitable block with first stmt only, so check if it is a super call
                        BlockStmt blockStmt = new BlockStmt();
                        List<Statement> l = new ArrayList<>();
                        l.add(n.getBlock().getStmts().get(0));
                        blockStmt.setStmts(l);
                        sfv.visit(blockStmt,  rf);
                        if (rf.hasRet()){
                            // if has a super call
                            insertinitcall=1;
                        }
                        x.add(insertinitcall, new ExpressionStmt(mcB));

                    }
                }
                else {
                    int operations = MethodChangerVisitor.countOperations(n);
                    if(operations>=NUMBER_OF_OPERATIONS){
                        MethodCallExpr getContext = new MethodCallExpr();
                        getContext.setName(InstrumentHelper.getApplicationFullName() + ".getAppContext");


                        MethodCallExpr mcB = ((MethodOrientedProfiler) InstrumentHelper.getInstrumenter()).markMethodStart(getContext,metodo);
                        int insertinitcall=0;
                        if ( ! n.getBlock().getStmts().isEmpty() ) {
                            // if constructor has a super call, this call has to be the first stmt in the blocl
                            // otherwise java raises a compile time error
                            ReturnFlag rf = new ReturnFlag();

                            SuperFinderVisitor sfv = new SuperFinderVisitor();
                            // create a visitable block with first stmt only, so check if it is a super call
                            BlockStmt blockStmt = new BlockStmt();
                            List<Statement> l = new ArrayList<>();
                            l.add(n.getBlock().getStmts().get(0));
                            blockStmt.setStmts(l);
                            sfv.visit(blockStmt,  rf);
                            if (rf.hasRet()){
                                // if has a super call
                                insertinitcall=1;
                            }
                            x.add(insertinitcall, new ExpressionStmt(mcB));

                        }
                        MethodCallExpr mcE = ((MethodOrientedProfiler) InstrumentHelper.getInstrumenter()).marKMethodStop(getContext,metodo);
                        ReturnFlag hasRet = new ReturnFlag();
                        ReturnFlag unReachable = new ReturnFlag();
                        new ReturnVisitor().visit(n, hasRet);
                        new WhileChangerVisitor().visit(n, unReachable);
                        new ForChangerVisitor().visit(n, unReachable);
                        if(hasRet.hasRet()){
                            new GenericBlockVisitor().visit(n, new ExpressionStmt(mcE));
                        }
                        String stm = x.get(x.size()-1).getClass().getName();
                        if(stm.contains("ReturnStmt") || stm.contains("ThrowStmt")){
                            x.add((x.size()-1), new ExpressionStmt(mcE));
                        }else if(stm.contains("VoidType")){
                            if(!unReachable.hasRet()){
                                x.add(new ExpressionStmt(mcE));
                            }
                        }
                        ClassM cm = this.getClass(cDef.getName(), cDef.getPack());
                        cm.getMethods().add(n.getName());
                    }
                }
            }
        }
    }






}
