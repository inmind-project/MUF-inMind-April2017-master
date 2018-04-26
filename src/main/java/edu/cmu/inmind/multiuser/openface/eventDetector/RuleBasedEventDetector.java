package edu.cmu.inmind.multiuser.openface.eventDetector;

import edu.cmu.inmind.multiuser.openface.FeatureType;
import edu.cmu.inmind.multiuser.openface.OpenFaceEvent;
import edu.cmu.inmind.multiuser.openface.input.OpenFaceInput;

import java.util.Map;

public class RuleBasedEventDetector extends EventDetector {

	boolean isSmiling;
	boolean wasSmiling = false;
	
	public RuleBasedEventDetector(OpenFaceInput ofi) {
		super(ofi);
	}

	@Override
    OpenFaceEvent consumeFrame(Map<FeatureType,Float> f) {
		assert f != null;
		assert f.containsKey(FeatureType.AU12_r);

		boolean isSmiling = f.get(FeatureType.AU12_r) > 1.3; 
		wasSmiling = isSmiling;
		return (new OpenFaceEvent()).setSmile(isSmiling, f.get(FeatureType.AU12_r));
	}

}
