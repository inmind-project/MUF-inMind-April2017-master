/* -------------------------------------------------------------------------

   PPrintTracer.java
     - An implementation of BeatModuleTracer for the BEAT gesture toolkit

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

/** An implementation of BeatModuleTracer which displays a pretty-printed
  (indented) representation of the XML tree to System.out, preceded by a specified text
  prompt.

    <h3>Change log:</h3>
    <table border="1">
    <tr><th>Date<th>Who<th>What</tr>
    <tr><td>6/7/01<td>T. Bickmore<td>Created.</tr>
    </table>
    */

public class PPrintTracer implements BeatModuleTracer {
  private String prompt;

  /** Pass the text prompt you would like displayed at the top of the tree
      in to the constructor. */
  public PPrintTracer(String prompt) {
    this.prompt=prompt;
  }

  /** The method called by BeatModule to trace input or output. */
  public void trace(String xml) {
    System.out.println("\n----"+prompt+"--->>\n");
    try { new XMLWrapper(xml).pprint(); }catch(Exception efoo){};
    System.out.println("");
  }
}
