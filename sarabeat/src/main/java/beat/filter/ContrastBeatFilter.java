/* -------------------------------------------------------------------------

   ContrastBeatFilter.java
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

import org.w3c.dom.*;

import beat.utilities.XMLWrapper;

/** Removes all gestures of type "beat", such as
    <pre>
    &lt;GESTURE_LEFT TYPE="BEAT"&gt;

    </pre>
    from within the temporal duration of a PAIR of contrasting gestures,
    such as
    <pre>
    &lt;GESTURE_LEFT TYPE="CONSTRAST_1"&gt;
    &nbsp;&nbsp;...
    &lt;/GESTURE_LEFT&gt;
    ...
    &lt;GESTURE_RIGHT TYPE="CONTRAST_2"&gt;
    &nbsp;&nbsp;...
    &lt;/GESTURE_RIGHT&gt;

    </pre>
    Intended to be an additional filter, processed after
    NVBConflictFilter. This does not look at contrast gestures
    that are not pairs.
    <h3>Change log:</h3>
    <table border="1">
    <tr><th>Date<th>Who<th>What</tr>
    <tr><td>6/29/01<td>Y. Gao<td>Created.</tr>
    </table>
*/

public class ContrastBeatFilter extends NVBFilter {
    XMLWrapper xmlw = null;
    
    /**Primary XML transducer that does all the work of this filter.
       It gathers a list of contrast gesture nodes and beat gesture nodes 
       from the XML document and prunes out those beats that reside within the
       temporal duration of any PAIRED constrast gestures. 
       (Temporal location and duration are determined from WORDINDEX and 
       WORDCOUNT values of nodes as produced by computeWordIndex method in 
       XMLWrapper.)
    */
    public Document run(Document xml) throws Exception {
	xmlw=new XMLWrapper(xml);
	xmlw.computeWordIndex();
	Vector contrastPairs = xmlw.getAllNodesOfType(new String[]{"GESTURE_RIGHT" , "GESTURE_LEFT"});

	for(int i = 0; i < contrastPairs.size(); ){
	    Element e = (Element)contrastPairs.get(i);

	    if(!(XMLWrapper.getXMLAttribute(e, "TYPE").equals("CONTRAST_1") || 
		 XMLWrapper.getXMLAttribute(e, "TYPE").equals("CONTRAST_2"))){
		contrastPairs.remove(i);
	    }
	    else{
		i++;
	    }
	}

	//as a check, after prunning, there should be an even number of
	//elements in the contrastPairs vector.
	if((contrastPairs.size() % 2) != 0){
	    System.out.println("Error discovered in ConstrastBeatFilter:\nVector contrastPairs does not contain an even number of elements after prunning.");
	    System.exit(-1);
	}
	
	//sort contrastPairs according to wordindex
	Collections.sort(contrastPairs, new myComparator());

	//Now it is assumed that the sorted contrastPairs are ordered
	//contrast_1, contrast_2, contrast_1, contrast_2, etc.

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

	Element start = null;
	Element end = null;
	int startIndex;
	int endIndex;

	for(int i = 0; i < contrastPairs.size(); i=i+2){
	    start = (Element)contrastPairs.get(i);
	    startIndex = ((Integer)xmlw.getNXMLAttribute(start, "WORDINDEX")).intValue();
	    end = (Element)contrastPairs.get(i+1);
	    endIndex = ((Integer)xmlw.getNXMLAttribute(end, "WORDINDEX")).intValue();
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

    private class myComparator implements Comparator {
	static final int LESS_THAN = -1;
	static final int EQUAL_TO = 0;
	static final int GREATER_THAN = 1;

	Element node1 = null;
	Element node2 = null;
	int value1;
	int value2;

	public int compare(Object o1, Object o2){

	    node1 = (Element)o1;
	    node2 = (Element)o2;
	    value1 = ((Integer)xmlw.getNXMLAttribute(node1, "WORDINDEX")).intValue();
	    value2 = ((Integer)xmlw.getNXMLAttribute(node2, "WORDINDEX")).intValue();

	    if(value1 < value2){
		return LESS_THAN;
	    }
	    else if(value1 > value2){
		return GREATER_THAN;
	    }
	    else{
		return EQUAL_TO;
	    }
	}
    }
}
