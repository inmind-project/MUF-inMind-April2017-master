/* -------------------------------------------------------------------------

   PantomineSparkCompilter.java
     - A compiler for Pantomine in the SPARK chat room 

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

import beat.speechTiming.TimingSource;
import beat.utilities.FlattenTreeModule;


/** Takes array of abstract animation events (output from FlattenTreeModule) 
    and returns a string
    representation of the animation commands required to execute the script
    in the Pantomime animation system using pre-computed, speech-relative timing
    information. Uses Ivan's smoothed hand and arm animation methods, which
    requires scheduling all hand arm motions at the start of the script, followed
    by timed specification of all other nonverbal behaviors. 
    <p>
    Establishes a network connection with PantomimeServer upon construction,
    and sends animation scripts to Pantomime directly as soon as they are
    compiled.
    <p>
    See FlattenTreeModule or Compiler for the format of the input tree.
    <h3>Change log:</h3>
    <table border="1">
    <tr><th>Date<th>Who<th>What</tr>
    <tr><td>4/1/01<td>T. Bickmore<td> Created. </tr>
    <tr><td>6/8/01<td>T. Bickmore<td>Modified to use output from FlattenTreeModule.</tr>
    <tr><td>1/9/02<td>H. Vilhjalmsson<td>Now uses "acttime" for gesture scheduling.</tr>
    <tr><td>1/9/02<td>H. Vilhjalmsson<td>Handles DEICTIC gestures with "schedulerpoint"</tr>
    <tr><td>1/11/02<td>T. Bickmore<td>Added posture shifts.</tr>
    <tr><td>3/20/02<td>H. Vilhjalmsson<td>Gesture shapes for the SPARK system</tr>
    </table>
*/

public class PantomimeSparkCompiler extends Compiler {
    /** Time in seconds between animator issuing speech command and sound starting. */
    private  double TTS_DELAY=0.3; //was 0.1: increase if sound is late
    
    /** Minimum time between start of script and first gesture peak (else get discontinuities). */
    private static final double MIN_GESTURE_OFFSET=0.1;

    /** The output stream to PantomimeServer. */
    private OutputStream out;
    /** The output socket to PantomimeServer. */
    private Socket socket;
    
    /** In order to change certain defaults in Ivan's smoothed drivers, the drivers
	need to be stopped and restarted. This flag indicates that the defaults
	have been changed from PantomimeCompiler's assumed values so that it can
	issue commands to reset them. */
    private boolean schedulerDefaultsChanged=false;

    /** Keeps track of character's posture. Indexed by character (speaker) name. */
    private Hashtable postureControls=new Hashtable();

    /** Constructor takes the host and port that PantomimeServer is running on. */
    public PantomimeSparkCompiler(String host,int port) throws Exception {
	super();
	System.out.println("[PantomimeCompiler] Connecting to Pantomime at "+host+":"+port+"...");
	try {
	    socket = new Socket(host,port); 
	    out = socket.getOutputStream();
	    System.out.println("[PantomimeCompiler] Connected to Pantomime.");
	} catch(Exception e1) {
	    System.err.println("[PantomimeCompiler] Couldn't connect to Pantomime at "+host+"! Trying localhost..."); 
	    host = "localhost";
	    try {
		socket = new Socket(host,port); 
		out = socket.getOutputStream();
		System.out.println("[PantomimeCompiler] Connected to Pantomime.");
	    } catch(Exception e2) {
		System.err.println("[PantomimeCompiler] Couldn't connect to localhost either.  Using text output only!"); 
		out = null;
	    }
	}
    }
    
    /** Sets the delay time between Pantomime issuing a SPEAK command and 
	actual production of audio. */
    public void setTTSDELAY(double delay) {
	TTS_DELAY=delay;
    }
    
    /** Indicates that the arm & hand interpolator approach time defaults have 
	been changed. */
    public void schedulerDefaultsChanged() {
	schedulerDefaultsChanged=true;
    }
    
     /** Specifies the posture shift state machines for a character (if desired). */
  public void defineCharacter(String speaker,
			      int[][][] upperTransitions,String[][] upperShapes,String[] upperNames,
			      int[][][] lowerTransitions,String[] lowerShapes) {
    PostureControl posture=new PostureControl(speaker);
    posture.setPostures(upperTransitions,upperShapes,upperNames,lowerTransitions,lowerShapes);
    postureControls.put(speaker,posture);
  }

    /** Does the work of the module. */
    public String compile(Document xml) throws Exception {
	if(DEBUG) System.out.println("PantomimeCompiler running...");
	
	double speechStartTime=0.0;
	double speechEndTime=0.0;
	double speechCommandTime=0.0;
	double firstGestureTime=Double.MAX_VALUE;
	boolean haveRGestures=false;
	boolean haveLGestures=false;
	double lastVisemeTime=0.0;
	String speechID="";

	//Gather the animation instructions into a list...
	Element script=getScript(xml);
	String speaker=script.getAttribute("SPEAKER");
	String hearer=script.getAttribute("HEARER");

	//if there is no existing posture control for the speaker, create one..
	PostureControl posture=(PostureControl)postureControls.get(speaker);
	if(posture==null) {
	  posture=new PostureControl(speaker);
	  postureControls.put(speaker,posture);
	};

	for(int i=0;i<getNumberSteps(xml);i++) { //collect some info...
	    Element step=getStep(xml,i);
	    String action=step.getAttribute(FlattenTreeModule.ACTION);
	    double srTime=getSRTime(step);
	    boolean actionUsesRArm=usesRArm(step);
	    boolean actionUsesLArm=usesLArm(step);
	    if(isSTART(step) && (actionUsesRArm||actionUsesLArm)) {
		if(!haveLGestures && !haveRGestures) 
		    firstGestureTime=srTime;
		if(actionUsesRArm)
		    haveRGestures=true;
		if(actionUsesLArm)
		    haveLGestures=true;
	    }else if(action.equals("VISEME")) {
		lastVisemeTime=srTime;
	    }else if(action.equals("SPEAK")) {
		speechID=step.getAttribute(FlattenTreeModule.ACTION_ID);
	    };
	};

	//First compute time offsets relative to speech.
	//1. if time of first gesture is less than  MIN_GESTURE_OFFSET, then offset...
	if((haveRGestures || haveLGestures) && firstGestureTime<MIN_GESTURE_OFFSET) 
	    speechStartTime=MIN_GESTURE_OFFSET-firstGestureTime;
	//2. Now compute time speech command must be issued...
	speechCommandTime=speechStartTime-TTS_DELAY;
	//3. If the above is negative, offset everything to make zero..
	if(speechCommandTime<0) {
	    speechStartTime-=speechCommandTime;
	    speechCommandTime=0;
	};
	
	//4. Adjust event times to correspond to new speech start time.
	if(speechStartTime!=0.0) {
	    firstGestureTime+=speechStartTime;
	    if(DEBUG) System.out.println("Shifting all NVB times by "+speechStartTime);
	    for(int i=0;i<getNumberSteps(xml);i++) {
		Element step=getStep(xml,i);
		step.setAttribute("SRT",""+(getSRTime(step)+speechStartTime));
	    };
	    lastVisemeTime+=speechStartTime;
	};
	
	//5. Finally, reset speech time to zero...
	if(speechID.length()==0) throw new Exception("Did not find SPEECH!");
	changeTime(xml,speechID,0,speechCommandTime,WHICH_START); //word index not used if time provided..
	
	//Ready to assemble output...
	StringBuffer out=initializeCommand(speaker);
	
	outputScript(0.0,"schedulelarm  :msg \"activate\"",out);
	outputScript(0.0,"schedulelhand :msg \"activate\"",out);
	outputScript(0.0,"schedulerarm  :msg \"activate\"",out);
	outputScript(0.0,"schedulerhand :msg \"activate\"",out);

	//First output all of the gesture commands immediately...
	if(haveLGestures || haveRGestures) {
	    for(int i=0;i<getNumberSteps(xml);i++) {
		Element step=getStep(xml,i);
		String action=step.getAttribute(FlattenTreeModule.ACTION);
		double srTime=getSRTime(step);
		int wordIndex=getWordIndex(step);
		boolean actionUsesRArm=usesRArm(step);
		boolean actionUsesLArm=usesLArm(step);
		if(actionUsesRArm||actionUsesLArm) {
		  if(isSTART(step)) {
		    if(action.equals("POSTURESHIFT")) 
		      //do upper body posture shift...
		      posture.changeUpperBody(srTime,posture.mapEnergy(step.getAttribute("ENERGY")),out);
		    else //start a gesture
		      mapGesture(action,step,true,posture,srTime,out);
		  } else if(!action.equals("POSTURESHIFT")) {
		    //END GESTURE
		      mapGesture(action,step,false,posture,srTime,out);
		  };
		};
	    };
	};
	//All gestures output, now just do everything else in order...
	int lastViseme=-1;
	for(int i=0;i<getNumberSteps(xml);i++) {
	    Element step=getStep(xml,i);
	    boolean isSTART=isSTART(step);
	    String action=step.getAttribute(FlattenTreeModule.ACTION);
	    double srTime=getSRTime(step);
	    int wordIndex=getWordIndex(step);
	    
	    if(action.equals("SPEAK")) {
		outputScript(speechCommandTime,"speak :id 1 :text \" "+step.getAttribute("SPEECH")+"\" ",out);	
	    } else if(action.equals("EYEBROWS")) {
		if(isSTART)
		    outputScript(srTime,"eyebrows :amount 0.6 :max 0.03 :approach 0.1 :hold -1.0",out); 
		else
		    outputScript(srTime,"eyebrows :amount 0.0 :max 0.03 :approach 0.1 :hold -1.0",out); 
	    } else if(action.equals("HEADNOD")) {
		if(isSTART)
		    outputScript(srTime,"headnod :amt 0.1 :duration 0.3",out);
	    } else if(action.equals("VISEME")) {
		String visemeString=step.getAttribute("TYPE");
		int visemeIndex=TimingSource.visemeCode(visemeString);
		if(visemeIndex!=lastViseme) {
		    outputScript(srTime,
				 "lipshape :viseme \""+visemeString+"\"",out);
		    lastViseme=visemeIndex;
		};
		speechEndTime=srTime;
	    } else if(action.equals("GAZE") && isSTART) {
		if(step.getAttribute("DIRECTION").equals("AWAY_FROM_HEARER")) {
		    if(step.getAttribute("FOCUS")!=null)
			outputScript(srTime,"eyetrack :target \""+step.getAttribute("FOCUS")+"\"",out);	    
		    else
			outputScript(srTime,"saccade :dXRot -0.1 :dYRot -0.4",out);
		} else if(step.getAttribute("DIRECTION").equals("TOWARDS_HEARER"))
		    outputScript(srTime,"eyetrack :target \""+hearer+"\"",out);
	    } else if(action.equals("POSTURESHIFT") && isSTART) {
	      String part=step.getAttribute("BODYPART");
System.out.println("Compiling PS: part="+part);
	      if(part.equals("LOWER")||part.equals("BOTH")) 
		posture.changeLowerBody(srTime,posture.mapEnergy(step.getAttribute("ENERGY")),out);
	    };
	};	
	
	//Shut yer mouth
	outputScript(lastVisemeTime+0.1,"lipshape :viseme \""+
		     TimingSource.visemeString(TimingSource.VISEME_CLOSED)+"\"",out);

	outputScript(lastVisemeTime+2.0,"schedulelarm  :msg \"stop\"",out);
	outputScript(lastVisemeTime+2.0,"schedulelhand :msg \"stop\"",out);
	outputScript(lastVisemeTime+2.0,"schedulerarm  :msg \"stop\"",out);
	outputScript(lastVisemeTime+2.0,"schedulerhand :msg \"stop\"",out);

	//all done...
	String cmd=finishCommand(out);
	send(cmd);
	return cmd;
    }

  private StringBuffer initializeCommand(String speaker) {
    return new StringBuffer("(tell :recipient \""+speaker+"\" :content (script :content [\n");
  }

  private String finishCommand(StringBuffer out) {
    out.append("]))");
    return out.toString();    
  }

  private boolean usesRArm(Element step) {
    String action=step.getAttribute(FlattenTreeModule.ACTION);
    String part=step.getAttribute("BODYPART");
    return action.equals("GESTURE_RIGHT") ||
      action.equals("GESTURE_BOTH") ||
      (action.equals("POSTURESHIFT") && 
       (part.equals("UPPER")||part.equals("BOTH")));
  }
  private boolean usesLArm(Element step) {
    String action=step.getAttribute(FlattenTreeModule.ACTION);
    String part=step.getAttribute("BODYPART");
    return action.equals("GESTURE_LEFT") ||
      action.equals("GESTURE_BOTH") ||
      (action.equals("POSTURESHIFT") && 
       (part.equals("UPPER")||part.equals("BOTH")));
  }
  private final String[] READY_SHAPES={"arm_ready0","arm_ready1"};

    //<tag TYPE=type [RIGHT_HANDSHAPE=handshape] [RIGHT_TRAJECTORY=traj] ...>
    //tag ::= GESTURE_RIGHT | GESTURE_LEFT | GESTURE_BOTH
    //type ::= BEAT | ICONIC | METAPHORIC | READY | CONTRAST_1 | CONTRAST_2 | DEICTIC | RELAX
    private void mapGesture(String tag,Element source,boolean isStart,PostureControl posture,
			    double peakTime, StringBuffer out) {
	String handshape;
	String trajectory;
	String handChar;
	String otherHandChar;
	if(tag.equals("GESTURE_BOTH")) {
	    mapGesture("GESTURE_RIGHT",source,isStart,posture,peakTime,out);
	    mapGesture("GESTURE_LEFT",source,isStart,posture,peakTime,out);
	    return;
	};
	if(tag.equals("GESTURE_RIGHT")) {
	    handChar="r";
	    otherHandChar="l";
	    handshape=source.getAttribute("RIGHT_HANDSHAPE");
	    trajectory=source.getAttribute("RIGHT_TRAJECTORY");
	} else {
	    handChar="l";
	    otherHandChar="r";
	    handshape=source.getAttribute("LEFT_HANDSHAPE");
	    trajectory=source.getAttribute("LEFT_TRAJECTORY");
	};
	String time=formatTime(peakTime);
	String type=source.getAttribute("TYPE");
	if(handshape==null)
	    handshape="HAND_OPEN";
	else
	    handshape=handshape.toUpperCase();
	if(trajectory==null)
	    trajectory="BEAT";
	else
	    trajectory=trajectory.toUpperCase();
	if(type.equals("BEAT")) {
	    if(!isStart) return;
	    outputScript(0.0,"schedule"+handChar+"arm  :acttime "+time+" :beat \"beat_arm\"",out);
	    outputScript(0.0,"schedule"+handChar+"hand :acttime "+time+" :beat \"beat_hand\"",out);
	} else if(type.equals("READY")) {
	    if(!isStart) return;
	    String readyShape=READY_SHAPES[pick(READY_SHAPES.length)];
	    String readyHandShape="hand_square";
	    outputScript(0.0,"schedule"+handChar+"arm  :acttime "+time+" :shape \""+readyShape+"\"",out);
	    outputScript(0.0,"schedule"+handChar+"hand :acttime "+time+" :shape \""+readyHandShape+"\"",out);
	} else if(type.equals("ICONIC") || type.equals("METAPHORIC") || 
		  type.equals("CONTRAST_1") || type.equals("CONTRAST_2")) {
	  if(isStart || armTrajectoryIsShape(trajectory)) 
	    outputScript(0.0,"schedule"+handChar+"arm  :acttime "+time+" "+mapArmTrajectory(trajectory),out);
	  if(isStart || handTrajectoryIsShape(handshape))
	    outputScript(0.0,"schedule"+handChar+"hand :acttime "+time+" "+mapHandShape(handshape),out);         
	  //Special case hack...just for CONTRAST: re-issue contrast shape for first hand:
	  if(type.equals("CONTRAST_2") && isStart) {
	    outputScript(0.0,"schedule"+otherHandChar+"arm  :acttime "+time+" "+mapArmTrajectory("CONTRAST_FINAL_SHAPE"),out);
	    outputScript(0.0,"schedule"+otherHandChar+"hand :acttime "+time+" "+mapHandShape("CONTRAST_FINAL_SHAPE"),out); 
	  };
	  /*
	} else if(type.equals("CONTRAST_2")) {
	    outputScript(0.0,"schedule"+handChar+"arm  :acttime "+time+" "+mapArmTrajectory(trajectory),out);
	    outputScript(0.0,"schedule"+handChar+"hand :acttime "+time+" "+mapHandShape(handshape),out);            
	    //re-issue contrast shape for first hand..
	   outputScript(0.0,"schedule"+otherHandChar+"arm  :acttime "+time+" "+mapArmTrajectory("CONTRAST_FINAL_SHAPE"),out);
	   outputScript(0.0,"schedule"+otherHandChar+"hand :acttime "+time+" "+mapHandShape("CONTRAST_FINAL_SHAPE"),out); 
	   */
	} else if(type.equals("DEICTIC")) {
            // For both START and END!
	    if(!isStart) return;  // NO! Should only point *once*!  -hhv
	    outputScript(0.0,"schedulerpoint :acttime "+time+" :object \""+source.getAttribute("TARGET")+"\" :extend 0.96",out);
	} else if(type.equals("RELAX")) {
	  if(!isStart) return;
	  if(tag.equals("GESTURE_RIGHT"))
	    relaxRight(posture,peakTime,out);
	  else
	    relaxLeft(posture,peakTime,out);
	};
    }
        
    /** Issues commands to relax the left hand and arm. 
	Commands are accumulated into the specified StringBuffer. */
    private void relaxLeft(PostureControl posture,double srTime,StringBuffer out) {
	outputScript(0.0,"schedulelarm  :acttime "+formatTime(srTime)+" :shape \""+posture.getLArmRelax()+"\"",out);
	outputScript(0.0,"schedulelhand :acttime "+formatTime(srTime)+" :shape \""+posture.getLHandRelax()+"\"",out);
    }
    
    /** Issues command sto relax the right hand and arm. 
	Commands are accumulated into the specified StringBuffer. */
    private void relaxRight(PostureControl posture,double srTime,StringBuffer out) {
	outputScript(0.0,"schedulerarm  :acttime "+formatTime(srTime)+" :shape \""+posture.getRArmRelax()+"\"",out);
	outputScript(0.0,"schedulerhand :acttime "+formatTime(srTime)+" :shape \""+posture.getRHandRelax()+"\"",out);
    }
    
    /** Outputs a script command to be executed at the specified time. 
	Commands are accumulated into the specified StringBuffer. */
    private void outputScript(double srTime,String cmd,StringBuffer out) {
	out.append("  (step :starttime "+formatTime(srTime)+" :content ("+cmd+"))\n");
    }

    /** Returns true if the specified arm trajectory is a shape (vs. a motion). */
    private boolean armTrajectoryIsShape(String trajectory) {
      return mapArmTrajectory(trajectory).startsWith(":shape");
    }
    private boolean handTrajectoryIsShape(String trajectory) {
      return mapHandShape(trajectory).startsWith(":shape");
    }

    /** Maps a BEAT name for an arm trajectory into a Pantomime command segment
	to be embedded in a scheduleXarm command. */
    private String mapArmTrajectory(String trajectory) {
	if(trajectory.equals("MOVEUPTRAJECTORY")) 
	    return ":shape \"arm_highup\"";
	else if(trajectory.equals("HALFCIRCLETRAJECTORY")) 
	    return ":motion \"halfcircle\"";
	else if(trajectory.equals("OPENUPTRAJECTORY"))
	    return ":motion \"openup\"";
	else if(trajectory.equals("MOVEFORWARDTRAJECTORY"))
	    return ":motion \"straightforward\"";
	else if(trajectory.equals("OVERTHERETRAJECTORY"))
	    return ":motion \"overthere\"";
	else if(trajectory.equals("COMETRAJECTORY"))
	    return ":motion \"come\"";
	else if(trajectory.equals("TRAVERSETRAJECTORY"))
	    return ":motion \"traverse\"";
	else if(trajectory.equals("STATIONARY_CENTRAL_TRAJECTORY"))
	    return ":motion \"armcentral\"";
	else if(trajectory.equals("MOVETO_LEFT_CENTRAL_TRAJECTORY"))
	    return ":motion \"nearcentral\"";
	else if(trajectory.equals("CENTER_MOVINGIN_TRAJECTORY")) 
	    return ":motion \"centermovingin\"";
	else if(trajectory.equals("MOVETO_RCC_TRAJECTORY")) 
	    return ":shape \"arm_typing\"";
	else if(trajectory.equals("MOVETO_LCC_TRAJECTORY"))
	    return ":shape \"arm_typing\"";
	else if(trajectory.equals("SPIRAL_TRAJECTORY"))
	    return ":motion \"spiral\"";
	else if(trajectory.equals("MOVETO_PCR_TRAJECTORY")) 
	    return ":shape \"arm_jacuzzi\"";
	else if(trajectory.equals("MOVETO_PCL_TRAJECTORY"))
	    return ":shape \"arm_jacuzzi\"";
	else if(trajectory.equals("CONTRAST_TRAJECTORY"))
	    return ":motion \"arm_contrast\"";
	else if(trajectory.equals("CONTRAST_FINAL_SHAPE"))
	    return ":shape \"contrast1\"";
	else if(trajectory.equals("WAVENEAR_TRAJECTORY"))
	    return ":motion \"wave\"";
	else if(trajectory.equals("MOVETO_RIGHTSIDE_TRAJECTORY"))
	    return ":shape \"arm_jacuzzipoint\"";
	else if(trajectory.equals("VIRTUAL"))
	    return ":motion \"arm_virtual\"";
	//return ":shape \"arm_forward_0\"";
	else if(trajectory.equals("BOX"))
	    return ":motion \"arm_box\"";
	else if(trajectory.equals("SQUEEZE"))
	    return ":motion \"arm_box\"";
	else if(trajectory.equals("BECKON"))
	    return ":shape \"arm_beckon\"";
	else if(trajectory.equals("MOTION"))
	    return ":motion \"arm_motion\"";
	else if(trajectory.equals("HERE"))
	    return ":shape \"arm_point_right_back\"";
	else if(trajectory.equals("STATIONARY_PERIPHERALUP_TRAJECTORY"))
	    return ":shape \"arm_point_right_back\"";
	else if(trajectory.equals("PRESENT"))
	    return ":shape \"arm_presents\"";
	else if(trajectory.equals("FLOOR"))
	    return ":motion \"arm_floor\"";
	else if(trajectory.equals("LEFT_POINT_TRAJECTORY"))
	    return ":shape \"arm_chimenypoint\"";
	else if(trajectory.equals("POINT_DOWN_TRAJECTORY"))
	    return ":shape \"arm_relax\"";
	else //default -> beat
	    return ":beat \"beat_arm\"";
    }
    
    /** Maps a BEAT specification of a hand shape into a command segment
	to be embedded in a scheduleXhand command. */
    private String mapHandShape(String shape) {
	if(shape.equals("BENDHANDSHAPE")) 
	    return ":shape \"hand_bend\"";
	else if(shape.equals("HAND_SPREAD"))
	    return ":shape \"spread\"";
	else if(shape.equals("HAND_OPEN"))
	    return ":shape \"hand_open\"";
	else if(shape.equals("ARCHANDSHAPE"))
	    return  ":motion \"arc\" :repeat 1";
	else if(shape.equals("WALKINGHANDSHAPE"))
	    return ":motion \"walking\" :repeat 5"; //should be while arm moving
	else if(shape.equals("VERTICAL_SQUARE_HANDSHAPE"))
	    return ":shape \"hand_square\"";
	else if(shape.equals("VERTICAL_OPEN_HANDSHAPE"))
	    return ":motion \"wristtwist\" :repeat 1";
	else if(shape.equals("POINTERHANDSHAPE"))
	    return ":shape \"hand_point\"";
	else if(shape.equals("TYPING_HANDSHAPE"))
	    return ":motion \"typing\" :repeat 4"; //shoudl be while arm moving
	else if(shape.equals("SPIRAL_HANDSHAPE"))
	    return ":shape \"hand_point\"";
	else if(shape.equals("BUBBLING_HANDSHAPE"))
      return ":motion \"bubble\" :repeat 5"; //should be while arm moving
	else if(shape.equals("CONTRAST"))
	    return ":motion \"contrast\"";
	else if(shape.equals("CONTRAST_FINAL_SHAPE"))
	    return ":shape \"hand_contrast1\"";
	else if(shape.equals("VIRTUAL"))
	    return ":motion \"hand_virtual\" :repeat 2"; //should be while arm moving
	else if(shape.equals("BOX"))
	    return ":motion \"hand_box\"";
	else if(shape.equals("SQUEEZE"))
	    return ":motion \"hand_box\"";
	else if(shape.equals("BECKON"))
	    return ":motion \"beckon\"";
	else if(shape.equals("MOTION"))
	    return ":motion \"hand_motion\"";
	else if(shape.equals("SPREAD"))
	    return ":shape \"hand_spread\"";
	else if(shape.equals("HERE"))
	    return ":shape \"hand_point_right_back\"";
	else if(shape.equals("PRESENT"))
	    return ":shape \"hand_presents\"";
	else if(shape.equals("FLOOR"))
	    return ":motion \"hand_floor\"";
	else //default -> beat
	    return ":beat \"beat_hand\"";
    }
    
    
    /** Sends a complete PantomimeServer KQML command to PantomimeServer
	over the open socket. */
    public void send(String cmd) throws Exception {
	if(DEBUG) System.out.println("\n  Sending command:\n"+cmd);
	if(out!=null) {  
	    cmd=cmd.replace('\n',' ');
	    String KQML="(tell :recipient AM :content "+cmd+")";
	    String msg=(KQML.length()+1)+"\n"+KQML+"\n ";
	    byte[] bytes=msg.getBytes();
	    //if(DEBUG) System.out.println("\n  Command to animator:\n"+msg);
	    out.write(bytes);
	    out.flush();
	} else
	    System.out.println("\nPantomime not ready.  Command not executed.");
    }

  /* ------------------------- POSTURE SHIFT STUFF ----------------------------------- */
  /* Currently ignoring issues of PS duration, and issues of whether PS occurs completely
   pre-speech or not...simply scheduled along with other NVB at start of utterance. */

  public class PostureControl {
    private String speaker;

    /** Create a posture control object for a named character. */
    public PostureControl(String speaker) { this.speaker=speaker; }

    /** Set the posture control state machines for a character (override defaults).
        <ul>
	<li> upperTransitions ::= upper body state transition table, 
	      [state][energy:low..hi][alternative] -> new state
	<li> upperShapes ::= the names of hand arm shapes for each upper body shape,
	        (right hand, right arm, left hand, left arm).
	<li> upperNames ::= list of names of each upper body state, used for debugging printouts.
	<li> lowerTransitions ::= lower body state transition table,
	      [state][energy:low..hi][alternative] -> new state
        <li> lowerShapes ::= list of lowerbodyshapes for each lower body state.
	</ul>
	*/
    public void setPostures(int[][][] upperTransitions,String[][] upperShapes,String[] upperNames,
			    int[][][] lowerTransitions,String[] lowerShapes) {
      this.upperTransitions=upperTransitions;
      this.UPS_NAMES=upperShapes;
      this.UPS_STATE_NAMES=upperNames;
      this.lowerTransitions=lowerTransitions;
      this.LPS_NAMES=lowerShapes;
    }

    //enums...
    public static final int UPPER_BODY=0;
    public static final int LOWER_BODY=1;
    public static final int BOTH_BODY=2;
    public static final int LOW_ENERGY=0;
    public static final int HIGH_ENERGY=1;

    private String lHandRelax="hand_mic";
    private String lArmRelax="arm_mic";
    private String rHandRelax="hand_relax";
    private String rArmRelax="arm_relax";
    
    //internals
    private static final int NOOP=-1;
    private int upperBodyState=UPS_HANDS_AT_SIDE;
    //values....
    private static final int UPS_HANDS_AT_SIDE=0;
    private static final int UPS_HANDS_ON_HIPS=1;
    private static final int UPS_DROP_HAND=2;
    private static final int UPS_HANDS_CROSSED=3;
    //indices into UPS_NAMES: 
    private static final int UPS_RHAND=0;
    private static final int UPS_RARM=1;
    private static final int UPS_LHAND=2;
    private static final int UPS_LARM=3;
    //For debugging only...
    private String[] UPS_STATE_NAMES={"hands_at_side","ups_hands_on_hips","ups_drop_hand","ups_hands_crossed"};
    //actual names of upperbody shapes:
    private String[][] UPS_NAMES={
	//{"hand_ps_at_side","arm_ps_at_side","hand_ps_at_side","arm_ps_at_side"}, //"hands_at_side",
	{"hand_flat","arm_ps_on_table_side","hand_flat","arm_ps_on_table_side"},
      {"hand_ps_on_hips","arm_ps_on_hips","hand_ps_on_hips","arm_ps_on_hips"}, //"hands_on_hips",
      {"hand_flat", "arm_ps_on_table_side","hand_ps_on_hips","arm_ps_on_hips"}, //"drop_hand",
      {"hand_ps_hands_crossed","arm_ps_hands_crossed","hand_ps_hands_crossed","arm_ps_hands_crossed"} //"hands_crossed"
    };
    //transition table: [state][energy:low..hi][alternative]->new state
    private  int upperTransitions[][][]={
      {{UPS_DROP_HAND,NOOP},{UPS_HANDS_ON_HIPS,UPS_HANDS_CROSSED}}, //at side
      {{UPS_DROP_HAND,UPS_HANDS_CROSSED},{UPS_HANDS_AT_SIDE,NOOP}},              //on hips
      {{UPS_HANDS_ON_HIPS,UPS_HANDS_AT_SIDE},{UPS_HANDS_CROSSED,NOOP}}, //drop hand
      {{UPS_DROP_HAND,UPS_HANDS_ON_HIPS},{UPS_HANDS_AT_SIDE,NOOP}}  //hands crossed
    };

    public String getRHandRelax() { return UPS_NAMES[upperBodyState][UPS_RHAND]; }
    public String getRArmRelax() {  return UPS_NAMES[upperBodyState][UPS_RARM]; }
    public String getLHandRelax() { return UPS_NAMES[upperBodyState][UPS_LHAND]; }
    public String getLArmRelax() {  return UPS_NAMES[upperBodyState][UPS_LARM]; }

    private final int LPS_RELAX=0;
    private final int LPS_WEIGHT_LEFT=1;
    private final int LPS_WEIGHT_RIGHT=2;
    private final int LPS_CROSS_LEFT=3;
    private final int LPS_CROSS_RIGHT=4;
    private int lowerBodyState=LPS_WEIGHT_RIGHT;
    //actual names of lowerBodyShapes: 
    private String[] LPS_NAMES={"lps_relax","lps_weight_left","lps_weight_right",
					     "lps_legs_crossed_weight_left","lps_legs_crossed_weight_right"};
    //transition table: [state][energy:low..hi][alternative]->new state
    //currently not using relax, since transitions to it are too subtle to see
    private int lowerTransitions[][][]={
      {{LPS_WEIGHT_LEFT,LPS_WEIGHT_RIGHT},{LPS_CROSS_LEFT,LPS_CROSS_RIGHT}}, //relax
      {{LPS_WEIGHT_RIGHT,NOOP},{LPS_CROSS_LEFT,LPS_CROSS_RIGHT}}, //weight_left
      {{LPS_WEIGHT_LEFT,NOOP},{LPS_CROSS_LEFT,LPS_CROSS_RIGHT}}, //weight_right
      {{LPS_CROSS_RIGHT,NOOP},{LPS_WEIGHT_RIGHT,NOOP}}, //cross_left
      {{LPS_CROSS_LEFT,NOOP},{LPS_WEIGHT_LEFT,NOOP}} //cross_right
    };

    private int getNextState(int[][][] transitions,int currentState,int energy) {
      int[] candidates=transitions[currentState][energy];
      int numCandidates=0;
      for(int i=0;i<candidates.length;i++)
	if(candidates[i]!=NOOP) numCandidates++;
      if(numCandidates<=1) return candidates[0];
      return candidates[pick(numCandidates)];
    }

    /** Command change in posture. Returns the commands required to immediately
        effect the change. */
    public void changeUpperBody(double srTime,int energy,StringBuffer out) {
      upperBodyState=getNextState(upperTransitions,upperBodyState,energy);
      if(DEBUG) System.out.println("Posture shift for "+speaker+": UPPER "+UPS_STATE_NAMES[upperBodyState]);
      //Currently ignoring duration issues:  schedulerDefaultsChanged();
      relaxLeft(this,srTime,out);
      relaxRight(this,srTime,out);
    }

    public void changeLowerBody(double srTime,int energy,StringBuffer out) {
      lowerBodyState=getNextState(lowerTransitions,lowerBodyState,energy);
      if(DEBUG) System.out.println("Posture shift for "+speaker+": LOWER "+LPS_NAMES[lowerBodyState]);
      issueLowerPostureShiftCmd(srTime,LPS_NAMES[lowerBodyState],out);
    }

    private void issueLowerPostureShiftCmd(double srTime,String name,StringBuffer out) {
      outputScript(srTime,"lowerbodyshape :shape \""+name+"\" :duration 0.0",out);
    }

    public int mapEnergy(String energy) {
      if(energy.equals("LOW")) 
        return LOW_ENERGY;
      else
        return HIGH_ENERGY;
    }

  }

    public int pick(int numItems) { //Given N, this returns 0..N-1 with equal distribution. 
      double P=Math.random(); 
      for(int i=0;i<numItems;i++) 
	if(P<(double)(i+1)/(double)numItems) 
	  return i; 
      return numItems-1; 
    } 


}




