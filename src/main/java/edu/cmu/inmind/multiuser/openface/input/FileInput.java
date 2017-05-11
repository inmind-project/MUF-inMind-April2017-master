package edu.cmu.inmind.multiuser.openface.input;

import java.io.File;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.SynchronousQueue;

import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListenerAdapter;

import edu.cmu.inmind.multiuser.openface.FeatureType;

public class FileInput extends TailerListenerAdapter implements OpenFaceInput {

	private FeaturesFactory featFac;

	SynchronousQueue<String> lines;
	Tailer tailer;
	/** stop once EOF is reached (i.e., do not use wait heuristics) */
	boolean singleFileMode = false;

	/** implicit constructor to be used by subclasses; make sure to call openFile() when you subclass! */
	FileInput() {}
	
	/* * setup of the file-reading * */
	
	public FileInput(String filename) {
		openFile(filename);
		singleFileMode = true;
	}
	
	void openFile(String filename) {
		lines = new SynchronousQueue<>();
		tailer = new Tailer(new File(filename), this, 20); // 20 milliseconds of delay between read attempts
		new Thread(tailer).start();
	}
	
	/* * implementation of the OpenFaceInput interface * */

	@Override
	public Map<FeatureType,Float> getFeaturesForNextFrame() {
		Map<FeatureType,Float> f = null;
		try {
			waitingThread = Thread.currentThread();
			String line = lines.take();
			waitingThread = null;
			if (line != null) {
				if (line.startsWith("frame")) {
					destroyForcibly();
					return f;
				}
				assert !line.startsWith("frame") : "I was not expecting this line in the middle";
				f = featFac.newFromLine(line);
			}
		} catch (InterruptedException e) {
			if (hasMoreFrames()) {
				e.printStackTrace();
				destroyForcibly();
			} // else: we're already shutting down and this causes the interruption 
		}
		return f;
	}

	@Override
	public boolean hasMoreFrames() {
		return tailer != null;
	}

	/* * overriding the relevant parts of the TailListenerAdapter * */

	@Override
	public void handle(String line) {
		cancelTimeout();
		if (featFac == null)
			featFac = FeaturesFactory.newFeaturesFactoryFromLine(line);
		else 
			try {
				lines.put(line);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
	}

	@Override
	public void endOfFileReached() {
		//System.err.println("waiting for further input");
		if (singleFileMode) {
			destroyForcibly();
		} else {
			setTimeout();
		}
	}

	/* * teardown of the file-reading * */

	Thread waitingThread;
	
	public void destroyForcibly() {
		tailer.stop(); // stop tailing the file
		tailer = null;
		t.cancel(); // release Timer thread
		if (waitingThread != null) {
			waitingThread.interrupt();
		}
	}

	/* * timer to avoid running forever when tailing a closed file * */
	final Timer t = new Timer();
	TimerTask tt = null;
	
	private synchronized void setTimeout() {
		cancelTimeout();
		tt = new TimerTask() {
			@Override public void run() {
				System.err.println("timeout on tailer reached, shutting down");
				destroyForcibly();
			}
		};
		//System.err.println("setting a timeout");
		t.schedule(tt, 5000); // our timeout is 500 ms
	}
	
	private void cancelTimeout() {
		if (tt != null) {
			tt.cancel();
		}
	}
	
}
