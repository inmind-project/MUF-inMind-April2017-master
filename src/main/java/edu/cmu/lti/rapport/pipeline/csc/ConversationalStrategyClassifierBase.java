package edu.cmu.lti.rapport.pipeline.csc;

public abstract class ConversationalStrategyClassifierBase implements ConversationalStrategyClassifier {

	// Column Features from other module
	protected double loudness = 0.0;
	protected double f0 = 0.0;
	protected double jitterDDP = 0.0;
	protected double jitterLocal = 0.0;
	protected double shimmer = 0.0;
	
	protected String previousNLGResult = "";
	protected String asrResult = "";

	@Override
	public void setNewNonverbalBehaviour(boolean smiling, boolean nodding, boolean gazing) {
		// TODO: do not ignore these.
	}

	@Override
	public void setNewProsodicFeatures(double loudness, double f0, double jitterDDP, double jitterLocal, double shimmer) {
		this.loudness = loudness;
		this.f0 = f0;
		this.jitterDDP = jitterDDP;
		this.jitterLocal = jitterLocal;
		this.shimmer = shimmer;
	}
	
	@Override
	public void setPreviousSystemUtterance(String nlgResult) {
		this.previousNLGResult = nlgResult;
	}


	@Override
	public void setNewASRResult(String asrResult) {
		this.asrResult = asrResult;
	}

}
