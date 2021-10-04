
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
public class CobolTest
{

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Rule
    public TestName name = new TestName();

    String simpleGrammar =
	"grammar Simple;\n" +
	"prog : dataDesc+ ; \n" +
	"dataDesc : LEVEL ID ('PICTURE'|'PIC') pictureString EOS;\n" +
	"LEVEL : ('0' .. '9')('0' .. '9'); \n" +
	"ID : FIRST_NAME NAME* ;\n" +
	"fragment FIRST_NAME : ('a' .. 'z' ) | ('A' .. 'Z') ;\n"  + 
	"fragment NAME : ('a' .. 'z' ) | ('A' .. 'Z') | ('0' .. '9') ;\n" +
	"pictureString : ('X'+ '(' DIGIT ')')+ ;\n" +
        "DIGIT : ('0' .. '9')+ ;\n" +
	"EOS : '.' [ \\t\\n]+ ;\n";
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
	ParseTree tree = execStartRule(p,"prog");
	assertThat(tree.toStringTree(p),
		   is(l("prog",
			l("dataDesc","01","XXXXX","PIC",
			  l("pictureString","X","(","9",")"),".\\n"),
			l("dataDesc","02","YYYYY","PIC",
			  l("pictureString","X","(","9",")"),".\\n")).toString()));
    }


    @Test public void test2() throws Exception
    {

	Parser p = createTestParserGrammarString
	    (tempFolder.getRoot().getPath(),
	     "Simple",
	     simpleGrammar,
	     "01 XXXXX PIC X(9).\n" +
	     "02 YYYYY PIC X(9).\n"
	     );
	ParseTree tree = execStartRule(p,"prog");
	assertThat(tree.toStringTree(p),
		   is(l("prog",
			l("dataDesc","01","XXXXX","PIC",
			  l("pictureString","X","(","9",")"),".\\n"),
			l("dataDesc","02","YYYYY","PIC",
			  l("pictureString","X","(","9",")"),".\\n")).toString()));
    }


    @Test public void test3() throws Exception
    {

	Parser p = createTestParserGrammarString
	    (tempFolder.getRoot().getPath(),
	     "Simple",
	     simpleGrammar,
	     "01 XXXXX PIC X(9).\n" +
	     "#\n" + 
	     "02 YYYYY PIC X(9).\n"
	     );
	ParseTree tree = execStartRule(p,"prog");
	assertThat(tree.toStringTree(p),
		   is(l("prog",
			l("dataDesc","01","XXXXX","PIC",
			  l("pictureString","X","(","9",")"),".\\n"),
			l("dataDesc","02","YYYYY","PIC",
			  l("pictureString","X","(","9",")"),".\\n")).toString()));
    }


    @Test public void test4() throws Exception
    {

	Parser p = createTestParserGrammarString
	    (tempFolder.getRoot().getPath(),
	     "Simple",
	     simpleGrammar,
	     "01 XXXXX PIC X(9).\n" +
	     "02 # YYYYY PIC X(9).\n"
	     );
	ParseTree tree = execStartRule(p,"prog");
	assertThat(tree.toStringTree(p),
		   is(l("prog",
			l("dataDesc","01","XXXXX","PIC",
			  l("pictureString","X","(","9",")"),".\\n"),
			l("dataDesc","02","YYYYY","PIC",
			  l("pictureString","X","(","9",")"),".\\n")).toString()));
    }

    @Test public void test5() throws Exception
    {

	Parser p = createTestParserGrammarString
	    (tempFolder.getRoot().getPath(),
	     "Simple",
	     simpleGrammar,
	     "01 XXXXX PIC X(9).\n" +
	     "02 YYYYY # PIC X(9).\n"
	     );
	ParseTree tree = execStartRule(p,"prog");
	assertThat(tree.toStringTree(p),
		   is(l("prog",
			l("dataDesc","01","XXXXX","PIC",
			  l("pictureString","X","(","9",")"),".\\n"),
			l("dataDesc","02","YYYYY","PIC",
			  l("pictureString","X","(","9",")"),".\\n")).toString()));
    }

    @Test public void test6() throws Exception
    {

	Parser p = createTestParserGrammarString
	    (tempFolder.getRoot().getPath(),
	     "Simple",
	     simpleGrammar,
	     "01 XXXXX PIC X(9).\n" +
	     "02 YYYYY PIC # X(9).\n"
	     );
	ParseTree tree = execStartRule(p,"prog");
	assertThat(tree.toStringTree(p),
		   is(l("prog",
			l("dataDesc","01","XXXXX","PIC",
			  l("pictureString","X","(","9",")"),".\\n"),
			l("dataDesc","02","YYYYY","PIC",
			  l("pictureString","X","(","9",")"),".\\n")).toString()));
    }


    @Test public void test7() throws Exception
    {

	Parser p = createTestParserGrammarString
	    (tempFolder.getRoot().getPath(),
	     "Simple",
	     simpleGrammar,
	     "01 XXXXX PIC X(9).\n" +
	     "# 02 YYYYY PIC X(9).\n"
	     );
	ParseTree tree = execStartRule(p,"prog");
	assertThat(tree.toStringTree(p),
		   is(l("prog",
			l("dataDesc","01","XXXXX","PIC",
			  l("pictureString","X","(","9",")"),".\\n"),
			l("dataDesc","02","YYYYY","PIC",
			  l("pictureString","X","(","9",")"),".\\n")).toString()));
    }


    @Test public void test8() throws Exception
    {

	Parser p = createTestParserGrammarString
	    (tempFolder.getRoot().getPath(),
	     "Simple",
	     simpleGrammar,
	     "01 XXXXX PIC X(9).\n" +
	     "# # # # # 02 YYYYY PIC X(9).\n"
	     );
	ParseTree tree = execStartRule(p,"prog");
	assertThat(tree.toStringTree(p),
		   is(l("prog",
			l("dataDesc","01","XXXXX","PIC",
			  l("pictureString","X","(","9",")"),".\\n"),
			l("dataDesc","02","YYYYY","PIC",
			  l("pictureString","X","(","9",")"),".\\n")).toString()));
    }


    @Test public void testMissing1() throws Exception
    {

	Parser p = createTestParserGrammarString
	    (tempFolder.getRoot().getPath(),
	     "Simple",
	     simpleGrammar,
	     "01 XXXXX X(9).\n" +
	     "02 YYYYY PIC X(9).\n"
	     );
	ParseTree tree = execStartRule(p,"prog");
	assertThat(tree.toStringTree(p),
		   is(l("prog",
			l("dataDesc","01","XXXXX",
			  l("pictureString","X","(","9",")"),".\\n"),
			l("dataDesc","02","YYYYY","PIC",
			  l("pictureString","X","(","9",")"),".\\n")).toString()));
    }


    @Test public void testMissing2() throws Exception
    {

	Parser p = createTestParserGrammarString
	    (tempFolder.getRoot().getPath(),
	     "Simple",
	     simpleGrammar,
	     "01 XXXXX PIC X(9)\n" +
	     "02 YYYYY PIC X(9).\n"
	     );
	ParseTree tree = execStartRule(p,"prog");
	assertThat(tree.toStringTree(p),
		   is(l("prog",
			l("dataDesc","01","XXXXX","PIC",
			  l("pictureString","X","(","9",")"),"<missing EOS>"),
			l("dataDesc","02","YYYYY","PIC",
			  l("pictureString","X","(","9",")"),".\\n")).toString()));
    }

    @Test public void testMissing3() throws Exception
    {

	Parser p = createTestParserGrammarString
	    (tempFolder.getRoot().getPath(),
	     "Simple",
	     simpleGrammar,
	     "01 XXXXX PIC X(9).\n" +
	     "02 PIC X(9).\n"
	     );
	ParseTree tree = execStartRule(p,"prog");
	assertThat(tree.toStringTree(p),
		   is(l("prog",
			l("dataDesc","01","XXXXX","PIC",
			  l("pictureString","X","(","9",")"),".\\n"),
			l("dataDesc","02","<missing ID>","PIC",
			  l("pictureString","X","(","9",")"),".\\n")).toString()));
    }


    @Test public void testMissing4() throws Exception
    {

	Parser p = createTestParserGrammarString
	    (tempFolder.getRoot().getPath(),
	     "Simple",
	     simpleGrammar,
	     "01 XXXXX PIC X(9).\n" +
	     "02 YYYYY PIC .\n"
	     );
	ParseTree tree = execStartRule(p,"prog");
	assertThat(tree.toStringTree(p),
		   is(l("prog",
			l("dataDesc","01","XXXXX","PIC",
			  l("pictureString","X","(","9",")"),".\\n"),
			l("dataDesc","02","YYYYY","PIC",
			  "pictureString",".\\n")).toString()));
    }

    
    @Test @Ignore public void testMissing10() throws Exception
    {

	Parser p = createTestParserGrammarString
	    (tempFolder.getRoot().getPath(),
	     "Simple",
	     simpleGrammar,
	     "01 XXXXX PIC X(9).\n" +
	     " YYYYY PIC X(9).\n" +
	     "02 ZZZZZ PIC X(9).\n"
	     );
	ParseTree tree = execStartRule(p,"prog");
	assertThat(tree.toStringTree(p),
		   is(l("prog",
			l("dataDesc","01","XXXXX","PIC",
			  l("pictureString","X","(","9",")"),"<missing EOS>"),
			l("dataDesc","02","YYYYY","PIC",
			  l("pictureString","X","(","9",")"),".\\n")).toString()));
    }


    String simpleGrammar2 =
	"grammar Simple;\n" +
	"prog : dataDesc+ ; \n" +
	"dataDesc : LEVEL ID ('PICTURE'|'PIC') PICTURESTR EOS;\n" +
	"LEVEL : ('0' .. '9')('0' .. '9'); \n" +
	"ID : FIRST_NAME NAME* ;\n" +
	"fragment FIRST_NAME : ('a' .. 'z' ) | ('A' .. 'Z') ;\n"  + 
	"fragment NAME : ('a' .. 'z' ) | ('A' .. 'Z') | ('0' .. '9') ;\n" +
	"PICTURESTR : PICTURESTR0 ;\n" +
	"fragment PICTURESTR0 : ('X'+ '(' DIGIT ')')+ ;\n" +
        "DIGIT : ('0' .. '9')+ ;\n" +
	"EOS : '.' [ \\t\\n]+ ;\n";
	;


    @Test public void testFragment1() throws Exception
    {

	Parser p = createTestParserGrammarString
	    (tempFolder.getRoot().getPath(),
	     "Simple",
	     simpleGrammar2,
	     "01 XXXXX PIC X(9).\n" +
	     "02 YYYYY PIC X(9).\n"
	     );
	ParseTree tree = execStartRule(p,"prog");
	assertThat(tree.toStringTree(p),
		   is(l("prog",
			l("dataDesc","01","XXXXX","PIC","X(9)",".\\n"),
			l("dataDesc","02","YYYYY","PIC","X(9)",".\\n")).toString()));

    }

    @Test @Ignore public void testFragment2() throws Exception
    {

	Parser p = createTestParserGrammarString
	    (tempFolder.getRoot().getPath(),
	     "Simple",
	     simpleGrammar2,
	     "01 XXXXX PIC X (9).\n" +
	     "02 YYYYY PIC X(9).\n"
	     );
	ParseTree tree = execStartRule(p,"prog");
	assertThat(tree.toStringTree(p),
		   is(l("prog",
			l("dataDesc","01","XXXXX","PIC","X(9)",".\\n"),
			l("dataDesc","02","YYYYY","PIC","X(9)",".\\n")).toString()));

    }

    
}
