

package com.github.hiroshinke.antlrsample;

import com.github.hiroshinke.antlrsample.ArithmeticBaseVisitor;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.Map;
import java.util.HashMap;
import java.util.List;


public class EvalVisitor extends ArithmeticBaseVisitor<Integer> {

    Map<String, Integer> bindings = new HashMap<String, Integer>();
    
    @Override
    public Integer visitFile_(ArithmeticParser.File_Context ctx) {
	System.out.println("root=" + ctx.getText());
	Integer ret = null;
	List<ArithmeticParser.StatContext> eqs = ctx.stat();
	for( ArithmeticParser.StatContext e: eqs) {
	    ret = visit(e);
	}
	return ret;
    }

    @Override
    public Integer visitExprStat(ArithmeticParser.ExprStatContext ctx) {
	int value = visit(ctx.expression());
	System.out.println( value );
	return value;
    }

    @Override
    public Integer visitAssignStat(ArithmeticParser.AssignStatContext ctx) {

	String id = ctx.expression(0).getText();
	int value = visit(ctx.expression(1));
	bindings.put(id,value);
	return value;
    }

    @Override
    public Integer visitParExpr(ArithmeticParser.ParExprContext ctx) {
	int value = visit(ctx.expression());
	return value;
    }

    @Override
    public Integer visitUnaryExpr(ArithmeticParser.UnaryExprContext ctx) {

	List<TerminalNode> minuses = ctx.MINUS();
	int sign = minuses.size() % 2 == 0 ? 1 : -1;
	int value = visit(ctx.atom());
	return sign * value;
    }

    @Override
    public Integer visitAddtitiveExpr(ArithmeticParser.AddtitiveExprContext ctx) {

	int left = visit(ctx.expression(0));
	int right = visit(ctx.expression(1));

	if( ctx.PLUS() != null ){
	    return left + right;
	}
	else if( ctx.MINUS() != null ){
	    return left - right;
	}
	else {
	    throw new IllegalArgumentException();
	}
    }
    @Override
    public Integer visitPowExpr(ArithmeticParser.PowExprContext ctx) {
	int left = visit(ctx.expression(0));
	int right = visit(ctx.expression(1));
	return (int)Math.pow(left,right);
    }

    @Override
    public Integer visitMultpricativeExpr(ArithmeticParser.MultpricativeExprContext ctx) {

	int left = visit(ctx.expression(0));
	int right = visit(ctx.expression(1));
	
	if( ctx.TIMES() != null ){
	    return left * right;
	}
	else if( ctx.DIV() != null ){
	    return left / right;
	}
	else {
	    throw new IllegalArgumentException();
	}
    }

    @Override
    public Integer visitAtom(ArithmeticParser.AtomContext ctx) {

	ArithmeticParser.ScientificContext ctx1 = ctx.scientific();
	if( ctx1 != null ){
	    return Integer.valueOf(ctx1.getText());
	}
	
	ArithmeticParser.VariableContext ctx2 = ctx.variable();
	if( ctx2 != null ){
	    String id = ctx2.getText();
	    if( bindings.containsKey(id) ){
		return bindings.get(id);
	    }
	    else {
		return 0;
	    }
	}
	return 0;
    }
}
