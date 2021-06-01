/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jInst.Instrumentation;


import AndroidProjectRepresentation.APICallUtil;

import jInst.JInst;
import jInst.util.FileUtils;
import jInst.util.PackageM;
import jInst.util.XMLParser;
import jInst.visitors.MethodChangerVisitor;


import java.io.File;


/**
 *
 * @author user
 */
public class InstrumentGradleHelper extends InstrumentHelper{
    //private String manifestSource;
    //private String manifestTests;

    public InstrumentGradleHelper() {
        super();
        manifest="";
        manifestTest="";
    }



    public InstrumentGradleHelper(APICallUtil apu , String tName, String work, String proj, String tests, String manifSource, String manifTests, JInst.InstrumentationType type, String approach) {
        super(apu, tName, work, proj, tests,type,approach);
        this.originalManifest = manifSource;
        this.manifest = super.transFolder+manifSource.replace("//","/").replace(super.project.replace("//","/"), "");
        this.manifestTest = manifTests.equals("-") ? "" : super.transTests+manifTests.replace(super.tests, "");
//        setApplicationClass(manifSource);
        setInstrumentationType(type);
        setApproach(approach);
    }

    @Override
    public void generateTransformedProject() throws Exception{
        File fProject = new File(project);
        File fTransf = new File(transFolder); fTransf.mkdir();
        File[] listOfFiles = fProject.listFiles();
        //Copy all the files to the new project folder
        for(File f : listOfFiles){
            if(f.isDirectory()){
                if(!f.getName().equals(tName)){
                    //PASS THE FILES THROUGH THE PARSING AND INSTRUMENTATION TOOL
                    //PASS THE FILES THROUGH THE PARSING AND INSTRUMENTATION TOOL
                    this.instrumentSource(f, new File(transFolder+f.getName()));
                }
            }else if(f.isFile()){
                File ax = new File(transFolder+f.getName());
                ax.createNewFile();
                FileUtils.copyFile(f, ax);
            }
        }
        //Save all methods definition in file
        String allMethods = "";
        FileUtils.copyAll(MethodChangerVisitor.getPackages(), packages);
        for(PackageM p : packages){
            allMethods += p.toString();
        }
        File auxF = new File(aux); auxF.mkdir();
//        FileUtils.writeFile(new File(aux+"AllMethods"), allMethods);
        packages.clear() ;
        MethodChangerVisitor.restartPackages();

        this.changeRunner();
      //  this.findLauncher();
        XMLParser.addLibOverrideToManifests(this.findAllManifests());
    }






    @Override
    public void addPermission(){
        File ax = new File(manifest);
        if(!manifest.equals("") && ax.exists()){
            XMLParser.editManifest(manifest);
            //XMLParser.insertReadWriteExternalPermissions(manifest);
        }
        if (type== JInst.InstrumentationType.ACTIVITY && isWhiteBox()){
            XMLParser.insertReadWriteExternalPermissionsAndDebbugable(manifest);
        }
    }

    @Override
    protected void changeRunner(){
        File ax = new File(manifestTest);
        if(!manifestTest.equals("-") && ax.exists()){
            XMLParser.editRunner(manifestTest);

        }

        //insertReadWriteExternalPermissions(manifest);
        //String proj = XMLParser.getTestProjName()+".launch";
        //This line was only necessary if we were to run the tests via IDE
        //XMLParser.editRunConfiguration(workspace+".metadata/.plugins/org.eclipse.debug.core/.launches/"+proj, runnerClass);
    }




    protected void justCopy(File src, File dest) throws Exception{

        if(src.isDirectory()) {
            //if directory not exists, create it
            if (!dest.exists()) {
                dest.mkdir();
            }

            //list all the directory contents
            String files[] = src.list();


            for (String file : files) {
                //construct the src and dest file structure
                File srcFile = new File(src, file);
                File destFile = new File(dest, file);
                //recursive Instrumentation
                justCopy(srcFile,destFile);

            }

        }
        else {

            FileUtils.copyFile(src, dest);

        }


    }

    @Override
    protected void instrumentSource(File src, File dest) throws Exception{
        if(src.isDirectory()){
            //if directory not exists, create it
            if(!dest.exists()){
                dest.mkdir();
            }
            //list all the directory contents
            String files[] = src.list();

            if( src.isDirectory() && src.getName().equals("test")) {
                justCopy(src,dest);
            }
            else{
                for (String file :files){
                    if(file.endsWith("build.gradle")){
                        setCompiledSdkVersion(new File(src,file).getAbsolutePath());
                    }
                }
                for (String file : files) {
                    //construct the src and dest file structure
                    File srcFile = new File(src, file);
                    File destFile = new File(dest, file);
                    //recursive Instrumentation
                    instrumentSource(srcFile,destFile);
                }
            }


        }
        else{
            //if file, then instrument it
            if(src.getAbsolutePath().endsWith(".java")) {
                String res = "";
                if (this.isTestCase(src)) {
                    if(hasToInstrumentProjectTests()){
                        res = transformTest(src.getAbsolutePath());
                    }
                    else {
                        FileUtils.copyFile(src, dest);
                    }
                }
                else {
                    // is a normal java file
                    if ( ! src.getAbsolutePath().contains("build/generated")) {
                        if (this.isWhiteBox()){
                            res = transform(src.getAbsolutePath());
                        }
                        else {
                            FileUtils.copyFile(src, dest);
                        }
                    } else {
                        // belongs to proj external libs
                        FileUtils.copyFile(src, dest);
                    }
                }

                if (!res.equals("")) {
                    dest.createNewFile();
                    FileUtils.writeFile(dest, res);
                }

            } else if(src.getAbsolutePath().endsWith(".kt")){
                String res = "";
                res=transformKotlin(src.getAbsolutePath());
                if(!res.equals("")){
                    dest.createNewFile();
                    FileUtils.writeFile(dest, res);
                }
            }else{
                FileUtils.copyFile(src, dest);
            }
        }

    }

    private String transformTestKotlinTest(String absolutePath) {
        return absolutePath;
    }


}
