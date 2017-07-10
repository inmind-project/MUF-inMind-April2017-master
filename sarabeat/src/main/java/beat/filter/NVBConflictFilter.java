/* -------------------------------------------------------------------------

   NVBConflictFilter.java
     - A NVB filter for the BEAT gesture toolkit

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

//Need to fix:
// 1. off-by-article check

/** Removes conflicting proposed nonverbal behaviors (NVBs).
    Two NVBs which use the same degree-of-freedom (DOF) can never have the
    same peak (start) time.
    Two NVBs which use the same DOF cannot overlap, unless they are declared to
    co-articulate (see NVBTypes.xml). Higher priority wins. Same priority results
    in arbitrary selection. 
    <h3>Change log:</h3>
    <table border="1">
    <tr><th>Date<th>Who<th>What</tr>
    <tr><td>4/1/01<td>T. Bickmore<td> Created. </tr>
    <tr><td>3/26/02<td>T. Bickmore<td> Fixed 'isOverlap'. </tr>    
    </table>
*/

public class NVBConflictFilter extends NVBFilter {

    /** Does all the work of the filter. An XML transducer. */
  public Document run(Document xml) throws Exception {
      XMLWrapper xmlw=new XMLWrapper(xml);
      xmlw.computeWordIndex();
      Vector words=extractWords(xml);

      //Collect list of suggestions per DOF..
      String[] dofs=module.getNVBTypes().getDOFs();
      Vector[] DOFSuggestions=new Vector[dofs.length];
      for(int i=0;i<dofs.length;i++) DOFSuggestions[i]=new Vector();
      gatherSuggestions(xmlw.getAllNodesOfType(module.getNVBTypes().getNVBTypes()),DOFSuggestions);

      //Now work per DOF, resolving conflicts:
      for(int dof=0;dof<dofs.length;dof++) {
	Vector nvbs=DOFSuggestions[dof];
	//Work in descending order of priority...
	for(int i=0;i<nvbs.size()-1;i++) {
	  Element nvb=(Element)nvbs.elementAt(i);
	  int nvbStartIndex=((Integer)xmlw.getNXMLAttribute(nvb,"WORDINDEX")).intValue()-1;
	  int nvbEndIndex=nvbStartIndex+((Integer)xmlw.getNXMLAttribute(nvb,"WORDCOUNT")).intValue()-1;
	  //If any other nvbs conflict with this one then prune *them*.
	  //I know, this is N^2...can you do it for less? --but separated by DOF
	  for(int j=i+1;j<nvbs.size();j++) {
	    Element conflictor=(Element)nvbs.elementAt(j);
	    int conStartIndex=((Integer)xmlw.getNXMLAttribute(conflictor,"WORDINDEX")).intValue()-1;
	    int conEndIndex=conStartIndex+((Integer)xmlw.getNXMLAttribute(conflictor,"WORDCOUNT")).intValue()-1;
	    if(module.DEBUG) System.out.println("    Checking "+nvb.getNodeName()+"@WORD"+nvbStartIndex+"-"+nvbEndIndex+
						" against "+conflictor.getNodeName()+"@WORD"+conStartIndex+"-"+conEndIndex);
	    if(withinAnArticle(nvbStartIndex,conStartIndex,words)|| //never allowed
	       (isOverlap(nvbStartIndex,nvbEndIndex,conStartIndex,conEndIndex) &&
		!canCoarticulate(nvb,conflictor))) { 
	      //delete the conflictor & do *not* increment 'j'!
	      if(module.DEBUG) System.out.println("    Removing "+conflictor.getNodeName()+"@WORD"+conStartIndex);
	      nvbs.removeElement(conflictor);
	      xmlw.prune(conflictor);
	      j--; //keep same j value next iteration...since object at that idx is same
	    };
	  };
	};
      };
      return xmlw.getDocument();
  }

    //note: still doesn't completely take care of problem...
    private boolean canCoarticulate(Element nvb1,Element nvb2) {
	//temp hack...beat beat can't co-articulate with contrast
	if(nvb1.getNodeName().startsWith("GESTURE_") && nvb2.getNodeName().startsWith("GESTURE_")) {
	    String t1=nvb1.getAttribute("TYPE");
	    String t2=nvb2.getAttribute("TYPE");
	    if((t1.equals("BEAT") && t2.startsWith("CONTRAST"))||
	       (t2.equals("BEAT") && t1.startsWith("CONTRAST"))) {
		return false;
	    } else {
		return t1.equals("BEAT")||t2.equals("BEAT");
	    }
	};
	return module.getNVBTypes().canCoarticulate(nvb1,nvb2);
    }

  /** Each vector of suggestions is kept sorted by descending priority: */
  private void gatherSuggestions(Vector nvbs,Vector[] suggestions) {
    int numDOFs=module.getNVBTypes().getDOFs().length;
    for(int i=0;i<nvbs.size();i++) {
      Element nvb=(Element)nvbs.elementAt(i);
      boolean[] DOFs=module.getNVBTypes().getDOFUsage(nvb.getNodeName());
      for(int j=0;j<numDOFs;j++)
	if(DOFs[j])
	  insertNVB(nvb,suggestions[j]);
    };
  };
  
  //Insert suggestion into suggestions, by descending priority:
  private void insertNVB(Element nvb,Vector suggestions) {
    int nvbPriority=NVBPriorityFilter.getPriority(nvb);
    for(int i=0;i<suggestions.size();i++)
      if(nvbPriority>=NVBPriorityFilter.getPriority((Element)suggestions.elementAt(i))) {
	suggestions.insertElementAt(nvb,i);
	return;
      };
    suggestions.addElement(nvb);
  }

  /** Returns true if the two integer intervals overlap, <em> including endpoints! </em> */
  public static boolean isOverlap(int start1,int end1,int start2, int end2) {
      //return !(end1<=start2 || end2<=start1);
      return !(end1<start2 || end2<start1);
  }

    /** Returns a Vector of the words in the XML tree (each as separate string). 
      Assumes each leaf text item is either a single word, or a single punctuation mark
      (see LanguageModule.SimpleTokenizer). */
  public static Vector extractWords(Document xml) {
    Vector words=new Vector();
    XMLWrapper.preorderTraversal(xml,new XMLWrapper.NodeVisitor(words) {
      public boolean visit(Node n) {
	if(n instanceof Text) {
	  String text=((Text)n).getData().replace('\"',' ').trim().toUpperCase();
	  if(text.length()>0) {
	      if(Character.isLetterOrDigit(text.charAt(0)))
		  ((Vector)argument).addElement(((Text)n).getData().replace('\"',' ').trim().toUpperCase());
	  }
        };
	return true;
      }});
    return words;
  }

    /** Returns true if i1==i2, or if either is an article, if they differ by one. */
  public static boolean withinAnArticle(int i1,int i2,Vector words) {
    //System.out.println("Checking if WORD"+i1+" = "+words.elementAt(i1)+" against WORD"+i2+" = "+words.elementAt(i2));
    if(i1==i2) return true;
    //See if word i1 or i2 is an article...
    if(i1<words.size() && isArticle((String)words.elementAt(i1)) && (i1+1)==i2) return true;
    if(i2<words.size() && isArticle((String)words.elementAt(i2)) && i1==(i2+1)) return true;
    return false;
  }

    /** List of articles in English. */
  public static final String[] articles={"A","AN","THE"};

    /** Returns true if the given word is an article. */
  public static boolean isArticle(String word) {
    for(int i=0;i<articles.length;i++) 
	if(word.equals(articles[i])) {
	  return true;
	};

    return false;
  }
}
