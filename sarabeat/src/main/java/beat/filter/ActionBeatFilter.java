/* -------------------------------------------------------------------------

   ActionBeatFilter.java
     - A nonverbal behavior filter for the BEAT gesture toolkit

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

import beat.utilities.XMLWrapper;
import org.w3c.dom.*;

/** Removes all gestures of type "beat", such as 
    <pre>
    &lt;GESTURE_LEFT TYPE="BEAT"&gt; 
    
    </pre>
    from within the temporal duration of an Action gesture, such as
    <pre>
    &lt;GESTURE_BOTH&gt;
    &nbsp;&nbsp;...
    &nbsp;&nbsp;&lt;ACTION&gt;
    &nbsp;&nbsp;&nbsp;&nbsp;...
    &nbsp;&nbsp;&lt;/ACTION&gt;
    &lt;/GESTURE_BOTH&gt;

    </pre>
    This is necessary because usually beat gestures can be coarticulated 
    and therefore they will not be otherwise filtered out by the 
    NVBConflictFilter when placed within an Action gesture.
    (Obviously this only works if the linguistic-tags filter is
    moved to the end of the filter module in the pipeline, in order
    to retain the &lt;Action&gt; tags.)
    <h3>Change log:</h3>
    <table border="1">
    <tr><th>Date<th>Who<th>What</tr>
    <tr><td>6/29/01<td>Y. Gao<td>Created.</tr>
    </table>
*/

public class ActionBeatFilter extends NVBFilter {
    XMLWrapper xmlw = null;
    /**Primary XML transducer that does all the work of this filter.
       It gathers a list of Action nodes and beat gesture nodes from the
       XML document and prunes out those beats that reside within the
       temporal duration of any Action gestures. (Temporal location and
       duration are determined from WORDINDEX and WORDCOUNT values of
       nodes as produced by computeWordIndex method in XMLWrapper.)
    */
    public Document run(Document xml) throws Exception {

	xmlw=new XMLWrapper(xml);
	xmlw.computeWordIndex();

	//System.out.println("Input to ActionBeatFilter is ------->");
	//xmlw.pprint();
	//System.out.println();

	Vector actionNodes = xmlw.getAllNodesOfType("ACTION");

	//filter out all the actions that do not generate a gesture
	//from actionNodes.
	for(int i = 0; i < actionNodes.size(); ){
	    Element e = (Element)actionNodes.get(i);
	    if((xmlw.getAncestorOfType(e, "GESTURE_LEFT") != null)||
	       (xmlw.getAncestorOfType(e, "GESTURE_RIGHT") != null)||
	       (xmlw.getAncestorOfType(e, "GESTURE_BOTH") != null)){
		i++;
	    }
	    else{
		actionNodes.remove(i);
	    }
	}

	Vector beats = xmlw.getAllNodesOfType(new String[]{"GESTURE_RIGHT" , "GESTURE_LEFT" , "GESTURE_BOTH"});

	for(int i = 0; i < beats.size(); ){
	    Element e = (Element)beats.get(i);
	    if(!(XMLWrapper.getXMLAttribute(e, "TYPE").equals("BEAT"))){
		beats.remove(i);
	    }
	    else{
		i++;
	    }
	}

	Element actionNode = null;
	int startIndex;
	int endIndex;

	for(int i = 0; i < actionNodes.size(); i++){
	    actionNode = (Element)actionNodes.get(i);
	    startIndex = ((Integer)xmlw.getNXMLAttribute(actionNode, "WORDINDEX")).intValue();
	    endIndex = startIndex + ((Integer)xmlw.getNXMLAttribute(actionNode, "WORDCOUNT")).intValue();
	    for(int j = 0; j < beats.size(); j++){
		Element beat = (Element)beats.get(j);
		int beatIndex = ((Integer)xmlw.getNXMLAttribute(beat, "WORDINDEX")).intValue();
		if(beatIndex >= startIndex && beatIndex <= endIndex){
		    xmlw.prune(beat);
		}
	    }
	}
	return xmlw.getDocument();		
    }
}



