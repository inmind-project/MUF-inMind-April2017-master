/* -------------------------------------------------------------------------

   NVBNodeGenerator.java
     - Convenience class for creating NVB generators in the BEAT gesture toolkit

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

/** Convenience class for creating nonverbal behavior generators
    which operate by performing tests on a specified type of XML tree
    node and, if the tests succeed, adding a proposed nonverbal
    behavior element to span the subtree rooted at the node.
    During construction a list of the tag types this generator is
    interested in is specified (array of strings). At runtime, each
    node of the XML tree being processed whose tag type is in the list
    is passed to this generators 'processNode' method. If the generator decides
    that a new nonverbal behavior is to be added to the node, the 'processNode'
    method simply returns the new tag specifying the nonverbal behavior.
    If no nonverbal behavior is to be added, 'processNode' returns null. 
    <h3>Change log:</h3>
    <table border="1">
    <tr><th>Date<th>Who<th>What</tr>
    <tr><td>4/1/01<td>T. Bickmore<td> Created. </tr>
    </table>
*/

public abstract class NVBNodeGenerator extends NVBGenerator {
    /** The list of input tree tag types this node will be given. */
  protected String[] nodeTypes;

    /** Constructor for a NVBNodeGenerator which only operates on a single type
       of tree node. */
  public NVBNodeGenerator(String nodeType) {
    nodeTypes=new String[]{nodeType};
  }

    /** Constructor for a NVBNodeGenerator which operates on any of a specified
	list of tree node types. */
  public NVBNodeGenerator(String[] nodeTypes) {
    this.nodeTypes=nodeTypes;
  }

    /** Does the work of the generator, an XML transducer. */
  public Document run(Document xml) throws Exception {
    Vector nodes = XMLWrapper.getAllNodesOfType(xml.getDocumentElement(),new Vector(),nodeTypes);
    for(int j=0;j<nodes.size();j++) {
      Node node=(Node)nodes.elementAt(j);
      Element suggestion=processNode(node);
      if(suggestion!=null) 
	XMLWrapper.spliceParent(node,suggestion);
    };
    return xml;
  }

    /** Passed each node of interest to the generator, this returns either
	a new nonverbal behavior to add to the node, or null. */
  public abstract Element processNode(Node n);
}
