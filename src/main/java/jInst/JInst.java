/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jInst;


import AndroidProjectRepresentation.APICallUtil;
import jInst.Instrumentation.InstrumentGradleHelper;
import jInst.Instrumentation.InstrumentHelper;
import jInst.util.FileUtils;
import jInst.util.XMLParser;

import java.io.File;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author marco
 */
public class JInst {

     public enum InstrumentationType {
         ACTIVITY,
         ANNOTATION,
         TEST, // for test oriented, when test  case instrumentation is needed
         UNITTEST,
         EGIN_METHOD,
         IOMETHOD,
         METHOD, // when start  and end of method has to be instrumented
         NONE
     }

    public enum TestingFramework {
        MONKEY,
        MONKEYRUNNER,
        JUNIT,
        OTHER
    }


    public static TestingFramework inferTestingFramework(String target ){
        String test_target=target.toLowerCase().replace("-","");
        if (test_target.equals("monkey")){
            return TestingFramework.MONKEY;
        }
        else if (test_target.equals("monkeyrunner")){
            return TestingFramework.MONKEYRUNNER;
        }
        else if (test_target.equals("junit")){
            return TestingFramework.JUNIT;
        }
        else return TestingFramework.OTHER;
    }


     // TODO
    public static InstrumentationType inferInstrumentationType(String target ){
         String test_target=target.toLowerCase().replace("-","");
         if (test_target.equals("testoriented")){
            return InstrumentationType.TEST;
         }
         else if (test_target.equals("methodorienteed")){
             return InstrumentationType.METHOD;
         }
         else if (test_target.equals("activityoriented")){
             return InstrumentationType.ACTIVITY;
         }
         else if (test_target.equals("annotationoriented")){
             return InstrumentationType.ANNOTATION;
         }

         else return InstrumentationType.NONE;
    }

    public static String getProjectID(String path){
        String [] x = path.split("/");
        return x.length>0? ( x[x.length-1].equals("latest")? x[x.length-2]:x[x.length-1]) :"unknown";

    }

    public static void main(String[] args) {
        String projType = args[0];
        switch (projType){
            case "-sdk":
                if(args.length != 9){
                    System.err.println("[jInst] Error: Bad arguments length for SDK project. Expected 6, got "+args.length+".");
                    return;
                }else{
                    String approach = args[8];
                    String tName = args[1];
                    String workspace = args[2];
                    String project = args[3];
                    String tests = args[4];
                    //boolean testOriented = args[5].equals("-TestOriented");
                    InstrumentationType instrType = inferInstrumentationType(args[5]);
                    TestingFramework testingFramework = inferTestingFramework( args[7]);
                    String appID = args[7];
                    try {
                        APICallUtil apu = ((APICallUtil) (new APICallUtil().fromJSONObject(new APICallUtil().fromJSONFile(project+"/"+ tName +"/"+appID+".json"))));
                        InstrumentHelper helper = new InstrumentHelper(apu,tName, workspace, project, tests,instrType,approach);
                        helper.testingFramework = testingFramework;
                        helper.projectID = getProjectID(project);
                        helper.generateTransformedProject();
                        helper.generateTransformedTests();
                        FileUtils.writeFile(new File( project+"/"+ tName +"/"+appID+".json"), helper.getAcu().toJSONObject(apu.proj.projectID).toJSONString());
                        helper.registMethods();
                        XMLParser.buildAppPermissionsJSON(helper.getManifest(),helper.getTransFolder());
                    } catch (Exception ex) {
                        Logger.getLogger(JInst.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                break;
            case "-gradle":
                if(args.length != 10){
                    System.err.println("[jInst] Error: Bad arguments length for Gradle project. Expected 8, got "+args.length+".");
                    return;
                }else{
                    String approach = args[9];
                    String appID = args[8];
                    String tName = args[1];
                    String workspace = args[2];
                    String project = args[3];
                    String manifestSource = args[4];
                    String manifestTests = args[5];
                    InstrumentationType instrType = inferInstrumentationType(args[6]);
                    //boolean monkeyTest = args.length>7 ? args[7].equals("-Monkey") : false;
                    TestingFramework testingFramework = inferTestingFramework( args[7]);
                    try {
                        APICallUtil apu = ((APICallUtil) (new APICallUtil().fromJSONObject(new APICallUtil().fromJSONFile(project+"/"+ tName +"/"+appID+".json"))));
                        apu.proj.projectBuildTool="gradle";
                        apu.proj.projectDescription="";
                        InstrumentGradleHelper helper = new InstrumentGradleHelper(apu,tName, workspace, project, "", manifestSource, manifestTests, instrType, approach );
                        helper.testingFramework = testingFramework;
                        helper.projectID = apu.proj.projectID;
                        helper.generateTransformedProject();
                        helper.addPermission();
                        FileUtils.writeFile(new File( project+"/"+ tName +"/"+appID+".json"), helper.getAcu().toJSONObject(apu.proj.projectID).toJSONString());
                        helper.registMethods();
                        XMLParser.buildAppPermissionsJSON( helper.getOriginalManifest(),helper.getTransFolder());
                    } catch (Exception ex) {
                        Logger.getLogger(JInst.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                break;
            default :
                System.out.println("Unknown argument");
                break;
                //
        }
   }
}
