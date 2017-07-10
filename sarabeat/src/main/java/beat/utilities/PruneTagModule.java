/* -------------------------------------------------------------------------

   PruneTagModule.java
     - A utility class for removing tags in the BEAT gesture toolkit

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
package beat.utilities;

import org.w3c.dom.*;

/** A BEAT module which removes all tags from a specified list from the XML tree. 
    The list of tags are specified in the constructor. 
    <p>
    This module is currently used to simplify debugging dumps of the final tree
    by removing the linguistic tags added by the LanguageModule from later
    stages of processing when they are no longer needed.
    <h3>Change log:</h3>
    <table border="1">
    <tr><th>Date<th>Who<th>What</tr>
    <tr><td>4/1/01<td>T. Bickmore<td> Created. </tr>
    </table>
*/

public class PruneTagModule extends BeatModule {
    /** The list of tags to be removed (array of strings). */
  String[] tagsToPrune;

    /** Constructor requires the list of tags to remove, plus the BEAT module
	to output to. */
  public PruneTagModule(String[] tagsToPrune,BeatModule output) {
    super(output);
    this.tagsToPrune=tagsToPrune;
  }

    /** Does the work of the module, an XML transducer. */
  public Document transduce(Document xml) throws Exception {
    XMLWrapper.pruneAllNodesOfType(xml,tagsToPrune);
    return xml;
  }

    /** Does the work of the module. Operates on string representations
	of the XML tree. */
  public String transduce(String xml) throws Exception {
    return XMLWrapper.toString(transduce(XMLWrapper.parseXML(xml)));
  }
}
