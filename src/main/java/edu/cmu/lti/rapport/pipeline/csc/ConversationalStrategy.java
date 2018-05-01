package edu.cmu.lti.rapport.pipeline.csc;

import java.util.EnumSet;

public enum ConversationalStrategy {
	SD, // self disclosure
	QESD, // question to elicit self disclosure
	ASN, // adherence to social norms
	VSN, // violation of social norms
	Praise, // appraisal of the interlocutor
	BC, // back-channel
	SE, // referring to shared experience
	//HEDGE,
	//...
	;

	public static EnumSet<ConversationalStrategy> toplevelStrategies() {
		return EnumSet.of(SD, Praise, VSN, SE, BC);//, HEDGE, BC);
	}
	
	public static EnumSet<ConversationalStrategy> derivedStrategies() {
		return EnumSet.complementOf(toplevelStrategies());
	}

	public ConversationalStrategy getBaseStrategy() {
		switch (this) {
		case QESD: 
			return SD;
		case ASN: 
			return VSN;
		default:
			throw new IllegalArgumentException("you're attempting to get the base category of a base type");
		}
	}

	public String shortName() {
		return this.equals(Praise) ? "PR" : name();
	}

}
