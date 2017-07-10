/* -------------------------------------------------------------------------

   McNeillCompiler.java

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
package beat.compiler;

import java.io.*;
import java.lang.*;
import java.util.*;

import org.w3c.dom.*;


/** Takes an un-flattened XML tree of speech and nonverbal behaviors
    and returns a text representation of the speech and nonverbal behaviors
    following McNeill 1992.
    <p>
    Currently sends output to the PrintStream specified in the constructor.
    <h3>Change log:</h3>
    <table border="1">
    <tr><th>Date<th>Who<th>What</tr>
    <tr><td>7/17/01<td>T. Bickmore<td> Created. </tr>
    <tr><td>1/12/02<td>T. Bickmore<td> Added posture shift. </tr>
    </table>
*/

public class McNeillCompiler extends Compiler {
    /** The output stream. */
  private PrintStream out;

  /** The list of NVBMap objects. Each of these is responsible for translating a single
      type of nonverbal behavior into various textual representations. */
  private Vector nvbMaps=new Vector();

  /** A list of strings, representing footnotes for the in-text annotations. */
  private Vector auxiliaryLegend=new Vector();

  /** The index number (for display purposes) of the last gesture output. */
  int gestureIndex;

    /** Constructor takes the PrintStream to output to. */
  public McNeillCompiler(PrintStream out) throws Exception {
    super();
    this.out=out;
    
    //Gaze away:
    nvbMaps.addElement(new TabularNVBMap("(GA)",null,"(GA) \t=\tgaze away from hearer/audience"){
      boolean isApplicable(Element e) {
	return e.getNodeName().equalsIgnoreCase("GAZE") && 
	       e.getAttribute("DIRECTION").equalsIgnoreCase("AWAY_FROM_HEARER");
      }});

    //Gaze towards:
    nvbMaps.addElement(new TabularNVBMap("(GT)",null,"(GT) \t=\tgaze towards hearer/audience"){
      boolean isApplicable(Element e) {
	return e.getNodeName().equalsIgnoreCase("GAZE") && 
	       e.getAttribute("DIRECTION").equalsIgnoreCase("TOWARDS_HEARER");
      }});

    //Eyebrows:
    nvbMaps.addElement(new TabularNVBMap("{","}","{  } \t=\teyebrows raised duration"){
      boolean isApplicable(Element e) {
	return e.getNodeName().equalsIgnoreCase("EYEBROWS");
      }});

    //Pitch accent:
    nvbMaps.addElement(new TabularNVBMap("*",null,"*    \t=\tpitch accent"){
      boolean isApplicable(Element e) {
	return e.getNodeName().equalsIgnoreCase("INTONATION_ACCENT");
      }});

    //Posture shift:
    nvbMaps.addElement(new TabularNVBMap("(PS)",null,"(PS) \t=\tposture shift"){
      boolean isApplicable(Element e) {
	return e.getNodeName().equalsIgnoreCase("POSTURESHIFT");
      }});

    //Gesture:
    nvbMaps.addElement(new NVBMap(){
      boolean isApplicable(Element e) {
	boolean ok= e.getNodeName().equalsIgnoreCase("GESTURE_RIGHT") ||
	       e.getNodeName().equalsIgnoreCase("GESTURE_LEFT") ||
	       e.getNodeName().equalsIgnoreCase("GESTURE_BOTH");
	if(ok) 
	  gestureIndex++;
	return ok;
      }
      String getInlinePrefixRepresentation(Element e) { 
	return "["+gestureIndex+" ";
      }
      String getInlinePostfixRepresentation(Element e) { return "]"; }
      String getAuxiliaryRepresentation(Element e) { 
	return "["+gestureIndex+" = "+formatGestureText(e);
      }});
  }

    /** Does the work of the module. */
  public String compile(Document xml) throws Exception {
    if(DEBUG) System.out.println("McNeillCompiler running...\n");
    gestureIndex=0;
    auxiliaryLegend.removeAllElements();

    //Output generic legend:
    out.println("\n\nLegend:\n");
    for(int i=0;i<nvbMaps.size();i++) {
      String item=((NVBMap)nvbMaps.elementAt(i)).getLegendRepresentation();
      if(item!=null) out.println(item);
    };

    //Output tree contents:
    out.println("\nOutput from BEAT:\n");
    compileTree(xml.getDocumentElement());

    //Output gestures, etc:
    out.println("\n");
    for(int i=0;i<auxiliaryLegend.size();i++)
      out.println(auxiliaryLegend.elementAt(i));

    //Have to return something...
    return null;
  }
    
  /** Recurses through the tree. For each node, if there is a NVBMap that
      isApplicable(), then a prefix is output, followed by recursive call
      to the children (in-order traversal), followed by postfix output.
      If the node is Text it is simply output and compileTree returns. */
  private void compileTree(Node node) {
    //If a Text node, just output it.
    if(node instanceof Text) 
      out.print(node.getNodeValue());
    else {

      //See if any of the NVBMaps apply...
      NVBMap map=null;
      for(int i=0;i<nvbMaps.size();i++)
	if(((NVBMap)nvbMaps.elementAt(i)).isApplicable((Element)node)) {
	  map=(NVBMap)nvbMaps.elementAt(i);
	  //See if any auxiliary footnotes need to be output.
	  String aux=map.getAuxiliaryRepresentation((Element)node);
	  if(aux!=null) auxiliaryLegend.addElement(aux);
	  //See if a prefix needs to be output.
	  String prefix=map.getInlinePrefixRepresentation((Element)node);
	  if(prefix!=null) out.print(prefix);
	  break;
	};

      //Recurse on the children..
      NodeList children=node.getChildNodes();
      for(int i=0;i<children.getLength();i++) 
	compileTree(children.item(i));

      //Output postfix string, if any.
      if(map!=null) {
	String postfix=map.getInlinePostfixRepresentation((Element)node);
	if(postfix!=null) out.print(postfix);
      };
    };
  }

    //Formats auxiliary legend gesture description text.
    public String formatGestureText(Element e) {
      if(e.getAttribute("TYPE").equalsIgnoreCase("BEAT"))
	return "Beat";
      else 
	return e.getAttribute("TYPE"); //punt for now...
    }    

  /** Inner class of NVBMap object, responsible for translating a single
    type of nonverbal behavior into various textual representations. */
  public abstract class NVBMap {
    /** REturns true if this NVBMap object is applicable (can translate) 
      the specified tree node. */
    abstract boolean isApplicable(Element e);

    /** Returns the inline prefix string, or null if none. */
    String getInlinePrefixRepresentation(Element e) { return null; }

    /** Returns the inline postfix string, or null if none. */
    String getInlinePostfixRepresentation(Element e) { return null; }

    /** Returns the generic legend entry for this NVBMap, or null if none. */
    String getLegendRepresentation() { return null; }

    /** Returns the auxiliary legend (footnote) string for this NVBMap, or null
      if none. */
    String getAuxiliaryRepresentation(Element e) { return null; }
  }

  /** Inner subclass of NVBMap, for NVBMaps which only specify constant
    legend, prefix and postfix inline Strings (i.e., do not vary by instance
    and do not require auxiliary legend output). */
  public abstract class TabularNVBMap extends NVBMap {
    String inlinePrefix;
    String inlinePostfix;
    String legend;

    /** Constructor takes the constant prefix, postfix and legend Strings. */
    public TabularNVBMap(String inlinePrefix,String inlinePostfix,String legend) {
      this.inlinePrefix=inlinePrefix;
      this.inlinePostfix=inlinePostfix;
      this.legend=legend;
    }
    
    String getInlinePrefixRepresentation(Element e) { 
      return inlinePrefix;
    }
    String getInlinePostfixRepresentation(Element e) { 
      return inlinePostfix;
    }
    String getLegendRepresentation() { 
      return legend;
    }
  }
    
}
