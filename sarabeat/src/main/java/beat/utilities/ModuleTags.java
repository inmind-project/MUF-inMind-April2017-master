/* -------------------------------------------------------------------------

   ModuleTags.java
     - Maintains information about the types of tags produced by each module for the BEAT gesture toolkit

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

/** Maintains information about the types of tags produced by each BEAT
    module. Typically loaded from XMLData\ModuleTags.xml. 
    <p>
    Source file syntax: 
<ul>
<li> file ::= &lt;DATA&gt; module* &lt;DATA&gt;
<li> module ::= &lt;MODULE NAME="name"&gt; tag* &lt;MODULE&gt;
<li> tag ::= &lt;tagname&gt;
</ul>
    <p>Following construction, getModuleTags returns an array of the tags
       output by each BEAT module (array of Strings).
    
    <h3>Change log:</h3>
    <table border="1">
    <tr><th>Date<th>Who<th>What</tr>
    <tr><td>4/1/01<td>T. Bickmore<td>Created.</tr>
    </table>
    */

public class ModuleTags {
  /** Stores the tags output by each module as a Hashtable of arrays of String.
      When indexed by BEAT module name (String) the Hashtable indexes an array
      of Strings which represent the tag names. */
  protected Hashtable moduleTags=new Hashtable(); //get(modulename) -> String[] of tagnames

  /** Constructs a ModuleTags object given the XML data file spec. 
      Loads the contents into a local data structure. */
  public ModuleTags(File xmlFile) throws Exception {
    try {
      XMLWrapper xmlw=new XMLWrapper(xmlFile);
      xmlw.pruneAllNodesOfType(new String[]{xmlw.TEXT});

      //Parse modules
      Vector modules=xmlw.getAllNodesOfType("MODULE"); //of element
      for(int i=0;i<modules.size();i++) {
	Element module=(Element)modules.elementAt(i);
	NodeList children=module.getChildNodes();
	String[] tags=new String[children.getLength()];
	for(int j=0;j<children.getLength();j++)
	  tags[j]=children.item(j).getNodeName();
	moduleTags.put(module.getAttribute("NAME"),tags);
      };
    }catch(Exception ex) {
      System.out.println("Error processing "+xmlFile);
      throw ex;
    };
  }

  /** Given the name of a BEAT module, returns the tag names output by that module
      as an array of Strings. */
  public String[] getModuleTags(String module) { return (String[])moduleTags.get(module); }

  //--------------------------- TEST STUB ------------------
  /*
  public static void main(String[] a) {
    try {
      ModuleTags mt=new ModuleTags(new File("XMLData\\ModuleTags.xml"));
      System.out.println("tags for Tagger:");
      String[] tags=mt.getModuleTags("Tagger");
      for(int i=0;i<tags.length;i++)
	System.out.println(tags[i]);
    }catch(Exception e){
      System.out.println("ex: "+e);
    };
  }
  */
}
