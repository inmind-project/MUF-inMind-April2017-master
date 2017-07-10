/* -------------------------------------------------------------------------

   PunctuationGenerator.java
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

/** Generates an eyebrow movement for utterances that end in either
    "!" or "?". Therefore, the additon of 
    <pre>
    &lt;EYEBROWS&gt;
    &nbsp;&nbsp;...
    &lt;/EYEBROWS&gt;

    </pre>
    would be inserted to enclose a clause such as
    <pre>
    &lt;CLAUSE&gt;
    &nbsp;&nbsp;...
    &nbsp;&nbsp;&lt;W SYN="PE"&gt;
    &nbsp;&nbsp;&nbsp;&nbsp;!
    &nbsp;&nbsp;&lt;/W&gt;
    &lt;/CLAUSE&gt;

    </pre>
    (This is the "Java" version of the punctuation rule. 
    Another version, written in xslt, can be registered with 
    NVBXSLGenerator to accomplish the same result.)

    <h3>Change log:</h3>
    <table border="1">
    <tr><th>Date<th>Who<th>What</tr>
    <tr><td>6/29/01<td>Y. Gao<td>Created.</tr>
    </table>
*/

public class PunctuationGenerator extends NVBGenerator {
    /** The XML transducer that does all the work for this generator.
     */
    public Document run(Document xml) {
	XMLWrapper xmlw = new XMLWrapper(xml);
	Vector nodes = XMLWrapper.getAllNodesOfType(xml,new Vector(),
						    new String[]{"W"});
	Node node = null;
	NamedNodeMap attrs = null;
	Node synAttr = null;
	Node clause = null;
	Vector siblings = null;

	for(int i=0;i<nodes.size();i++) {
	    node=(Node)nodes.elementAt(i);
	    attrs = node.getAttributes();
	    synAttr = attrs.getNamedItem("SYN");
	    if(synAttr != null){
		if(synAttr.getNodeValue().equals("PE") ||
		   synAttr.getNodeValue().equals("PQ")) {

		    //add special eyebrow movement element,
		    //enclosing the parent clause, in the document, xml
		    clause = xmlw.getAncestorOfType(node, "CLAUSE");
		    siblings = nodeListToVector(clause.getChildNodes());

		    Element specialEyebrow = xmlw.createElement("EYEBROWS");
		    xmlw.lowerSiblings(specialEyebrow, siblings);

		}
		else{
		    //no ! or ?
		    //do nothing
		}
	    }
		
	    else{
		//do nothing
	    }
	}	
         
	return xml;
    }
    /** A utility method that takes a NodeList and returns it in the
	form of a Vector.
    */
    static Vector nodeListToVector(NodeList nl){
	Vector returnVector = new Vector();
	for(int i = 0; i < nl.getLength(); i++){
	    returnVector.add(nl.item(i));
	}
	return returnVector;
    }
}



