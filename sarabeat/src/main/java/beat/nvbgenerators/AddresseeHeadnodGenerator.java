/* -------------------------------------------------------------------------

   MonologuePostureShiftGenerator.java
     - A behavior generator for the BEAT gesture toolkit

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

/** Generates feedback headnods in addressee based on feedback eliciting behaviors found
    in the speaker's already generated behaviors.  Therefore this generator has to be
    run <b>after</b> the speaker behavior generators. 
    @author Hannes
    @version 1.0, 02/22/2002
 */

public class AddresseeHeadnodGenerator extends  NVBGenerator {
    public Document run(Document xml) {
	ParticipationFramework pf = this.module.getParticipationFramework(getScene(xml));
	if(pf==null) return xml;

	Vector nodes = XMLWrapper.getAllNodesOfType(xml,"EYEBROWS");
	
	String name = pf.getAddressee();

	if(name.length()==0) return xml;

	//System.out.println(nodes+"\n");
	for(int i=0;i<nodes.size();i++) {
	    Element element=(Element)nodes.elementAt(i);
	    
	    Element nextelement = (Element)element.getNextSibling();
	    if(nextelement!=null) { 
		if(nextelement.getNodeName().equals("EYEBROWS")) continue;
	    }
	    //System.out.println(element+"\n");
	    Element lastword = (Element)XMLWrapper.getLastNodeOfType(element,"W");
	    if(lastword!=null) {
		//System.out.println(lastword+"\n");
		Element parentdone = (Element)XMLWrapper.getAncestorOfType(lastword,"HEARER_HEADNOD");
		if(parentdone==null) {
		    Element newelement = xml.createElement("HEARER_HEADNOD");
		    newelement.setAttribute("HEARER",name);
		    newelement.setAttribute("AMPLITUDE","LOW");
		    newelement.setAttribute("LOD","MEDIUM");
		    XMLWrapper.spliceParent(lastword,newelement);
		}
	    }
	    
	}
    
	return xml;
    }
}



