package beat.main;
import beat.compiler.PantomimeCompiler;
import beat.filter.ContrastBeatFilter;
import beat.filter.NVBConflictFilter;
import beat.filter.NVBFilterModule;
import beat.kb.KnowledgeBase;
import beat.languageProcessing.LanguageModule;
import beat.nvbgenerators.*;
import beat.nvbgenerators.*;
import beat.nvbgenerators.*;
import beat.scheduler.SchedulerModule;
import beat.speechTiming.FixedTimingSource;
import beat.speechTiming.TimingSource;
import beat.filter.ActionBeatFilter;
import beat.utilities.FlattenTreeModule;
import beat.nvbgenerators.*;
import beat.utilities.ModuleTags;
import beat.utilities.NVBTypes;
import beat.utilities.PPrintTracer;
import beat.utilities.PruneTagModule;
import beat.utilities.XMLWrapper;

import java.io.*;
import java.util.*;
import java.net.*;

/**
  A sample application that constructs a complete BEAT processing pipeline.

  If a command line argument is given, it is assumed to be the name of an XML 
  file to be read as input for processing.  The format of the input file 
  should be:

     file ::= <?xml version='1.0' encoding='utf-8'?> <DATA> utterance* </DATA>
     utterances ::= <UTTERANCE> xml-text </UTTERANCE>
		 xml-text ::= Any text, optionally including XML tags 
  
  If no command line argument is given, users are asked to type one utterance 
  at a time at a simple onscreen prompt until a "." is typed by itself.

  The output from BEAT is displayed as a time ordered list of onsets and 
  endtimes of all scheduled behaviors, relative to the start of speech.

**/   
public class ExampleExecutive {

    public static final String TTSTIMESOURCE_HOST = "localhost";
    public static final int    TTSTIMESOURCE_PORT = 8022;  //BEAT=8022 REA=8012
    
    public static final String POSTAGGER_HOST = "localhost";
    public static final int    POSTAGGER_PORT = 11111; //9090

    public static final String PANTOMIME_HOST = "localhost";
    public static final int    PANTOMIME_PORT = 9000;
		
    public static final String XMLDIR="XMLData/";
    
    //Can pass in a text file, or be prompted for text..
    public static void main(String args[]) throws Exception {
	
	String myHost = (InetAddress.getLocalHost()).getHostName();
	System.out.println("BEAT is starting up on "+myHost+"...");
	
	//Load DBs:
	NVBTypes nvbTypes=new NVBTypes(new File(XMLDIR+"NVBTypes.xml"));
	ModuleTags moduleTags=new ModuleTags(new File(XMLDIR+"ModuleTags.xml"));
	KnowledgeBase KB = new KnowledgeBase(XMLDIR+"database.xml");
	
	//BEAT modules
	//System.out.println("--- Building BEAT pipeline ---");
	//BeatModule devNull = new BeatModule();
	/* You can replace the "devNull" module above with a compiler for your 
	   animation system.  Here is an example of how to use the PantomimeCompiler:*/
	/*
	System.out.println("Making PantomimeCompiler..."); 
	McNeillCompiler compiler = new McNeillCompiler(System.out);
	compiler.setDEBUG(true);*/
	
	//Build compiler...
	System.out.println("Making PantomimeCompiler..."); 
	PantomimeCompiler compiler=new PantomimeCompiler(PANTOMIME_HOST,PANTOMIME_PORT);
	compiler.setOutputTracer(new PPrintTracer("OUTPUT FROM PANTOMIME COMPILER"));
	compiler.setDEBUG(true);
	
	//Build flattener... (Arranges all behaviors on a timeline)
	FlattenTreeModule treeFlattener=new FlattenTreeModule(nvbTypes,compiler);
	treeFlattener.setOutputTracer(new PPrintTracer("OUTPUT FROM FLATTEN TREE MODULE"));
	
	//Build scheduler...
	//TimingSource festival=new FestivalTimingSource(myHost,8030,TTSTIMESOURCE_HOST,TTSTIMESOURCE_PORT); 
	TimingSource fixed=new FixedTimingSource(nvbTypes,2.0);
	SchedulerModule scheduler=new SchedulerModule(nvbTypes,fixed,treeFlattener);
	//SchedulerModule scheduler=new SchedulerModule(nvbTypes,fixed,mcneilCompiler);
	scheduler.setOutputTracer(new PPrintTracer("OUTPUT FROM SCHEDULER MODULE"));
	
	//Build tagfilter... (Removes the original linguistic tags from the xml)
	PruneTagModule filterLinguisticTags = new PruneTagModule(moduleTags.getModuleTags("Tagger"),scheduler);
	filterLinguisticTags.setOutputTracer(new PPrintTracer("OUTPUT FROM PRUNE TAG MODULE"));
	
	//Build filters...
	NVBFilterModule filter=new NVBFilterModule(nvbTypes,filterLinguisticTags);
	filter.setOutputTracer(new PPrintTracer("OUTPUT FROM NVB FILTER MODULE"));
	filter.register(new NVBConflictFilter());
	filter.register(new ActionBeatFilter());
	filter.register(new ContrastBeatFilter());
	
	//Build generator...
	NVBGeneratorModule generator=new NVBGeneratorModule(nvbTypes,KB,filter);
	generator.setOutputTracer(new PPrintTracer("OUTPUT FROM NVB GENERATOR MODULE"));
	generator.register(new GazeGenerator());
	generator.register(new ContrastGestureGenerator());
	generator.register(new IconicGestureGenerator());
	generator.register(new DeicticGestureGenerator());
	generator.register(new MonologuePostureShiftGenerator());
	generator.register(new NVBXSLGenerator(new File(XMLDIR+"BeatGenerator.xsl")));
	generator.register(new NVBXSLGenerator(new File(XMLDIR+"EyebrowsGenerator.xsl")));
	generator.register(new NVBXSLGenerator(new File(XMLDIR+"IntonationGenerator.xsl")));
	generator.register(new NVBXSLGenerator(new File(XMLDIR+"MetaGenerator.xsl")));
	generator.register(new NVBXSLGenerator(new File(XMLDIR+"PunctuationGenerator.xsl")));
	generator.register(new NVBXSLGenerator(new File(XMLDIR+"HeadnodGenerator.xsl")));
	
	//Build tagger...
	//POSTagger postagger = new POSTaggerServer(POSTAGGER_HOST,POSTAGGER_PORT);
	LanguageModule tagger=new LanguageModule(KB,null,generator);
	//tagger.setDEBUG(true);
	tagger.setOutputTracer(new PPrintTracer("OUTPUT FROM LANGUAGE MODULE"));
	
	System.out.println("Pipeline built!");
	
	System.out.println("\n--- Ready to process input ---");
	
	String key; String input;
	BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
	
	//Process input UTTERANCE by UTTERANCE, either from file or from stdin...
	if(args.length>0) { // file name specified
	    String inputfile = args[0];
	    System.out.println("\nLoading test input from "+inputfile+"...");
	    XMLWrapper xml= new XMLWrapper(new File(inputfile));
	    Vector utterances = xml.getAllNodesOfType("UTTERANCE");
	    String resetval = "";
	    for(int i=0;i<utterances.size();i++) {
		System.out.println("\n=========== Processing Utterance "+(i+1)+" ===========\n");
		org.w3c.dom.Element utt = (org.w3c.dom.Element)utterances.elementAt(i);
		resetval = utt.getAttribute("RESET");
		if(resetval!=null) {
		    if(resetval.equals("1")) {
			System.out.println("\n **** RESETTING DISCOURSE HISTORY ****\n");
			tagger.reset();
		    }
		}
		System.out.println(utt);
		tagger.process(XMLWrapper.parseXML(utt.toString()));
		System.out.println("\nHIT ENTER TO PROCESS NEXT UTTERANCE..."); key = stdin.readLine();
	    }
	} else { // no file name, using stdin
	    System.out.println("\nType your an input utterance at the prompt ('.' by itself quits)\n");
	    while(true) {
		System.out.print("\nInput> ");
		input = stdin.readLine();
		if(input.equals(".")) break;
		tagger.process(XMLWrapper.parseXML("<UTTERANCE>"+input+"</UTTERANCE>"));
	    }
	}			
	System.exit(42);
    }		
}

