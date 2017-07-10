/* -------------------------------------------------------------------------

   NVBTypes.java
     - Maintains information about the NVB tags for the BEAT gesture toolkit

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

import java.util.*;
import java.io.File;

import org.w3c.dom.*;

/** Maintains information about the nonverbal behavior tags created by the generators and
    used by the filters and scheduler. The information is loaded from an XML file
    specified in the constructor.
    <p>
    File format:
    <ul>
    <li> file ::= &lt;DATA&gt; dofdata nvbtypes coarticulation &lt;/DATA&gt;
    <li> dofdata ::= &lt;DOFS&gt; dof* &lt;/DOFS&gt; 
            <em> (just lists the independent DOFs used in the animation system) </em>
    <li> dof ::= &lt; dofname &gt; <em> (a 'degree of freedom', such as RIGHT_ARM or
          SPEECH, that is used by nonverbal behaviors, defined here for the purpose
	  of conflict resolution among proposed nonverbal behaviors). </em>
    <li> nvbtypes ::= &lt;NVBTYPES&gt; { &lt; nvbtype &gt; dof* &lt;/ nvbtype &gt;  }*
         &lt;NVBTYPES&gt; <em> (specifies the DOFs used by each nvbtype) </em>
    <li> coarticulation ::= &lt;COARTICULATION&gt; cp* &lt;/COARTICULATION&gt;
    <li> cp ::= &lt;CP&gt; nvbpattern nvbpattern &lt;/CP&gt; 
             <em> (defines a pair of nonverbal behaviors which can overlap in time
	            even though they both use one or more of the same DOFs) </em>
    <li> nvbpattern ::= &lt; nvbtype { attribute = "value" }* &gt;
    </ul>
    <h3>Change log:</h3>
    <table border="1">
    <tr><th>Date<th>Who<th>What</tr>
    <tr><td>4/1/01<td>T. Bickmore<td> Created. </tr>
    </table>
*/

public class NVBTypes {
    /** A list of all  nonverbal behavior tag names that might be generated. */
  protected String[] nvbTypes;
    /** A list of all DOFs used in the system. */
  protected String[] dofs;
    /** Maps a nonverbal behavior tag name to a list of the DOFs used by
	that nonverbal behavior (as an array of String). */
  protected Hashtable dofTable=new Hashtable(); //get(nvbType) -> String[] of dofs
    /** A list of the co-articulation pairs specified in the NVBTypes file.
	A Nx2 array of Element. 
	First array index simply indexes each pair, while the second array index
	indexes each part of the pair (0..1). */
  protected Element[][] coarticulation; //coarticulation[i][part]->NVB tag pattern; part::=0..1
    /** Given a nonverbal behavior tag name, this returns information about the DOFs the
	behavior requires. A hashtable indexed by the nonverbal behavior tag name returns
	an array of boolean which specifies for each of the dofs in order (see 'dofs' member)
	whether the behavior requires it or not. */
  protected Hashtable dofUsageTable=new Hashtable(); //get(nvbType) -> boolean[] t if uses DOF i

    /** Constructor takes a specification of the XML file of the format specified above. */
  public NVBTypes(File xmlFile) throws Exception {
    try {
      XMLWrapper xmlw=new XMLWrapper(xmlFile);
      xmlw.pruneAllNodesOfType(new String[]{xmlw.TEXT});

      //Parse DOFS
      Node dofsNode=xmlw.getFirstNodeOfType("DOFS");
      if(dofsNode==null) throw new Exception("Could not find <DOFS>.");
      NodeList dofChildren=dofsNode.getChildNodes();
      dofs=new String[dofChildren.getLength()];
      for(int i=0;i<dofChildren.getLength();i++)
	dofs[i]=dofChildren.item(i).getNodeName();
      
      //Parse NVBTypes
      Node nvbTypesNode=xmlw.getFirstNodeOfType("NVBTYPES");
      if(nvbTypesNode==null) throw new Exception("Could not find <NVBTYPES> info.");
      NodeList nvbChildren=nvbTypesNode.getChildNodes();
      nvbTypes=new String[nvbChildren.getLength()];
      for(int i=0;i<nvbChildren.getLength();i++) {
	boolean[] dofUsage=new boolean[dofs.length];
	Node nvbType=nvbChildren.item(i);
	nvbTypes[i]=nvbType.getNodeName();
	NodeList nvbTypeChildren=nvbType.getChildNodes();
	String[] nvbtDOFs=new String[nvbTypeChildren.getLength()];
	for(int j=0;j<nvbTypeChildren.getLength();j++) {
	  String DOF=nvbTypeChildren.item(j).getNodeName();
	  nvbtDOFs[j]=DOF;
	  int index=DOFIndex(DOF);
	  if(index<0) throw new Exception("Unknown DOF "+DOF);
	  dofUsage[index]=true;
	};
	dofTable.put(nvbType.getNodeName(),nvbtDOFs);
	dofUsageTable.put(nvbType.getNodeName(),dofUsage);
      };
      
      //Parse co-articulation info:
      Vector cnodes=xmlw.getAllNodesOfType("CP");
      coarticulation=new Element[cnodes.size()][2];
      for(int i=0;i<cnodes.size();i++) {
	Element pair=(Element)cnodes.elementAt(i);
	NodeList parts=pair.getChildNodes();
	if(parts.getLength()!=2) throw new Exception("Illegal coarticulation pair: "+pair);
	coarticulation[i][0]=(Element)parts.item(0);
	coarticulation[i][1]=(Element)parts.item(1);
      };
    }catch(Exception ex) {
      System.out.println("Error processing "+xmlFile);
      throw ex;
    };
  }

    /** Returns a list of all  nonverbal behavior tag names that might be generated. */
  public String[] getNVBTypes() { return nvbTypes; }
    /** Returns a list of all DOFs used in the system. */
  public String[] getDOFs() { return dofs; }
    /** Maps a nonverbal behavior tag name to a list of the DOFs used by
	that nonverbal behavior (as an array of String). */
  public String[] getDOFs(String nvbType) { return (String[])dofTable.get(nvbType); }
    /** Given a nonverbal behavior tag name, this returns information about the DOFs the
	behavior requires. A hashtable indexed by the nonverbal behavior tag name returns
	an array of boolean which specifies for each of the dofs in order (see 'dofs' member)
	whether the behavior requires it or not. */
  public boolean[] getDOFUsage(String nvbType) { return (boolean[])dofUsageTable.get(nvbType); }

    /** Returns the index position (0..N-1) of the specified DOF in the list of DOFs (see
	'dofs' member. */
  private int DOFIndex(String DOF) {
    for(int i=0;i<dofs.length;i++)
      if(DOF.equals(dofs[i]))
	return i;
    return -1;
  }

  /** Returns true if the two NVBTypes use the same DOF. */
  public boolean isConflict(String nvbType1,String nvbType2) {
    String[] s1dofs=getDOFs(nvbType1);
    String[] s2dofs=getDOFs(nvbType2);
    for(int i=0;i<s1dofs.length;i++)
      for(int j=0;j<s2dofs.length;j++)
	if(s1dofs[i].equals(s2dofs[j]))
	  return true;
    return false;
  }

    /** Given two Elements which each completely specify a nonverbal behavior,
	this returns true if they are allowed to co-articulate (i.e., co-occur
	as long as they do not start at exactly the same time). */
  public boolean canCoarticulate(Element nvb1,Element nvb2) {
    //Assume there's a small number of table entries...
    for(int i=0;i<coarticulation.length;i++) 
      if((XMLWrapper.elementMatch(nvb1,coarticulation[i][0]) && 
	  XMLWrapper.elementMatch(nvb2,coarticulation[i][1])) ||
	 (XMLWrapper.elementMatch(nvb2,coarticulation[i][0]) && 
	  XMLWrapper.elementMatch(nvb1,coarticulation[i][1])))
	return true;
    return false;
  }

}
