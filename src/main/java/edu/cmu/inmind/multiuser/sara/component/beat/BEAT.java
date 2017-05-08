package edu.cmu.inmind.multiuser.sara.component.beat;

/**
 * Copyright (C) Carnegie Mellon University - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * This is proprietary and confidential.
 * Written by members of the ArticuLab, directed by Justine Cassell, 2015.
 * 
 * @author Yoichi Matsuyama <yoichim@cs.cmu.edu>
 * 
 */

import beat.beat.compiler.BSONCompiler;
import beat.beat.filter.ActionBeatFilter;
import beat.beat.filter.ContrastBeatFilter;
import beat.beat.filter.NVBConflictFilter;
import beat.beat.filter.NVBFilterModule;
import beat.beat.kb.KnowledgeBase;
import beat.beat.languageProcessing.LanguageModule;
import beat.beat.languageProcessing.POSTagger;
import beat.beat.languageProcessing.StanfordTagger;
import beat.beat.nvbgenerators.*;
import beat.beat.scheduler.SchedulerModule;
import beat.beat.speechTiming.FixedTimingSource;
import beat.beat.speechTiming.TimingSource;
import beat.beat.utilities.*;

import java.io.File;
import java.io.PrintStream;
import java.util.Scanner;

public class BEAT {
	// Compiler
	private BSONCompiler bsonCompiler;
	private LanguageModule tagger;
	private PrintStream printout = System.out;
	private static String speaker, hearer, scene;
	private InputTheread inputThread;
	private BeatCallback callback;

	public static void main(String[] args) throws Exception {
		BEAT beat = new BEAT();
	}

	public BEAT() throws Exception {
		initBEAT();
		scene = "MIT";
		speaker = "AGENT";
		hearer = "USER";
	}

	public void initBEAT() throws Exception {
		// Load DBs:
		NVBTypes nvbTypes = new NVBTypes(new File(beat.Config.XMLDIR + "NVBTypes.xml"));
		ModuleTags moduleTags = new ModuleTags(new File(beat.Config.XMLDIR + "ModuleTags.xml"));
		KnowledgeBase KB = new KnowledgeBase(beat.Config.XMLDIR + beat.Config.KNOWLEGEBASE_FILE);
		//
		//inputThread = new InputTheread(this);
		//inputThread.start();
		/**
		 * Build compiler...
		 */
		System.out.println("--- Building BEAT pipeline ---");
		bsonCompiler = new BSONCompiler(this);
		bsonCompiler.setDEBUG(false);
		/**
		 * Build flattener... (Arranges all behaviors on a timeline)
		 */
		FlattenTreeModule treeFlattener = new FlattenTreeModule(nvbTypes, bsonCompiler);// Compiler
		//
		if(beat.Config.logging) {
			BeatModuleTracer tracerForScheduler = new TextTracer(printout);
			treeFlattener.setOutputTracer(tracerForScheduler);
		}
		/**
		 * Build scheduler...
		 */
		TimingSource fixed = new FixedTimingSource(nvbTypes, 2.0);
		SchedulerModule scheduler = new SchedulerModule(nvbTypes, fixed, treeFlattener);
		/**
		 * Build filters...
		 */
		NVBFilterModule filter = new NVBFilterModule(nvbTypes, scheduler);

		if(beat.Config.logging) {
			filter.setOutputTracer(new PPrintTracer("OUTPUT FROM NVB FILTER MODULE"));
			BeatModuleTracer tracerForFilter = new TextTracer(printout);
			scheduler.setInputTracer(tracerForFilter);
		}
		filter.register(new NVBConflictFilter());
		filter.register(new ActionBeatFilter());
		filter.register(new ContrastBeatFilter());
		/**
		 * Build generator...
		 */
		NVBGeneratorModule generator = new NVBGeneratorModule(nvbTypes, KB, filter);
		if(beat.Config.logging){
			BeatModuleTracer tracerForGenerator = new TextTracer(printout);
			generator.setOutputTracer(tracerForGenerator);
		}
		//
		generator.register(new GazeGenerator());
		generator.register(new ContrastGestureGenerator());
		generator.register(new IconicGestureGenerator());
		generator.register(new DeicticGestureGenerator());
		generator.register(new MonologuePostureShiftGenerator());
		generator.register(new NVBXSLGenerator(new File(beat.Config.XMLDIR + "BeatGenerator.xsl")));
		generator.register(new NVBXSLGenerator(new File(beat.Config.XMLDIR + "EyebrowsGenerator.xsl")));
		generator.register(new NVBXSLGenerator(new File(beat.Config.XMLDIR + "IntonationGenerator.xsl")));
		generator.register(new NVBXSLGenerator(new File(beat.Config.XMLDIR + "MetaGenerator.xsl")));
		generator.register(new NVBXSLGenerator(new File(beat.Config.XMLDIR + "PunctuationGenerator.xsl")));
		generator.register(new NVBXSLGenerator(new File(beat.Config.XMLDIR + "HeadnodGenerator.xsl")));
		// Build tagger...
		POSTagger postagger = new StanfordTagger(beat.Config.POSTAGGER_HOST, beat.Config.POSTAGGER_PORT);
		tagger = new LanguageModule(KB, postagger, generator);
		if(beat.Config.logging) tagger.setOutputTracer(new TextTracer(printout));
		//
		System.out.println("Pipeline built!");
		System.out.println("\n--- Ready to process input ---");
	}

	private class TextTracer implements BeatModuleTracer {
		private PrintStream outputText;
		public TextTracer(final PrintStream outputText) {
			this.outputText = outputText;
		}
		public void trace(final String xml) {
			outputText.flush();
		}
	}

	public void startProcess(String input) {
		try {
			String inputXML = "<UTTERANCE SCENE=\"" + scene + "\" SPEAKER=\"" + speaker + "\" HEARER=\"" + hearer
					+ "\">" + input + "</UTTERANCE>";
			if(beat.Config.logging) System.out.println("startProcess: " + inputXML);
			tagger.process(XMLWrapper.parseXML(inputXML));
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	public BeatCallback getCallback() {
		return callback;
	}

	public void setCallback(BeatCallback callback) {
		this.callback = callback;
	}

	public BSONCompiler getBsonCompiler() {
		return bsonCompiler;
	}

	public void receiveBSON(String bson){
		System.out.println(bson);
		callback.receiveMessage(bson);
	}

	public static class InputTheread extends Thread{
		BEAT beat;
		Scanner scanner = new Scanner(System.in);

		public InputTheread(BEAT beat){
			this.beat = beat;
		}
		public void run(){
			while(true){
				String input = scanner.nextLine();
				beat.bsonCompiler.setPlainText(input);
				beat.startProcess(input);
			}
		}
	}

}
