package edu.cmu.inmind.multiuser.openface.input;

import edu.cmu.inmind.multiuser.openface.FeatureType;

import java.util.Map;

public interface OpenFaceInput {

	Map<FeatureType,Float> getFeaturesForNextFrame();

	boolean hasMoreFrames();
}
