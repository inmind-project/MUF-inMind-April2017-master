package edu.cmu.inmind.multiuser.openface.eventDetector;

import java.util.Map;

import edu.cmu.inmind.multiuser.openface.Event;
import edu.cmu.inmind.multiuser.openface.FeatureType;
import edu.cmu.inmind.multiuser.openface.input.OpenFaceInput;

public class RuleBasedEventDetector extends EventDetector {

	Event e = new Event();
	boolean isSmiling;
	boolean wasSmiling;
	
	public RuleBasedEventDetector(OpenFaceInput ofi) {
		super(ofi);
	}

	@Override
	Event consumeFrame(Map<FeatureType,Float> f) {
		assert f != null;
		assert f.containsKey(FeatureType.AU12_r);
		wasSmiling = e.getSmile();
		
		if (f.get(FeatureType.AU12_r)>0.5){
			isSmiling = true;
		} else {
			isSmiling = false;
		}
		e.setSmile(isSmiling);
		
		if ((isSmiling && !wasSmiling) || (!isSmiling && wasSmiling)){
			return e;		
		} else {
			return null;
		}
	
	}

}
