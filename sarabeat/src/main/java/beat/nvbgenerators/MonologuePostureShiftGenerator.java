/* -------------------------------------------------------------------------

   MonologuePostureShiftGenerator.java
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

/** A nonverbal behavior generator which generates posture shift behavior as a function
    of clauses and TOPICSHIFT indicators.
    Follows Cassell, Nagano and Bickmore 2001 for monologues: during a topic shift
    there is a 0.84 probability of a posture shift, otherwise posture shifts occur
    at 0.04 per second. Guesstimating 4.0 seconds per clause (WAG), this indicates
    that generating randomly occurring posture shifts should be generated per clause
    at a probability of 0.16. Priority is in between BEAT (10) and ICONIC (20) gestures.
    If there are any topicshift-generated posture-shifts in an utterance, then no non-topicshift-generated
    posture-shifts will be generated for the utterance.
    <h3>Change log:</h3>
    <table border="1">
    <tr><th>Date<th>Who<th>What</tr>
    <tr><td>1/11/02<td>T. Bickmore<td> Created. </tr>
    </table>
*/

public class MonologuePostureShiftGenerator extends  NVBGenerator {
  public Document run(Document xml) {
    Vector nodes = XMLWrapper.getAllNodesOfType(xml,"CLAUSE");
    int numPostureShifts=0;
    boolean[] hasTopicShift=new boolean[nodes.size()];
    for(int i=0;i<nodes.size();i++) {
      Node node=(Node)nodes.elementAt(i);
      Node topicshift=XMLWrapper.getFirstNodeOfType(node,"TOPICSHIFT");
      Node firstWord=XMLWrapper.getFirstNodeOfType(node,"W");
      if(topicshift!=null) {
	hasTopicShift[i]=true;
	//Generate topic-driven posture shift.
	if(module.DEBUG) System.out.println("Considering posture shift for topic shift at "+firstWord+"...");
	if(randBool(0.84)) {
	  if(module.DEBUG) System.out.println("...generating posture shift.");
	  numPostureShifts++;
	  XMLWrapper.spliceParent(firstWord,
				  makeNVBSuggestion(xml,"POSTURESHIFT",
						    new String[]{"BODYPART","ENERGY","PRIORITY"},
						    new String[]{"BOTH","HIGH","15"}));
	  };
      };
    };
    //Now pass through for clause-based posture shifts, ONLY IF no other posture shifts have been generated.
    if(numPostureShifts==0) 
      for(int i=0;i<nodes.size();i++) {
	if(hasTopicShift[i]) continue;
	Node node=(Node)nodes.elementAt(i);
	Node firstWord=XMLWrapper.getFirstNodeOfType(node,"W");
	//Generate randomly-occurring posture shift.
	if(module.DEBUG) System.out.println("Considering posture shift for clause at "+firstWord+"...");
	if(randBool(0.16)) {
	  if(module.DEBUG) System.out.println("...generating posture shift.");
	  XMLWrapper.spliceParent(firstWord,
				  makeNVBSuggestion(xml,"POSTURESHIFT",
						    new String[]{"BODYPART","ENERGY"},
						    new String[]{"BOTH","LOW"}));
	};
      };

    return xml;
  }

    /** Returns true P*100% of the time (uniform distribution). */
  public static boolean randBool(double P) { 
      return P>Math.random(); 
  } 

}

