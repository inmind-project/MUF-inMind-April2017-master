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

public enum Strategy {
	/**
	 * No Strategy
	 */
	NONE(0),	
	
	//for Enhance/Maintain Rapport
	/**
	 * Phatic (Conversation opener)
	 */
	PHATIC_OPENER(1),
	
	/**
	 * Phatic (The end of a conversation)
	 */
	PHATIC_END(2),
	
	/**
	 * Phatic (Space filler to avoid silence)
	 */
	PHATIC_FILLER(3),
	
	/**
	 * Self-disclosure (information)
	 */
	SD_INFORMATION(4),
	
	/**
	 * Self-disclosure (thoughts)
	 */
	SD_THOUGHTS(5),
	
	/**
	 * Self-disclosure (feeling)
	 */
	SD_FEELINGS(6),
	
	/**
	 * Question that elicits self-disclosure (information)
	 */
	SQ_INFORMATION(7),
	
	/**
	 * Question that elicits self-disclosure (thoughts)
	 */
	SQ_THOUGHTS(8),
	
	/**
	 * Question that elicits self-disclosure (feeling)
	 */
	SQ_FEELINGS(9),
	
	/**
	 * Refer to shared experiences (Inside current experience)
	 */
	INSIDE_CURRENT_EXPERIENCE(10),
	
	/**
	 * Shared experiences (Outside current experience)
	 */
    OUTSIDE_CURRENT_EXPERIENCE(11),
    
    /**
     * Refer to shared interests
     */
    REFER_TO_SHARED_INTEREST(12),
    
    /**
     * Praise
     */
	PRAISE(13),
	
	/**
	 * Reciprocate previous action (respond to self-disclosure)
	 */
	RECIPROCATE(14),
	
	//for Destroy Rapport
	/**
	 * Keep silent & away ask to repeat
	 */
	KEEP_SILENT(15),
	
	/**
	 * Behave more rudely than expected
	 */
	BEHAVE_RUDELY(16),
	
	/**
	 * Topic irrelevant & not sensitivity to preople's vulnerability
	 */
	TOPIC_IRRELEVANT(17),
	
	/**
	 * Do no reciprocate previous action
	 */
	DO_NOT_RECIPROCATE(18);
	
	
	Strategy(int n) { value = n; }        
    public final int value;
    
    public static Strategy getStrategy(int i) {
        switch(i) {
        case 0:
        	return NONE;
        case 1:
        	return PHATIC_OPENER;
        case 2:
        	return PHATIC_END;
        case 3:
        	return PHATIC_FILLER;
        case 4:
        	return SD_INFORMATION;
        case 5:
        	return SD_THOUGHTS;
        case 6:
        	return SD_FEELINGS;
        case 7:
        	return SQ_INFORMATION;
        case 8:
        	return SQ_THOUGHTS;
        case 9:
        	return SQ_FEELINGS;
        case 10:
        	return INSIDE_CURRENT_EXPERIENCE;
        case 11:
        	return OUTSIDE_CURRENT_EXPERIENCE;
        case 12:
        	return REFER_TO_SHARED_INTEREST;
        case 13:
        	return PRAISE;
        case 14:
        	return RECIPROCATE;
        case 15:
        	return KEEP_SILENT;
        case 16:
        	return BEHAVE_RUDELY;
        case 17:
        	return TOPIC_IRRELEVANT;
        case 18:
        	return DO_NOT_RECIPROCATE;
        }
        return null;
    }
}

