package edu.cmu.inmind.multiuser.openface.output;

import edu.cmu.inmind.multiuser.openface.FeatureType;

import java.io.PrintStream;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
