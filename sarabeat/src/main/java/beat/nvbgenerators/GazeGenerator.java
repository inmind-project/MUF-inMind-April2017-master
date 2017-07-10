/* -------------------------------------------------------------------------

   GazeGenerator.java
     - A behavior generator for the BEAT gesture toolkit

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

import org.w3c.dom.*;

import beat.utilities.XMLWrapper;

/** A nonverbal behavior generator which generates gaze behavior as a function
    of TURN and THEME-RHEME.
    Follows Torres, Cassell & Prevost algorithm: 
    <ul>
    <li> If in THEME and beginning of turn, look AWAY from hearer.
    <li> If in THEME and not beginning of turn, look AWAY from hearer 70% of the time.
    <li> If in RHEME and end of turn, look TOWARDS hearer.
    <li> If in RHEME and not end of turn, look TOWARDS hearer 73% of the time.
    </ul>
    <h3>Change log:</h3>
    <table border="1">
    <tr><th>Date<th>Who<th>What</tr>
    <tr><td>4/1/01<td>T. Bickmore<td> Created. </tr>
    <tr><td>2/21/02<td>H. Vilhjalmsson<td>Added a <code>TARGET</code> attribute specifying <b>which</b> hearer to look at</tr>
    </table>Do y
*/

public class GazeGenerator extends NVBGenerator {

    protected ParticipationFramework pf;

  public Document run(Document xml) {
      pf = this.module.getParticipationFramework(getScene(xml));
    Vector nodes = XMLWrapper.getAllNodesOfType(xml,new Vector(),new String[]{"THEME","RHEME"});
    for(int i=0;i<nodes.size();i++) {
      Node node=(Node)nodes.elementAt(i);
      Element suggestion=generateGaze(xml,i==0,i==nodes.size()-1,node);
      if(suggestion!=null) {
	XMLWrapper.spliceParent(node,suggestion);
      };
    };
    return xml;
  }

  private Element generateGaze(Document xml,boolean isBeginningOfTurn,boolean isEndOfTurn,Node node) {
    if(node.getNodeName().equals("THEME")) {
	if(isBeginningOfTurn || randBool(0.7)) 
	return makeNVBSuggestion(xml,"GAZE",new String[]{"DIRECTION","PRIORITY"},new String[]{"AWAY_FROM_HEARER","1"});
    } else { //a RHEME
	if(isEndOfTurn || randBool(0.73)) {
	    Element ele=xml.createElement("GAZE");
	    String target = "";
	    if(pf!=null) target = pf.getAddressee();
	    if(target.length()==0) target = "ANY";
	    ele.setAttribute("DIRECTION","TOWARDS_HEARER");
	    ele.setAttribute("FOCUS",target);
	    ele.setAttribute("PRIORITY","5");
	    return ele;
	}
    };;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    return null; //no suggestion
  }

    /** Returns true P*100% of the time (uniform distribution). */
  public static boolean randBool(double P) { 
      return P>Math.random(); 
  } 

}

