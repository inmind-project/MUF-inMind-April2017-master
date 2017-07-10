/* -------------------------------------------------------------------------

   NVBFilter.java
     - An abstract super class for all filters in the BEAT gesture toolkit

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

import org.w3c.dom.*;

/** Abstract class for a nonverbal behavior filter, intended to
    remove unwanted (proposed) nonverbal behaviors from the XML tree.
    Primary method is 'run' to modify the input XML tree. 
    <h3>Change log:</h3>
    <table border="1">
    <tr><th>Date<th>Who<th>What</tr>
    <tr><td>4/1/01<td>T. Bickmore<td> Created. </tr>
    </table>
*/

public abstract class NVBFilter {
    /** Handle to the NVBFilterModule, which maintains a list of the
	NVBTypes (tag names for all nonverbal behavior tags). */
  protected NVBFilterModule module;

    /** Sets the handle to the NVBFilterModule. Called during filter registration. */
  public void setModule(NVBFilterModule module) { this.module=module; }

    /** Returns the name of the Generator instance as a String */
    public String getName() { return this.getClass().getName(); }

    /** Primary method called to do the work of the filter, an XML transducer.
	Passed the XML tree, this is responsible for identifying nonverbal behaviors
	that need to be filtered and removing them directly from the tree. */
  public abstract Document run(Document xml) throws Exception;
}
  

