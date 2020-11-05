package jInst.visitors.utils;

import com.github.javaparser.ast.Node;

public class PairStringNode {
    public String first;
    public Node second;

    public PairStringNode(String first, Node second) {
        this.first = first;
        this.second = second;
    }
}
