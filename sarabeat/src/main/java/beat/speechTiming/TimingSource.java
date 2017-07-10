/* -------------------------------------------------------------------------

   TimingSource.java
     - Abstract class for a source of speech timing information.

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

package beat.speechTiming;

import java.io.*;
import java.util.*;

import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

/** A source of speech timing information for scheduling nonverbal behaviors. 
    This abstract class specifies a method--getTiming()--that takes the XML tree
    to be produced (after generation and filtering)--and returns the timing information
    for the speech in the form of a Vector of TimingSource.TimedEvent objects.
    Each such object has a time stamp (relative to the start of speech) and an 
    event which specifies either the start of a word or a viseme.

    <h3>Change log:</h3>
    <table border="1">
    <tr><th>Date<th>Who<th>What</tr>
    <tr><td>4/1/01<td>T. Bickmore<td>Created.</tr>
	<tr><td>7/10/01<td>H. Vilhjalmsson<td>Added getSpeaker().</tr>
    </table>
    */

public abstract class TimingSource {
  /** Values for TimedEvent.event */
    public static final int WORD=-1;

  /** Values for TimedEvent.event */
    public static final int VISEME_CLOSED=0;

  /** Values for TimedEvent.event */
    public static final int VISEME_A=1;

  /** Values for TimedEvent.event */
    public static final int VISEME_O=2;

  /** Values for TimedEvent.event */
    public static final int VISEME_E=3;

  /** Values for TimedEvent.event */
    public static final int VISEME_U=4;

  /** Values for TimedEvent.event */
    public static final int VISEME_OO=5;

  /** Values for TimedEvent.event */
    public static final int VISEME_B=6;

  /** Values for TimedEvent.event */
    public static final int VISEME_D=7;

  /** Values for TimedEvent.event */
    public static final int VISEME_F=8;

  /** Values for TimedEvent.event */
    public static final int VISEME_TH=9;

  /** Values for TimedEvent.event */
    public static final int VISEME_R=10;

  /** Values for TimedEvent.event */
    public static final int VISEME_SMILE=11;

  /** Values for TimedEvent.event */
    public static final int VISEME_DEBUG=100;

  /** Flag which indicates whether debug messages should be sent to System.out or not. */
    protected boolean DEBUG;

  /** Represents a single timed event from the source of timing information. */
    public static class TimedEvent {
      /** The time of the event relative to start of speech. */
        public double time;
      /** The type of the event. */
        public int event; 
        
        public TimedEvent() {};
        public TimedEvent(double time,int event) {
            this.time=time;
            this.event=event;
        }
    }

  public TimingSource() throws Exception {}

  /** Returns Vector of TimedEvent sorted by time. */
  public abstract Vector getTiming(Document xml) throws Exception; 

	/** Returns the string value of the speaker attribute found in the document meta information tag */
	public String getSpeaker(Document xml) {
		NodeList metaelements=xml.getElementsByTagName("META");
		if(metaelements.getLength()>0) {
			Element meta = (Element)metaelements.item(0);
			String speaker = meta.getAttribute("SPEAKER");
			if(speaker!=null)
				return speaker;
		}
		return "DEFAULT";
	}

  /** Sets the DEBUG flag which controls whether debug messages are sent to System.out or not. */
  public void setDEBUG(boolean DEBUG) { this.DEBUG=DEBUG; }

  /** Utility function to map enumerated viseme values to printable string representations. */
  public static String visemeString(int viseme) {
    switch(viseme) {
    case VISEME_CLOSED: return "CLOSED";
    case VISEME_A: return "A";
    case VISEME_O: return "O";
    case VISEME_E: return "E";
    case VISEME_U: return "U";
    case VISEME_OO: return "OO";
    case VISEME_B: return "B";
    case VISEME_D: return "D";
    case VISEME_F: return "F";
    case VISEME_TH: return "TH";
    case VISEME_R: return "R";
    default: return "?";
    }
  }

  /** Utility function to map string representations of visemes to enumerated type values. */
  public static int visemeCode(String viseme) {
    if(viseme.equals("A")) return VISEME_A;
    if(viseme.equals("O")) return VISEME_O;
    if(viseme.equals("E")) return VISEME_E;
    if(viseme.equals("U")) return VISEME_U;  
    if(viseme.equals("OO")) return VISEME_OO;
    if(viseme.equals("B")) return VISEME_B;  
    if(viseme.equals("D")) return VISEME_D;  
    if(viseme.equals("F")) return VISEME_F;  
    if(viseme.equals("TH")) return VISEME_TH;
    if(viseme.equals("R")) return VISEME_R;  
    return VISEME_CLOSED;
  }

}
