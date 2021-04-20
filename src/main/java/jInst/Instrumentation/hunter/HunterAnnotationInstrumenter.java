package jInst.Instrumentation.hunter;

import com.github.javaparser.ast.ImportDeclaration;

import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MarkerAnnotationExpr;
import com.github.javaparser.ast.expr.NameExpr;
import java.util.Arrays;
import java.util.List;

public class HunterAnnotationInstrumenter implements AnnotationInstrumenter {


    private static String hunterAnnotationName = "HunterDebug";
    private static String hunterLibrary = "com.hunter.library.debug" + "." + hunterAnnotationName;

    @Override
    public List<AnnotationExpr> getAnnotations() {
        return Arrays.asList(new MarkerAnnotationExpr(new NameExpr(hunterAnnotationName)));
    }

    @Override
    public List<ImportDeclaration> getImports() {
        return Arrays.asList( new ImportDeclaration(new NameExpr(hunterLibrary),false,false));
    }
}
