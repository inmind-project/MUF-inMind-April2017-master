package edu.cmu.inmind.multiuser.openface.input;

import edu.cmu.inmind.multiuser.controller.common.Utils;
import edu.cmu.inmind.multiuser.controller.log.Log4J;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

/** 
 * start OpenFace as a separate process which writes to a temporary file
 * read from the temporary file 
 */
public class ProcessInput extends FileInput {

	Process openFace;
	Process ffmpeg;

	public ProcessInput(String fileOrURL) throws IOException {
		String binaryName = Utils.getProperty("openface.featureExtraction.binaryName");
		String logDir = Utils.getProperty("openface.featureExtraction.logDir");
		File featFile = File.createTempFile("OpenFace", ".openfacefeatures", new File(logDir));
		Log4J.info(this, "logging OpenFace features to " + featFile.toString());
		Log4J.info(this, "logging OpenFace features to " + featFile.toString());
		//tmpFile.deleteOnExit(); // clean up once we're done --> do not clean up, we want the log!
		new FileOutputStream(featFile).close(); // make sure the tmpFile exists (and is empty)
		ProcessBuilder pb = new ProcessBuilder(binaryName, "-f", fileOrURL, "-of", featFile.toString(), "-q");
		pb.inheritIO();
		openFace = pb.start();
		String mp4Filename = featFile.toString().replace(".openfacefeatures", ".mp4");
		pb = new ProcessBuilder("ffmpeg", "-i", fileOrURL, "-vcodec", "copy", mp4Filename);
		//pb.inheritIO();
		ffmpeg = pb.start();
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			@Override
			public void run() {
				openFace.destroyForcibly();
				PrintStream ps = new PrintStream(ffmpeg.getOutputStream());
				ps.println("q");
				ps.close();
			}
		}));
		openFile(featFile.toString());
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
