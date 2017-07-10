/* -------------------------------------------------------------------------

   ForkModule.java
     - Forks BEAT pipeline to two downstream modules.

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

import beat.utilities.BeatModule;
import beat.utilities.XMLWrapper;
import org.w3c.dom.*;

/** Forks BEAT pipeline to two downstream modules.
    <h3>Change log:</h3>
    <table border="1">
    <tr><th>Date<th>Who<th>What</tr>
    <tr><td>1/28/02<td>T. Bickmore<td>Created.</tr>
    </table>
*/

public class ForkModule extends BeatModule {
  /** Second module to output to. */
  private BeatModule outputTo2=null; 

  /** Primary constructor. Passed the BeatModule this module is to output to. */
  public ForkModule(BeatModule outputTo1,BeatModule outputTo2) { 
    super(outputTo1);
    this.outputTo2=outputTo2;
  }

  //------------------------ TEXT PIPELINE -----------------
  /** Primary method call to invoke this module on the text representation of 
    an XML tree. */
  public void process(String xml) throws Exception {
    if(inputTracer!=null) inputTracer.trace(xml);
    if(outputTracer!=null) outputTracer.trace(xml);
    try {
	if(outputTo!=null) outputTo.process(xml);
    }catch(Exception e){
	System.err.println("FORK: caught exception in output #1 ("+outputTo+"):\n  "+e);
	throw e;
    };
    try {
	if(outputTo2!=null) outputTo2.process(xml);
    }catch(Exception e){
	System.err.println("FORK: caught exception in output #2 ("+outputTo2+"):\n  "+e);
	throw e;
    };
  }

    /** Identical to process, except returns the results of the final module
        in the pipeline as a string. Concatenates the results of the two outputs. */
    public String processAndReturn(String xml) throws Exception {
	if(inputTracer!=null) inputTracer.trace(xml);
	if(outputTracer!=null) outputTracer.trace(xml);
	String result="";
	if(outputTo!=null) 
	  result=outputTo.processAndReturn(xml);
	if(outputTo2!=null)
	  result+=outputTo2.processAndReturn(xml);
	return result;
    }

  //------------------------ DOM PIPELINE -----------------
  /** Primary method call to invoke this module on the DOM representation of 
    an XML tree. */

  public void process(Document xml) throws Exception {
    if(inputTracer!=null) inputTracer.trace(XMLWrapper.toString(xml));
    if(outputTracer!=null) outputTracer.trace(XMLWrapper.toString(xml));
    try {
	if(outputTo!=null) outputTo.process(xml);
    }catch(Exception e){
	System.err.println("FORK: caught exception in output #1 ("+outputTo+"):\n  "+e);
	throw e;
    };
    try {
	if(outputTo2!=null) outputTo2.process(xml);
    }catch(Exception e){
	System.err.println("FORK: caught exception in output #2 ("+outputTo2+"):\n  "+e);
	throw e;
    };
  }

}

