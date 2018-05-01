package edu.cmu.lti.rapport.pipeline.csc;

import java.util.HashMap;

public interface StrategyScorer {

	public double score(String asrResult, HashMap<String, String> sentenceFeatures); 

}
