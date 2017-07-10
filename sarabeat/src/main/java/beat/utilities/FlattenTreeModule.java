/* -------------------------------------------------------------------------

   FlattenTreeModule.java
     - Converts a tree of nonverbal behaviors into a linear animation script for the BEAT gesture toolkit

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

import org.w3c.dom.*;

/** Converts a tree of nonverbal behaviors into a linear animation script. 
    Should be run after all generators and filters have been applied.
    <p>
    Contains primitives
    for manipulating the abstract animation commands prior to translation (removeStep,
    addStartStep, changeTime, checkOrder), and for extracting information from the
    animation steps (getSRTime, getWordIndex, getIndex, getScript, getNumberSteps,
    getStep, isSTART). Also provides a method for determining if a hand is not used
    for an interval time so that it can be relaxed (handNotUsedForAwhile).
    <p>
    Format of output tree:
    <ul>
    <li> script ::= &lt;AnimationScript SPEAKER="speaker" HEARER="hearer"&gt; {step}*
                    &lt;/AnimationScript&gt;
    <li> step ::= &lt; type AID="id" SRT="time" WI="index" ACTION="action" {args}* /&gt;
    <li>  type ::= START | STOP
    <li>  id ::= unique ID for the step. If there are START and STOP steps for
         the same command they are given the same ID.
    <li>  time ::= time in seconds from the start of speech
    <li>  index ::= word index (pre-first-word = 0)
    <li>  action ::= type of nonverbal behavior (e.g., GESTURE_RIGHT)
    <li>  args ::= arguments specified for the nonverbal behavior
    </ul>
    <h3>Change log:</h3>
    <table border="1">
    <tr><th>Date<th>Who<th>What</tr>
    <tr><td>6/8/01<td>T. Bickmore<td>Created.</tr>
    </table>
*/

public class FlattenTreeModule extends BeatModule {
  private NVBTypes nvbTypes;
  private int stepCount=0;
		
    /** Constructor requires an NVBTypes object (access to the list of nonverbal
	behavior tags) and the beat module to output to. */
  public FlattenTreeModule(NVBTypes nvbTypes,BeatModule output) {
    super(output);
    this.nvbTypes=nvbTypes;
  }

    /** Does all the work of the module, an XML transducer. */
  public Document transduce(Document xml) throws Exception {
    XMLWrapper result=new XMLWrapper("<?xml version='1.0' encoding='utf-8'?> "+
				     "<"+SCRIPT+"></"+SCRIPT+">");
    Element script=(Element)result.getFirstNodeOfType(SCRIPT);
    script.setAttribute("SPEAKER",getSpeaker(xml));
    script.setAttribute("HEARER",getHearer(xml));
    script.appendChild(makeAnimationStep(result,START,getID(),"SPEAK",
					 new String[]{"SPEECH","WI","SRT"},
					 new String[]{extractSpeech(xml),"0","0.0"}));
    flattenTree(xml,result,script);
    return result.getDocument();
  }

    /** Does all the work of the module, handles string representation of XML tree. */
  public String transduce(String xml) throws Exception {
    return XMLWrapper.toString(transduce(XMLWrapper.parseXML(xml)));
  }

  //------------------------------------------------------
    /** Tag name for the SCRIPT tag enclosing all animation steps. */
  public static final String SCRIPT="AnimationScript";

    /** Tag name for START animation steps. */
  public static final String START="START";

    /** Tag name for STOP animation steps. */
  public static final String STOP="STOP";

    /** Attribute name for action ID attributes. */
  public static final String ACTION_ID="AID";

    /** Attribute name for ACTION attributes. */
  public static final String ACTION="ACTION";

  /** Attribute name for speech relative time attribute. */
  public static final String SRT="SRT";

  /** Attribute name for word index attribute. */
  public static final String WI="WI";

  private String getID() { return "A"+(++stepCount); }

  private void flattenTree(Node node,XMLWrapper xmlw,Element script) {
    boolean isNVB=XMLWrapper.isTypeMatch(node,nvbTypes.getNVBTypes());
    			//&& !node.getNodeName().startsWith("INTONATION");
    String id=getID();
    if(isNVB) 
      script.appendChild(makeAnimationStep(xmlw,START,id,(Element)node));
    NodeList children=node.getChildNodes();
    for(int i=0;i<children.getLength();i++) 
      flattenTree(children.item(i),xmlw,script);
    if(isNVB && !node.getNodeName().equals("VISEME")) 
      script.appendChild(makeAnimationStep(xmlw,STOP,id,(Element)node));
  }

  private Element makeAnimationStep(XMLWrapper xmlw,String cmd,String id,Element node) {
    NamedNodeMap nodeAttributes=node.getAttributes();
    Element step=xmlw.createElement(cmd);
    step.setAttribute(ACTION_ID,id);
    step.setAttribute(ACTION,node.getTagName());
    for(int i=0;i<nodeAttributes.getLength();i++) {
      Attr attribute=(Attr)nodeAttributes.item(i);
      String attName=attribute.getName();
      if(attName.equals("SRT_START")) {
	if(cmd.equals(START)) step.setAttribute(SRT,attribute.getValue());
      }else if(attName.equals("SRT_END")) {
	if(cmd.equals(STOP)) step.setAttribute(SRT,attribute.getValue());
      }else if(attName.equals("WI_START")) {
	if(cmd.equals(START)) step.setAttribute(WI,attribute.getValue());
      }else if(attName.equals("WI_END")) {
	if(cmd.equals(STOP)) step.setAttribute(WI,attribute.getValue());
      }else
	step.setAttribute(attName,attribute.getValue());
    };
    return step;
  }

  private Element makeAnimationStep(XMLWrapper xmlw,String cmd,String id,String action,String[] attributes,String[] values) {
    Element step=xmlw.createElement(cmd);
    step.setAttribute(ACTION_ID,id);
    step.setAttribute(ACTION,action);
    for(int i=0;i<attributes.length;i++) {
      String attName=attributes[i];
      if(attName.equals("SRT_START")) {
	if(cmd.equals(START)) step.setAttribute(SRT,values[i]);
      }else if(attName.equals("SRT_END")) {
	if(cmd.equals(STOP)) step.setAttribute(SRT,values[i]);
      }else if(attName.equals("WI_START")) {
	if(cmd.equals(START)) step.setAttribute(WI,values[i]);
      }else if(attName.equals("WI_END")) {
	if(cmd.equals(STOP)) step.setAttribute(WI,values[i]);
      }else
	step.setAttribute(attName,values[i]);
    };
    return step;
  }

  protected String extractSpeech(Document xml) {
    StringBuffer speech=new StringBuffer();
    XMLWrapper.preorderTraversal(xml,new XMLWrapper.NodeVisitor(speech) {
      public boolean visit(Node n) {
	if(n instanceof Text)
	  ((StringBuffer)argument).append(((Text)n).getData().replace('\"',' '));
	return true;
      }});
    return speech.toString();
  }

  protected String getSpeaker(Document xml) {
    Node utterance=XMLWrapper.getFirstNodeOfType(xml,"META");
    if(utterance==null) return "AGENT";
    String speaker=((Element)utterance).getAttribute("SPEAKER");
    if(speaker==null || speaker.trim().length()==0)
      return "AGENT";
    else
      return speaker;
  }

  protected String getHearer(Document xml) {
    Node utterance=XMLWrapper.getFirstNodeOfType(xml,"META");
    if(utterance==null) return "USER";
    String hearer=((Element)utterance).getAttribute("HEARER");
    if(hearer==null || hearer.trim().length()==0)
      return "USER";
    else
      return hearer;
  }

}
