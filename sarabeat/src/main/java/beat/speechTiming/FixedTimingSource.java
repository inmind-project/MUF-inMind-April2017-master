/* -----------------------------------------------------------------------
   FixedTimingSource.java
     - Provides a fixed time between all words

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

import java.util.*;

import beat.utilities.XMLWrapper;
import org.w3c.dom.*;

import beat.utilities.NVBTypes;

/** 

    A TimingSource that returns TimedEvents with fixed intervals.

    <h3>Change log:</h3>
    <table border="1">
    <tr><th>Date<th>Who<th>What</tr>
    <tr><td>10/08/01<td>Hannes Vilhjalmsson<td>Created.</tr>
    </table>
    */

public class FixedTimingSource extends TimingSource {

  NVBTypes nvbTypes;

  protected double m_fInterval = 1;

  /** Constructs an FixedTimingSource that uses the passed time as the
      fixed interval between words */
  public FixedTimingSource(NVBTypes nvbTypes, double interval) throws Exception {
      m_fInterval = interval;
      this.nvbTypes = nvbTypes;
  }

    /** Returns Vector of TimedEvent sorted by time */
  public Vector getTiming(Document xml) throws Exception {
    Vector result=new Vector();
    XMLWrapper xmlw = new XMLWrapper(xml);
    Vector allwords=xmlw.getAllNodesOfType("W");

    Vector nvbs=xmlw.getAllNodesOfType(nvbTypes.getNVBTypes());

    //System.out.println("getTiming nvbs = "+nvbs);

    int lastindex = 0; int tempindex = 0;
    for(int i=0;i<nvbs.size();i++) {
      tempindex = (new Integer(((Element)nvbs.elementAt(i)).getAttribute("WI_END"))).intValue();
      if(tempindex>lastindex)
	lastindex = tempindex;
    } 
    for(int i=0;i<=(lastindex+1);i++) 
	result.addElement(new TimedEvent(m_fInterval*i,WORD));
    return result;
  }

}

