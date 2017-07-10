/* -------------------------------------------------------------------------

   Compiler.java
     - Implements an animation compiler for the BEAT gesture toolkit

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

import org.w3c.dom.*;

import beat.utilities.BeatModule;
import beat.utilities.FlattenTreeModule;
import beat.utilities.XMLWrapper;


/** Takes array of abstract animation events (output from FlattenTreeModule) 
    and returns a string representation of the animation commands required to realize the 
    plan (in some implementations may actually pass the commands on to the animation system). 
    Abstract superclass for particular compiler instantions. 
    <p>
    Format of input tree:
    <ul>
    <li> script ::= &lt;AnimationScript SPEAKER="speaker" HEARER="hearer"&gt; {step}*
                    &lt;/AnimationScript&gt;
    <li> step ::= &lt; type AID="id" SRT="time" WI="index" ACTION="action" {args}* /&gt;
    <li>  type ::= START | STOP
    <li>  id ::= unique ID for the step. If there are START and STOP steps for
         the same command they are given the same ID.
    <li>  time ::= time in seconds from the start of speech
    <li>  index ::= word index (pre-first-word = 0)
    <li>  action ::= type of nonverbal behavior (e.g., GESTURE_RIGHT)
    <li>  args ::= arguments specified for the nonverbal behavior
    </ul>
    <h3>Change log:</h3>
    <table border="1">
    <tr><th>Date<th>Who<th>What</tr>
    <tr><td>4/1/01<td>T. Bickmore<td> Created. </tr>
    <tr><td>6/8/01<td>T. Bickmore<td>Modified to use output from FlattenTreeModule.</tr>
    <tr><td>1/18/02<td>T. Bickmore<td>Made abstract script utilities public static for use in other modules.</tr>
       </tr>
    </table>
*/

public abstract class Compiler extends BeatModule {

    /** This module is typically end of the BEAT pipeline, so an output module
	is not required. */
  public Compiler() throws Exception {
    super(); //End of the pipeline
  }

  /** The primary method called by the compiler's input module. 
      Takes a string representation of the input XML tree as input and just calls compile. */
  public String transduce(String xml) throws Exception {
      return compile(XMLWrapper.parseXML(xml));
  }

  /** The primary method called by the compiler's input module. 
      Takes the input XML tree as input and just calls compile. */
  public Document transduce(Document xml) throws Exception {
      return XMLWrapper.parseXML(
	 "<?xml version='1.0' encoding='utf-8'?><SCRIPT>"+compile(xml)+"</SCRIPT>");
  }

    /** The method that does all of the work of the compiler. */
  protected abstract String compile(Document xml) throws Exception;

  /* --------- UTILITIES FOR OPERATING ON FLATTENED TREES (ABSTRACT SCRIPTS) ------- */

  /** Removes all animation steps with the given action ID (i.e., START and STOP). */
  public static void removeStep(Document xml,String id) {
    Element script=getScript(xml);
    NodeList steps=script.getChildNodes();
    for(int i=steps.getLength()-1;i>=0;i--) {
      Element step=(Element)steps.item(i);
      if(step.getAttribute(FlattenTreeModule.ACTION_ID).equals(id))
	script.removeChild(step);
    };
  }

  /** Adds a new START animation step. Keeps steps in order sorted by srTime and wordIndex. */
  public static void addStartStep(Document xml,String id,int wordIndex,double srTime,String action,
			      String[] attributes,String[] values) {
    Element script=getScript(xml);
    Element step=xml.createElement(FlattenTreeModule.START);
    step.setAttribute(FlattenTreeModule.ACTION_ID,id);
    step.setAttribute(FlattenTreeModule.ACTION,action);
    step.setAttribute(FlattenTreeModule.SRT,formatTime(srTime));
    if(wordIndex>=0) step.setAttribute(FlattenTreeModule.WI,""+wordIndex);
    for(int i=0;i<attributes.length;i++) 
      step.setAttribute(attributes[i],values[i]);
    script.appendChild(step);
    checkOrder(xml,step);
  }

  /** Value for changeTime 'which' parameter. */
  protected static final int WHICH_START=0;
  /** Value for changeTime 'which' parameter. */
  protected static final int WHICH_STOP=1;
  /** Value for changeTime 'which' parameter. */
  protected static final int WHICH_ALL=2; 
  /** Changes the time of the START and/or STOP animation actions specified by ID.
      If WHICH_ALL is specified, the specified newSRTime is for the START action
      and the END action will be modified so that the action duration is constant.
    Keeps steps in order sorted by srTime and wordIndex. */
  protected void changeTime(Document xml,String id,int wordIndex,
			    double newSRTime,int which) throws Exception {
    Element start=null;
    Element stop=null;
    NodeList steps=getScript(xml).getChildNodes();
    for(int i=0;i<steps.getLength();i++) {
      Element step=(Element)steps.item(i);
      if(step.getAttribute(FlattenTreeModule.ACTION_ID).equals(id)) {
	if(step.getTagName().equals(FlattenTreeModule.START))
	  start=step;
	else if(step.getTagName().equals(FlattenTreeModule.STOP))
	  stop=step;
	else 
	  throw new Exception("Unknown ACTION type in script: "+step.getTagName());
      };
    };
        
    if(which==WHICH_ALL && start!=null && stop!=null) {
      //Do the duration calc and move both...
      int wordDuration=getWordIndex(stop)-getWordIndex(start);
      start.setAttribute(FlattenTreeModule.WI,""+wordIndex);
      stop.setAttribute(FlattenTreeModule.WI,""+(wordIndex+wordDuration));	
      if(start.getAttribute(FlattenTreeModule.SRT).length()>0) {
	//by time..
	double duration=getSRTime(stop)-getSRTime(start);
	start.setAttribute(FlattenTreeModule.SRT,""+newSRTime);
	stop.setAttribute(FlattenTreeModule.SRT,""+(newSRTime+duration));
      };
      checkOrder(xml,start);
      checkOrder(xml,stop);
    } else if((which==WHICH_ALL || which==WHICH_START) && start!=null) {
      //Update start only
      start.setAttribute(FlattenTreeModule.WI,""+wordIndex);
      if(start.getAttribute(FlattenTreeModule.SRT).length()>0) {
	//by time..
	start.setAttribute(FlattenTreeModule.SRT,""+newSRTime);
      };
      checkOrder(xml,start);
    } else if((which==WHICH_ALL || which==WHICH_STOP) && stop!=null) {
      //update stop only
      stop.setAttribute(FlattenTreeModule.WI,""+wordIndex);
      if(stop.getAttribute(FlattenTreeModule.SRT).length()>0) {
	//by time..
	stop.setAttribute(FlattenTreeModule.SRT,""+newSRTime);
      };
      checkOrder(xml,stop);
    };
  }

  /** Checks if an animation step is out of temporal order and moves it, if necessary. */
  public static void checkOrder(Document xml,Element node) {
    Element script=getScript(xml);
    int index=getIndex(xml,node);

    //See if needs to be moved...
    if((index>0 && order(node,getStep(xml,index-1))<0) ||   //needs to move up
       (index<getNumberSteps(xml)-1 && order(node,getStep(xml,index+1))>0)) {
      //Remove it...
      script.removeChild(node);
      //Now find out where it should go...
      for(int i=0;i<getNumberSteps(xml);i++) {
	Element step=getStep(xml,i);
	if(order(node,step)<0) {
	  script.insertBefore(node,step);
	  return;
	};
      };
      //If we got here...must go at end.
      script.appendChild(node);
    };
  }

  /** Compares order of two animation steps. Returns e1.position - e2.position. */
  public static double order(Element e1,Element e2) {
    if(e1.getAttribute(FlattenTreeModule.SRT).length()>0) {
      //using speech timings
      return getSRTime(e1)-getSRTime(e2);
    } else {
      //using word indices
      return getWordIndex(e1)-getWordIndex(e2);
    }
  }

  /** Returns the speech relative time of the specified animation step. */
  public static double getSRTime(Element e) {
    return new Double(e.getAttribute(FlattenTreeModule.SRT)).doubleValue();
  }

  /** Returns the word index of the specified animation step. */
  public static int getWordIndex(Element e) {
    String wi=e.getAttribute(FlattenTreeModule.WI);
    if(wi!=null && wi.trim().length()>0)
      return Integer.parseInt(wi);
    else
      return -1;
  }

  /** Returns the index (zero-based) of the given animation step in the
      list of animation steps. */
  public static int getIndex(Document xml,Element step) {
    NodeList steps=getScript(xml).getChildNodes();
    for(int i=0;i<steps.getLength();i++)
      if(steps.item(i)==step)
	return i;
    return -1;
  }

  /** Returns the parent SCRIPT node enclosing all animation steps. */
  public static Element getScript(Document xml) {
    return (Element)XMLWrapper.getFirstNodeOfType(xml,FlattenTreeModule.SCRIPT);
  }

  /** Returns the current number of animation steps in the script. */
  public static int getNumberSteps(Document xml) {
    return getScript(xml).getChildNodes().getLength();
  }

  /** Returns the ith animation step in the script (zero-based). */
  public static Element getStep(Document xml,int i) {
    return (Element)getScript(xml).getChildNodes().item(i);
  }

  /** Returns true if the specified step is a START (else a STOP). */
  public static boolean isSTART(Element step) {
    return step.getTagName().equals(FlattenTreeModule.START);
  }


  /** Value for handNotUsedForAwhile 'hand' parameter. */
  public static final int LEFT=0;
  /** Value for handNotUsedForAwhile 'hand' parameter. */
  public static final int RIGHT=1;

  /** Duration between gestures which must exist for a relax to be scheduled. */
  public static final double RELAX_INTERVAL_TIME=1.8;
  /** Words between gestures which must exist for a relax to be scheduled. 
      Only used when timing information is not available. */
  public static final int RELAX_INTERVAL_WORDS=3;
  /** Returns true if the hand is not used for a gesture within
      RELAX_INTERVAL of the indicated start time. */
  public static boolean handNotUsedForAwhile(int hand,Element endAction,
					 int endEventIndex,Document xml) {
    boolean endSRTimeDEFINED=(endAction.getAttribute(FlattenTreeModule.SRT).length()>0);
    for(int i=endEventIndex+1;i<getNumberSteps(xml);i++) {
      Element step=getStep(xml,i);
      boolean isSTART=isSTART(step);
      String action=step.getAttribute(FlattenTreeModule.ACTION);
      //See if we are outside the interval...
      if((endSRTimeDEFINED && getSRTime(endAction)+RELAX_INTERVAL_TIME<=getSRTime(step)) ||
	 (!endSRTimeDEFINED && getWordIndex(endAction)+RELAX_INTERVAL_WORDS<=getWordIndex(step))) {
	return true;
      };
      //See if this is a gesture of the right type...
      if(isSTART && 
	 ((hand==RIGHT && (action.equals("GESTURE_RIGHT") || action.equals("GESTURE_BOTH"))) ||
	  (hand==LEFT && (action.equals("GESTURE_LEFT") || action.equals("GESTURE_BOTH")))))
	return false;
    };
    return true;
  }

    /** 	This is used to format all floating point numbers. 
		Java doesn't have an easy way to specify precision in floats formatted
		into Strings and PantomimeServer gags on floats with too many digits.
    */
    public static String formatTime(double time) { //truncate to ms - amazingly stupid
	String timeString=""+time;
	int pointIndex=timeString.indexOf('.');
	int numSigDigits=timeString.length()-pointIndex-1;
	if(pointIndex<0 || numSigDigits<=3) return timeString;
	return timeString.substring(0,pointIndex+4);
    }

}

