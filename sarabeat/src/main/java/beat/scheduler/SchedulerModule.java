/* -------------------------------------------------------------------------

   SchedulerModule.java
     - Implements a scheduler for the BEAT gesture toolkit

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
package beat.scheduler;

import java.util.*;

import beat.utilities.BeatModule;
import org.w3c.dom.*;

import beat.speechTiming.TimingSource;
import beat.utilities.NVBTypes;
import beat.utilities.XMLWrapper;

/** A BEAT pipeline module which assigns WORDINDEX and RSTIME attributes to all
    nonverbal behavior tags. WORDINDEX is the index into the speech
    (zero=before first word). RSTIME is the time relative to the start
    of speech in seconds. Note that RSTIME is only assigned if a TimingSource
    is specified in the constructor. This also splices VISEME tags into the
    tree if provided by the timing source. <p> The full set of attributes 
    added to each nonverbal behavior are:
    <ul>
    <li> WORDINDEX - the index into the speech (zero=before first word).
    <li> WORDCOUNT - the number of words contained in the subtree rooted
           at each nonverbal behavior tag.
    <li> SRT_START - the start time of the nonverbal behavior, in seconds, relative
            to the start of speech.
    <li> SRT_END - the end time of the nonverbal behavior, in seconds, relative
            to the start of speech.
    </ul>
    <h3>Change log:</h3>
    <table border="1">
    <tr><th>Date<th>Who<th>What</tr>
    <tr><td>4/1/01<td>T. Bickmore<td> Created. </tr>
    </table>
*/

public class SchedulerModule extends BeatModule {
    /** Handle to the object which provides information about word timings, or
	null if no such object exists. */
  TimingSource timingSource;
    /** Handle to the NVBTypes object which provides information about the 
	types of nonverbal behaviors that can be generated. */
  NVBTypes nvbTypes;

    /** Constructor requires:
	<ul> 
	<li> Handle to the NVBTypes object which provides information about the 
	types of nonverbal behaviors that can be generated. 
	<li> Handle to the object which provides information about word timings, or
	null if no such object exists. 
	<li> the BEAT module to output to.
	</ul>
    */
  public SchedulerModule(NVBTypes nvbTypes,TimingSource timingSource,BeatModule output) {
    super(output);
    this.timingSource=timingSource;
    this.nvbTypes=nvbTypes;
  }

    /** Does the work of the module. Operates on a string representation of the XML tree. */
  public String transduce(String xml) throws Exception {
    return XMLWrapper.toString(transduce(XMLWrapper.parseXML(xml)));
  }
    
    /** Does the work of the module by assigning WORDINDEX and SRTIME attributes
	to all nonverbal behaviors, and splicing in VISEME tags, if provided.
	An XML transducer. */
  public Document transduce(Document xml) throws Exception {
    if(DEBUG) System.out.println("SchedulerModule running...");
    XMLWrapper xmlw=new XMLWrapper(xml);
    
    //First assign WORDINDEX attributes..
    xmlw.computeWordIndex();
    Vector nvbs=xmlw.getAllNodesOfType(nvbTypes.getNVBTypes());
    for(int i=0;i<nvbs.size();i++) {
      Node node=(Node)nvbs.elementAt(i);
      int wiStart=((Integer)xmlw.getNXMLAttribute(node,"WORDINDEX")).intValue()-1;
      int wiEnd=wiStart+((Integer)xmlw.getNXMLAttribute(node,"WORDCOUNT")).intValue();
      xmlw.setXMLAttribute((Element)node,"WI_START",""+wiStart);
      xmlw.setXMLAttribute((Element)node,"WI_END",""+wiEnd);
    };

    //If there is no timing info, just punt...
    if(timingSource==null) return xml;

    //Else get timings...vector of TimingSource.TimedEvent sorted by time.
    if(DEBUG) System.out.print("  Getting timing info...");
    Vector timingV=timingSource.getTiming(xml);
    if(DEBUG) System.out.println("  ...got timings.");

    //Normalize the word timings...sort by time and ensure words precede their visemes..
    TimingSource.TimedEvent[] timing=sortTiming(timingV);

    if(DEBUG) System.out.println("Building index of wordIndex->TimedEvent");

    //Build index of wordIndex->TimedEvent
    double lastTime=0.0; //last time of any event
    Vector wordTimings=new Vector();
    for(int i=0;i<timing.length;i++) {
      TimingSource.TimedEvent event=timing[i];
      if(event.event==TimingSource.WORD)
	wordTimings.addElement(event);
      if(event.time>lastTime) lastTime=event.time;
    };

    if(DEBUG) System.out.println("Adding SRTIME to NVBS");

    //Now add SRTIME to NVBs
    for(int i=0;i<nvbs.size();i++) {
      Node node=(Node)nvbs.elementAt(i);
      int wiStart=((Integer)xmlw.getNXMLAttribute(node,"WORDINDEX")).intValue();
      int wiEnd=wiStart+((Integer)xmlw.getNXMLAttribute(node,"WORDCOUNT")).intValue();

      if(DEBUG) System.out.println("wiStart = "+wiStart+"  wiEnd = "+wiEnd);
      
      TimingSource.TimedEvent startTiming=(TimingSource.TimedEvent)wordTimings.elementAt(wiStart-1);
      if(startTiming!=null) 
	xmlw.setXMLAttribute((Element)node,"SRT_START",""+startTiming.time);
      if(wiEnd>wordTimings.size()) {
	xmlw.setXMLAttribute((Element)node,"SRT_END",""+lastTime);
      } else {
	TimingSource.TimedEvent endTiming=(TimingSource.TimedEvent)wordTimings.elementAt(wiEnd-1);
        if(endTiming!=null)
	  xmlw.setXMLAttribute((Element)node,"SRT_END",""+endTiming.time);
      };
    };

    if(DEBUG) System.out.println("Splicing in visemes");

    //Splice in visemes...done at level just above text...
    Vector leaves=xmlw.getAllNodesOfType(xmlw.TEXT);
    for(int i=leaves.size()-1;i>=0;i--) {
      Text leaf=(Text)leaves.elementAt(i);
      if(xmlw.wordCount(leaf)==0) leaves.removeElement(leaf);
    };

    int wordIndex=1;
    int leafIndex=0;
    Text leaf=null;
    int leafStart=-1;
    int leafEnd=-1; 
    Element leafParent=null;
    for(int i=0;i<timing.length;i++) {
      TimingSource.TimedEvent event=timing[i];
      if(event.event==TimingSource.WORD) {
	wordIndex++;
	if(wordIndex>=leafEnd) {
	  for(;leafIndex<leaves.size();leafIndex++) {
	    leaf=(Text)leaves.elementAt(leafIndex);
	    leafStart=((Integer)xmlw.getNXMLAttribute(leaf,"WORDINDEX")).intValue();
	    leafEnd=leafStart+((Integer)xmlw.getNXMLAttribute(leaf,"WORDCOUNT")).intValue();
	    leafParent=(Element)leaf.getParentNode();
	    if(wordIndex>=leafStart && wordIndex<=leafEnd)
	      break;
	  };
	  if(leafIndex>leaves.size())
	    throw new Exception("BEAT.SM: Error 1 (Should never happen.)\n"+
				"  leafIndex="+leafIndex+" leavs.size="+leaves.size());
	};
      } else { //a viseme to splice..
	if(leaf==null) throw new Exception("BEAT.SM: Error 2 (Should never happen (leaf==null)).");
	leafParent.insertBefore(xmlw.createElement("VISEME",
				       new String[]{"TYPE","WI_START","SRT_START"},
				       new String[]{TimingSource.visemeString(event.event),
						      ""+(wordIndex-1), ""+event.time}),
				leaf);
      };
    };

    return xml;
  }

  //Sort ascending by time...ensure WORDs precede their visemes.
  private TimingSource.TimedEvent[] sortTiming(Vector timingV) {

    if(DEBUG) System.out.println("Sort ascending by time");

    if(DEBUG) System.out.println("timingV size = "+timingV.size());

    TimingSource.TimedEvent[] result=new TimingSource.TimedEvent[timingV.size()];
    for(int i=0;i<result.length;i++)
      result[i]=(TimingSource.TimedEvent)timingV.elementAt(i);
    Arrays.sort(result,
      new Comparator(){
        public int compare(Object o1,Object o2) { //return o1-o2
	  TimingSource.TimedEvent e1=(TimingSource.TimedEvent)o1;
	  TimingSource.TimedEvent e2=(TimingSource.TimedEvent)o2;
	  if(e1.time<e2.time) return -1;
	  if(e1.time>e2.time) return +1;
	  if((e1.event==TimingSource.WORD && e2.event==TimingSource.WORD)||
	     (e1.event!=TimingSource.WORD && e2.event!=TimingSource.WORD)) return 0;
	  if(e1.event==TimingSource.WORD) return -1;
	  return +1;
        }
        //public boolean equals(Object o) { return false; } 
      });
    return result;
  }
}



