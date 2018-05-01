package edu.cmu.lti.rapport.pipeline.csc;

import java.util.EnumMap;

public class ConversationalStrategyDistribution extends EnumMap<ConversationalStrategy, Double> {

	public ConversationalStrategyDistribution() {
		super(ConversationalStrategy.class);
	}

	/** get the top-ranked strategy */
	public ConversationalStrategy getBest() {
		ConversationalStrategy best = null;
		double score = -1.0;
		for (ConversationalStrategy cs : this.keySet()) {
			if (get(cs) > score) {
				best = cs;
				score = get(cs);
			}
		}
		return best;
	}
}
