

package com.github.hiroshinke.antlrsample;

public class AtomExpr extends Expr {
    String str;
    AtomExpr(String str){
	this.str = str;
    }
    public boolean equals(Object e){
	if( e == null ){
	    return false;
	} else if( e instanceof AtomExpr ){
	    AtomExpr a = (AtomExpr)e;
	    return str.equals(a.str);
	} else {
	    return false;
	}
    }
    public String toString() {
	return str;
    }
    public String getName() {
	return str;
    }
    
}
