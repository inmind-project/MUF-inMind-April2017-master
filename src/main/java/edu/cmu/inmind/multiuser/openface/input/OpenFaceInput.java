package edu.cmu.inmind.multiuser.openface.input;

import java.util.Map;

import edu.cmu.inmind.multiuser.openface.FeatureType;

public interface OpenFaceInput {

	Map<FeatureType,Float> getFeaturesForNextFrame();

	boolean hasMoreFrames();
}
