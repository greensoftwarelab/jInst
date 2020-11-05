package jInst.Instrumentation;

import com.github.javaparser.ast.ImportDeclaration;

import java.util.List;

public interface Instrumenter {

    List<ImportDeclaration> getImports();

}
