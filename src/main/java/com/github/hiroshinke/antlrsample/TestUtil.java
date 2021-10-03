
package com.github.hiroshinke.antlrsample;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.Lexer;
    
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Collections;
import org.apache.commons.io.FileUtils;

import java.net.URL;
import java.net.URLClassLoader;
import java.net.MalformedURLException;
    
import org.antlr.v4.Tool;
import org.antlr.v4.runtime.misc.Pair;
import org.antlr.v4.runtime.misc.Utils;
import org.antlr.v4.tool.DefaultToolListener;

import javax.tools.ToolProvider;
import javax.tools.JavaCompiler;
import javax.tools.StandardJavaFileManager;
import javax.tools.JavaFileObject;

/**
 * Unit test for simple App.
 */
public class TestUtil
{

    public static final String CLASSPATH = System.getProperty("java.class.path");
    public static final String PATH_SEP = System.getProperty("path.separator");

    
    public static ListExpr l(Object ... objs){
	ListExpr l = new ListExpr();
	for (Object o: objs){
	    if( o instanceof Expr ){
		l.addElement((Expr)o);
	    }
	    else if( o instanceof String ){
		l.addElement(new AtomExpr((String)o));
	    }
	    else {
		throw new RuntimeException("bad element");
	    }
	}
	return l;
    }

    public static AtomExpr a(String name){
	return new AtomExpr(name);
    }

    
    public static boolean compile(String root,
				  String... fileNames) throws IOException {

	List<File> files = new ArrayList<File>();
	
	for (String fileName : fileNames) {
	    File f = new File(root, fileName);
	    files.add(f);
	}

	JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

	StandardJavaFileManager fileManager =
	    compiler.getStandardFileManager(null, null, null);

	Iterable<? extends JavaFileObject> compilationUnits =
	    fileManager.getJavaFileObjectsFromFiles(files);

	Iterable<String> compileOptions =
	    Arrays.asList("-g",
			  "-source", "1.8",
			  "-target", "1.8",
			  "-implicit:class", "-Xlint:-options",
			  "-d", root,
			  "-cp", root + PATH_SEP + CLASSPATH);
			  

	JavaCompiler.CompilationTask task =
	    compiler.getTask(null, fileManager, null, compileOptions, null,
			     compilationUnits);
	boolean ok = task.call();

	try {
	    fileManager.close();
	} catch (IOException ioe) {
	    ioe.printStackTrace(System.err);
	}

	return ok;
    }

    public static void antlrOnString(String workdir,
				     String grammarFileName,
				     boolean defaultListener,
				     String... extraOptions)
    {
	final List<String> options = new ArrayList<>();
	Collections.addAll(options, extraOptions);

	if ( !options.contains("-o") ) {
	    options.add("-o");
	    options.add(workdir);
	}
	if ( !options.contains("-lib") ) {
	    options.add("-lib");
	    options.add(workdir);
	}
	if ( !options.contains("-encoding") ) {
	    options.add("-encoding");
	    options.add("UTF-8");
	}
	options.add(new File(workdir,grammarFileName).toString());

	final String[] optionsA = new String[options.size()];
	options.toArray(optionsA);
	Tool antlr = new Tool(optionsA);
	// ErrorQueue equeue = new ErrorQueue(antlr);
	// antlr.addListener(equeue);
	if (defaultListener) {
	    antlr.addListener(new DefaultToolListener(antlr));
	}

	antlr.processGrammarsOnCommandLine();
    }

    public static Class<?> loadClassFromTempDir(String workDir,
					 String name)
	throws MalformedURLException,
	       ClassNotFoundException {
	       
	ClassLoader loader =
	    new URLClassLoader(new URL[]{new File(workDir).toURI().toURL()},
			       ClassLoader.getSystemClassLoader());
	return loader.loadClass(name);
    }

    public static Parser createParser(String workDir,
				      String parserName,
				      String lexerName,
				      String src)
	throws MalformedURLException,
	       ClassNotFoundException,
	       NoSuchMethodException,
	       InstantiationException,
	       IllegalAccessException,
	       InvocationTargetException
    {
	
	Class<?>  pc = loadClassFromTempDir(workDir,parserName);
	Class<?>  lc = loadClassFromTempDir(workDir,lexerName);

	Class<? extends Parser>  pcp = pc.asSubclass(Parser.class);
	Class<? extends Lexer>  lcp = lc.asSubclass(Lexer.class);
	
	Constructor<? extends Parser> pcc = pcp.getConstructor(TokenStream.class);
	Constructor<? extends Lexer> lcc = lcp.getConstructor(CharStream.class);

	ANTLRInputStream input = new ANTLRInputStream(src); 
	
	Lexer lexer = (Lexer)lcc.newInstance(input);
	CommonTokenStream tokens = new CommonTokenStream(lexer); 
	
	return (Parser)pcc.newInstance(tokens);
    }


    public static ParseTree execStartRule(Parser parser,String startRuleName)
	throws IllegalAccessException, InvocationTargetException,
	       NoSuchMethodException {
	Method startRule = null;
	Object[] args = null;
	try {
	    startRule = parser.getClass().getMethod(startRuleName);
	} catch (NoSuchMethodException nsme) {
	    startRule = parser.getClass().getMethod(startRuleName, int.class);
	    args = new Integer[]{0};
	}
	ParseTree result = (ParseTree) startRule.invoke(parser, args);
	return result;
    }


    public static Parser createTestParserGrammarString(String workDir,
						       String grammarName,
						       String grammarString,
						       String toParse)
	throws Exception {

	final String grammarFileName = grammarName + ".g";

	
	File file = new File(workDir,grammarFileName);
	FileUtils.writeStringToFile(file,grammarString,StandardCharsets.UTF_8);

	antlrOnString(workDir,grammarFileName,true);
	compile(workDir,
		grammarName + "Parser.java",
		grammarName + "Lexer.java");

	return createParser(workDir,
			    grammarName + "Parser",
			    grammarName + "Lexer",
			    toParse);
    }

}
