package jInst.amp;

import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import jInst.Instrumentation.Instrumenter;


import java.util.List;

public interface ActivityInstrumenter extends Instrumenter {

    List<Expression> onActivityCreate(Object arg);

    List<Expression> onActivityDestroy(Object arg);


    List<String> getManifestEntries(); // TODO


}
