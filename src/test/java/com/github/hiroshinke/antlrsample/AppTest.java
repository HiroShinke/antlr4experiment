
package com.github.hiroshinke.antlrsample;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestName;
import static org.hamcrest.Matchers.is;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.Lexer;
    
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Collections;
import org.apache.commons.io.FileUtils;

import static com.github.hiroshinke.antlrsample.TestUtil.createTestParserGrammarString;
import static com.github.hiroshinke.antlrsample.TestUtil.createParser;
import static com.github.hiroshinke.antlrsample.TestUtil.execStartRule;
import static com.github.hiroshinke.antlrsample.TestUtil.l;
import static com.github.hiroshinke.antlrsample.TestUtil.a;

/**
 * Unit test for simple App.
 */
public class AppTest 
{

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Rule
    public TestName name = new TestName();

    public static final String CLASSPATH = System.getProperty("java.class.path");
    public static final String PATH_SEP = System.getProperty("path.separator");    

    @Test public void test1()
    {
	ArithmeticParser p = createArithmeticParser("x=1+2\n");
	ParseTree tree = p.file_();
	assertThat(tree.toStringTree(p),
		   is("(file_ (stat (expression (atom (variable x))) " +
		      "(relop =) (expression (expression " +
		      "(atom (scientific 1))) + (expression (atom (scientific 2))))" +
		      " \\n) <EOF>)"));
    }

    @Test public void test2()
    {
	ArithmeticParser p = createArithmeticParser("x");
	ParseTree tree = p.file_();
	assertThat(tree.toStringTree(p),
		   is("(file_ stat (stat x) <EOF>)"));
    }

    @Test public void test3() throws Exception
    {
	ArithmeticParser p = (ArithmeticParser)
	    createParser(tempFolder.getRoot().getPath(),
			 "com.github.hiroshinke.antlrsample.ArithmeticParser",
			 "com.github.hiroshinke.antlrsample.ArithmeticLexer",
			 "x");
	ParseTree tree = execStartRule(p,"file_");
	assertThat(tree.toStringTree(p),is("(file_ stat (stat x) <EOF>)"));
    }


    @Test public void test4() throws Exception
    {

	Parser p = createTestParserGrammarString(tempFolder.getRoot().getPath(),
						 "X",
						 "grammar X;\n" +
						 "startRule: DIGIT* EOF;\n" +
						 "DIGIT: ('0' .. '9');\n" +
						 "WS : [ \\t\\n] -> skip ;\n",
						 "10");
	ParseTree tree = execStartRule(p,"startRule");
	assertThat(tree.toStringTree(p),is("(startRule 1 0 <EOF>)"));
    }

    @Test public void test5() throws Exception
    {

	Parser p = createTestParserGrammarString(tempFolder.getRoot().getPath(),
						 "X",
						 "grammar X;\n" +
						 "startRule: number* EOF;\n" +
						 "number : DIGIT ;\n" +
						 "DIGIT: ('0' .. '9')+ ;\n" +
						 "WS : [ \\t\\n] -> skip ;\n",
						 "125 300");
	ParseTree tree = execStartRule(p,"startRule");
	assertThat(tree.toStringTree(p),
		   is( l("startRule",
			 l("number", "125"),
			 l("number", "300"),
			 "<EOF>").toString() ));
    }

    @Test public void test6() throws Exception
    {

	Parser p = createTestParserGrammarString
	    (tempFolder.getRoot().getPath(),
	     "X",
	     "grammar X;\n" +
	     "startRule : sync ( stuff sync )* EOF;\n" +
	     "sync : { int type = getInputStream().LA(1);" +
	     "         while( type == ALPH ) " +
	     "         { consume(); type = getInputStream().LA(1); }}\n" +
	     ";\n" +
	     "stuff : DIGIT;\n" +
	     "DIGIT : ('0' .. '9');\n"+
	     "ALPH  : ('a' .. 'z' ) | ('A' .. 'Z' );\n" +
	     "WS : [ \\t\\n] -> skip ;\n",
	     "123a45bb56");
	ParseTree tree = execStartRule(p,"startRule");
	assertThat(tree.toStringTree(p),
		   is( l("startRule",
			 "sync",
			 l("stuff", "1"),
			 "sync",
			 l("stuff", "2"),
			 "sync",
			 l("stuff", "3"),
			 l("sync","a"),
			 l("stuff", "4"),
			 "sync",
			 l("stuff", "5"),
			 l("sync","b","b"),
			 l("stuff", "5"),
			 "sync",
			 l("stuff", "6"),
			 "sync",
			 "<EOF>").toString() ));
    }

    @Test public void test7() throws Exception
    {

	Parser p = createTestParserGrammarString
	    (tempFolder.getRoot().getPath(),
	     "X",
	     "grammar X;\n" +
	     "startRule : stuff* EOF;\n" +
	     "stuff : DIGIT;\n" +
	     "DIGIT : ('0' .. '9');\n"+
	     "ALPH  : ('a' .. 'z' ) | ('A' .. 'Z' );\n" +
	     "WS : [ \\t\\n] -> skip ;\n",
	     "123a45bb56");
	ParseTree tree = execStartRule(p,"startRule");
	assertThat(tree.toStringTree(p),
		   is( l("startRule",
			 l("stuff", "1"),
			 l("stuff", "2"),
			 l("stuff", "3"),
			 "a",
			 l("stuff", "4"),
			 l("stuff", "5"),
			 "b",
			 "b",
			 l("stuff", "5"),
			 l("stuff", "6"),
			 "<EOF>").toString() ));
    }


    @Test public void test8() throws Exception
    {

	Parser p = createTestParserGrammarString
	    (tempFolder.getRoot().getPath(),
	     "X",
	     "grammar X;\n" +
	     "startRule : stuff* EOF;\n" +
	     "stuff : DIGIT;\n" +
	     "DIGIT : ('0' .. '9');\n"+
	     "ALPH  : ('a' .. 'z' ) | ('A' .. 'Z' );\n" +
	     "WS : [ \\t\\n] -> skip ;\n",
	     "12 3a 45 b b5 6");
	ParseTree tree = execStartRule(p,"startRule");
	assertThat(tree.toStringTree(p),
		   is( l("startRule",
			 l("stuff", "1"),
			 l("stuff", "2"),
			 l("stuff", "3"),
			 "a",
			 l("stuff", "4"),
			 l("stuff", "5"),
			 "b",
			 "b",
			 l("stuff", "5"),
			 l("stuff", "6"),
			 "<EOF>").toString() ));
    }
    
    ArithmeticParser createArithmeticParser(String src){
    
        ANTLRInputStream input = new ANTLRInputStream(src); 
        ArithmeticLexer lexer = new ArithmeticLexer(input); 
        CommonTokenStream tokens = new CommonTokenStream(lexer); 
        ArithmeticParser parser = new ArithmeticParser(tokens);

	return parser;
    }

}
