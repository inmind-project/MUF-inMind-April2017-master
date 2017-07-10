package beat.compiler;

public class SentenceFrame {

	private String phase;
	private String intention;
	private String strategy;
	private String rapport;
	private String sentence;
	private String ssml;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
	}

	public void setSArray(String[] sArray){
		this.phase = sArray[0];
		this.intention = sArray[1];
		this.strategy = sArray[2];
		this.rapport = sArray[3];
		this.sentence = sArray[4];
		this.ssml = sArray[5];
	}
	
	public String getPhase() {
		return phase;
	}

	public void setPhase(String phase) {
		this.phase = phase;
	}

	public String getIntention() {
		return intention;
	}

	public void setIntention(String intention) {
		this.intention = intention;
	}

	public String getStrategy() {
		return strategy;
	}

	public void setStrategy(String strategy) {
		this.strategy = strategy;
	}

	public String getRapport() {
		return rapport;
	}

	public void setRapport(String rapport) {
		this.rapport = rapport;
	}

	public String getSentence() {
		return sentence;
	}

	public void setSentence(String sentence) {
		this.sentence = sentence;
	}

	public String getSsml() {
		return ssml;
	}

	public void setSsml(String ssml) {
		this.ssml = ssml;
	}

}
