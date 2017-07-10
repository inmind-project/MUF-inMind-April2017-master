/* -------------------------------------------------------------------------

   NVBFilterModule.java
     - Registration module for all NVB filters in the BEAT gesture toolkit

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
package beat.filter;

import java.util.*;

import beat.utilities.BeatModule;
import org.w3c.dom.*;

import beat.utilities.NVBTypes;
import beat.utilities.XMLWrapper;

/** A BEAT pipeline module which applies all of the registered nonverbal
    behavior filters to the proposed nonverbal behaviors in the XML tree.
    Essentially keeps track of the registered filters and applies them in order.
    <h3>Change log:</h3>
    <table border="1">
    <tr><th>Date<th>Who<th>What</tr>
    <tr><td>4/1/01<td>T. Bickmore<td> Created. </tr>
    </table>
*/

public class NVBFilterModule extends BeatModule {
  private Vector filters=new Vector(); //of NVBFilter
  private NVBTypes nvbTypes;

    /** Constructor requires an NVBTypes object, which provides information
	about all of the tag names used to specify nonverbal behaviors, as
	well as the BEAT module to output to. */
  public NVBFilterModule(NVBTypes nvbTypes,BeatModule output) {
    super(output);
    this.nvbTypes=nvbTypes;
  }

    /** Registers a filter with the NVBFilterModule. */
  public void register(NVBFilter filter) {
    filters.addElement(filter);
    filter.setModule(this);
    System.out.println("    Registering "+filter.getName());
  }
  
    /** Does the work of this module by calling each of the registered
	filters in order on the given XML tree and returning the resulting
	XML tree. */
  public Document transduce(Document xml) throws Exception {
    if(DEBUG) System.out.println("NVBFilterModule running...");
    for(int i=0;i<filters.size();i++) {
      NVBFilter filter=(NVBFilter)filters.elementAt(i);
      if(DEBUG) System.out.println("  Running "+filter);
      xml=filter.run(xml);
    };
    return xml;        
  }


    /** Does the work of this module by calling each of the registered
	filters in order on the given XML tree and returning the resulting
	XML tree. Operates on string representations of the XML tree. */
  public String transduce(String xml) throws Exception {
    return XMLWrapper.toString(transduce(XMLWrapper.parseXML(xml)));
  }

    /** Returns a handle to a NVBTypes object, which contains information
	about all of the tags used to specify nonverbal behaviors. */
  public NVBTypes getNVBTypes() { return nvbTypes; }
}
