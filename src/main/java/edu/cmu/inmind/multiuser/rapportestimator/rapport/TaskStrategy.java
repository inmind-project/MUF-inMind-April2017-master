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

public enum TaskStrategy {
	/**
	 * No Strategy
	 */
	NONE(0),	
	ASK_TOPIC(1),
	SUGGEST_NEW_ARTICLE(2),
	SUGGEST_PRESENT_ARTICLE(3),
	ASK_FEEDBACK(4);
		
	TaskStrategy(int n) { value = n; }        
    public final int value;
    
    public static TaskStrategy getTaskStrategy(int i) {
        switch(i) {
        case 0:
        	return NONE;
        case 1:
        	return ASK_TOPIC;
        case 2:
        	return SUGGEST_NEW_ARTICLE;
        case 3:
        	return SUGGEST_PRESENT_ARTICLE;
        case 4:
        	return ASK_FEEDBACK;
        }
        return null;
    }
}

