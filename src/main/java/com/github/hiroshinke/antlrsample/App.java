
package com.github.hiroshinke.antlrsample;

/**
 * Hello world!
 *
 */

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;

public class App {

    public static void main(String[] args) throws Exception {
        // create a CharStream that reads from standard input

	InputStream is = System.in;

	if( args.length > 0 ){
	    String filePath = args[0];
	    File fileInput = new File(filePath);
	    is = new FileInputStream(fileInput);
	} 
	    
        ANTLRInputStream input = new ANTLRInputStream(is); 
        ArithmeticLexer lexer = new ArithmeticLexer(input); 
        CommonTokenStream tokens = new CommonTokenStream(lexer); 
        ArithmeticParser parser = new ArithmeticParser(tokens);
        ParseTree tree = parser.file_();
	EvalVisitor visitor = new EvalVisitor();
	int i = visitor.visit(tree);
	System.out.println("visitor result: i = " + i);
    }
}

