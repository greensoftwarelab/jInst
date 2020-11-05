package jInst.visitors;


import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.ObjectCreationExpr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MethodIdentifierVisitorBackwards{


    public static List<String> getMethodDefinitionToks(MethodDeclaration md){
       List<String> l = new ArrayList<>();
       Node tmp = md.getParentNode();
       while (tmp != null){
           if (tmp instanceof ClassOrInterfaceDeclaration){
               l.add( ((ClassOrInterfaceDeclaration) tmp).getName() );
           }
           else if (tmp instanceof EnumDeclaration){
               l.add( ((EnumDeclaration) tmp).getName() );
           }
           else if (tmp instanceof ObjectCreationExpr){
               l.add( ((ObjectCreationExpr) tmp).getType().getName() );
           }
           else if (tmp instanceof MethodDeclaration){
               l.add( ((MethodDeclaration) tmp).getName() );
           }
           else if (tmp instanceof ConstructorDeclaration){
               l.add( (((ConstructorDeclaration) tmp).getName()));
           }

           tmp=tmp.getParentNode();

       }
       Collections.reverse(l);
       return l ;
    }

    public static List<String> getMethodDefinitionToks(ConstructorDeclaration md){
        List<String> l = new ArrayList<>();
        Node tmp = md.getParentNode();
        while (tmp != null){
            if (tmp instanceof ClassOrInterfaceDeclaration){
                l.add( ((ClassOrInterfaceDeclaration) tmp).getName() );
            }
            else if (tmp instanceof EnumDeclaration){
                l.add( ((EnumDeclaration) tmp).getName() );
            }
            else if (tmp instanceof ObjectCreationExpr){
                l.add( ((ObjectCreationExpr) tmp).getType().getName() );
            }
            else if (tmp instanceof MethodDeclaration){
                l.add( ((MethodDeclaration) tmp).getName() );
            }
            else if (tmp instanceof ConstructorDeclaration){
                l.add( (((ConstructorDeclaration) tmp).getName()));
            }

            tmp=tmp.getParentNode();

        }
        Collections.reverse(l);
        return l ;
    }

}
