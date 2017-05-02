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

public enum SocialStrategy {
	/**
	 * No Strategy
	 */
	NONE(0),	
	PHATIC_OPENER(1),
	PHATIC_FILLER(2),
	PHATIC_END(3),
	QE_SELF_DISCLOSURE(4),
	POSITIVE_SELF_DISCLOSURE(5),
	NEGATIVE_SELF_DISCLOSURE(6),
	REFER_TO_SHARED_EXPERIENCE(7),
	PRAISE(8),
	ACKNOWLEDGEMENT(9),
	ADHERE_TO_NORMS(10);
	
	SocialStrategy(int n) { value = n; }        
    public final int value;
    
    public static SocialStrategy getSocialStrategy(int i) {
        switch(i) {
        case 0:
        	return NONE;
        case 1:
        	return PHATIC_OPENER;
        case 2:
        	return PHATIC_FILLER;
        case 3:
        	return PHATIC_END;
        case 4:
        	return QE_SELF_DISCLOSURE;
        case 5:
        	return POSITIVE_SELF_DISCLOSURE;
        case 6:
        	return NEGATIVE_SELF_DISCLOSURE;
        case 7:
        	return REFER_TO_SHARED_EXPERIENCE;
        case 8:
        	return PRAISE;
        case 9:
        	return ACKNOWLEDGEMENT;
        case 10:
        	return ADHERE_TO_NORMS;
        }
        return null;
    }
}

