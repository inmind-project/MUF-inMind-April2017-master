package edu.cmu.inmind.multiuser.openface.input;

import edu.cmu.inmind.multiuser.common.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/** 
 * start OpenFace as a separate process which writes to a temporary file
 * read from the temporary file 
 */
public class ProcessInput extends FileInput {

	Process openFace;
	
	public ProcessInput(String fileOrURL) throws IOException {
		//String binaryName = System.getProperty("openface.featureExtraction.binaryName", "/home/timo/uni/software/OpenFace/OpenFace/build/bin/FeatureExtraction");
		String binaryName = System.getProperty("openface.featureExtraction.binaryName", Utils.getProperty("binaryName"));
		File tmpFile = File.createTempFile("OpenFace", ".out");
		new FileOutputStream(tmpFile).close(); // make sure the tmpFile exists (and is empty)
		//tmpFile.deleteOnExit(); // clean up once we're done
		ProcessBuilder pb = new ProcessBuilder(binaryName, "-f", fileOrURL, "-of", tmpFile.toString()); // "-q", 
		pb.inheritIO();
		openFace = pb.start();
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			@Override
			public void run() {
				openFace.destroyForcibly();
			}
		}));
		openFile(tmpFile.toString());
	}

	@Override
	public void destroyForcibly() {
		super.destroyForcibly();
		openFace.destroyForcibly();
	}

	@Override
	public boolean hasMoreFrames() {
		return super.hasMoreFrames() && openFace.isAlive();
	}

}
