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

/** Generates gaze for any hearers present in the participation framework based on 
    the speaker's gaze.  This generator has to be run <b>after</b> the speaker's
    behavior generators.

    @author Hannes
    @version 1.0, 03/21/2002

 */

public class NonSpeakerGazeGenerator extends  NVBGenerator {
    public Document run(Document xml) {
	ParticipationFramework pf = this.module.getParticipationFramework(getScene(xml));
	if(pf==null) return xml;
	Vector hearers = pf.getHearers();
	Vector nodes = XMLWrapper.getAllNodesOfType(xml,"GAZE");
	
	String direction;
	String name;
	//System.out.println(nodes+"\n");
	for(int i=0;i<nodes.size();i++) {
	    Element element=(Element)nodes.elementAt(i);
	
	    direction = element.getAttribute("DIRECTION");
    
	    if(direction.equals("AWAY_FROM_HEARER")) {
		for(int h=0;h<hearers.size();h++) {
		    Element newelement = xml.createElement("HEARER_GAZE");
		    name = (String)hearers.elementAt(h);
		    newelement.setAttribute("HEARER",name);
		    newelement.setAttribute("DIRECTION","TOWARDS_SPEAKER");
		    XMLWrapper.spliceParent(element,newelement);
		}
	    } else if(direction.equals("TOWARDS_HEARER")) {
		for(int h=0;h<hearers.size();h++) {
		    Element newelement = xml.createElement("HEARER_GAZE");
		    name = (String)hearers.elementAt(h);
		    newelement.setAttribute("HEARER",name);
		    if((name==pf.getAddressee())||(pf.getAddressee()==null)) {
			newelement.setAttribute("DIRECTION","TOWARDS_SPEAKER");
			XMLWrapper.spliceParent(element,newelement);
		    } else {
			newelement.setAttribute("DIRECTION","TOWARDS_HEARER");
			newelement.setAttribute("FOCUS",pf.getAddressee());
			Element lastword = (Element)XMLWrapper.getLastNodeOfType(element,"W");
			XMLWrapper.spliceParent(lastword,newelement);
		    }
		}
		
	    }
    	    
	}
    
	return xml;
    }
}



