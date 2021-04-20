package jInst.Instrumentation.hunter;

import com.github.javaparser.ast.expr.AnnotationExpr;
import jInst.Instrumentation.Instrumenter;

import java.util.List;

public interface AnnotationInstrumenter extends Instrumenter {

    List<AnnotationExpr> getAnnotations();

}