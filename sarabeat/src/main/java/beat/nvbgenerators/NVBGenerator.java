
/* -------------------------------------------------------------------------

   NVBGenerator.java
     - Abstract superclass for all NVB generators in the BEAT gesture toolkit

   Copyright(C) 2000 by the Massachusetts Institute of Technology.  
   All rights reserved.							  
 									  
   Developed by the Gesture and Narrative Language Group
   at the Media Laboratory, MIT, Cambridge, Massachusetts.		  
 									  
   Permission to use, copy, or modify this software and its documentation
   for educational and research purposes only and without fee is hereby
   granted, provided  that this copyright notice and the original authors'
   names appear on all copies and supporting documentation. If individual
   files are separated from this distribution directory structure, this
   copyright notice must be included. For any other uses of this software
   in original or modified form, including but not limited to distribution
   in whole or in part, specific prior permission must be obtained from
   MIT. These programs shall not be used, rewritten, or adapted as the
   basis of a commercial software or hardware product without first
   obtaining appropriate licenses from MIT. MIT makes no representation
   about the suitability of this software for any purpose. It is provided
   "as is" without express or implied warranty.		

   ------------------------------------------------------------------------*/
package beat.nvbgenerators;

import org.w3c.dom.*;

import beat.utilities.XMLWrapper;

/** Abstract class for a nonverbal behavior generator, responsible for
    proposing possible nonverbal behaviors by adding them to the XML tree.
    An XML transducer. These must be registered with and called by the NVBGeneratorModule.
    <h3>Change log:</h3>
    <table border="1">
    <tr><th>Date<th>Who<th>What</tr>
    <tr><td>4/1/01<td>T. Bickmore<td> Created. </tr>
    </table>
*/

public abstract class NVBGenerator {
    /** Handle to the NVBGeneratorModule containing this generator. */
  protected NVBGeneratorModule module;

    /** Sets the handle to the NVBGeneratorModule (called during registration). */
  public void setModule(NVBGeneratorModule module) { this.module=module; }

    /** Returns the name of the Generator instance as a String */
    public String getName() { return this.getClass().getName(); }

    /** Does the work of this generator. */
  public abstract Document run(Document xml) throws Exception;

    /** Utility function for creating new nonverbal behavior suggestion tags. */
  public Element makeNVBSuggestion(Document xml,String type,String[] attributes,String[] values) {
    Element ele=xml.createElement(type);
    for(int i=0;i<attributes.length;i++)
      ele.setAttribute(attributes[i],values[i]);
    return ele;
  }

    /** Utility function for getting the id of the current conversatoin scene. */
    public String getScene(Document xml) {
	Element node = (Element)XMLWrapper.getFirstNodeOfType(xml.getDocumentElement(),"UTTERANCE");
	if(node!=null) 
	    return node.getAttribute("SCENE");
	else
	    return "";
    }

}
  

