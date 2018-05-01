package edu.cmu.lti.rapport.pipeline.csc;

public interface ConversationalStrategyClassifier {

	public void setNewNonverbalBehaviour(boolean smiling, boolean nodding, boolean gazing);
	public void setNewProsodicFeatures(double loudness, double f0, double jitterDDP, double jitterLocal, double shimmer);
	public void setPreviousSystemUtterance(String nlgResult);
	
	public void setNewASRResult(String asrResult);

	public ConversationalStrategyDistribution computeConversationalStrategy();

}