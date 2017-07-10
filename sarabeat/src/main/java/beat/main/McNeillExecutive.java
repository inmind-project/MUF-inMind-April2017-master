package beat.main;

/* -------------------------------------------------------------------------

   McNeillExecutive.java
     - Runs McNeillCompiler for output.

   BEAT is Copyright(C) 2000-2001 by the MIT Media Laboratory.  
   All Rights Reserved.

   Developed by Hannes Vilhjalmsson, Timothy Bickmore, Yang Gao and Justine 
   Cassell at the Media Laboratory, MIT, Cambridge, Massachusetts, with 
   support from France Telecom, AT&T and the other generous sponsors of the 
   MIT Media Lab.

   For use by academic research labs, only with prior approval of Professor
   Justine Cassell, MIT Media Lab.

   This distribution is approved by Walter Bender, Director of the Media
   Laboratory, MIT.

   Permission to use, copy, or modify this software for educational and 
   research purposes only and without fee is hereby granted, provided  
   that this copyright notice and the original authors' names appear on all 
   copies and supporting documentation. If individual files are separated 
   from this distribution directory structure, this copyright notice must be 
   included. For any other uses of this software in original or modified form, 
   including but not limited to distribution in whole or in part, specific 
   prior permission must be obtained from MIT.  These programs shall not be 
   used, rewritten, or adapted as the basis of a commercial software or 
   hardware product without first obtaining appropriate licenses from MIT. 
   MIT makes no representation about the suitability of this software for 
   any purpose. It is provided "as is" without express or implied warranty.

   ------------------------------------------------------------------------*/

import beat.compiler.McNeillCompiler;
import beat.filter.ContrastBeatFilter;
import beat.filter.DeicticBeatFilter;
import beat.filter.NVBConflictFilter;
import beat.filter.NVBFilterModule;
import beat.kb.KnowledgeBase;
import beat.languageProcessing.ConexorPOSTagger;
import beat.languageProcessing.LanguageModule;
import beat.languageProcessing.POSTagger;
import beat.nvbgenerators.*;
import beat.nvbgenerators.*;
import beat.utilities.ModuleTags;
import beat.nvbgenerators.*;
import beat.nvbgenerators.*;
import beat.utilities.PPrintTracer;
import beat.filter.ActionBeatFilter;
import beat.utilities.NVBTypes;
import beat.utilities.XMLWrapper;

import java.io.*;
import java.util.*;
import java.net.*;

import org.w3c.dom.*;


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
   
   The output from BEAT is displayed in a form similar to McNeill's (1992)
   gesture annotations.  This form is meant to be read by humans as opposed 
   to other automated processes.

    <h3>Change log:</h3>
    <table border="1">
    <tr><th>Date<th>Who<th>What</tr>
    <tr><td>7/17/01<td>T. Bickmore<td> Created. </tr>
    <tr><td>1/12/02<td>T. Bickmore<td> Added posture shift. </tr>
    </table>
**/   
public class McNeillExecutive {
    
    //public static final String POSTAGGER_HOST = "aragog.media.mit.edu";
	public static final String POSTAGGER_HOST = "localhost";
    public static final int    POSTAGGER_PORT = 9090;
    
    public static final String XMLDIR="XMLData/";
    
    //Can pass in a text file, or be prompted for text..
    public static void main(String args[]) throws Exception {
	
	String myHost = (InetAddress.getLocalHost()).getHostName();
	System.out.println("BEAT is starting up on "+myHost+"...");
	
	//Load DBs:
	NVBTypes nvbTypes=new NVBTypes(new File(XMLDIR+"NVBTypes.xml"));
	ModuleTags moduleTags=new ModuleTags(new File(XMLDIR+"ModuleTags.xml"));
	KnowledgeBase KB = new KnowledgeBase(XMLDIR+"database.xml");
	
	System.out.println("--- Building BEAT pipeline ---");
	
	McNeillCompiler compiler=new McNeillCompiler(System.out);
	compiler.setDEBUG(true);
	//Does *not* use a scheduler (no speech timing information needed)
	
	//Build filters...
	NVBFilterModule filter=new NVBFilterModule(nvbTypes,compiler);
	filter.setOutputTracer(new PPrintTracer("OUTPUT FROM NVB FILTER MODULE"));
	filter.register(new NVBConflictFilter());
	filter.register(new ActionBeatFilter());
	filter.register(new ContrastBeatFilter());
	filter.register(new DeicticBeatFilter());
	
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
	POSTagger postagger = new ConexorPOSTagger(POSTAGGER_HOST,POSTAGGER_PORT);
	LanguageModule tagger=new LanguageModule(KB,postagger,generator);
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
	    for(int i=0;i<utterances.size();i++) {
		System.out.println("\n=========== Processing Utterance "+(i+1)+" ===========\n");
		tagger.process(((Element)utterances.elementAt(i)).toString());
		System.out.println("\nHIT ENTER TO PROCESS NEXT UTTERANCE..."); key = stdin.readLine();
	    }
	} else { // no file name, using stdin
	    System.out.println("\nType your an input utterance at the prompt ('.' by itself quits)\n");
	    while(true) {
		System.out.print("\nInput> ");
		input = stdin.readLine();
		if(input.equals(".")) break;
		tagger.process("<UTTERANCE>"+input+"</UTTERANCE>");
	    }
	}			
	System.exit(42);
    }		
}

