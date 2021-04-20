package jInst.Instrumentation.amp;

import com.github.javaparser.ast.expr.Expression;
import jInst.Instrumentation.Instrumenter;


import java.util.List;

public interface ActivityInstrumenter extends Instrumenter {

    List<Expression> onActivityCreate(Object arg);

    List<Expression> onActivityDestroy(Object arg);


    List<String> getManifestEntries(); // TODO


}
