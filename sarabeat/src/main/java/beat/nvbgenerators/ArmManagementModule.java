/* -------------------------------------------------------------------------

   ArmManagementModule.java
     - Inserts READY and RELAX arm motions when appropriate.

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
package beat.nvbgenerators;

import java.util.*;

import beat.compiler.Compiler;
import beat.utilities.BeatModule;
import beat.utilities.FlattenTreeModule;
import beat.utilities.XMLWrapper;
import org.w3c.dom.*;

/** Operates on Flattened Trees before compilation (see FlattenTreeModule).
    Actually munges the input script by inserting new gesture commands where needed.
    Assumes NVBConflictFilter has already been run, so that there are no overlapping
    uses of arms. Assumes speech-relative times (srTime) is available, with utterance
    starting at time zero. Assumes compiler is target-based, i.e., when a gesture
    is commanded at a specific time, that time is either the peak time of the gesture
    or the time at which the gesture end-shape should be achieved.
    <h3>Change log:</h3>
    <table border="1">
    <tr><th>Date<th>Who<th>What</tr>
    <tr><td>1/14/02<td>T. Bickmore<td>Created.</tr>
    </table>
*/

public class ArmManagementModule extends BeatModule {
  public ArmManagementModule(BeatModule outputTo) {
    super(outputTo);
  }

  /** Time to move arm into ready position for a beat. */
  private static final double BEAT_READY_TIME=0.25;

  /** Time to relax arms. */
  private static final double TIME_TO_RELAX=1.0;

  private int stepCount;
  private static final int INITIAL_STEP=1000;
  private String getID() { return "A"+(++stepCount); }

  private static String[] GESTURE_ATTRIBUTES=new String[]{"TYPE","PRIORITY"};
  private static String[] READY_VALUES=new String[]{"READY","0"};
  private static String[] RELAX_VALUES=new String[]{"RELAX","0"};

    /** Does all the work of the module, an XML transducer. */
  public Document transduce(Document xml) throws Exception {
    if(DEBUG) System.out.println("ArmManagementModule running...");

    //First collect information on the time intervals that arms are in use...
    stepCount=INITIAL_STEP;
    Element script= Compiler.getScript(xml);
    Vector rArmUses=new Vector();
    Vector lArmUses=new Vector();
    Vector bothArmUses=new Vector(); //just whether BOTH arms are NOT_USEDED, or POSTURESHIFT or either one is in gesture.
    double lastEventTime=0.0;
    //assume start at zero
    rArmUses.addElement(new ArmUse(0.0,ArmUse.NOT_USED));
    lArmUses.addElement(new ArmUse(0.0,ArmUse.NOT_USED));
    bothArmUses.addElement(new ArmUse(0.0,ArmUse.NOT_USED));
    step: for(int i=0;i<Compiler.getNumberSteps(xml);i++) { //collect some info...
      Element step=Compiler.getStep(xml,i);
      String action=step.getAttribute(FlattenTreeModule.ACTION);
      boolean isSTART=Compiler.isSTART(step);
      String part=step.getAttribute("BODYPART");
      double srTime=Compiler.getSRTime(step);
      lastEventTime=srTime;
      String type=step.getAttribute("TYPE");
      //Major hack: treat CONTRAST_1/CONTRAST_2 pairs as a two-handed gesture.
      if(action.equals("GESTURE_BOTH")||(action.equals("GESTURE_RIGHT")&&type.equals("CONTRAST_1"))){
	if(isSTART) {
	    if(type.equals("BEAT")) {
		addNextEvent(lArmUses,srTime,ArmUse.BEAT);
		addNextEvent(rArmUses,srTime,ArmUse.BEAT);
	    } else { //including iconic, deictic and contrasts...
		addNextEvent(rArmUses,srTime,ArmUse.GESTURE);
		addNextEvent(lArmUses,srTime,ArmUse.GESTURE);
	    };
	    if(getCurrentEvent(bothArmUses).type==ArmUse.NOT_USED) 
		addNextEvent(bothArmUses,srTime,ArmUse.GESTURE);
	} else if(action.equals("GESTURE_RIGHT")&&type.equals("CONTRAST_1")) {
	  //*** HACK ALERT ***
	  //Set the end time to the end of the associated CONTRAST_2.
	  for(int j=i+1;j<Compiler.getNumberSteps(xml);j++) {
	    Element step2=Compiler.getStep(xml,j);
	    String action2=step2.getAttribute(FlattenTreeModule.ACTION);
	    boolean isSTART2=Compiler.isSTART(step2);
	    double srTime2=Compiler.getSRTime(step2);
	    String type2=step2.getAttribute("TYPE");
	    if(action2.equals("GESTURE_LEFT")&&type2.equals("CONTRAST_2")&&!isSTART2) {
	      addNextEvent(lArmUses,srTime2,ArmUse.NOT_USED);
	      addNextEvent(rArmUses,srTime2,ArmUse.NOT_USED);
	      addNextEvent(bothArmUses,srTime2,ArmUse.NOT_USED);
	      continue step;
	    };
	  };
	  System.out.println("\n***DID NOT FIND CONTRAST_2 for CONTRAST_1 at t="+srTime+"***\n");
	  addNextEvent(lArmUses,srTime,ArmUse.NOT_USED);
	  addNextEvent(rArmUses,srTime,ArmUse.NOT_USED);
	  addNextEvent(bothArmUses,srTime,ArmUse.NOT_USED);
	} else { //isEND for two-hand, non-contrast gestures
	  addNextEvent(lArmUses,srTime,ArmUse.NOT_USED);
	  addNextEvent(rArmUses,srTime,ArmUse.NOT_USED);
	  addNextEvent(bothArmUses,srTime,ArmUse.NOT_USED);
	};
      } else if(action.equals("GESTURE_RIGHT")) {
	if(isSTART) {
	  if(type.equals("BEAT"))
	    addNextEvent(rArmUses,srTime,ArmUse.BEAT);
	  else
	    addNextEvent(rArmUses,srTime,ArmUse.GESTURE);
	  if(getCurrentEvent(bothArmUses).type==ArmUse.NOT_USED) 
	    addNextEvent(bothArmUses,srTime,ArmUse.GESTURE);
	} else { //isEND
	  addNextEvent(rArmUses,srTime,ArmUse.NOT_USED);
	  if(getCurrentEvent(lArmUses).type==ArmUse.NOT_USED)
	    addNextEvent(bothArmUses,srTime,ArmUse.NOT_USED);
	};
      } else if(action.equals("GESTURE_LEFT")) {
	if(isSTART) {
	  if(type.equals("BEAT"))
	    addNextEvent(lArmUses,srTime,ArmUse.BEAT);
	  else
	    addNextEvent(lArmUses,srTime,ArmUse.GESTURE);
	  if(getCurrentEvent(bothArmUses).type==ArmUse.NOT_USED) 
	    addNextEvent(bothArmUses,srTime,ArmUse.GESTURE);
	} else { //isEND
	  addNextEvent(lArmUses,srTime,ArmUse.NOT_USED);
	  if(getCurrentEvent(rArmUses).type==ArmUse.NOT_USED)
	    addNextEvent(bothArmUses,srTime,ArmUse.NOT_USED);
	};
      } else if(action.equals("POSTURESHIFT") &&
		(part.equals("UPPER")||part.equals("BOTH"))) {
	if(isSTART) {
	  addNextEvent(rArmUses,srTime,ArmUse.POSTURESHIFT);
	  addNextEvent(lArmUses,srTime,ArmUse.POSTURESHIFT);
	  addNextEvent(bothArmUses,srTime,ArmUse.POSTURESHIFT);
	} else { //isEND 
	  addNextEvent(rArmUses,srTime,ArmUse.NOT_USED);
	  addNextEvent(lArmUses,srTime,ArmUse.NOT_USED);
	  addNextEvent(bothArmUses,srTime,ArmUse.NOT_USED);	  
	};
      };
    };
    endCurrentEvent(rArmUses,lastEventTime);
    endCurrentEvent(lArmUses,lastEventTime);
    endCurrentEvent(bothArmUses,lastEventTime);

    if(DEBUG) {
      printUsage("Right Arm Usage:",rArmUses);
      printUsage("Left Arm Usage:",lArmUses);
      printUsage("Both Arm Usage:",bothArmUses);      
    };

    //Now determine when READY and RELAX gestures should be inserted...

    //Rule 1: If a BEAT is preceded by a NOT-USED of at least BEAT_READY seconds from start
    //        of utterance, then command to READY:
    rule1(xml,rArmUses,"RIGHT");
    rule1(xml,lArmUses,"LEFT");

    //Rule 2: If a BEAT is preceded by an arm use followed by a NOT-USED of at least
    //        BEAT_READY+TIME_TO_RELAX seconds of utterance, then command to READY:
    rule2(xml,rArmUses,"RIGHT");
    rule2(xml,lArmUses,"LEFT");

    //Rule 3: If any gest is followed by a NOT-USED for BOTH arms of at least BEAT_READY+TIME_TO_RELAX
    //        seconds followed by another arm use, then command a RELAX.
    for(int i=0;i<bothArmUses.size()-1;i++) {
      ArmUse gesture=(ArmUse)bothArmUses.elementAt(i);
      ArmUse notused=(ArmUse)bothArmUses.elementAt(i+1);
      if((gesture.type==ArmUse.GESTURE || gesture.type==ArmUse.BEAT) && notused.type==ArmUse.NOT_USED &&
	 notused.duration()>(BEAT_READY_TIME+TIME_TO_RELAX)) {
	double relaxTime=gesture.end+TIME_TO_RELAX;
	if(DEBUG) System.out.println("  Adding BOTH RELAX at "+relaxTime+" by rule 3.");
	Compiler.addStartStep(xml,getID(),-1,relaxTime,"GESTURE_RIGHT",GESTURE_ATTRIBUTES,RELAX_VALUES);
	Compiler.addStartStep(xml,getID(),-1,relaxTime,"GESTURE_LEFT",GESTURE_ATTRIBUTES,RELAX_VALUES);
      };
    };

    //Rule 4: Command RELAX following last use of BOTH arms.
    //Note: may produce redundant relax with last application of rule 3. so what?
    ArmUse lastNotUsed=(ArmUse)bothArmUses.lastElement();
    double relaxTime=lastNotUsed.start+TIME_TO_RELAX;
    if(DEBUG) System.out.println("  Adding BOTH RELAX at "+relaxTime+" by rule 4.");
    Compiler.addStartStep(xml,getID(),-1,relaxTime,"GESTURE_RIGHT",GESTURE_ATTRIBUTES,RELAX_VALUES);
    Compiler.addStartStep(xml,getID(),-1,relaxTime,"GESTURE_LEFT",GESTURE_ATTRIBUTES,RELAX_VALUES);

    return xml;
  }

  private void rule1(Document xml,Vector armUses,String label) {
    //Rule 1: If a BEAT is preceded by a NOT-USED of at least BEAT_READY seconds from start
    //        of utterance, then command to READY:
    if(armUses.size()<2) return;
    ArmUse firstArmUse=(ArmUse)armUses.elementAt(0);
    ArmUse secondArmUse=(ArmUse)armUses.elementAt(1);
    if(firstArmUse.type==ArmUse.NOT_USED && secondArmUse.type==ArmUse.BEAT && firstArmUse.duration()>=BEAT_READY_TIME) {
      if(DEBUG) System.out.println("  Adding "+label+" READY at "+(secondArmUse.start-BEAT_READY_TIME)+" by rule 1.");
      Compiler.addStartStep(xml,getID(),-1,secondArmUse.start-BEAT_READY_TIME,
			    "GESTURE_"+label,GESTURE_ATTRIBUTES,READY_VALUES);
    };
  }

  public void rule2(Document xml,Vector armUses,String label) {
    //Rule 2: If a BEAT is preceded by an arm use followed by a NOT-USED of at least
    //        BEAT_READY+TIME_TO_RELAX seconds of utterance, then command to READY:
    for(int i=0;i<armUses.size()-2;i++) {
      ArmUse firstUse=(ArmUse)armUses.elementAt(i);
      ArmUse notUsed=(ArmUse)armUses.elementAt(i+1);
      ArmUse beat=(ArmUse)armUses.elementAt(i+2);
      if(firstUse.type!=ArmUse.NOT_USED && notUsed.type==ArmUse.NOT_USED && beat.type==ArmUse.BEAT &&
	 notUsed.duration()>=(BEAT_READY_TIME+TIME_TO_RELAX)) {
	if(DEBUG) System.out.println("  Adding "+label+" READY at "+(beat.start-BEAT_READY_TIME)+" by rule 2.");	
	Compiler.addStartStep(xml,getID(),-1,beat.start-BEAT_READY_TIME,
			      "GESTURE_"+label,GESTURE_ATTRIBUTES,READY_VALUES);	
      };
    };
  }

    /** Does all the work of the module, handles string representation of XML tree. */
  public String transduce(String xml) throws Exception {
    return XMLWrapper.toString(transduce(XMLWrapper.parseXML(xml)));
  }

  public ArmUse getCurrentEvent(Vector events) {
    return (ArmUse)events.lastElement();
  }
    public void endCurrentEvent(Vector events,double endTime) {
      getCurrentEvent(events).setEnd(endTime);
    }
    public void addNextEvent(Vector events,double srTime,int type) {
      endCurrentEvent(events,srTime);
      events.addElement(new ArmUse(srTime,type));
    }

  /** Debugging routine. */
  private void printUsage(String label,Vector events) {
    System.out.println("  "+label);
    for(int i=0;i<events.size();i++) {
      ArmUse use=(ArmUse)events.elementAt(i);
      System.out.println("    "+Compiler.formatTime(use.start)+"\t"+
			        Compiler.formatTime(use.end)+"\t"+
			        use);
    }
  }

  /** Tracks a single arm-use event over an interval of time. */
  public class ArmUse {
    double start,end;
    int type;
    //values of type:
    public static final int NOT_USED=0;
    public static final int BEAT=1;
    public static final int GESTURE=2;
    public static final int POSTURESHIFT=3;
    
    public ArmUse(double start,int type) {
      this.start=start;
      this.end=-1.0;
      this.type=type;
    }
    public void setEnd(double end) {
      this.end=end;
    }
    public double duration() {
      return end-start;
    }
    public String toString() {
      switch(type) {
      case NOT_USED: return "NOT_USED";
      case BEAT: return "BEAT";
      case GESTURE: return "GESTURE";
      case POSTURESHIFT: return "POSTURESHIFT";
      default : return "???";
    }
    }
  }
}


