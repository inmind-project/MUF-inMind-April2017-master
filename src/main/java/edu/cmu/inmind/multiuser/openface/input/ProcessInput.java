package edu.cmu.inmind.multiuser.openface.input;

import edu.cmu.inmind.multiuser.common.Utils;
import edu.cmu.inmind.multiuser.controller.log.Log4J;

import java.io.*;

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
		File featFile = File.createTempFile("OpenFace", ".out", new File(logDir));
		Log4J.info(this, "logging OpenFace features to " + featFile.toString());
		File mp4File = File.createTempFile("OpenFace", ".mp4", new File(logDir));
		Log4J.info(this, "logging OpenFace features to " + featFile.toString());
		//tmpFile.deleteOnExit(); // clean up once we're done --> do not clean up, we want the log!
		new FileOutputStream(featFile).close(); // make sure the tmpFile exists (and is empty)
		ProcessBuilder pb = new ProcessBuilder(binaryName, "-f", fileOrURL, "-of", featFile.toString(), "-q");
		pb.inheritIO();
		openFace = pb.start();
		pb = new ProcessBuilder("ffmpeg", "-i", fileOrURL, "-vcodec", "copy", mp4File.toString());
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
