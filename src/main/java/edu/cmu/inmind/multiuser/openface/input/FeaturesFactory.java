package edu.cmu.inmind.multiuser.openface.input;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import edu.cmu.inmind.multiuser.openface.FeatureType;

public class FeaturesFactory {

	private final List<FeatureType> orderingOfFeatures;
	
	private FeaturesFactory(List<FeatureType> orderingOfFeatures) {
		this.orderingOfFeatures = orderingOfFeatures;
	}
	
	public static FeaturesFactory newFeaturesFactoryFromLine(String line) {
		List<FeatureType> orderingOfFeatures = new ArrayList<>();
		for (String tok : line.split(", ")) {
			FeatureType ft = FeatureType.valueOf(tok);
			assert ft != null;
			orderingOfFeatures.add(ft);
		}
		return new FeaturesFactory(orderingOfFeatures);
	}
	
	public int expectedFeatsPerLine() {
		return orderingOfFeatures.size();
	}
	
	public Map<FeatureType,Float> newFromLine(String line) {
		final EnumMap<FeatureType, Float> f = new EnumMap<>(FeatureType.class);
		Iterator<FeatureType> featIt = orderingOfFeatures.iterator();
		for (String tok : line.split(", ")) {
			float value = Float.parseFloat(tok);
			f.put(featIt.next(), value);
		}
		return Collections.unmodifiableMap(f);
	}

}
