package edu.cmu.inmind.openfacequalifier;

/**
 * this should be the output of the classification; 
 * it seems that at the moment we have 1 event (with 3 yes/no states?) every second, so maybe stick with this?
 * (I'd really prefer something that merrits the name "event", ie., comes at some point or span in time
 * and then the three things we investigate should be three separate events of separate types. Let's discuss this later.)
 */
public class Event {

	
	private boolean smile = false;
	private boolean gaze_at = false;
	private float smileStrength = 0f;
	public boolean getSmile(){
		return smile;
	}
	
	public Event setSmile(boolean sm, float smileStrength){
		smile = sm;
//		assert smileStrength >= 0f : smileStrength;
//		assert smileStrength <= 1.0f : smileStrength;
		this.smileStrength = smileStrength;
		return this;
	}
	
	public boolean getGaze(){
		return gaze_at;
	}
	
	public void getSmile(boolean gaze){
		gaze_at=gaze;
	}
	
	@Override
	public String toString() {
		return "Event smile: " + getSmile() + ", gaze: " + getGaze(); 
	}

	public float getSmileStrength() {
		return smileStrength;
	}
	
}
