/* -------------------------------------------------------------------------

   BeatModule.java
     - Abstract class for an XML transducer in the BEAT gesture toolkit

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

/** Abstract class for an XML transducer used in the BEAT pipeline.
    Now supports both text XML and DOM pipelines. 
    <br>
    Primary usage is to construct with the BeatModule to output to as an
    argument, then call module.process() on the XML to be processed. If passed
    a String, all modules will be called with String representations. If passed
    a DOM tree, all modules will be called with DOM representations. 
    Each module can also have an input and/or output tracer which is simply
    passed a text representation of the tree before/after it is invoked. 
    Note that copies of the DOM tree need not be made within modules; it is assumed
    they can freely munge their input.
    <h3>Change log:</h3>
    <table border="1">
    <tr><th>Date<th>Who<th>What</tr>
    <tr><td>12/12/00<td>T. Bickmore<td>Created as GBModule.</tr>
    <tr><td>4/1/01<td>T. Bickmore<td>Changed to BeatModule and adapted for w3c.dom trees. 
               Added ability for both text and DOM tree pipelines. </tr>
    </table>
*/

public class BeatModule {
  protected BeatModule outputTo=null;

  /** Utility member that can be used to turn debugging messages on and off. */
  public boolean DEBUG = false;

  /** Primary constructor. Passed the BeatModule this module is to output to. */
  public BeatModule(BeatModule outputTo) { 
	  System.out.println("["+this.getClass().getName()+"] Initializing"); 
	  this.outputTo=outputTo; 
  }

  /** Constructor for modules which do not output (end of the pipeline). */
  public BeatModule() {
	  System.out.println("["+this.getClass().getName()+"] Initializing"); 
  }

  /** Sets the debugging flag status, intended to control the display of debugging
    messages. This does not control the behavior of input and output tracers; these
    are always called. */
  public void setDEBUG(boolean DEBUG) { this.DEBUG=DEBUG; }

  //------------------------ TEXT PIPELINE -----------------
  /** Primary method call to invoke this module on the text representation of 
    an XML tree. */
  public void process(String xml) throws Exception {
    if(inputTracer!=null) inputTracer.trace(xml);
    String result=transduce(xml);
    if(outputTracer!=null) outputTracer.trace(result);
    if(outputTo!=null) outputTo.process(result);
  }

    /** Identical to process, except returns the results of the final module
        in the pipeline as a string. */
    public String processAndReturn(String xml) throws Exception {
	if(inputTracer!=null) inputTracer.trace(xml);
	String result=transduce(xml);
	if(outputTracer!=null) outputTracer.trace(result);
	if(outputTo!=null) 
	    return outputTo.processAndReturn(result);
	else
	    return result;
    }

  /** The primary method that needs to be overriden by each module implementation to
    do the work of the module. Takes the input XML tree as input, returns the output tree. 
    String version. */
  protected String transduce(String xml) throws Exception {
    return xml; 
  }  //XML transducer

  //------------------------ DOM PIPELINE -----------------
  /** Primary method call to invoke this module on the DOM representation of 
    an XML tree. */

  public void process(Document xml) throws Exception {
    if(inputTracer!=null) inputTracer.trace(XMLWrapper.toString(xml));
    Document result=transduce(xml);
    if(outputTracer!=null) outputTracer.trace(XMLWrapper.toString(result));
    if(outputTo!=null) outputTo.process(result);
  }

  /** The primary method that needs to be overriden by each module implementation to
    do the work of the module. Takes the input XML tree as input, returns the output tree. 
    DOM version. */
  protected Document transduce(Document xml) throws Exception {
    return xml; 
  }  //XML transducer

  //Logging & debugging:
  /** Handle to the input tracer object, if it exists. */
  protected BeatModuleTracer inputTracer;
  /** Handle to the output tracer object, if it exists. */
  protected BeatModuleTracer outputTracer;

  /** Sets the input tracer object for the module, called with a string
    representation of the XML before it begins processing. 
    Can call with null to disable tracing. */
  public void setInputTracer(BeatModuleTracer tracer) { inputTracer=tracer; }

  /** Sets the output tracer object for the module, called with a string
    representation of the XML after it finished processing.
    Can call with null to disable tracing. */
  public void setOutputTracer(BeatModuleTracer tracer) { outputTracer=tracer; }
}

