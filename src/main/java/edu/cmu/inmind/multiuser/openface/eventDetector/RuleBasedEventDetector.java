package edu.cmu.inmind.openfacequalifier.eventDetector;

import java.util.Map;

import edu.cmu.inmind.openfacequalifier.Event;
import edu.cmu.inmind.openfacequalifier.FeatureType;
import edu.cmu.inmind.openfacequalifier.input.OpenFaceInput;

public class RuleBasedEventDetector extends EventDetector {

	boolean isSmiling;
	boolean wasSmiling = false;
	
	public RuleBasedEventDetector(OpenFaceInput ofi) {
		super(ofi);
	}

	@Override
	Event consumeFrame(Map<FeatureType,Float> f) {
		assert f != null;
		assert f.containsKey(FeatureType.AU12_r);

		boolean isSmiling = f.get(FeatureType.AU12_r) > 1.3; 
		wasSmiling = isSmiling;
		return (new Event()).setSmile(isSmiling, f.get(FeatureType.AU12_r));
	}

}
