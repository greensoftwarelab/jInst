package jInst;

import kotlin.text.Charsets;
import sun.misc.ClassLoaderUtil;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ResourceLoader {
    private static String defaultJVMConventionsLocation =  "JVMConventions.cfg";
    private static String defaultAnaDroidConventionsLocation =  "AnaDroidConventions.cfg";



    public  String retrieveConfigJVMConventions(String propName) {
        //getClass().getClassLoader().getResource()
        //System.out.println(readFilesWithNIO(defaultJVMConventionsLocation).stream().filter(it -> it.startsWith(propName)).count());
        InputStream i = this.getClass().getResourceAsStream(defaultJVMConventionsLocation);
        return new BufferedReader(new InputStreamReader(i, StandardCharsets.UTF_8)).lines().collect(Collectors.toList()).stream().filter(it -> it.startsWith(propName)).findFirst().get().split("=")[1];
    }

    public  String retrieveConfigAnaDroidConventions(String propName) {
        //System.out.println(readFilesWithNIO(defaultJVMConventionsLocation).stream().filter(it -> it.startsWith(propName)).count());
        InputStream i = this.getClass().getResourceAsStream(defaultAnaDroidConventionsLocation);
        return new BufferedReader(new InputStreamReader(i, StandardCharsets.UTF_8)).lines().collect(Collectors.toList()).
                stream().filter(it -> it.startsWith(propName)).findFirst().get().split("=")[1];

       // return readFilesWithNIO(getFile(defaultAnaDroidConventionsLocation)).stream().filter(it -> it.startsWith(propName)).findFirst().get().split("=")[1];
    }

    public static List<String> readFilesWithNIO(String filePath){
        Path p = Paths.get(filePath);
        List<String>  l = null ;
        try {
            l = Files.readAllLines(p, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return l;
    }
}
