

package com.github.hiroshinke.antlrsample;

import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.DefaultErrorStrategy;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.InputMismatchException;
import org.antlr.v4.runtime.DefaultErrorStrategy;
import org.antlr.v4.runtime.atn.ATN;
import org.antlr.v4.runtime.atn.ATNState;
import org.antlr.v4.runtime.atn.RuleTransition;
import org.antlr.v4.runtime.misc.IntervalSet;
import org.antlr.v4.runtime.misc.Pair;


public class RecoverTestStrategy extends DefaultErrorStrategy {

    @Override
    public void sync(Parser recognizer) throws RecognitionException {
	ATNState s = recognizer.getInterpreter().atn.states.get(recognizer.getState());
	System.err.println("sync @ "+s.stateNumber+"="+s.getClass().getSimpleName());
	// If already recovering, don't try to sync
	if (inErrorRecoveryMode(recognizer)) {
	    return;
	}

        TokenStream tokens = recognizer.getInputStream();
        int la = tokens.LA(1);
	
	int ri = recognizer.getRuleContext().getRuleIndex();
	System.err.println("sync @ la=" + Integer.toString(la) + "," +
			   (( 0 <= la ) ? recognizer.getTokenNames()[la] : "??"));
	System.err.println("sync @ rc=" + Integer.toString(ri) + "," +
			   (( 0 <= ri ) ? recognizer.getRuleNames()[ri] : "??"));
	

        // try cheaper subset first; might get lucky. seems to shave a wee bit off
	IntervalSet nextTokens = recognizer.getATN().nextTokens(s);
	System.err.println("sync @ nextTokens=" + nextTokens);
	if (nextTokens.contains(la)) {
	    // We are sure the token matches
	    nextTokensContext = null;
	    nextTokensState = ATNState.INVALID_STATE_NUMBER;
	    return;
	}

	System.err.println("sync @ nexttokenHasEpsilon=" + nextTokens.contains(Token.EPSILON));
	if (nextTokens.contains(Token.EPSILON)) {
	    if (nextTokensContext == null) {
	 	// It's possible the next token won't match; information tracked
		// by sync is restricted for performance.
	 	nextTokensContext = recognizer.getContext();
	 	nextTokensState = recognizer.getState();
	    }
	    return;
	}

	switch (s.getStateType()) {
	case ATNState.BLOCK_START:
	case ATNState.STAR_BLOCK_START:
	case ATNState.PLUS_BLOCK_START:
	case ATNState.STAR_LOOP_ENTRY:
	    // report error and recover if possible
	    if ( singleTokenDeletion(recognizer)!=null ) {
		return;
	    }

	    throw new InputMismatchException(recognizer);

	case ATNState.PLUS_LOOP_BACK:
	case ATNState.STAR_LOOP_BACK:
	    System.err.println("at loop back: "+s.getClass().getSimpleName());
	    reportUnwantedToken(recognizer);
	    // recoverInline(recognizer);
	     IntervalSet expecting = recognizer.getExpectedTokens();
	     IntervalSet whatFollowsLoopIterationOrRule =
		 expecting.or(getErrorRecoverySet(recognizer));
	     consumeUntil(recognizer, whatFollowsLoopIterationOrRule);
	    break;

	default:
	    // do nothing if we can't identify the exact kind of ATN state
	    break;
	}
    }


    /**
     * {@inheritDoc}
     *
     * <p>The default implementation resynchronizes the parser by consuming tokens
     * until we find one in the resynchronization set--loosely the set of tokens
     * that can follow the current rule.</p>
     */
    @Override
    public void recover(Parser recognizer, RecognitionException e) {
	System.out.println("recover @ in "+recognizer.getRuleInvocationStack()+

			   " index="+recognizer.getInputStream().index()+
			   ", lastErrorIndex="+
			   lastErrorIndex+
			   ", states="+lastErrorStates);

	if ( lastErrorIndex==recognizer.getInputStream().index() &&
	     lastErrorStates != null &&
	     lastErrorStates.contains(recognizer.getState()) ) {
	    System.err.println("seen error condition before index="+
			       lastErrorIndex+", states="+lastErrorStates);
	    System.err.println("FAILSAFE consumes "+recognizer.getTokenNames()[recognizer.getInputStream().LA(1)]);
	}
	super.recover(recognizer,e);
    }


    @Override
    public Token recoverInline(Parser recognizer)
	throws RecognitionException
    {
	System.out.println("recoverInline @ in "+recognizer.getRuleInvocationStack()+
			   " index="+recognizer.getInputStream().index()+
			   ", lastErrorIndex="+
			   lastErrorIndex+
			   ", states="+lastErrorStates);
	return super.recoverInline(recognizer);
	
    }

    
}

