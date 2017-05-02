/**
 * Copyright (C) Carnegie Mellon University - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * This is proprietary and confidential.
 * Written by members of the ArticuLab, directed by Justine Cassell, 2014.
 * 
 * @author Yoichi Matsuyama <yoichim@cs.cmu.edu>
 * 
 */
package edu.cmu.inmind.multiuser.rapportestimator.rapport;

/**
 * Dialog Acts for Rapport project
 * originally proposed by Ran Zhao
 */
public enum DialogAct {
	NONE(0),
	REQUEST(1),
	SUGGEST(2),
	STATEMENT(3),
	PHATIC(4),
	FEEDBACK_POSITIVE(5),
	FEEDBACK_NEGATIVE(6),
	FEEDBACK_BACKCHANNELING(7);
	
	DialogAct(int n) { value = n; }        
    public final int value;
    
    public static DialogAct getDialogAct(int i) {
        switch(i) {
        case 0:
        	return NONE;
        case 1:
        	return REQUEST;
        case 2:
        	return SUGGEST;
        case 3:
        	return STATEMENT;
        case 4:
        	return PHATIC;
        case 5:
        	return FEEDBACK_POSITIVE;
        case 6:
        	return FEEDBACK_NEGATIVE;
        case 7:
        	return FEEDBACK_BACKCHANNELING;
        }
        return null;
    }
}
