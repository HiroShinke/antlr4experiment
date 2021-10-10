
package com.github.hiroshinke.antlrsample;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.Rule;
import org.junit.Ignore;
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
public class ExprTest
{

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Rule
    public TestName name = new TestName();

    String simpleGrammar =
	"grammar Expr;\n" +
	"startRule : expr EOF ; \n" +
	"expr : expr ('*' | '/') expr \n" +
	"     | expr ('+' | '-') expr \n" +
	"     | DIGIT \n" + 
	"     | ID \n" + 
        "     | '(' expr ')' ; \n" +
	"ID : FIRST_NAME NAME* ;\n" +
	"fragment FIRST_NAME : ('a' .. 'z' ) | ('A' .. 'Z') ;\n"  + 
	"fragment NAME : ('a' .. 'z' ) | ('A' .. 'Z') | ('0' .. '9') ;\n" +
        "DIGIT : ('0' .. '9')+ ;\n" +
	"WS : [ \\t\\n]+ -> skip ;\n"
	;

    @Test public void test1() throws Exception
    {

	Parser p = createTestParserGrammarString
	    (tempFolder.getRoot().getPath(),
	     "Expr",
	     simpleGrammar,
	     "100 + 100"
	     );

	ParseTree tree = execStartRule(p,"startRule");
	assertThat(tree.toStringTree(p),
		   is(l("startRule",
			l("expr",
			  l("expr","100"),
			  "+",
			  l("expr","100")
			  ),
			"<EOF>").toString()));
    }

    @Test public void test2() throws Exception
    {

	Parser p = createTestParserGrammarString
	    (tempFolder.getRoot().getPath(),
	     "Expr",
	     simpleGrammar,
	     "100 + 200 + 300"
	     );

	ParseTree tree = execStartRule(p,"startRule");
	assertThat(tree.toStringTree(p),
		   is(l("startRule",
			l("expr",
			  l("expr",
			    l("expr","100"),
			    "+",
			    l("expr","200")),			    
			  "+",
			  l("expr","300")
			  ),
			"<EOF>").toString()));
    }


    @Test public void test3() throws Exception
    {

	Parser p = createTestParserGrammarString
	    (tempFolder.getRoot().getPath(),
	     "Expr",
	     simpleGrammar,
	     "100 + 200 * 300"
	     );

	ParseTree tree = execStartRule(p,"startRule");
	assertThat(tree.toStringTree(p),
		   is(l("startRule",
			l("expr",
			  l("expr","100"),
			  "+",
			  l("expr",
			    l("expr","200"),
			    "*",
			    l("expr","300"))
			  ),
			"<EOF>").toString()));
    }

    
}
