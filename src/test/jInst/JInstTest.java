package jInst;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.CompilationUnit;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class JInstTest {





    @Test
    void isFileParsable() throws IOException, ParseException {
        String file = "/Users/ruirua/repos/AnaDroid/demoProjects/N2AppTest/app/src/main/java/uminho/di/greenlab/n2apptest/MainActivity.java";
        CompilationUnit cu;
        FileInputStream in = new FileInputStream(file);
        try {
            // parse the file
            cu = JavaParser.parse(in,null, false);
        } finally {
            in.close();
        }
        assertEquals(cu.getPackage().getName().toString(),"uminho.di.greenlab.n2apptest");
    }


    @Test
    void correctActivityInstrumentation(){

    }

}