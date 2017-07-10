/* -------------------------------------------------------------------------

   ContrastGestureGenerator.java
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

import beat.utilities.XMLWrapper;
import org.w3c.dom.*;

/** Generates gestures for items which are contrasted in an utterance.
    If there are exactly two items being contrasted (CONTRAST tags have the
    same ID values) then a right-hand and left-hand contrast is generated.
    Otherwise, beats are generated for all contrasted items.
    <p>
    Matches on &lt;CONTRAST ID="value"&gt; tags.
    <h3>Change log:</h3>
    <table border="1">
    <tr><th>Date<th>Who<th>What</tr>
    <tr><td>4/1/01<td>T. Bickmore<td> Created. </tr>
    </table>
*/

public class ContrastGestureGenerator extends NVBGenerator {

    /** Does all the work of the generator. An XML transducer. */
  public Document run(Document xml) {
    Vector nodes = XMLWrapper.getAllNodesOfType(xml,new Vector(),new String[]{"CONTRAST"});
    //Check if exactly two with same ID attribute...
    Node[] contrastItems=findContrastItems(nodes); //returns 2 items or null
    if(contrastItems!=null) {
      XMLWrapper.spliceParent(contrastItems[0],makeRightGesture(xml));
      XMLWrapper.spliceParent(contrastItems[1],makeLeftGesture(xml));
    } else { //just generate a beat for every  contrast...
      for(int i=0;i<nodes.size();i++) 
	XMLWrapper.spliceParent((Node)nodes.elementAt(i),makeBeatGesture(xml));
    };
    return xml;
  }

  //See if for any ID there are exactly two nodes...
  private Node[] findContrastItems(Vector nodes) {
    Hashtable idTable=new Hashtable(); //index by ID -> Vector of contrast
    for(int i=0;i<nodes.size();i++) {
      Node contrast=(Node)nodes.elementAt(i);
      String id=XMLWrapper.getXMLAttribute((Element)contrast,"ID");
      if(id==null) continue;
      Vector byid=(Vector)idTable.get(id);
      if(byid==null) byid=new Vector();
      byid.addElement(contrast);
      idTable.put(id,byid);
    };
    //now walk through table and stop at first ID with 2 items...
    Enumeration en=idTable.keys();
    while(en.hasMoreElements()) {
      String key=(String)en.nextElement();
      if(key.trim().length()==0) continue;
      Vector byid=(Vector)idTable.get(key);
      if(byid.size()==2) {
	Node[] result=new Node[2];
	result[0]=(Node)byid.elementAt(0);
	result[1]=(Node)byid.elementAt(1);
	return result;
      };
    };
    return null;
  }

  private Element makeRightGesture(Document xml) {
    return XMLWrapper.createElement(xml,"GESTURE_RIGHT",
        new String[]{"TYPE","RIGHT_TRAJECTORY","RIGHT_HANDSHAPE","PRIORITY"},
	new String[]{"CONTRAST_1","CONTRAST_TRAJECTORY","CONTRAST","5"});
  }

  private Element makeLeftGesture(Document xml) {
    return XMLWrapper.createElement(xml,"GESTURE_LEFT",
        new String[]{"TYPE","LEFT_TRAJECTORY","LEFT_HANDSHAPE","PRIORITY"},
	new String[]{"CONTRAST_2","CONTRAST_TRAJECTORY","CONTRAST","5"});
  }

  private Element makeBeatGesture(Document xml) {
    return XMLWrapper.createElement(xml,"GESTURE_RIGHT",
        new String[]{"TYPE","PRIORITY"},
	new String[]{"BEAT","3"});
  }

}
