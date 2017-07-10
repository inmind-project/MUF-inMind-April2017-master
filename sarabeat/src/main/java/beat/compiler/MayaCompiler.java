/* -------------------------------------------------------------------------

   MayaCompilter.java
     - A compiler for Maya in the BEAT gesture toolkit

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
package beat.compiler;

import java.io.*;
import java.lang.*;
import java.net.*;
import java.util.*;

import org.w3c.dom.*;

import beat.utilities.FlattenTreeModule;


/** Takes array of abstract animation events (output from FlattenTreeModule) 
    and returns a string representation of the animation commands required to 
    execute the script in the Maya animation system using pre-computed, 
    speech-relative timing information. 
    <p>
    Establishes a network connection with Maya server upon construction,
    and sends animation scripts to Maya directly as soon as they are
    compiled.
    <p>
    See FlattenTreeModule or Compiler for the format of the input tree.
    <h3>Change log:</h3>
    <table border="1">
    <tr><th>Date<th>Who<th>What</tr>
    <tr><td>7/30/01<td>Y. Gao<td> Created. </tr>
    </table>
*/

public class MayaCompiler extends Compiler {

    String scheduler = "";
    private OutputStream out;
    private String nvb;
    private Socket socket;
    private int leftBeatPrep = 16;
    private int rightBeatPrep = 16;
    private int visemeSeparation = 2;
    private int eyebrowSeparation = 5;

    public MayaCompiler(String host, int port) throws Exception{
	
	super();
	System.out.println("[MayaCompiler] Connecting to Maya at "+host+":"+port+"...");
	try {
	    socket = new Socket(host,port); 
	    out = socket.getOutputStream();
	    System.out.println("[MayaCompiler] Connected to Maya.");
	} catch(Exception e) {
	    System.err.println("[MayaCompiler] Couldn't connect to Maya!"); 
	    out = null;
	}
    }
    
    public String compile(Document xml) throws Exception {

	// TODO: place primers in front of first instance of each arm gesture (not fixed like here) -hhv

	StringBuffer out = new StringBuffer(
					     "character -q -sc \"leftarm\";\n"
					    + "character -q -sc \"rightarm\";\n"
					    + "character -q -sc \"eyebrows\";\n"
					    + "character -q -sc \"head\";\n"
					    + "character -q -sc \"viseme\";\n"
					    + "clipSchedule -instance VISEME_ISource -start 1 \"mouthScheduler1\";\n"
					     + "clipSchedule -instance leftarmprimerSource -start 66 \"leftarmScheduler1\";\n"
					     + "clipSchedule -instance rightarmprimerSource -start 1 \"rightarmScheduler1\";\n");
	
	//counters for degrees of freedom
	int leftArmCount = 1;
	int rightArmCount = 1;
	int visemeCount = 1;  //at frame 1 there is a viseme_I
	int eyebrowsCount = 0;
	int headCount = 0;
	int lastVFrame = 1;
	int lastEBFrame = -1;

	for(int i=0;i<getNumberSteps(xml);i++) { //collect some info...
	    Element step = getStep(xml,i);
	    if(step.getTagName().equals("STOP") &&
	       !(step.getAttribute(FlattenTreeModule.ACTION).equals("EYEBROWS"))){
		continue;
	    }
	    String action = step.getAttribute(FlattenTreeModule.ACTION);
	    double srTime=getSRTime(step);
	    int srFrame = convertToFrames(srTime);
	    String type = step.getAttribute("TYPE");

	    if(action.equals("GESTURE_RIGHT")){
		if(type.equals("CONTRAST_1")){
		    type = "BEAT";
		}
		if(type.equals("DEICTIC")) {
		    nvb = action+"_"+type;
		    scheduler = "leftarmScheduler1";		    
		    srFrame = srFrame - 7;
		    outputScript(srFrame, nvb, out, (leftArmCount > 0), scheduler, leftArmCount);
		    leftArmCount++;
		} else {
		    nvb = action+"_"+type;
		    scheduler = "rightarmScheduler1";
		    if(type.equals("BEAT")){
			// use short beats at the beginning, larger towards the end (needs improving) --hhv
			if(rightArmCount<3) {
			    nvb = nvb+"3";
			    srFrame = srFrame - 6;
			//account for prep time
			} else {
			    srFrame = srFrame - rightBeatPrep;
			}
		    }
		    outputScript(srFrame, nvb, out, (rightArmCount > 0), scheduler, rightArmCount);
		    rightArmCount++;
		}

	    }

	    else if(action.equals("GESTURE_LEFT")){
		if(type.equals("CONTRAST_2")){
		    type = "BEAT";
		}
		nvb = action+"_"+type;
		scheduler = "leftarmScheduler1";
		if(type.equals("BEAT")){
		    srFrame = srFrame - leftBeatPrep;
		}
		outputScript(srFrame, nvb, out, (leftArmCount > 0), scheduler, leftArmCount);
		leftArmCount++;
	    }

	    else if(action.equals("EYEBROWS")){
		if(eyebrowsCount%2 == 0){
		    nvb = action+"_UP";
		}
		else{
		    nvb = action+"_DOWN";
		}
		scheduler = "eyebrowsScheduler1";
		if(lastEBFrame == -1){
		}
		else{
		    if(lastEBFrame+eyebrowSeparation > srFrame){
			srFrame = lastEBFrame + eyebrowSeparation;
		    }
		}
		lastEBFrame = srFrame;
		outputScript(srFrame, nvb, out, (eyebrowsCount > 0), scheduler, eyebrowsCount);
		eyebrowsCount++;
	    }

	    else if(action.equals("HEADNOD")){
		nvb = action;
		scheduler = "headScheduler1";
		outputScript(srFrame, nvb, out, (headCount > 0), scheduler, headCount);
		headCount++;
	    }

	    else if(action.equals("GAZE")){
		nvb = action+"_"+step.getAttribute("DIRECTION");
		scheduler = "headScheduler1";
		outputScript(srFrame, nvb, out, (headCount > 0), scheduler, headCount);
		headCount++;
	    }

	    else if(action.equals("VISEME")){
		if((lastVFrame+visemeSeparation) > srFrame){ //hack for separation
		}
		else{
		    lastVFrame = srFrame;
		    nvb = action+"_"+type;
		    scheduler = "mouthScheduler1";
		    outputScript(srFrame, nvb, out, (visemeCount > 0), scheduler, visemeCount);
		    visemeCount++;
		}
	    }
	}//end of for loop	    

	out.append("clipSchedule -instance VISEME_ISource -start "+(lastVFrame+5)+" \"mouthScheduler1\";\nclipSchedule -b "+(visemeCount-1)+" "+visemeCount+" \"mouthScheduler1\";\n"
		   +"sound -file \"G:/temp/festival.wav\" -offset 0 -name speech3;\nsetSoundDisplay speech3 1;\n");
	String script = out.toString();
	StringTokenizer st = new StringTokenizer(script, "\n");
	long lastSend = 0;
	while(st.hasMoreTokens()){
	    System.out.println("Now sending the following line to Maya: ");
	    String temp = st.nextToken();
	    send(temp);
	    lastSend = System.currentTimeMillis();
	    while(System.currentTimeMillis() < (lastSend + 100)){
		//busy wait
	    }
	    System.out.println(temp);
	}
	return script;
    }
    
    public void send(String text) throws Exception{
	if(DEBUG) System.out.println("\n  Sending command:\n"+text);
	if(out!=null) {  
	    byte[] bytes=text.getBytes();
	    out.write(bytes);
	    out.flush();
	} 
	else{
	    System.out.println("\nMaya not ready.  Script not executed.");
	}
    }

    private void outputScript(int srFrame, String nvb,  StringBuffer out, boolean needToBlend, String scheduler, int count){
	out.append("clipSchedule -instance "+nvb+"Source "+" -start "+srFrame+" "+"\""+scheduler+"\";\n");
	if(needToBlend){
	    out.append("clipSchedule -b "+(count-1)+" "+count+" \""+scheduler+"\";\n");
	}
    }

    private int convertToFrames(double time){
	int frameNumber = (int) (24 * time); //assuming 24 frames per second
	if(frameNumber == 0){
	    frameNumber = 1; //frames starts at 1, not zero
	}
	return frameNumber;
    }
}

    







