/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jInst.Instrumentation;

//import greendroid.tools.Util;

import AndroidProjectRepresentation.APICallUtil;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.*;
import com.github.javaparser.ASTHelper;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.ReferenceType;
import com.github.javaparser.ast.type.VoidType;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import jInst.Instrumentation.Utils.KtlTestsInstrumenterUtil;
import jInst.Instrumentation.Utils.ProjectMethods;
import jInst.Instrumentation.hunter.HunterAnnotationInstrumenter;
import jInst.JInst;
import jInst.ResourceLoader;
import jInst.Instrumentation.amp.AMPInstrumenter;
import jInst.Instrumentation.profiler.Profiler;
import jInst.Instrumentation.profiler.ProfilerAbstractFactory;
import jInst.Instrumentation.profiler.trepn.TrepnLibrary;
import jInst.Instrumentation.profiler.trepn.TrepnProfilerFactory;
import jInst.util.*;
import jInst.visitors.*;
import jInst.visitors.utils.ClassDefs;
import kastree.ast.Node;

/**
 *
 * @author User
 */
public class InstrumentHelper {

//    public static String runnerClass = "com.zutubi.android.junitreport.JUnitReportTestRunner";
    protected static ArrayList<String> instrumented = new ArrayList<String>();
    protected static ArrayList<String> testCase = new ArrayList<String>();
    protected String launchActivity = "" ;
    public static final String testClasses = "testClasses.txt";
    protected static String notTestable = "TestCase";
    public static Integer compiledSdkVersion=0;
    protected String tName;
    protected String workspace;
    public String project;
    protected String tests;
    public static String projectID = "";
    protected String transFolder;
    protected String transTests;
    protected String aux;
    protected String manifest;
    protected String manifestTest;
    protected String projectDesc;
    protected String devPackage;
    protected ArrayList<PackageM> packages;
    protected String originalManifest;
    protected JInst.InstrumentationType type;
    private TestingApproach  approach = TestingApproach.WhiteBox;

    public static JInst.TestingFramework testingFramework;
    public static ProjectMethods allMethods = new ProjectMethods();

    //protected int JUnitVersion;
    protected Map<String,String> testType = new HashMap<>();
    private static ClassDefs appPackage = new ClassDefs();
    protected static Instrumenter instrumenter;
    private APICallUtil acu = new APICallUtil();

    enum TestingApproach {
        Blackbox,
        WhiteBox
    }




    public List<String> findAllManifests(){
        try {
            return Files.find(Paths.get(this.transFolder), 6, (p, bfa) -> bfa.isRegularFile() && p.getFileName().toString().equals("AndroidManifest.xml") ).map(x-> x.toAbsolutePath().toString()).collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public InstrumentHelper(APICallUtil apu , String tName, String work, String proj, String tests, JInst.InstrumentationType type, String approach) {
        this.acu =apu;
        //acu.app = new AppInfo(projectID, proj, "", "Java", "Gradle", 0.0, "unknown", "unknown");
        this.tName = tName;
        this.workspace = work;
        this.project = proj;
        this.tests = tests;
        this.transFolder = project+"/"+tName+"/";
        this.transTests = transFolder+"tests"+"/";
        this.manifest = transFolder+"AndroidManifest.xml";
        this.manifestTest = transTests+"AndroidManifest.xml";
        this.projectDesc = project+".project";
        this.aux = transFolder+"_aux_/";
        this.devPackage = "";
        this.packages = new ArrayList<>();
        this.type = type;


        instrumented.add("ActivityUnitTestCase");
        instrumented.add("ActivityIntrumentationTestCase2");
        instrumented.add("ActivityTestCase");
        instrumented.add("ProviderTestCase");
        instrumented.add("SingleLaunchActivityTestCase");
        instrumented.add("SyncBaseInstrumentation");
        instrumented.add("ActivityInstrumentationTestCase");
        instrumented.add("ActivityInstrumentationTestCase2");
        //instrumented.add("WizardPageActivityTestBase");

        testCase.add("AndroidTestCase");
        testCase.add("ApplicationTestCase");
        testCase.add("LoaderTestCase");
        testCase.add("ProviderTestCase2");
        testCase.add("ServiceTestCase");
        //testCase.add("TestCase");
        //testCase.add()
      //  setInstrumentationType(type);
       // setApproach(approach);


    }

    public String getTransFolder() {
        return transFolder;
    }

    public void setTransFolder(String transFolder) {
        this.transFolder = transFolder;
    }




    public static boolean isApplicationClass(String s){
        return appPackage.getName()!=null;
    }

    public static String getApplicationClass(){
        return appPackage.getName();
    }
    public static String getApplicationFullName(){
        return appPackage.getPack()  +"." +appPackage.getName();
    }



    public InstrumentHelper() {
    }


    public void setInstrumentationType(JInst.InstrumentationType type){

        ProfilerAbstractFactory pfact = new TrepnProfilerFactory();
        if (type== JInst.InstrumentationType.TEST){
            this.instrumenter = pfact.createTestOrientedProfiler();
        }
        else if(type== JInst.InstrumentationType.METHOD) {
            //System.out.println("Criei method oriented profiler");
            this.instrumenter = pfact.createMethodOrientedProfiler();
        }
        else if(type== JInst.InstrumentationType.ACTIVITY){
            instrumenter = new  AMPInstrumenter();
            this.findLauncher();
            this.launchActivity  = XMLParser.getLauncher();
        }
        else if(type== JInst.InstrumentationType.ANNOTATION){
            instrumenter = new HunterAnnotationInstrumenter();
        }
    }


    public String getManifest() {
        return manifest;
    }

    public String getOriginalManifest() {
        return originalManifest;
    }



    //public int getJUnitVersion(){return this.JUnitVersion;}
    //public void setJUnitVersion(int JUnitVersion){this.JUnitVersion = JUnitVersion;}

    public void setCompiledSdkVersion(String filename){
        compiledSdkVersion = getSdkVersion(filename);
    }

    public void addTestType(String path, String testType){
        this.testType.put(path,testType);
    }

    public String getTestType(String filepath){
        return this.testType.get(filepath);
    }


    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
        this.transFolder = project+tName+"/";
        this.transTests = transFolder+"tests"+"/";
        this.manifest = transFolder+"AndroidManifest.xml";
        this.manifestTest = transTests+"AndroidManifest.xml";
        this.devPackage = "";
    }

    public String getTests() {
        return tests;
    }

    public void setTests(String tests) {
        this.tests = tests;
    }

    public APICallUtil getAcu() {
        return acu;
    }

    public void setAcu(APICallUtil acu) {
        this.acu = acu;
    }

    public static int getSdkVersion(String filename){

        if(!filename.endsWith("build.gradle")){
           return 0;
        }
        int version =0;
        BufferedReader b = null;
        try {
            String s = "";
            b = new BufferedReader(new FileReader(filename));
            while ((s = b.readLine()) != null) {
                String s1 = new String(s);
                if(s.matches(".*compileSdkVersion.*")){
                    String [] tokens = s.trim().split("(\\s+)");
                    for (String tok:
                            tokens) {
                       //System.out.println("tok -> " + tok + " " + (tok.chars().allMatch( Character::isDigit)  ));
                        if (tok.chars().allMatch( Character::isDigit))
                            return Integer.parseInt(tok.trim());
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return version;

    }

    public void generateTransformedTests() throws Exception{
        File fProject = new File(tests);
        File fTransf = new File(transTests);
        fTransf.mkdir();
        File[] listOfFiles = fProject.listFiles();

        //Copy all the files to the new project folder
        for(File f : listOfFiles){
            if(f.isDirectory()){
                if(!f.getName().equals("src") && !f.getName().equals(tName)){
                    FileUtils.copyFolder(f, new File(transTests+f.getName()));
                }else if(f.getName().equals("src")){
                    //PASS THE FILES THROUGH THE PARSING AND INSTRUMENTATION TOOL
                    this.instrumentSource(f, new File(transTests+"src"));
                }
            }else if(f.isFile()){
                File aux = new File(transTests+f.getName());
                aux.createNewFile();
                FileUtils.copyFile(f, aux);
            }
        }
        //this.editProjectDesc();

        File libsT = new File(transTests+"libs");
        if(!libsT.exists()){
            libsT.mkdir();
        }
        /*
        //File runner = new File(libsT.getAbsolutePath()+"/polidea_test_runner_1.1.jar");
        File runner = new File(libsT.getAbsolutePath()+"/android-junit-report-1.5.8.jar");
        
        runner.createNewFile();
        //FileUtils.copyFile(new File("libsAdded/polidea_test_runner_1.1.jar"), runner);
        FileUtils.copyFile(new File("libsAdded/android-junit-report-1.5.8.jar"), runner);
        */
        this.changeRunner();
    }

    public void generateTransformedProject() throws Exception{
        File fProject = new File(project);
        File fTransf = new File(transFolder);
        fTransf.mkdir();
        File[] listOfFiles = fProject.listFiles();
         //Copy all the files to the new project folder
        for(File f : listOfFiles){
            if(f.isDirectory()){
                if(!f.getName().equals("src") && !f.getName().equals(tName) && !tests.contains(f.getAbsolutePath())){
                    FileUtils.copyFolder(f, new File(transFolder+f.getName()));
                }else if(f.getName().equals("src")){
                    //PASS THE FILES THROUGH THE PARSING AND INSTRUMENTATION TOOL
                    this.instrumentSource(f, new File(transFolder+"src"));
                }
            }else if(f.isFile()){
                File aux = new File(transFolder+f.getName());
                //aux.createNewFile();
                FileUtils.copyFile(f, aux);
            }
        }
        //Save all methods definition in file
        String allMethods = "";
        FileUtils.copyAll(MethodChangerVisitor.getPackages(), packages);
        for(PackageM p : packages){
            allMethods += p.toString();
        }
        File auxF = new File(aux); auxF.mkdir();
        FileUtils.writeFile(new File(aux+"AllMethods"), allMethods);
        packages.clear();
        MethodChangerVisitor.restartPackages();

        this.addPermission();
        /*
        File libs = new File(transFolder+"libs");
        if (!libs.exists()) {
            libs.mkdir();
        }
        File greenDroid = new File(libs.getAbsolutePath()+"/greendroid.jar");
        greenDroid.createNewFile();
        FileUtils.copyFile(new File("libsAdded/greendroid.jar"), greenDroid);
        */
        XMLParser.addLibOverrideToManifests(this.findAllManifests());
    }

    protected void findLauncher(){
        XMLParser.parseManifest(originalManifest);
        //this.devPackage = XMLParser.getDevPackage();
        this.devPackage = XMLParser.getBuildPackage();
    }

    protected void changeRunner(){
        File ax = new File(manifestTest);
        if(ax.exists()){
            XMLParser.editRunner(manifestTest);
        }
      //  String proj = XMLParser.getTestProjName()+".launch";
        //This line was only necessary if we were to run the tests via IDE
        //XMLParser.editRunConfiguration(workspace+".metadata/.plugins/org.eclipse.debug.core/.launches/"+proj, runnerClass);
    }

    public void addPermission(){
        XMLParser.editManifest(manifest);
        //insertReadWriteExternalPermissions(manifest);
    }

    protected void editProjectDesc(){
        XMLParser.editProjectDesc(this.transFolder+".project");
        XMLParser.editProjectDesc(this.transTests+".project");
        XMLParser.editClasspath(this.transTests+".classpath");
    }



    protected void instrumentSource(File src, File dest) throws Exception{
        if(src.isDirectory()){
            //if directory not exists, create it
            if(!dest.exists()){
                dest.mkdir();
            }
            //list all the directory contents
            String files[] = src.list();
            for (String file : files) {
                //construct the src and dest file structure
                File srcFile = new File(src, file);
                File destFile = new File(dest, file);
                //recursive Instrumentation
                instrumentSource(srcFile,destFile);
            }
        }
        else{
            //if file, then Instrumentation it
            if(src.getAbsolutePath().endsWith(".java")){
                String res = "";
                if(this.isTestCase(src)){
                    if (hasToInstrumentProjectTests()){
                        res = transformTest(src.getAbsolutePath());
                    }
                    else {
                        FileUtils.copyFile(src, dest);
                    }
                }else{ // is normal java file (!test file)
                    //acu.processJavaFile(src.getAbsolutePath());
                     res = transform(src.getAbsolutePath());
                }
                if(!res.equals("")){
                    dest.createNewFile();
                    FileUtils.writeFile(dest, res);
                }else{
                    dest.delete();
                }
            }else if(src.getAbsolutePath().endsWith(".kt")){
                String res = "";
                res=transformKotlin(src.getAbsolutePath());
                if(!res.equals("")){
                    dest.createNewFile();
                    FileUtils.writeFile(dest, res);
                }else{
                    dest.delete();
                }
                if(!res.equals("")){
                    dest.createNewFile();
                    FileUtils.writeFile(dest, res);
                }else{
                    dest.delete();
                }
            } else{
                setCompiledSdkVersion(src.getAbsolutePath());
                FileUtils.copyFile(src, dest);
            }
        }
    }

    public static boolean hasToInstrumentProjectTests() {
        return testingFramework!= JInst.TestingFramework.MONKEY &&  testingFramework!= JInst.TestingFramework.MONKEYRUNNER;
    }


    /*This will be problematic in the future...*/
    protected boolean isInstrumentedTestCase(String filename){
        //System.out.println("file " +filename);
       try {
           return (filename.contains("androidTest")); //|| isTestCaseInstrumented(new File(filename));
       }catch (Exception e){
           System.out.println("[jInst] error in isInstrumentedtestCase");
       }
       return false;
    }


    static boolean stateSetup = false;


    protected void setupAppClass(String pack,String file){

        if (!stateSetup) {
            String appclass = XMLParser.getApplicationClass(originalManifest);
            if (appclass == null) {
                CreateAppClass(pack,file);
                this.appPackage = new ClassDefs(pack, "TrepApp");
            }
            else { // already exists a appclass
                String[] sp = appclass.split("\\.");
                this.appPackage = new ClassDefs(pack, sp[sp.length - 1]);
                // TODO check this
            }
            stateSetup = true;
        }
    }



    protected String transform(String file) throws Exception {
        // creates an input stream for the file to be parsed
        FileInputStream in = new FileInputStream(file);
        CompilationUnit cu;
        try {
            // parse the file
            cu = JavaParser.parse(in,"utf-8", false);
        } finally {
            in.close();
        }
        if (cu.getPackage()==null){
            return "";
        }
        String pack = cu.getPackage().getName().toString();
        if(cu.getTypes()==null || cu.getTypes().isEmpty()){
            return cu.toString();
        }

        String cl = cu.getTypes().get(0).getName();
        ClassDefs cDef = new ClassDefs();
        cDef.setName(cl); cDef.setPack(pack);

        String classDefinition = cDef.getClassDefinition();

        if (type == JInst.InstrumentationType.ACTIVITY) {
            System.out.println("ACTIVITY");
            if (classDefinition.equals(launchActivity) ){
                cDef.setLauncher(true);
                cDef.setActivity(true);
                List<ImportDeclaration> imp2 = instrumenter.getImports();
                if(cu.getImports() != null){
                    cu.getImports().addAll(imp2);
                }else{
                    cu.setImports(new LinkedList<ImportDeclaration>());
                    cu.getImports().addAll(imp2);
                }

                LauncherActivityVisitor v = new LauncherActivityVisitor();
                v.visit(cu, new AMPInstrumenter());
                //System.out.println(cu.toString());
            }
        }
        else{
            List<ImportDeclaration> imp2 = instrumenter.getImports();
            if(cu.getImports() != null){
                cu.getImports().addAll(imp2);
            }else{
                cu.setImports(new LinkedList<ImportDeclaration>());
                cu.getImports().addAll(imp2);
            }

            if(isApplicationClass(pack+"."+cl)){
                ImportDeclaration im = new ImportDeclaration(ASTHelper.createNameExpr("android.content.Context"), false, false);
                if(cu.getImports() != null){
                    cu.getImports().add(im);
                }else{
                    cu.setImports(new LinkedList<ImportDeclaration>());
                    cu.getImports().add(im);
                }
            }
            // visit and change the methods names and parameters
            String classDec = cu.getTypes().get(0).getClass().getName();
            if(!classDec.contains("ClassOrInterfaceDeclaration")){
                return cu.toString();
            }
            ClassOrInterfaceDeclaration x = (ClassOrInterfaceDeclaration)cu.getTypes().get(0);
            String l = pack+"."+cl;
            if(this.devPackage.equals(l)){
                cDef.setLauncher(true);
            }
            if(!x.isInterface()){
                if(x.getExtends() != null){
                    for(ClassOrInterfaceType ci: x.getExtends()){
                        String name = ci.getName();
                        if(name.contains("Activity")){
                             cDef.setActivity(true);
                        }
                    }
                }
                if(type == JInst.InstrumentationType.TEST) {
                    System.out.println("NOT ACTIVITY - TEST");
                    ClassOrInterfaceVisitor v = new ClassOrInterfaceVisitor();
                       v.setCu(cu);
                       v.setTracedMethod(true);
                       v.visit(cu, cDef);
                }

                MethodChangerVisitor v1= new MethodChangerVisitor();
                   v1.setCu(cu);
                   v1.setTracedMethod(type != JInst.InstrumentationType.METHOD );
                   v1.visit(cu, cDef);

            }
            //this.getAcu().processJavaFile(file);
            //System.out.println(this.getAcu().proj);
        }
        return cu.toString();
    }


    protected String transformTest(String file) throws Exception {
        // append to file containing all classeswith tests
        // creates an input stream for the file to be parsed
        FileInputStream in = new FileInputStream(file);
        ClassDefs cDef = new ClassDefs();
        //System.out.println("Parsing & Instrumenting "+file);
        CompilationUnit cu;
        boolean isJunitTest = false;
        boolean isTestable = false;
        boolean isJunitTest4 = false;
        try {
            // parse the file
            cu = JavaParser.parse(in,"utf-8", false);
        } finally {
            in.close();
        }
        //CHECK THE IMPORTS

        String pack = cu.getPackage().getName().toString();
        String cl = cu.getTypes().get(0).getName();
        cDef.setName(cl);
        cDef.setPack(pack);
        cDef.setAppName(this.devPackage);
        ImportDeclaration imp2=null, imp4 = null;

        //CHECK IF THE TEST CAN BE CONSIDERED FOR ENERGY MONITORING
        ClassOrInterfaceDeclaration x = (ClassOrInterfaceDeclaration)cu.getTypes().get(0);
        if(x.getExtends() != null){
            for(ClassOrInterfaceType ci: x.getExtends()){
                String name = ci.getName();
                String name2 = ci.getName().replaceAll("<.*?>", "");
                if(testCase.contains(name)){
                    cDef.setInstrumented(false);
                    isTestable = true;
                }else if(instrumented.contains(name)){
                    cDef.setInstrumented(true);
                    isTestable = true;
                }else if(notTestable.equals(name)){
                    isJunitTest = true;
                }


                /*for(String ext : androidTests){
                    if(name.contains(ext)){
                        isTestable = true;
                    }
                }*/
            }
        }
        //added- check if is ijunit 4


            if(this.testType.get(file).equals("Junit4")){

                isTestable = true;
                cDef.setInstrumented(true);
                cDef.setJunit4(true);

            }

            if (this.testType.get(file).equals("Other")){

                cDef.setOther(true);
                isTestable=true;
            }


            else if(this.testType.get(file).equals("SuiteJunit4")) {

                isTestable = true;
                cDef.setInstrumented(true);
                cDef.setJunit4suite(true);
            }


//        if(isInstrumentedTestCase(file))
//            cDef.setInstrumented(true);
        if(isTestable){

            TestChangerVisitor t = new TestChangerVisitor();
            t.traceMethod = this.type== JInst.InstrumentationType.TEST;
            t.visit(cu, ((Object)cDef) );

            if( (instrumenter instanceof HunterAnnotationInstrumenter ) ){
                return cu.toString();
            }

            if(cDef.isJunit4suite()){
                if (!cDef.isBeforeClass()) {
                    //create the before method
                    LinkedList<ReferenceType> _throws = new LinkedList<>();

                    //_throws.add(new ReferenceType( new NameExpr("Exception")));
                    MethodDeclaration newSetUp = new MethodDeclaration();
                    newSetUp.setName("before");
                    newSetUp.setModifiers(ModifierSet.PUBLIC);
                    newSetUp.setThrows(_throws);
                    newSetUp.setType(new VoidType());
                    LinkedList<AnnotationExpr> anot = new LinkedList<AnnotationExpr>();
                    anot.add(new MarkerAnnotationExpr(new NameExpr("BeforeClass")));
                    newSetUp.setAnnotations(anot);
                    newSetUp.setBody(new BlockStmt());
//                    MethodCallExpr mcS = new MethodCallExpr();

//                    if(testOriented){
////                        mcS.setName("TrepnLib.startProfilingTest");
//
//                    }
//                    else {
//                        mcS.setName("TrepnLib.startProfiling");
//                    }

                    MethodCallExpr getContext = new MethodCallExpr();
                    if (!cDef.isInstrumented()) {
                        getContext.setName("this.getContext");
                    } else {
                        if (cDef.isOther()){
                            getContext.setName("this.getInstrumentation().getContext");
                        }
                        else{
                            getContext.setName("InstrumentationRegistry.getTargetContext");
                        }
                    }

                    MethodCallExpr mcS = ((Profiler) instrumenter).startProfiler(getContext);
//                    ASTHelper.addArgument(mcS, getContext);

                    ArrayList<Statement> body = new ArrayList<Statement>();
                    body.add(new ExpressionStmt(mcS));
                    newSetUp.getBody().setStmts(body);



                    x.getMembers().add(0, newSetUp);
                    imp2 = new ImportDeclaration(ASTHelper.createNameExpr("org.junit.BeforeClass"), false, false);


                }
                if (!cDef.isAfterClass()) {
                    //create the tearDown method
                    LinkedList<ReferenceType> _throws = new LinkedList<>();
                   // _throws.add(new NameExpr("Exception"));
                    MethodDeclaration newTearDown = new MethodDeclaration();
                    newTearDown.setName("after");
                    newTearDown.setThrows(_throws);
                    newTearDown.setModifiers(ModifierSet.PUBLIC);
                    newTearDown.setType(new VoidType());
                    LinkedList<AnnotationExpr> anot = new LinkedList<AnnotationExpr>();
                    anot.add(new MarkerAnnotationExpr(new NameExpr("AfterClass")));
                    newTearDown.setAnnotations(anot);
                    newTearDown.setBody(new BlockStmt());
//                    MethodCallExpr mcT = new MethodCallExpr();
//                    if(testOriented){
//                        mcT.setName("TrepnLib.stopProfilingTest");
//                    }
//                    else {
//                        mcT.setName("TrepnLib.stopProfiling");
//                    }
                    MethodCallExpr getContext = new MethodCallExpr();
                    if (!cDef.isInstrumented()) {
                        getContext.setName("this.getContext");
                    } else {
                        if (cDef.isOther()){
                            getContext.setName("this.getInstrumentation().getContext");
                        }
                        else{
                            getContext.setName("InstrumentationRegistry.getTargetContext");
                        }
                    }
                    MethodCallExpr mcT = ((Profiler) instrumenter).stopProfiler(getContext);
//                    ASTHelper.addArgument(mcT, getContext);
                    ArrayList<Statement> body = new ArrayList<Statement>();
                    body.add(new ExpressionStmt(mcT));
                    newTearDown.getBody().setStmts(body);

                    x.getMembers().add(Math.max(x.getMembers().size() - 1, 0), newTearDown);
                    imp4 = new ImportDeclaration(ASTHelper.createNameExpr("org.junit.AfterClass"), false, false);
                }

            }
            else if(!cDef.isSuite()){

                if(cDef.isJunit4()){

                    if (!cDef.hasBefore()) {
                        //create the before method
                        LinkedList<ReferenceType> _throws = new LinkedList<>();
                        //_throws.add(new NameExpr("Exception"));
                        MethodDeclaration newSetUp = new MethodDeclaration();
                        newSetUp.setName("before");
                        newSetUp.setModifiers(ModifierSet.PUBLIC);
                        newSetUp.setThrows(_throws);
                        newSetUp.setType(new VoidType());
                        LinkedList<AnnotationExpr> anot = new LinkedList<AnnotationExpr>();
                        anot.add(new MarkerAnnotationExpr(new NameExpr("Before")));
                        newSetUp.setAnnotations(anot);
                        newSetUp.setBody(new BlockStmt());

                        MethodCallExpr getContext = new MethodCallExpr();
                        if (!cDef.isInstrumented()) {
                            getContext.setName("this.getContext");
                        } else {
                            if (cDef.isOther()){
                                getContext.setName("this.getInstrumentation().getContext");
                            }
                            else{
                                getContext.setName("InstrumentationRegistry.getTargetContext");
                            }
                        }

//                        ASTHelper.addArgument(mcS, getContext);

                        MethodCallExpr mcS = ((Profiler) instrumenter).startProfiler(getContext);
                        ArrayList<Statement> body = new ArrayList<Statement>();
                        body.add(new ExpressionStmt(mcS));
                        if(InstrumentHelper.compiledSdkVersion>22) {
                            ExpressionStmt exp = new ExpressionStmt( TrepnLibrary.getReadPermissions());
                            ExpressionStmt exp1 = new ExpressionStmt(TrepnLibrary.getWritePermissions());
                            ExpressionStmt ex2 = new ExpressionStmt(TrepnLibrary.getTrepnlibReadPermissions());
                            ExpressionStmt exp3 = new ExpressionStmt(TrepnLibrary.getTrepnlibWritePermissions());
                            body.add(0,exp);
                            body.add(0,exp1);
                            body.add(0,ex2);
                            body.add(0,exp3);

                        }
                        newSetUp.getBody().setStmts(body);
                        x.getMembers().add(0, newSetUp);
                        imp2 = new ImportDeclaration(ASTHelper.createNameExpr("org.junit.Before"), false, false);



                    }
                    if (!cDef.hasAfter()) {
                        //create the tearDown method
                        LinkedList<ReferenceType> _throws = new LinkedList<>();
                       // _throws.add(new NameExpr("Exception"));
                        MethodDeclaration newTearDown = new MethodDeclaration();
                        newTearDown.setName("after");
                        //newTearDown.setThrows(_throws);
                        newTearDown.setModifiers(ModifierSet.PUBLIC);
                        newTearDown.setType(new VoidType());
                        LinkedList<AnnotationExpr> anot = new LinkedList<AnnotationExpr>();
                        anot.add(new MarkerAnnotationExpr(new NameExpr("After")));
                        newTearDown.setAnnotations(anot);
                        newTearDown.setBody(new BlockStmt());
//                        MethodCallExpr mcT = new MethodCallExpr();
//                        if(testOriented){
//                            mcT.setName("TrepnLib.stopProfilingTest");
//                        }
//                        else {
//                            mcT.setName("TrepnLib.stopProfiling");
//                        }
                        MethodCallExpr getContext = new MethodCallExpr();
                        if (!cDef.isInstrumented()) {
                            getContext.setName("this.getContext");
                        } else {
                            if (cDef.isOther()){
                                getContext.setName("this.getInstrumentation().getContext");
                            }
                            else{
                                getContext.setName("InstrumentationRegistry.getTargetContext");
                            }
                        }
//                        ASTHelper.addArgument(mcT, getContext);
                        MethodCallExpr mcT =  ((Profiler) instrumenter).stopProfiler(getContext);
                        ArrayList<Statement> body = new ArrayList<Statement>();
                        body.add(new ExpressionStmt(mcT));
                        newTearDown.getBody().setStmts(body);
                        x.getMembers().add(x.getMembers().size()-1 >0 ? x.getMembers().size()-1 :0, newTearDown);
                        imp4 = new ImportDeclaration(ASTHelper.createNameExpr("org.junit.After"), false, false);
                    }
                }
                else {

                    if (!cDef.hasSetUp()) {
                        //create the setUp method
                        LinkedList<ReferenceType> _throws = new LinkedList<>();
                       // _throws.add(new NameExpr("Exception"));
                        MethodDeclaration newSetUp = new MethodDeclaration();
                        newSetUp.setName("setUp");
                        newSetUp.setModifiers(ModifierSet.PUBLIC);
                       // newSetUp.setThrows(_throws);
                        newSetUp.setType(new VoidType());
                        LinkedList<AnnotationExpr> anot = new LinkedList<AnnotationExpr>();
                        anot.add(new MarkerAnnotationExpr(new NameExpr("Override")));
                        newSetUp.setAnnotations(anot);
                        newSetUp.setBody(new BlockStmt());

                        //add the setUp method
                        MethodCallExpr superSetUp = new MethodCallExpr();
                        superSetUp.setName("super.setUp");
                        MethodCallExpr getContext = new MethodCallExpr();
                        if (!cDef.isInstrumented()) {
                            getContext.setName("this.getContext");
                        } else {
                            if (cDef.isOther()){
                                getContext.setName("this.getInstrumentation().getContext");
                            }
                            else{
                                getContext.setName("InstrumentationRegistry.getTargetContext");
                            }
                        }
//                        ASTHelper.addArgument(mcS, getContext);
                        MethodCallExpr mcS =  ((Profiler) instrumenter).startProfiler(getContext);
                        ArrayList<Statement> body = new ArrayList<Statement>();
                        body.add(new ExpressionStmt(mcS));
                        body.add(new ExpressionStmt(superSetUp));
                        // -> invocacao das permissoes pode ser aqui???
                        if(InstrumentHelper.compiledSdkVersion>22) {
                            ExpressionStmt exp = new ExpressionStmt(TrepnLibrary.getReadPermissions());
                            ExpressionStmt exp1 = new ExpressionStmt(TrepnLibrary.getWritePermissions());
                            ExpressionStmt exp2 = new ExpressionStmt(TrepnLibrary.getTrepnlibReadPermissions());
                            ExpressionStmt exp3 = new ExpressionStmt(TrepnLibrary.getTrepnlibWritePermissions());
                            body.add(0,exp);
                            body.add(0,exp1);
                            body.add(0,exp2);
                            body.add(0,exp3);
                        }
                        newSetUp.getBody().setStmts(body);
                        x.getMembers().add(0, newSetUp);

                    }
                    if (!cDef.hasTearDown()) {
                        //create the tearDown method
                        LinkedList<ReferenceType> _throws = new LinkedList<>();
                        //_throws.add(new NameExpr("Exception"));
                        MethodDeclaration newTearDown = new MethodDeclaration();
                        newTearDown.setName("tearDown");
                        newTearDown.setThrows(_throws);
                        newTearDown.setModifiers(ModifierSet.PUBLIC);
                        newTearDown.setType(new VoidType());
                        LinkedList<AnnotationExpr> anot = new LinkedList<AnnotationExpr>();
                        anot.add(new MarkerAnnotationExpr(new NameExpr("Override")));
                        newTearDown.setAnnotations(anot);
                        newTearDown.setBody(new BlockStmt());

                        MethodCallExpr superTearDown = new MethodCallExpr();
                        superTearDown.setName("super.tearDown");
//                        MethodCallExpr mcT = new MethodCallExpr();
//                        if(testOriented){
//                            mcT.setName("TrepnLib.stopProfilingTest");
//                        }
//                        else {
//                            mcT.setName("TrepnLib.stopProfiling");
//                        }
                        MethodCallExpr getContext = new MethodCallExpr();
                        if (!cDef.isInstrumented()) {
                            getContext.setName("this.getContext");
                        } else {
                            if (cDef.isOther()){
                                getContext.setName("this.getInstrumentation().getContext");
                            }
                            else{
                                getContext.setName("InstrumentationRegistry.getTargetContext");
                            }
                        }
//                        ASTHelper.addArgument(mcT, getContext);
                        MethodCallExpr mcT =  ((Profiler) instrumenter).stopProfiler(getContext);
                        ArrayList<Statement> body = new ArrayList<Statement>();
                        body.add(new ExpressionStmt(mcT));
                        body.add(new ExpressionStmt(superTearDown));
                        newTearDown.getBody().setStmts(body);

                        x.getMembers().add(x.getMembers().size()-1 >0 ? x.getMembers().size()-1:0, newTearDown);
                    }
                }
            }
//            ImportDeclaration imp1 = new ImportDeclaration(ASTHelper.createNameExpr("com.greenlab.trepnlib.TrepnLib"), false, false);
            ImportDeclaration imp1 =  ((Profiler) instrumenter).getLibrary();
            ImportDeclaration imp3 = null;
           if(cDef.isJunit4()||cDef.isJunit4suite()){
                imp3 = new ImportDeclaration(ASTHelper.createNameExpr("android.support.test.InstrumentationRegistry"), false, false);

           }

            if(cu.getImports() != null){
                cu.getImports().add(imp1);
                if(imp3!=null)
                    cu.getImports().add(imp3);
                if(imp2!=null)
                    cu.getImports().add(imp2);
                if(imp4!=null)
                    cu.getImports().add(imp4);
            }else{
                cu.setImports(new LinkedList<ImportDeclaration>());
                cu.getImports().add(imp1);
                if(imp2!=null)
                    cu.getImports().add(imp2);
                if(imp4!=null)
                    cu.getImports().add(imp4);
                if(imp3!=null)
                    cu.getImports().add(imp3);
            }
        }else if(x.getExtends() != null){

            if(isJunitTest){

                new MethodVisitor().visit(cu, cDef);
                if(cDef.hasTests()){
                    return "";
                } 
            }
        }
        return cu.toString();
    }



    /** creates application class
     * instruments manifest.xml to add android:name="appclass"
     * puts appclass in package pack
     * sets appPackage
     * use when there is no android:name yet
     */
    public void CreateAppClass(String pack, String file){
       // build path to  create class

        String [] sp = file.split("/");
        String pathToFile ="";
        if(sp.length-1>0){
            for (int i = 0; i <sp.length-1 ; i++) {
                pathToFile += sp[i] +"/";
                if((pathToFile).equals(project)){
                    pathToFile +=  tName + "/";
                }
            }
        }
        pathToFile += "TrepApp.java";
        XMLParser.addApplicationName(manifest, pack + "." +"TrepApp");
        File src = new File("TrepApp.java");
        File dest = new File( pathToFile);
        try {
            FileUtils.copyFile(src, dest);
        }
        catch (Exception e){
            System.out.println("[JInst] CreateAppClass error in copying appfile ");
        }

        this.appPackage = new ClassDefs(pack,"TrepApp");


        //System.out.println("Parsing & Instrumenting "+file);
        CompilationUnit cu=null;
        FileInputStream in=null;
        try {
            // parse the file
             in = new FileInputStream(dest);
            cu = JavaParser.parse(in,null, false);
            PackageDeclaration p = new PackageDeclaration();
            p.setName(ASTHelper.createNameExpr(pack));
            cu.setPackage(p);
            FileUtils.writeFile(dest, cu.toString());
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }


    public boolean isTestCase(File src) throws IOException {
        BufferedReader b = new BufferedReader(new FileReader(src));
        String s = "";
        boolean res = false;

        while ((s = b.readLine()) != null) {
            if(s.matches(".*(@LargeTest|@MediumTest|@SmallTest|@Smoke|@Suppress)")
                    ||  s.matches(".* class .+ extends (TestCase|ActivityUnitTestCase|ActivityInstrumentationTestCase2|ActivityTestCase|ProviderTestCase|SingleLaunchActivityTestCase|SyncBaseInstrumentation|ActivityInstrumentationTestCase|ActivityInstrumentationTestCase2|AndroidTestCase|ApplicationTestCase|LoaderTestCase|ProviderTestCase2|ServiceTestCasefail).*")
                        ||  s.matches(".*(TestCase|ActivityUnitTestCase|ActivityInstrumentationTestCase2|ActivityTestCase|ProviderTestCase|SingleLaunchActivityTestCase|SyncBaseInstrumentation|ActivityInstrumentationTestCase|ActivityInstrumentationTestCase2|AndroidTestCase|ApplicationTestCase|LoaderTestCase|ProviderTestCase2|ServiceTestCasefail).*")
            ){
                addTestType(src.getAbsolutePath(),"Other");
                res=true;
            }
            else if (s.matches(".*@Test.*")){
                res = true;
                addTestType(src.getAbsolutePath(), "Junit4");
            }
            if(s.matches(".*@SuiteClass.*")){
                addTestType(src.getAbsolutePath(), "SuiteJunit4");
                res = true;
            }


            if(res) break;
        }
        return res;
    }

    public boolean isTestCaseInstrumented(File src) throws IOException {
        //List<String> content = Files.readAllLines(Paths.get(src.getAbsolutePath()), Charset.defaultCharset());
//        System.out.println("fileeeee " + src.getAbsolutePath());
        BufferedReader b = new BufferedReader(new FileReader(src));
        String s = "";
        boolean res = false;

        while ((s = b.readLine()) != null) {
            String s1 = new String(s);
            if(s.matches(".* class .+ extends (ActivityUnitTestCase|ActivityIntrumentationTestCase2|ActivityTestCase|ProviderTestCase|SingleLaunchActivityTestCase|SyncBaseInstrumentation|ActivityInstrumentationTestCase).*")){
                return res;

            }
            else if (s.matches(".*import[ \\t]android.*"))
                return true;
        }
        return res;
    }

    public ClassDefs getAppPackage() {
        return appPackage;
    }

    public void setAppPackage(ClassDefs appPackage) {
        this.appPackage = appPackage;
    }

    public  static Instrumenter getInstrumenter() {
        return instrumenter;
    }



    public static String wrapMethod(MethodDeclaration n, ClassDefs arg, String hash){

         List<String> ll = MethodIdentifierVisitorBackwards.getMethodDefinitionToks(n);
         return arg.getPack() +"."+ String.join(".", ll)  + "->" + n.getName()  + "|" + hash;

    }

    public static String wrapMethod(ConstructorDeclaration n, ClassDefs arg, String hash){
        List<String> ll = MethodIdentifierVisitorBackwards.getMethodDefinitionToks(n);
        return  arg.getPack() +"."+ String.join(".", ll)  + "->" + n.getName()  + "|" + hash;
    }



    public String transformKotlin(String absolutePath) {
        KotlinInstrumenter kt = null;
        try {
            if (this.type == JInst.InstrumentationType.TEST){
                kt = new  KotlinInstrumenter( JInst.InstrumentationType.TEST , JavaParser.parseExpression("null")  );
            }
            else  if (this.type == JInst.InstrumentationType.METHOD){
                kt = new  KotlinInstrumenter( JInst.InstrumentationType.METHOD , JavaParser.parseExpression("null")  );
            }
            else  if (this.type == JInst.InstrumentationType.ACTIVITY){
                kt = new  KotlinInstrumenter( JInst.InstrumentationType.ACTIVITY, JavaParser.parseExpression("null")  );
            }
            else  if (this.type == JInst.InstrumentationType.ANNOTATION){
                kt = new  KotlinInstrumenter( JInst.InstrumentationType.ANNOTATION, JavaParser.parseExpression("null")  );
            }
            else {
                System.out.println("bad instrumentation type (transformKotlin) ");
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        ParserFixed p = new ParserFixed();
        Node.File f = p.parseFile(FileUtils.stringFromFile(absolutePath),false);

        if (KtlTestsInstrumenterUtil.Companion.isTestFile(f) && this.isWhiteBox()){
            return KastreeWriterFixed.Companion.write( kt.instrumentTestFile(f), null);
        }
        else {
            if (this.isWhiteBox()&& this.type == JInst.InstrumentationType.ACTIVITY && KtlTestsInstrumenterUtil.Companion.isLauncherActivity(f, launchActivity)) {
                return KastreeWriterFixed.Companion.write(kt.instrument(f), null);
            }
            else if (this.isWhiteBox() && type != JInst.InstrumentationType.ACTIVITY ) {
                return KastreeWriterFixed.Companion.write(kt.instrument(f), null);
            }
            else if (this.isWhiteBox() && type != JInst.InstrumentationType.ANNOTATION ) {
                return KastreeWriterFixed.Companion.write(kt.instrument(f), null);
            }
            else {
                return KastreeWriterFixed.Companion.write(f, null);
            }
        }
    }

    public String transformKotlinTest(String absolutePath) {
        KotlinInstrumenter kt = null;
        try {
            if (this.type == JInst.InstrumentationType.TEST){
                kt = new  KotlinInstrumenter( JInst.InstrumentationType.TEST , JavaParser.parseExpression("null")  );
            }
            else if (this.type == JInst.InstrumentationType.METHOD) {
                kt = new  KotlinInstrumenter( JInst.InstrumentationType.METHOD , JavaParser.parseExpression("null")  );
            }
            else {
                System.out.println("bad instrumentation type (transformKotlin) ");
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        ParserFixed p = new ParserFixed();
        String ret =   KastreeWriterFixed.Companion.write( kt.instrumentTestFile( p.parseFile(FileUtils.stringFromFile(absolutePath),false) ), null);
        return ret;
    }


    public static void registMethods(){
        ResourceLoader l = new ResourceLoader();
        //File f = new File( l.retrieveConfigAnaDroidConventions("methodsFile"));
        File f = new File("allMethods.json");
        if (f.exists()){
            f.delete();
        }
        try {
            FileUtils.writeFile(f, allMethods.convertTOJSONObject());
        } catch (IOException  e ) {
            e.printStackTrace();
            System.out.println("[Jinst] An error occured while appending to allMethods file ");
        }
    }

    public boolean isBlackBox() {
        return this.approach == TestingApproach.Blackbox;
    }

    public boolean isWhiteBox() {
        return this.approach == TestingApproach.WhiteBox;
    }

    public TestingApproach getApproach() {
        return approach;
    }

    public void setApproach(TestingApproach approach) {
        this.approach = approach;
    }

    public void setApproach(String approach) {
       String app = approach.replace("-","").toLowerCase();
       if (app.contains("black")){
           this.approach = TestingApproach.Blackbox;
       }
       else {
           this.approach = TestingApproach.WhiteBox;
       }
    }

    //    public static String inferContext(ClassOrInterfaceDeclaration cid ) {
//
//        if (monkeyTest){
//            for( ClassOrInterfaceType cit : cid.getExtends()){
//
//            }
//        }
//        return null;
//    }



}
