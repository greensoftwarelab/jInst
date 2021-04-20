package jInst.Instrumentation.amp;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.NameExpr;

import java.util.ArrayList;
import java.util.List;

public class AMPInstrumenter implements ActivityInstrumenter {

    private String beginOnCreateCall = "Debug.startMethodTracing(new File(\"/sdcard\", \"FileReplace.trace\").getPath());";
  // private String auxbeginOnCreateCall = "File methodTracingFile = new File(\"/sdcard/\", \"FileReplace.trace\");";

    private String exitOnDestroyCall = "Debug.stopMethodTracing();";


    private ArrayList<ImportDeclaration> necessaryImports;
    private String library1= "android.os.Debug";
    private String library2= "java.io.File";


    private void initImports(){
        this.necessaryImports.add( new ImportDeclaration(new NameExpr(library1),false,false));
        this.necessaryImports.add( new ImportDeclaration(new NameExpr(library2),false,false));
    }

    public AMPInstrumenter(){
        necessaryImports = new ArrayList<>();
        initImports();
    }

    @Override
    public List<Expression> onActivityCreate(Object arg) {
        if (arg==null){
            arg = new String("anadroidDebugTrace.trace");
        }
        List<Expression> list = new ArrayList<>();
        try {
            String stArg = ((String) arg);
            Expression s1 =  JavaParser.parseExpression(beginOnCreateCall.replace("FileReplace.trace", stArg));
            list.add(s1);
           // System.out.println( s1.toStringWithoutComments());

        } catch (ParseException | ClassCastException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public List<Expression> onActivityDestroy(Object arg) {
        List<Expression> list = new ArrayList<>();
        try {
             Expression s1 =  JavaParser.parseExpression(exitOnDestroyCall);
            list.add(s1);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public List<ImportDeclaration> getImports() {
        return necessaryImports;
    }

    @Override
    public List<String> getManifestEntries() {
       return new ArrayList<>(); // TODO
    }
}
