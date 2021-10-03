
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
 * Unit test for Grammar.
 * 
 * Test cases from
 * The Definitive Reference's 9.Error Reporting and Recovery
 */
public class ErrorTest
{

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Rule
    public TestName name = new TestName();


    String simpleGrammar =
	"grammar Simple;\n" +
	"prog : classDef+ ;\n" +	     
	"classDef : 'class' ID '{' member+ '}'\n" +
	"    { System.out.println(\"class: \" + $ID.text);} ; \n" +
	"member : 'int' ID ';' \n" +
	"    { System.out.println(\"var: \" + $ID.text);}  \n" +
	"       |  'int' f=ID '(' ID ')' '{' stat+ '}' \n" +
	"    { System.out.println(\"method: \" + $f.text);}  \n" +	     
	"   ; \n" +
	"stat : expr ';' \n" +
	"    { System.out.println(\"found expr: \" );}  \n" +
	"     | ID '=' expr ';' \n" +
	"    { System.out.println(\"found assign: \" );}  \n" +
	"    ; \n" +
	"expr :   INT \n" +
	"     |   ID '(' INT ')' ;\n" +
	"INT :   [0-9]+ ;\n" +
	"ID  :   [a-zA-Z]+ ;\n" +
	"WS  :   [ \\t\\r\\n]+ -> skip ;\n";
    

    @Test public void test1() throws Exception
    {

	Parser p = createTestParserGrammarString
	    (tempFolder.getRoot().getPath(),
	     "Simple",
	     simpleGrammar,
	     "class T { int i; }");
	ParseTree tree = execStartRule(p,"prog");
	assertThat(tree.toStringTree(p),
		   is( l("prog",
			 l("classDef","class","T","{",
			   l("member",
			     "int", "i",";"),
			   "}")).toString()));
    }

    @Test public void test2() throws Exception
    {

	Parser p = createTestParserGrammarString
	    (tempFolder.getRoot().getPath(),
	     "Simple",
	     simpleGrammar,
	     "class T { int f(x) { a = 3 4 5; } }");
	ParseTree tree = execStartRule(p,"prog");
	assertThat(tree.toStringTree(p),
		   is( l("prog",
			 l("classDef","class","T","{",
			   l("member",
			     "int", "f", "(", "x", ")", "{",
			     l("stat", "a", "=", l("expr", "3"), "<missing ';'>"),
			     l("stat", l("expr","4"), "5",";"),
			     "}"),
			   "}")).toString()));
    }

    @Test public void test3() throws Exception
    {

	Parser p = createTestParserGrammarString
	    (tempFolder.getRoot().getPath(),
	     "Simple",
	     simpleGrammar,
	     "class T ; { int i; }");
	ParseTree tree = execStartRule(p,"prog");
	assertThat(tree.toStringTree(p),
		   is( l("prog",
			 l("classDef","class","T",";","{",
			   l("member",
			     "int", "i",";"),
			   "}")).toString()));
    }


    @Test public void test4() throws Exception
    {

	Parser p = createTestParserGrammarString
	    (tempFolder.getRoot().getPath(),
	     "Simple",
	     simpleGrammar,
	     "class T { int i; ");
	ParseTree tree = execStartRule(p,"prog");
	assertThat(tree.toStringTree(p),
		   is( l("prog",
			 l("classDef","class","T","{",
			   l("member",
			     "int", "i",";"),
			   "<missing '}'>")).toString()));
    }


    @Test public void test5() throws Exception
    {

	Parser p = createTestParserGrammarString
	    (tempFolder.getRoot().getPath(),
	     "Simple",
	     simpleGrammar,
	     "class T { int ; } ");
	ParseTree tree = execStartRule(p,"prog");
	assertThat(tree.toStringTree(p),
		   is( l("prog",
			 l("classDef","class","T","{",
			   "member",
			   l("member","int", ";"),
			   "}")).toString()));
    }


    @Test public void test8() throws Exception
    {

	Parser p = createTestParserGrammarString
	    (tempFolder.getRoot().getPath(),
	     "Simple",
	     simpleGrammar,
	     "class # { int i; } ");
	ParseTree tree = execStartRule(p,"prog");
	assertThat(tree.toStringTree(p),
		   is( l("prog",
			 l("classDef","class","<missing ID>","{",
			   l("member","int", "i", ";"),
			   "}")).toString()));
    }

    
}
