package edu.cmu.inmind.multiuser.openface.eventDetector;

import edu.cmu.inmind.multiuser.openface.FeatureType;
import edu.cmu.inmind.multiuser.openface.OpenFaceEvent;
import edu.cmu.inmind.multiuser.openface.input.OpenFaceInput;

import java.util.Map;

public class LinearClassifierDetector extends EventDetector {

	double[] scoringWindow = new double[5];
	
	public LinearClassifierDetector(OpenFaceInput ofi) {
		super(ofi);
	}

	@Override
    OpenFaceEvent consumeFrame(Map<FeatureType, Float> f) {
		// parameters as estimated from Justine's video:
		double smileScore = 0.74 + 
		f.get(FeatureType.confidence) * -4.37 +
		f.get(FeatureType.p_rx) * 3.28 +
		f.get(FeatureType.p_rz) * 5.55 +
		f.get(FeatureType.p_1) * -0.04 +
		f.get(FeatureType.p_6) * 0.11 +
		f.get(FeatureType.p_8) * 0.03 +
		f.get(FeatureType.p_9) * 0.08 +
		f.get(FeatureType.p_11) * -0.07 +
		f.get(FeatureType.p_13) * -0.14 +
		f.get(FeatureType.p_15) * -0.13 +
		f.get(FeatureType.p_18) * 0.11 +
		f.get(FeatureType.p_23) * 1.18 +
		f.get(FeatureType.p_24) * -1.95 +
		f.get(FeatureType.p_25) * 0.44 +
		f.get(FeatureType.p_27) * -7.06 +
		f.get(FeatureType.p_30) * -9.15 +
		f.get(FeatureType.p_31) * 6.6  +
		f.get(FeatureType.p_32) * 11.58 +
		f.get(FeatureType.AU02_r) * -0.16 +
		f.get(FeatureType.AU04_r) * 0.18 +
		f.get(FeatureType.AU06_r) * 2.42 +
		f.get(FeatureType.AU07_r) * 0.21 +
		f.get(FeatureType.AU09_r) * 0.25 +
		f.get(FeatureType.AU10_r) * -0.41 +
		f.get(FeatureType.AU14_r) * -0.62 +
		f.get(FeatureType.AU17_r) * -0.32 +
		f.get(FeatureType.AU20_r) * 0.2  +
		f.get(FeatureType.AU23_r) * 0.59 +
		f.get(FeatureType.AU26_r) * 0.35;
		//System.err.println(smileScore);
		System.arraycopy(scoringWindow, 0, scoringWindow, 1, scoringWindow.length - 1);
		scoringWindow[0] = smileScore;
		double scoreSum = scoringWindow[0] + scoringWindow[1] + scoringWindow[2] + scoringWindow[3] + scoringWindow[4];  
		return (new OpenFaceEvent()).setSmile(scoreSum >= 5, (float) smileScore);
	}

}
