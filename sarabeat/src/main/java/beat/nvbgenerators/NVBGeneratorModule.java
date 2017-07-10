/* -------------------------------------------------------------------------

   NVBGeneratorModule.java
     - Registration module for all generators in the BEAT gesture toolkit

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

import beat.kb.KnowledgeBase;
import beat.utilities.BeatModule;
import beat.utilities.NVBTypes;
import beat.utilities.XMLWrapper;

/** A BEAT pipeline module which applies all of the registered
    nonverbal behavior generators in order to annotate the XML
    tree with proposed nonverbal behaviors.
    <h3>Change log:</h3>
    <table border="1">
    <tr><th>Date<th>Who<th>What</tr>
    <tr><td>4/1/01<td>T. Bickmore<td> Created. </tr>
    <tr><td>2/20/02<td>H. Vilhjalmsson<td> ParticipationFrameworkBase added.</tr>
    </table>
*/

public class NVBGeneratorModule extends BeatModule {
  private Vector generators=new Vector(); //of NVBGenerator
  private NVBTypes nvbTypes;
    private KnowledgeBase kb;
    private ParticipationFrameworkBase pfb;
    /** Constructor requires:
	<ul>
	<li>the NVBTypes object which describes the tags which specify nonverbal behaviors.
	<li>the KnowledgeBase objet which contains information about objects, attributes,
	    and gestures in the application domain.
	<li>the ParticipationFrameworkBase that contains information about who is present, and
	<li>the BEAT module to output to
	</ul>
    */
  public NVBGeneratorModule(NVBTypes nvbTypes, KnowledgeBase kb, BeatModule output) {
    super(output);
    this.nvbTypes=nvbTypes;
    this.kb = kb;
    this.pfb = null;
  }

  public NVBGeneratorModule(NVBTypes nvbTypes, KnowledgeBase kb, ParticipationFrameworkBase pfb, BeatModule output) {
    super(output);
    this.nvbTypes=nvbTypes;
    this.kb = kb;
    this.pfb = pfb;
  }

    /** Registers a nonverbal behavior generator. Generators are run in the order registered. */
  public void register(NVBGenerator generator) {
    generators.addElement(generator);
    generator.setModule(this);
    System.out.println("    Registering "+generator.getName());
  }

    /** Does the work of the module by running each of the generators in order.
	An XML transduder. */
  public Document transduce(Document xml) throws Exception {
    if(DEBUG) System.out.println("\nNVBGeneratorModule running...");
    for(int i=0;i<generators.size();i++) {
      NVBGenerator generator=(NVBGenerator)generators.elementAt(i);
      if(DEBUG) System.out.println("  Running "+generator);
      xml=generator.run(xml);
    };
    return xml;        
  }
  
    /** Returns the NVBTypes object specified in the constructor (provides access
	for generators). */
  public NVBTypes getNVBTypes() { return nvbTypes; }
  
    /** Returns the KnowledgeBase object specified in the constructor (provides
	access for generators). */
  public KnowledgeBase getKnowledgeBase() { return kb; }
  
    /** Returns the ParticipationFramework object specified in the constructor (provides
	access for generators). */
    public ParticipationFrameworkBase getParticipationFrameworkBase() { return pfb; }
    public ParticipationFramework getParticipationFramework(String sceneid) { 
	if(pfb!=null) 
	    return pfb.getParticipationFramework(sceneid); 
	else 
	    return null;
    }

    /** Does the work of the module by running each of the generators in order.
	Operates on string representations of the XML tree. */
  public String transduce(String xml) throws Exception {
    return XMLWrapper.toString(transduce(XMLWrapper.parseXML(xml)));
  }
}
