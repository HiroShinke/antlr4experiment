
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
public class RecoverTest
{

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Rule
    public TestName name = new TestName();

    String simpleGrammar =
	"grammar Simple;\n" +
	"prog : dataDesc+ EOF ; \n" +
	"dataDesc : LEVEL ID ('PICTURE'|'PIC') pictureString EOS;\n" +
	"LEVEL : ('0' .. '9')('0' .. '9'); \n" +
	"ID : FIRST_NAME NAME* ;\n" +
	"fragment FIRST_NAME : ('a' .. 'z' ) | ('A' .. 'Z') ;\n"  + 
	"fragment NAME : ('a' .. 'z' ) | ('A' .. 'Z') | ('0' .. '9') ;\n" +
	"pictureString : ('X'+ '(' DIGIT ')')+ ;\n" +
        "DIGIT : ('0' .. '9')+ ;\n" +
	"EOS : '.' [ \\t\\n]+ ;\n" +
	"WS : [ \\t\\n]+ -> skip ;\n"
	;

    @Test public void test1() throws Exception
    {

	Parser p = createTestParserGrammarString
	    (tempFolder.getRoot().getPath(),
	     "Simple",
	     simpleGrammar,
	     "01 XXXXX PIC X(9).\n" +
	     "02 YYYYY PIC X(9).\n"
	     );

	// p.setErrorHandler(new RecoverTestStrategy());
	
	ParseTree tree = execStartRule(p,"prog");
	assertThat(tree.toStringTree(p),
		   is(l("prog",
			l("dataDesc","01","XXXXX","PIC",
			  l("pictureString","X","(","9",")"),".\\n"),
			l("dataDesc","02","YYYYY","PIC",
			  l("pictureString","X","(","9",")"),".\\n"),
			"<EOF>").toString()));
    }

    @Test public void testMissing10() throws Exception
    {

	Parser p = createTestParserGrammarString
	    (tempFolder.getRoot().getPath(),
	     "Simple",
	     simpleGrammar,
	     "01 XXXXX PIC X(9).\n" +
	     "YYYYY PIC X(9).\n" +
	     "02 ZZZZZ PIC X(9).\n"
	     );

	// p.setErrorHandler(new RecoverTestStrategy());
	
	ParseTree tree = execStartRule(p,"prog");
	assertThat(tree.toStringTree(p),
		   is(l("prog",
			l("dataDesc","01","XXXXX","PIC",
			  l("pictureString","X","(","9",")"),".\\n"),
			"YYYYY", "PIC", "X","(","9",")", ".\\n",
			l("dataDesc","02","ZZZZZ","PIC",
			  l("pictureString","X","(","9",")"),".\\n"),
			"<EOF>").toString()));
    }

}
