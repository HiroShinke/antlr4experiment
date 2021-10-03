

package com.github.hiroshinke.antlrsample;

import java.util.ArrayList;

public class ListExpr extends Expr {

    String tag;
    ArrayList<Expr> children = new ArrayList<Expr>();
    
    public ListExpr(String str){
	tag = str;
    }

    public ListExpr(){
	this(null);
    }

    public String getName() {
	return tag;
    }

    void addElement( Expr e ){
	if( tag == null ){
	    tag = ((AtomExpr)e).str;
	} else {
	    children.add(e);
	}
    }

    public boolean equals(Object e){
	
	if( e == null ){
	    return false;
	}
	else if( e instanceof ListExpr ){
	    ListExpr l = (ListExpr)e;
	    return
		tag.equals(l.tag) &&
		children.equals(l.children);
	} else {
	    return false;
	}
    }

    public String toString(){

	ArrayList<String> strs = new ArrayList<String>();
	strs.add(tag);
	for(Expr e : children){
	    strs.add(e.toString());
	}

	StringBuffer buff = new StringBuffer();
	buff.append('(');
	buff.append( String.join(" ",strs) );
	buff.append(')');
	return buff.toString();
    }
}

