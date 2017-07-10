/* -------------------------------------------------------------------------

   XMLTimingSource.java
     - Provides speech timing information for the BEAT gesture toolkit

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

import org.w3c.dom.*;

import beat.utilities.XMLWrapper;

/** Provides speech timing information for scheduling nonverbal behaviors from
    information in an XML file. Useful for producing behavior timed to pre-recorded
    audio tracks or similar. The timings for several utterances can be placed in one
    file, each specified by the UTTERANCE ID attribute (corresponding to the UTTERANCE ID
    attribute in the XML tree processed by BEAT).<br>
    
    The XML file format is the following: 
    <ul>
    <li> file ::= &lt;DATA&gt; { utterance }* &lt;/DATA&gt;
    <li> utterance ::= &lt;UTTERANCE ID="id"&gt; { item }* &lt;/UTTERANCE&gt;
    <li> item ::= word | viseme
    <li> word ::= &lt;WORD SRTIME="time"/&gt;
    <li> viseme ::= &lt;VISEME TYPE="vtype" SRTIME="time"/&gt;
    <li> time ::= float representing seconds from start of speech
    <li> vtype ::= A | O | E | U | OO | B | D | F | TH | R
    </ul>

    <br>The primary method--getTiming()--that takes the XML tree
    to be produced (after generation and filtering)--and returns the timing information
    for the speech in the form of a Vector of TimingSource.TimedEvent objects.
    Each such object has a time stamp (relative to the start of speech) and an 
    event which specifies either the start of a word or a viseme.

    <h3>Change log:</h3>
    <table border="1">
    <tr><th>Date<th>Who<th>What</tr>
    <tr><td>4/1/01<td>T. Bickmore<td>Created.</tr>
    </table>
    */

public class XMLTimingSource extends TimingSource {
  /** Handle to the data file XML tree. */
  XMLWrapper xmlw;

  /** Maps UTTERANCE ID attribute to NodeList of timing items for that utterance. */
  Hashtable utterances=new Hashtable(); //get(String ID) -> NodeList of children

  /** Constructs an XMLTimingSource object given a file containing timing information.
      See above for required format. */
  public XMLTimingSource(File xmlFile) throws Exception {
    xmlw=new XMLWrapper(xmlFile);
    xmlw.pruneAllNodesOfType(new String[]{xmlw.TEXT});
    Vector nodes=xmlw.getAllNodesOfType("UTTERANCE");
    for(int i=0;i<nodes.size();i++) {
      Element utterance=(Element)nodes.elementAt(i);
      String ID=utterance.getAttribute("ID");
      if(ID==null) throw new Exception("No ID for UTTERANCE.");
      utterances.put(ID,utterance.getChildNodes());
    };
  }

  /** Returns Vector of TimedEvent sorted by time. 
      Assumes tree will have one UTTERANCE with an ID tag used to index the file. */
  public Vector getTiming(Document xml) throws Exception {
    Element utterance=(Element)xmlw.getFirstNodeOfType(xml,"UTTERANCE");
    if(utterance==null) throw new Exception("No UTTERANCE node in tree.");
    String ID=utterance.getAttribute("ID");
    if(ID==null) throw new Exception("No ID in UTTERANCE.");
    NodeList timings=(NodeList)utterances.get(ID);
    if(timings==null) throw new Exception("No timings for UTTERANCE "+ID);
    Vector result=new Vector();
    for(int i=0;i<timings.getLength();i++) {
      Element element=(Element)timings.item(i);
      if(element.getNodeName().equals("WORD")) {
	TimedEvent event=new TimedEvent(
	    new Double(element.getAttribute("SRTIME")).doubleValue(),
	    WORD);
	result.addElement(event);
      } else if(element.getNodeName().equals("VISEME")) {
	TimedEvent event=new TimedEvent(
	    new Double(element.getAttribute("SRTIME")).doubleValue(),
	    visemeCode(element.getAttribute("TYPE")));
	result.addElement(event);
      } else
	throw new Exception("Unknown tag in XML timing file.");
    };
    return result;
  }

}
