package edu.cmu.inmind.multiuser.openface.eventDetector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.cmu.inmind.multiuser.openface.FeatureType;
import edu.cmu.inmind.multiuser.openface.OpenFaceEvent;
import edu.cmu.inmind.multiuser.openface.input.OpenFaceInput;
import edu.cmu.inmind.multiuser.openface.output.EventOutput;

public abstract class EventDetector {

	private final List<EventOutput> listeners = new ArrayList<>();
	OpenFaceInput ofi;
	
	public EventDetector(OpenFaceInput ofi) {
		this.ofi = ofi;
	}
	
	public void addListener(EventOutput listener) {
		listeners.add(listener);
	}
	
	/**   
	 * at present, this just produces output every 1 second and null otherwise
	 * @return null if no event occurs, return an actual event when something has changed
	 */
	abstract OpenFaceEvent consumeFrame(Map<FeatureType,Float> f);
	
	private void notifyListeners(OpenFaceEvent e) {
		for (EventOutput eo : listeners) {
			eo.nextEvent(e);
		}
	}
	
	public void run() {
		//CSVOutput out = new CSVOutput(System.err);
		while (ofi.hasMoreFrames()) {
			Map<FeatureType, Float> f = ofi.getFeaturesForNextFrame();
			//out.consumeFrame(f);
			if (f != null) {
				OpenFaceEvent e = consumeFrame(f);
				notifyListeners(e);
			}
		}
	}
	
}
