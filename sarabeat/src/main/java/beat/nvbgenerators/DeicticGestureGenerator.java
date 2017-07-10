/* -------------------------------------------------------------------------

   DeicticGestureGenerator.java
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

/** Generates deictic gestures for objects that have deictics associated
    with them. Not yet fully developed and is current meant to be a 
    place-holder for future additions. 
    The only rule currently in this generator is the MUTUALLY_OBSERVABLE rule, 
    which would match on the &lt;OBJECT&gt; tag in the following
    <pre>
    &lt;UTTERANCE SCENE="abc"&gt;
    &nbsp;&nbsp;...
    &nbsp;&nbsp;&lt;RHEME&gt;
    &nbsp;&nbsp;&nbsp;&nbsp;...
    &nbsp;&nbsp;&nbsp;&nbsp;&lt;OBJECT ID="xyz"&gt;
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;...
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;NEW&gt;
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;...
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;/NEW&gt;
    &nbsp;&nbsp;&nbsp;&nbsp;&lt;/OBJECT&gt;
    &nbsp;&nbsp;&lt;/RHEME&gt;
    &lt;/UTTERANCE&gt;
    
    </pre>
    if the object is indeed visible in the current scene. This criteria
    is determined by looking up the location of the object in the 
    knowledge base using the object's ID attribute value and comparing it
    with the value of the SCENE attribute of the enclosing utterance.
    If they match, then the object is "mutually observable".

    <h3>Change log:</h3>
    <table border="1">
    <tr><th>Date<th>Who<th>What</tr>
    <tr><td>6/29/01<td>Y. Gao<td>Created.</tr>
    </table>
*/
public class DeicticGestureGenerator extends NVBGenerator {
    
    /** Does all the work of the generator. An XML transducer. (Additional
	rules to generate deictic gestures should be placed here.)
    */
    public Document run(Document xml) {
	Vector nodes = XMLWrapper.getAllNodesOfType(xml, "OBJECT");
	XMLWrapper xmlw = new XMLWrapper(xml);
	Element node, gesture;
	String id, value, type;
	
	for(int i=0; i<nodes.size(); i++){
	    node = (Element)nodes.elementAt(i);
	    type = node.getTagName();
	    id = node.getAttribute("ID");

	    //place rules below

	    //MUTUALLY_OBSERVABLE rule (Yang 6/27)
	    if((XMLWrapper.getAncestorOfType(node,"RHEME")!=null)&&
	       (XMLWrapper.getFirstNodeOfType(node,"NEW")!=null)){
		//	       (module.getKnowledgeBase().isObservable(sceneName, type))){
		String sceneName = "";
		Node uNode = XMLWrapper.getAncestorOfType(node, "UTTERANCE");
		sceneName = (String)xmlw.getXMLAttribute((Element)uNode, "SCENE");
		
		if(sceneName.equals("")){
		    sceneName = "LOCAL";
		}
		
		if(module.getKnowledgeBase().isObservable(sceneName, id)){
		    //this is a hack, must fix. should have deictic with target
		    //gesture = module.getKnowledgeBase().getCompactGestureElement(xml, "HERE"); 

		    // -- hhv 112101 - fixed hack
		    gesture = xml.createElement("GESTURE_RIGHT"); 
		    gesture.setAttribute("TYPE","DEICTIC");
		    gesture.setAttribute("TARGET",id);
		    // --

		    if(gesture != null){
			gesture.setAttribute("PRIORITY", "20"); 	
			XMLWrapper.spliceParent(node, gesture);
		    }

		    /* Commenting this out for now - useful only for SPARK, figure out a way to 
                       have this co-exist with general BEAT  -- hhv
		    Element gaze = xml.createElement("GAZE"); 
		    gaze.setAttribute("DIRECTION","AWAY_FROM_HEARER");
		    gaze.setAttribute("FOCUS",id);
		    // --

		    if(gaze != null){
			gaze.setAttribute("PRIORITY", "5"); 	
			XMLWrapper.spliceParent(node, gaze  );
		    }

		    */

		}
		else{
		    //do nothing since object is not mutually observable.
		}
	    }	    
	}
	return xml;
    }
}




