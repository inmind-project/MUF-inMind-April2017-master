package edu.cmu.inmind.multiuser.openface.output;

import java.io.PrintStream;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import edu.cmu.inmind.multiuser.openface.FeatureType;

public class CSVOutput  {

	final PrintStream out;
	
	public CSVOutput(PrintStream out) {
		this.out = out;
		this.out.println(
				String.join(", ", 
						Stream.of(FeatureType.values()).map(FeatureType::name).collect(Collectors.toList())
			));
	}

	public void consumeFrame(Map<FeatureType, Float> f) {
		out.println(
				String.join(", ", 
						Stream.of(FeatureType.values()).map(ft -> f.get(ft)).map(fl -> fl.toString()).collect(Collectors.toList())
			));
	}
	
}
