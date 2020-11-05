/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jInst.util;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

/**
 *
 * @author User
 */
public class FileUtils {

    public static void registMethod(String filename ,String method){
        File file = new File(filename);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        FileOutputStream fOut = null;
        try {
            fOut = new FileOutputStream(file,true);
        } catch (FileNotFoundException e) {
            System.out.println("[Jinst] Error opening allMethods.txt");
        }
        OutputStreamWriter osw = new OutputStreamWriter(fOut);

        try {
            osw.write(method+"\n");
            osw.flush();
            osw.close();

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("[Jinst] An error occured while appending to allMethods file ");
        }
    }

    public static void copyFolder(File src, File dest) throws IOException{
 
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
    		   //recursive copy
    		   copyFolder(srcFile,destFile);
    		}
 
    	}else{
    		//if file, then copy it
                dest.createNewFile();
    		copyFile(src, dest);
    	}
    }
    
    public static void copyFile(File source, File dest) throws IOException {
        FileChannel sourceChannel = null;
        FileChannel destChannel = null;
        try {
            sourceChannel = new FileInputStream(source).getChannel();
            destChannel = new FileOutputStream(dest).getChannel();
            destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
            
        }finally{
            sourceChannel.close();
            destChannel.close();
       }
    }
    
    public static void writeFile(File file, String content) throws IOException{
        FileWriter fw = new FileWriter(file.getAbsoluteFile());
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write(content);
        bw.flush();
        bw.close(); 
    }

    public static void writeFile(File file, JSONObject jo) throws IOException{
        FileWriter fw = new FileWriter(file.getAbsoluteFile());
        BufferedWriter bw = new BufferedWriter(fw);
        JSONArray jas = new JSONArray();
        jas.addAll(jo.values());
        bw.write(jas.toJSONString());
        jas.clear();
        bw.flush();
        bw.close();
    }
    
    public static void copyAll(List<PackageM> source, List<PackageM> destiny) {
        for(PackageM obj : source){
            destiny.add(obj.clone());
        }
    }



    public static String stringFromFile(String filePath){
        StringBuilder contentBuilder = new StringBuilder();
        try (Stream<String> stream = Files.lines( Paths.get(filePath), StandardCharsets.UTF_8))
        {
            stream.forEach(s -> contentBuilder.append(s).append("\n"));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return contentBuilder.toString();
    }
    
}
