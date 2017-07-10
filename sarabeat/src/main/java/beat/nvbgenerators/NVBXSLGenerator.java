/* -------------------------------------------------------------------------

   NVBXSLGenerator.java
     - An XSL-based behavior generator for the BEAT gesture toolkit

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

import java.io.*;

import beat.utilities.XMLWrapper;
import org.w3c.dom.*;

/** A nonverbal behavior generator whose operation is specified
    in XSL/T script files. The XSL/T file is parsed and loaded
    during the construction of the generator object. The XSL file
    must function as an XML transducer and copy the input tree
    to the output in addition to specifying any new tags to be added.
    Information on XSL syntax can be found at:
    <br> http:://www.w3.org/TR/xslt
    <h3>Change log:</h3>
    <table border="1">
    <tr><th>Date<th>Who<th>What</tr>
    <tr><td>4/1/01<td>T. Bickmore<td> Created. </tr>
    <tr><td>2/20/02<td>H. Vilhjalmsson<td> Can return its name.</tr>
    </table>
*/

public class NVBXSLGenerator extends NVBGenerator {
    /** Handle to the loaded XSL. */
  XMLWrapper.Transform transform;

    String m_xslFileName;

    /** Constructor takes a specification of the location of the XSL file.
	Parsing occurs within the constructor. */
  public NVBXSLGenerator(File xslFile) throws Exception {
      m_xslFileName = xslFile.toString();
      //System.out.println("  Loading XSL generator "+xslFile+"...");
    try {
      transform=new XMLWrapper.Transform(xslFile);
    }catch(Exception e) {
      System.out.println("  Error loading "+xslFile+":\n");
      throw e;
    };
  }

    public String getName() { return "NVBXSLGenerator ("+m_xslFileName+")"; }

    /** Does the work of the generator, as an XML transducer. */
  public Document run(Document xml) throws Exception {
    return XMLWrapper.applyTransform(transform,xml);
  }
}
