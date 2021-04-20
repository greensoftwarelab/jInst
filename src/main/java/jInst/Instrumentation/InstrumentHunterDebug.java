//package jInst.Instrumentation;
//
//import com.github.javaparser.ast.body.MethodDeclaration;
//import com.github.javaparser.ast.expr.AnnotationExpr;
//import com.github.javaparser.ast.expr.MarkerAnnotationExpr;
//import com.github.javaparser.ast.expr.NameExpr;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class InstrumentHunterDebug {
//
//    public InstrumentHunterDebug() {
//
//    }
//
//    public void insertMarkerAnnotation(MethodDeclaration methodDeclaration, String name){
//        List<AnnotationExpr> oldAnnotations = methodDeclaration.getAnnotations();
//        AnnotationExpr annotationExpr = new MarkerAnnotationExpr();
//        NameExpr nameExpr = new NameExpr();
//        nameExpr.setName(name);
//        annotationExpr.setName(nameExpr);
//
//        List<AnnotationExpr> newAnnotations = new ArrayList<AnnotationExpr>();
//        newAnnotations.add(annotationExpr);
//        newAnnotations.addAll(oldAnnotations);
//        methodDeclaration.setAnnotations(newAnnotations);
//    }
//
//
//
//
//}
