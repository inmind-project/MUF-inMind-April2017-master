/* -------------------------------------------------------------------------

   XMLWrapper.java
     - Encapsulation of an XML tree with many manipulation routines.

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

import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;

import javax.xml.parsers.*;
import org.w3c.dom.*;

import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;


/** 
  Represents an utterance at various stages of processing. Implemented
  as a wrapper around javasoft's XML Document object (which is wrapped
  around the w3c DOM data structure). Provides many tree-munging operations
  and allows for the annotation of the tree with attributes that are not
  exported as XML. Most of these operations are also supported as static
  methods so that a wrapper does not need to always be constructed. (Motivation
  for the wrapper was to allow nodes to be efficiently annotated with arbitrary Java objects
  during processing.)

  <br>

  Some general assumptions/issues:
  <ol>
  <li>NVBSuggestions are now created as XML Elements of their respective
      types (e.g., GESTURE, etc.). It is the job of the filters to find
      these.
  <li>To effect tag 'pass through', simply call 'pruneAllNodesOfType'
      with a list of the tag types that should *not* be exported.
  </ol>

    <h3>Change log:</h3>
    <table border="1">
    <tr><th>Date<th>Who<th>What</tr>
    <tr><td>4/1/01<td>T. Bickmore<td>Created.</tr>
    <tr><td>2/21/02<td>H. Vilhjalmsson<td>Added getLastNodeOfType</tr>
    </table>

 */

public class XMLWrapper {
	/** Type specifier for Text nodes. */
	public static final String TEXT="TEXT";

	//------------------------- FIELDS ----------------------------
	/** The DOM object that is wrapped. */
	protected Document document;

	/** 
       Stores non-XML (non-exported) attributes for each tree node.
       Indexed by attribute (String) returns null or Hashtable indexed by
       Node to yield Object value. 
       This represents local scratch memory for each module, it is never
       exported from one module to another. Any information passed must
       be encoded in Elements and their (XML) attributes.
	 */
	protected Hashtable attributeTables=new Hashtable();

	//------------------------- CONSTRUCTORS ----------------------------

	/** Constructs XMLWrapper given the text (String) represetation of
    the XML tree. */
	public XMLWrapper(String xml) throws Exception {
		document=parseXML(xml);
	}

	/** Constructs XMLWrapper given the name of a file which contains a
    text represetation of the XML tree. */
	public XMLWrapper(File xmlFile) throws Exception {
		document=loadXML(xmlFile);
	}

	/** Constructs XMLWrapper given a DOM object tree to be wrapped. */
	public XMLWrapper(Document document) { this.document=document; }

	/** Essentially a copy constructor, but <em>clears the non-XML attribute tables!</em> */
	public XMLWrapper(XMLWrapper xmlw) { this.document=xmlw.document; }

	//------------------------- METHODS ----------------------------

	/** Sets a non-XML attribute value on a node. These are NOT exported
       as XML. Values can be arbitrary Java objects.
	 */
	public void setNXMLAttribute(Node node,String attribute,Object value) {
		Hashtable table=(Hashtable)attributeTables.get(attribute);
		if(table==null) {
			table=new Hashtable();
			attributeTables.put(attribute,table);
		};
		table.put(node,value);
	}

	/** Retrieves a non-XML attribute value from a node. */
	public Object getNXMLAttribute(Node node,String attribute) {
		Hashtable table=(Hashtable)attributeTables.get(attribute);
		if(table==null) return null;
		return table.get(node);
	}

	/** Returns the root of the XML DOM representation. */
	public Element getRoot() { return document.getDocumentElement(); }   

	/** Returns the XML DOM representation (Document object). */
	public Document getDocument() { return document; }

	/** Removes all Elements whose tag types are in the specified list. 
       A removed element has any children spliced into its current location.
	 */
	public void pruneAllNodesOfType(String[] tagsToPrune) {
		reversePostorderTraversal(getRoot(),
				new NodeVisitor(tagsToPrune) {
			public boolean visit(Node n) {
				if(((n instanceof Element)||(n instanceof Text)) && isTypeMatch(n,(String[])argument)) 
					prune(n);
				return false;
			}});
	}

	/** Removes all Elements whose tag types are in the specified list from the subtree
     rooted at the specified node. 
       A removed element has any children spliced into its current location.
	 */
	public static void pruneAllNodesOfType(Node start,String[] tagsToPrune) {
		reversePostorderTraversal(start,
				new NodeVisitor(tagsToPrune) {
			public boolean visit(Node n) {
				if(((n instanceof Element) || (n instanceof Text)) && isTypeMatch(n,(String[])argument)) 
					prune(n);
				return false;
			}});
	}

	/** Removes all Elements whose tag types are <em>not</em? in the specified list from
     the subtree rooted at the specified node.
       A removed element has any children spliced into its current location.
	 */
	public static void keepAllNodesOfType(Node start,String[] tagsToKeep) {
		reversePostorderTraversal(start,
				new NodeVisitor(tagsToKeep) {
			public boolean visit(Node n) {
				if(((n instanceof Element || n instanceof Text)) && !isTypeMatch(n,(String[])argument)) {
					prune(n);
				};
				return false;
			}});
	}

	/** Returns a vector consisting of all Elements whose tagName matches the type
       specified in the subtree rooted at the specified node. */
	public static Vector getAllNodesOfType(Node start,String type) {
		return getAllNodesOfType(start,new Vector(),new String[]{type});
	}

	/** Returns a vector consisting of all Elements whose tagName matches the type
       specified. */ 
	public Vector getAllNodesOfType(String type) {
		return getAllNodesOfType(getRoot(),new Vector(),new String[]{type});
	}

	/** Returns a vector consisting of all Elements whose tagName matches the type
       specified in the subtree rooted at the specified node. */
	public static Vector getAllNodesOfType(Node start, String[] types) {
		return getAllNodesOfType(start,new Vector(),types);
	}

	/** Returns a vector consisting of all Elements whose tagName matches the types
       specified. */ 
	public Vector getAllNodesOfType(String[] types) {
		return getAllNodesOfType(getRoot(),new Vector(),types);
	}

	/** Returns a vector consisting of all Elements whose tagName matches the types
       specified. Call with an empty vector. */
	public static Vector getAllNodesOfType(Node start,Vector result,String[] types) {
		preorderTraversal(start,
				new NodeVisitor(result,types){
			public boolean visit(Node n) {
				if(isTypeMatch(n,(String[])argument2)) 
					((Vector)argument).addElement(n);
				return false;
			}});
		return result;
	}


	/** Returns the first child node of the specified start node meeting the type 
      specs in a preorder traversal. */
	public static Node getFirstNodeOfType(Node start, String type) {
		return preorderSearch(start,
				new NodeVisitor(type){
			public boolean visit(Node n) {
				return isTypeMatch(n,(String)argument);
			}; });
	}

	/** Returns the first node meeting the type specs in a preorder traversal. */
	public Node getFirstNodeOfType(String type) {
		return getFirstNodeOfType(getRoot(),type);
	}

	/** Returns the last child node of the specified start node meeting the type 
      specs in a reverse post-order traversal. */
	public static Node getLastNodeOfType(Node start, String type) {
		return reversePostorderSearch(start,
				new NodeVisitor(type) {
			public boolean visit(Node n) {
				return isTypeMatch(n,(String)argument);
			};
		});
	}

	/** Returns true if the node's type matches the specification. Note that TEXT is handled specially. */
	public static boolean isTypeMatch(Node n,String type) {
		return ((n instanceof Text && type.equals(TEXT)) ||
				(n instanceof Element && type.equalsIgnoreCase(n.getNodeName())));
	}

	/** Returns true if the node's type is in the specified list. Note that TEXT is handled specially. */
	public static boolean isTypeMatch(Node n,String[] types) {
		for(int i=0;i<types.length;i++)
			if(isTypeMatch(n,types[i])) return true;
		return false;
	}

	/** Returns the nearest ancestor whose tagName matches the type specified, or null. */
	public static Node getAncestorOfType(Node n,String type) {
		if(n==null) return null;
		if(isTypeMatch(n,type)) return n;
		return getAncestorOfType(n.getParentNode(),type);
	}

	/** Recurses through tree and sets "WORDCOUNT" and "WORDINDEX" non-XML properties on all
       nodes. WORDCOUNT reflects the number of words under the node. WORDINDEX specifies
       the word index position of the node in an in-order traversal. <br>
       Note: Punctuation marks (even if pulled out in separate nodes) are not counted as words.
	 */
	public void computeWordIndex() {
		countWords(getRoot());
		indexWords(getRoot(),1);
	}

	/** Helper function for computeWordIndex (sets WORDCOUNT property). */
	private int countWords(Node n) {
		int wordCount=0;
		if(n instanceof Text) {
			wordCount=wordCount((Text)n);
			setNXMLAttribute(n,"WORDCOUNT",new Integer(wordCount));
			return wordCount;
		} else {
			NodeList children=n.getChildNodes();
			for(int i=0;i<children.getLength();i++)
				wordCount+=countWords(children.item(i));
			setNXMLAttribute(n,"WORDCOUNT",new Integer(wordCount));
			return wordCount;
		}
	}

	/** Helper function for computeWordIndex (sets WORDINDEX property). 
       Call with '1' initially.  */
	private void indexWords(Node n,int offset) {
		setNXMLAttribute(n,"WORDINDEX",new Integer(offset));
		int cumCount=0;
		NodeList children=n.getChildNodes();
		for(int i=0;i<children.getLength();i++) {
			Node child=children.item(i);
			indexWords(child,offset+cumCount);
			cumCount+=((Integer)getNXMLAttribute(child,"WORDCOUNT")).intValue();
		};
	}

	public int wordCount(Text n) {
		return (new StringTokenizer(n.getData()," \n\t\r.,?!;:()*&^%$#@~\"")).countTokens();
	}

	/** Returns a Vector of the words in the XML tree (each as separate string). 
      Behavior corresponding to word indexing scheme above. */
	public static Vector extractWords(Document xml) {
		Vector words=new Vector();
		XMLWrapper.preorderTraversal(xml,new XMLWrapper.NodeVisitor(words) {
			public boolean visit(Node n) {
				if(n instanceof Text) {
					StringTokenizer ST=new StringTokenizer(((Text)n).getData()," \n\t\r.,?!;:()*&^%$#@~\"");
					while(ST.hasMoreTokens())
						((Vector)argument).addElement(ST.nextToken().toUpperCase());
				};
				return true;
			}});
		return words;
	}

	//-------------------- LOW-LEVEL TREE-MUNGING FUNCTIONS -----------------------

	/** Disconnects a node (and all of its children) from the tree. */
	public void disconnect(Node node) {
		Node parent=node.getParentNode();
		if(parent!=null)
			parent.removeChild(node);
	}

	/** Creates a new node (Element) with the specified tag name. */
	public Element createElement(String tag) {
		return document.createElement(tag);
	}

	/** Creates a new node (Element) with the specified tag name, and list of
    attribute/values pairs. */
	public Element createElement(String tag,String[] attributes,String[] values) {
		Element ele=document.createElement(tag);
		for(int i=0;i<attributes.length;i++)
			ele.setAttribute(attributes[i],values[i]);
		return ele;
	}

	/** Creates a new node (Element) with the specified tag name, and list of
    attribute/values pairs. */
	public static Element createElement(Document document,String tag,String[] attributes,String[] values) {
		Element ele=document.createElement(tag);
		for(int i=0;i<attributes.length;i++)
			ele.setAttribute(attributes[i],values[i]);
		return ele;
	}

	/** Creates a Text node with the specified contents. */
	public Text createText(String data) {
		return document.createTextNode(data);
	}

	/** Sets an XML attribute on an element (output with the tree). */
	public void setXMLAttribute(Element node,String attribute,String value) {
		node.setAttribute(attribute,value);
	}

	/** Returns an XML attribute for an element. */
	public static String getXMLAttribute(Element node,String attribute) {
		return node.getAttribute(attribute);
	}

	/** Returns the parent of a node. */
	public Node getParent(Node n) {
		return n.getParentNode();
	}

	/** Value for <em>where</em> parameter of <em>addChild</em>, indicating
      new node is to be added as the first child. */
	public static final int FIRST=-2;

	/** Value for <em>where</em> parameter of <em>addChild</em>, indicating
      new node is to be added as the last child. */
	public static final int LAST=-1;

	/** Adds a new child node to the specified node.
     <br><em>where</em> ::= FIRST, LAST, or zero-based index */
	public void addChild(Node parent,Node child,int where) {
		NodeList children=parent.getChildNodes();
		if(where==0) 
			where=FIRST;
		else if(where>=children.getLength())
			where=LAST;
		if(!parent.hasChildNodes() || where==LAST)
			parent.appendChild(child);
		else if(where==FIRST)
			parent.insertBefore(child,parent.getFirstChild());
		else { //this seems very silly...
			Node at=parent.getFirstChild();
			for(int i=0;i<where;i++) 
				at=at.getNextSibling();
			parent.insertBefore(child,at);
		};
	}

	/** Disconnects node from tree; if the node has any children they are spliced
       into the previous location of the node. */     
	public static void prune(Node node) {
		Node parent=node.getParentNode();
		if((parent==null) || (parent instanceof Document))
			return; //How to remove the root node???
		else if(!node.hasChildNodes()) {
			parent.removeChild(node);
		} else { //general case
			NodeList kidsToSplice=node.getChildNodes();
			//Find out the index of node...
			//Do the splice...
			Node insertBeforePtr=node.getNextSibling();
			for(int k=kidsToSplice.getLength()-1;k>=0;k--) {
				Node kid=kidsToSplice.item(k);
				node.removeChild(kid);
				if(insertBeforePtr==null)
					parent.appendChild(kid);
				else
					parent.insertBefore(kid,insertBeforePtr);
				insertBeforePtr=kid;
				//addChild((Element)parent,kidsToSplice.item(k),index+k);
			};
			parent.removeChild(node);
		};
	}

	/** Inserts the specified node (newParent) between an existing node in the
	tree (existingChild) and its parent. */
	public static void spliceParent(Node existingChild,Element newParent) {
		Node parent=existingChild.getParentNode();
		parent.replaceChild(newParent,existingChild);
		newParent.appendChild(existingChild);
	}

	/** Returns the index number of node wrt its siblings (zero-based). */
	public int siblingIndex(Node n) {
		Node parent=n.getParentNode();
		NodeList siblings=parent.getChildNodes();
		int index=0;
		Node sibling;
		while((sibling=siblings.item(index))!=n) {
			index++;
		};
		return index;
	}

	/** Used by findCommonParent to generate unique node markers. */
	protected static int FCPCount=0;

	/** Find the common parent of the specified list of nodes, then
	returns the immediate children of that parent which are either
	ancestors of the specified nodes or the nodes themselves (when
	no such ancestor exists). The returned list is guaranteed to
	be a list of adjacent siblings. <br>
	Note: This is not thread safe. */
	public Vector findCommonParent(Vector nodes) {
		Integer mark=new Integer(FCPCount++);
		//Step 1: traverse from nodes up tree setting FCP_PATH marks
		for(int i=0;i<nodes.size();i++)
			FCPMarkParents((Node)nodes.elementAt(i),mark);
		//Step 2: Descend down tree looking for common parent.
		return FCPDescend(getRoot(),mark,nodes);
	}
	/** Helper function for findCommonParent. */
	private void FCPMarkParents(Node n,Integer mark) {
		if(n==null) return;
		Integer alreadyMarked=(Integer)getNXMLAttribute(n,"FCP_PATH");
		if(alreadyMarked==mark) return;
		setNXMLAttribute(n,"FCP_PATH",mark);
		FCPMarkParents(n.getParentNode(),mark);
	}
	/** Helper function for findCommonParent. */
	private Vector FCPDescend(Node n,Integer mark,Vector nodes) {
		NodeList children=n.getChildNodes();
		if(children.getLength()==0) return null; //something very wrong??
		Node childWithMark=null;
		boolean success=false;
		//Descend following marks until: 1) child in nodes OR 2) >1 child marked
		for(int i=0;i<children.getLength();i++) {
			Node child=children.item(i);
			if(nodes.contains(child)) {
				success=true;
				break;
			};
			if(getNXMLAttribute(child,"FCP_PATH")==mark) {
				if(childWithMark!=null) {
					success=true;
					break;
				} else
					childWithMark=child;
			};
		};
		if(!success) {
			if(childWithMark==null) 
				return null; //should never happen
			else
				return FCPDescend(childWithMark,mark,nodes);
		} else { //Found the common parent...
			//Just extract and return the chidren with marks.
			//NOTE: This includes ALL children between the first and last marked one (adj siblings)!
			int firstMarkIndex=-1;
			int lastMarkIndex=-1;
			for(int i=0;i<children.getLength();i++) {
				Node child=children.item(i);
				if(getNXMLAttribute(child,"FCP_PATH")==mark) {
					if(firstMarkIndex<0)
						firstMarkIndex=i;
					lastMarkIndex=i;
				};
			};
			Vector result=new Vector();
			for(int i=firstMarkIndex;i<=lastMarkIndex;i++) 
				result.addElement(children.item(i));
			return result;
		}
	}

	/** Takes a list of adjacent sibling nodes and a new node (P2), disconnects
       the siblings from their parent, replaces them with P2, and the reconnects
       the siblings as children of P2. */
	public void lowerSiblings(Node P2,Vector siblings) {
		Node firstSibling=(Node)siblings.firstElement();
		Node parent=firstSibling.getParentNode();
		int index=siblingIndex(firstSibling);
		for(int i=0;i<siblings.size();i++) {
			Node sibling=(Node)siblings.elementAt(i);
			parent.removeChild(sibling);
			P2.appendChild(sibling);
		};
		addChild(parent,P2,index);
	}

	/** Convolves lowerSiblings and findCommonParent. */
	public void lowerSubtree(Element newParent,Vector nodes) {
		lowerSiblings(newParent,findCommonParent(nodes));
	}

	/** Returns true if the two elements match of tagName, and all of the
      attributes in pattern are present in the instance. */
	public static boolean elementMatch(Element instance,Element pattern) {
		if(!instance.getNodeName().equals(pattern.getNodeName())) return false;
		NamedNodeMap pAttributes=pattern.getAttributes();
		for(int i=0;i<pAttributes.getLength();i++) {
			Attr pAttribute=(Attr)pAttributes.item(i);
			String iValue=instance.getAttribute(pAttribute.getName());
			if(iValue==null || iValue.trim().length()==0) return false;
			if(!iValue.equals(pAttribute.getValue())) return false;
		};
		return true;
	}

	//--------------------- TREE TRAVERSAL -------------------------------

	/** This is an adaptor class which is used to encaspulate a tree-walker
	function and its incremental results. */
	public static abstract class NodeVisitor {
		/** Argument available within the visit method. */
		public Object argument;
		/** Argument available within the visit method. */
		public Object argument2;
		public NodeVisitor(Object argument) { this.argument=argument; }
		public NodeVisitor(Object argument,Object argument2) { 
			this.argument=argument; this.argument2=argument2; }
		/** Called once on each node in the tree passed to preorderTraversal. 
	  When used with Search methods, this returns true if the search is to be halted. */
		public abstract boolean visit(Node n);
	}

	/** Performs a pre-order traversal of the XML tree with the visitor object's
	visit function called on each node. */
	public static void preorderTraversal(Node node,NodeVisitor visitor) {
		visitor.visit(node);
		NodeList children=node.getChildNodes();
		for(int i=0;i<children.getLength();i++) 
			preorderTraversal(children.item(i),visitor);
	}

	/** Performs a pre-order traversal of the XML tree with the visitor object's
	visit function called on each node. Continued until visit returns true. */
	public static Node preorderSearch(Node node,NodeVisitor visitor) {
		if(visitor.visit(node)) return node;
		NodeList children=node.getChildNodes();
		for(int i=0;i<children.getLength();i++) {
			Node result=preorderSearch(children.item(i),visitor);
			if(result!=null) return result;
		};
		return null;
	}

	/** Performs a reverse post-order traversal of the XML tree with the visitor object's
	visit function called on each node. */
	public static void reversePostorderTraversal(Node node,NodeVisitor visitor) {
		NodeList children=node.getChildNodes();
		for(int i=children.getLength()-1;i>=0;i--) 
			reversePostorderTraversal(children.item(i),visitor);
		visitor.visit(node);
	}

	/** Performs a reverse post-order traversal of the XML tree with the visitor object's
	visit function called on each node. Continued until visit returns true. */
	public static Node reversePostorderSearch(Node node,NodeVisitor visitor) {
		NodeList children=node.getChildNodes();
		for(int i=children.getLength()-1;i>=0;i--) {
			Node result=reversePostorderSearch(children.item(i),visitor);
			if(result!=null) return result;
		};
		if(visitor.visit(node)) return node; else return null;
	}

	//-------------------- DEBUG/DISPLAY UTILITIES -----------------------

	/** Sends a text representation of the tree to the specified output stream. */
	public void dump(OutputStream out) throws Exception {
		TransformerFactory tFactory =TransformerFactory.newInstance();
		Transformer transformer = tFactory.newTransformer();
		DOMSource source = new DOMSource(document);
		StreamResult result = new StreamResult(out);
		transformer.transform(source, result);
	}

	/** Prints out word indices. */
	public void dumpIndices(PrintStream out) {
		dumpIndices(getRoot(),out);
	}

	/** Prints out word indices for the indicated subtree. */
	public void dumpIndices(Node n,PrintStream out) {
		int i;
		if(n instanceof Text) 
			out.println(" {idx="+getNXMLAttribute(n,"WORDINDEX")+": "+((Text)n).getData().replace('\n',' ')+"} ");
		else if(n instanceof Element) {
			out.println("<"+((Element)n).getTagName()+":count="+getNXMLAttribute(n,"WORDCOUNT")+">");
			NodeList children=n.getChildNodes();
			for(i=0;i<children.getLength();i++)
				dumpIndices(children.item(i),out);
			out.println("</"+((Element)n).getTagName()+">");
		} else { //dunno what it is, so just skip it
			NodeList children=n.getChildNodes();
			for(i=0;i<children.getLength();i++)
				dumpIndices(children.item(i),out);
		};
	} 

	/** Utility function for tracers which returns text representation of tree. */
	public static String toString(Document xml) {
		try {
			TransformerFactory tFactory =TransformerFactory.newInstance();
			Transformer transformer = tFactory.newTransformer();
			DOMSource source = new DOMSource(xml);
			StringWriter writer=new StringWriter();
			StreamResult result = new StreamResult(writer);
			transformer.transform(source, result);    
			return writer.toString();
		}catch(Exception e) {
			return e.toString();
		}
	}

	/** Returns text representation of the tree. */
	public String toString() {
		return toString(document);
	}

	/** Prints a formatted/indented version of the XML tree. Note that this does <em>not</em>
      output valid XML. */
	public void pprint() {
		pprint(getRoot(),0,System.out);
	}

	/** Pretty-prints the tree into a returned String. */
	public String pprint2String() {
		return pprint2String(getRoot());
	}

	/** Pretty-prints the tree into a returned String including closing tags. */
	public String pprint2StringAll() {
		return pprint2StringAll(getRoot());
	}

	/** Pretty-prints the subtree rooted at the specified node into a String. */
	public String pprint2String(Node n) {
		ByteArrayOutputStream out=new ByteArrayOutputStream();
		pprint(n,0,new PrintStream(out));
		return out.toString();
	}

	/** Pretty-prints the subtree rooted at the specified node into a String including closing tags. */
	public String pprint2StringAll(Node n) {
		ByteArrayOutputStream out=new ByteArrayOutputStream();
		pprintAll(n,0,new PrintStream(out));
		return out.toString();
	}


	/** Prints a formatted/indented version of the XML tree. Note that this does <em>not</em>
      output valid XML. */
	public static void pprint(Node n,PrintStream out) {
		pprint(n,0,out);
	}
	
	/** Prints a formatted/indented version of the XML tree. Note that this does <em>not</em>
    output valid XML. */
	public static void pprintAll(Node n,PrintStream out) {
		pprintAll(n,0,out);
	}

	/** Pretty prints a tree. */
	private static void pprint(Node n,int indent,PrintStream out) {
		if(n instanceof Element) {
			out.print("\n");
			for(int i=0;i<indent;i++) out.print(" ");
			out.print("<"+n.getNodeName());
			NamedNodeMap attributes=((Element)n).getAttributes();
			for(int i=0;i<attributes.getLength();i++) {
				Attr attribute=(Attr)attributes.item(i);
				out.print(" "+attribute.getName()+"=\""+attribute.getValue()+"\"");
			};
			out.print(">");
			NodeList children=n.getChildNodes();
			for(int i=0;i<children.getLength();i++) 
				pprint(children.item(i),indent+3,out);
		} else if((n instanceof Text)&&((Text)n).getData().trim().length()>0) {
			out.print("\n");
			for(int i=0;i<indent;i++) out.print(" ");
			out.print(((Text)n).getData().replace('\n',' ').trim());
		};
	}

	/** Pretty prints a tree including closing tags. */
	private static void pprintAll(Node n, int indent, PrintStream out) {
		if(n instanceof Element) {
			out.print("\n");
			for(int i=0;i<indent;i++) out.print(" ");
			out.print("<"+n.getNodeName());
			NamedNodeMap attributes=((Element)n).getAttributes();
			for(int i=0;i<attributes.getLength();i++) {
				Attr attribute=(Attr)attributes.item(i);
				out.print(" "+attribute.getName()+"=\""+attribute.getValue()+"\"");
			};
			out.print(">");
			NodeList children=n.getChildNodes();
			for(int i=0;i<children.getLength();i++){
				pprintAll(children.item(i),indent+3,out);
				//out.print("</" + n.getNodeName() + ">\n");
				}
			for(int i=0;i<indent;i++) out.print(" ");
			out.print("</" + n.getNodeName() + ">\n");
		} else if((n instanceof Text)&&((Text)n).getData().trim().length()>0) {
			out.print("\n");
			for(int i=0;i<indent;i++) out.print(" ");
			out.print(((Text)n).getData().trim());
			out.print("\n");
			//out.print("</" + n.getNodeName() + ">\n");
		};
		//out.print("</" + n.getNodeName() + ">\n");
	}

	//----------------------- JTree Display -------------------------------

	/** Returns a JTree display of the XML. */
	public JTree makeJTree() { 
		return new JTree(new DomToTreeModelAdapter());
	}

	/** Actually pops up a JFrame with a scrolling JTree pane. */
	public void popupJTree(String title,int windowWidth,int windowHeight) {
		JTree tree=makeJTree();
		JScrollPane treeView = new JScrollPane(tree);
		JFrame frame = new JFrame(title);
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) { e.getWindow().dispose();}
		});
		// Set up the tree, the views, and display it all
		frame.getContentPane().add("Center", treeView );
		frame.pack();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int w = windowWidth + 10;
		int h = windowHeight + 10;
		frame.setLocation(screenSize.width/3 - w/2, screenSize.height/2 - h/2);
		frame.setSize(w, h);
		frame.setVisible(true);
	}

	/** Used in JTree construction.  An array of names for DOM node-types
     (Array indexes = nodeType() values.) */
	protected static final String[] typeName = {
		"none",
		"Element",
		"Attr",
		"Text",
		"CDATA",
		"EntityRef",
		"Entity",
		"ProcInstr",
		"Comment",
		"Document",
		"DocType",
		"DocFragment",
		"Notation",
	};

	/** Used for JTree construction.
       This class wraps a DOM node and returns the text we want to
    display in the tree. It also returns children, index values,
    and child counts. */
	protected class AdapterNode { 
		Node domNode;

		// Construct an Adapter node from a DOM node
		public AdapterNode(Node node) {
			domNode = node;
		}

		// Return a string that identifies this node in the tree
		// *** Refer to table at top of org.w3c.dom.Node ***
		public String toString() {
			String s = typeName[domNode.getNodeType()];
			String nodeName = domNode.getNodeName();
			if (! nodeName.startsWith("#")) {
				s += ": " + nodeName;
			}
			if (domNode.getNodeValue() != null) {
				if (s.startsWith("ProcInstr")) 
					s += ", "; 
				else 
					s += ": ";
				// Trim the value to get rid of NL's at the front
				String t = domNode.getNodeValue().trim();
				int x = t.indexOf("\n");
				if (x >= 0) t = t.substring(0, x);
				s += t;
			}
			return s;
		}


		/*
		 * Return children, index, and count values
		 */
		public int index(AdapterNode child) {
			//System.err.println("Looking for index of " + child);
			int count = childCount();
			for (int i=0; i<count; i++) {
				AdapterNode n = this.child(i);
				if (child.domNode == n.domNode) return i;
			}
			return -1; // Should never get here.
		}

		public AdapterNode child(int searchIndex) {
			//Note: JTree index is zero-based. 
			Node node = domNode.getChildNodes().item(searchIndex);
			return new AdapterNode(node); 
		}

		public int childCount() {
			return domNode.getChildNodes().getLength();  
		}
	}

	/** Used for construction of JTree. 
     This adapter converts the current Document (a DOM) into 
     a JTree model.  */
	protected class DomToTreeModelAdapter implements javax.swing.tree.TreeModel {
		// Basic TreeModel operations
		public Object  getRoot() {
			//System.err.println("Returning root: " +document);
			return new AdapterNode(document);
		}
		public boolean isLeaf(Object aNode) {
			// Determines whether the icon shows up to the left.
			// Return true for any node with no children
			AdapterNode node = (AdapterNode) aNode;
			if (node.childCount() > 0) return false;
			return true;
		}
		public int     getChildCount(Object parent) {
			AdapterNode node = (AdapterNode) parent;
			return node.childCount();
		}
		public Object getChild(Object parent, int index) {
			AdapterNode node = (AdapterNode) parent;
			return node.child(index);
		}
		public int getIndexOfChild(Object parent, Object child) {
			AdapterNode node = (AdapterNode) parent;
			return node.index((AdapterNode) child);
		}
		public void valueForPathChanged(TreePath path, Object newValue) {
			// Null. We won't be making changes in the GUI
			// If we did, we would ensure the new value was really new,
			// adjust the model, and then fire a TreeNodesChanged event.
		}
		/*
		 * Use these methods to add and remove event listeners.
		 * (Needed to satisfy TreeModel interface, but not used.)
		 */
		private Vector listenerList = new Vector();
		public void addTreeModelListener(TreeModelListener listener) {
			if ( listener != null 
					&& ! listenerList.contains( listener ) ) {
				listenerList.addElement( listener );
			}
		}
		public void removeTreeModelListener(TreeModelListener listener) {
			if ( listener != null ) {
				listenerList.removeElement( listener );
			}
		}

		/*
		 * Invoke these methods to inform listeners of changes.
		 * (Not needed for this example.)
		 * Methods taken from TreeModelSupport class described at 
		 *   http://java.sun.com/products/jfc/tsc/articles/jtree/index.html
		 * That architecture (produced by Tom Santos and Steve Wilson)
		 * is more elegant. I just hacked 'em in here so they are
		 * immediately at hand.
		 */
		public void fireTreeNodesChanged( TreeModelEvent e ) {
			Enumeration listeners = listenerList.elements();
			while ( listeners.hasMoreElements() ) {
				TreeModelListener listener = (TreeModelListener) listeners.nextElement();
				listener.treeNodesChanged( e );
			}
		} 
		public void fireTreeNodesInserted( TreeModelEvent e ) {
			Enumeration listeners = listenerList.elements();
			while ( listeners.hasMoreElements() ) {
				TreeModelListener listener = (TreeModelListener) listeners.nextElement();
				listener.treeNodesInserted( e );
			}
		}   
		public void fireTreeNodesRemoved( TreeModelEvent e ) {
			Enumeration listeners = listenerList.elements();
			while ( listeners.hasMoreElements() ) {
				TreeModelListener listener = (TreeModelListener) listeners.nextElement();
				listener.treeNodesRemoved( e );
			}
		}   
		public void fireTreeStructureChanged( TreeModelEvent e ) {
			Enumeration listeners = listenerList.elements();
			while ( listeners.hasMoreElements() ) {
				TreeModelListener listener =
						(TreeModelListener) listeners.nextElement();
				listener.treeStructureChanged( e );
			}
		}
	}

	//----------------------- XSL TRANSFORM -------------------------------

	/** Converts XSL into an object representation that can be applied to DOM trees. */
	public static class Transform {
		private Transformer transformer;
		/** Loads XSL from a file. */
		public Transform(File xslFile) throws Exception {
			TransformerFactory factory=TransformerFactory.newInstance();
			transformer=factory.newTransformer(new StreamSource(xslFile));
		}
		/** Loads XSL from a String. */
		public Transform(String xsl) throws Exception {
			TransformerFactory factory=TransformerFactory.newInstance();
			ByteArrayInputStream in=new ByteArrayInputStream(xsl.getBytes());
			transformer=factory.newTransformer(new StreamSource(in));
		}
		public Transformer getTransformer() { return transformer; }
	}

	/** Applies a Transform to a tree and returns the resulting tree. */
	public static Document applyTransform(Transform xsl,Document xml) throws Exception {
		DOMResult result=new DOMResult();
		xsl.getTransformer().clearParameters(); // Needed to reset between utterances - hhv 07/10/01 
		xsl.getTransformer().transform(new DOMSource(xml),result);
		return (Document)result.getNode();
	}

	//----------------------- CONVENIENCE FUNCTION -------------------------------

	/** Parses an xml file and returns the Document root. */
	public static Document loadXML(File xmlFile) throws Exception {
		DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
		DocumentBuilder builder=factory.newDocumentBuilder();
		return builder.parse(xmlFile);
	}

	/** Parses the String representation of an XML tree and returns the Document root. */
	public static Document parseXML(String xml) throws Exception {
		ByteArrayInputStream in=new ByteArrayInputStream(xml.getBytes());
		DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
		DocumentBuilder builder=factory.newDocumentBuilder();
		return builder.parse(in);
	}

	//----------------------- TEST STUB -------------------------------
	/** Internal test stub. */
	public static void main(String[] arg) {
		try {
			String xml="<?xml version='1.0' encoding='utf-8'?> "+
					"<root><a>a</a><b>b</b></root>";
			XMLWrapper xmlw=new XMLWrapper(new File("bug.xml"));
			//XMLWrapper xmlw=new XMLWrapper(xml);
			//System.out.println(xmlw);
			xmlw.pprint();
			System.out.println("\n------------------------------\n");

			xmlw.pruneAllNodesOfType(new String[]{"ACTION","CLAUSE","NEW","OBJECT","W"});
			xmlw.pprint();

			/*
       Vector anodes=xmlw.getAllNodesOfType("a");
       Vector bnodes=xmlw.getAllNodesOfType("b");
       Vector xnodes=xmlw.getAllNodesOfType("x");
       Vector dnodes=xmlw.getAllNodesOfType("d");
       Vector c1nodes=xmlw.getAllNodesOfType("c1");
       Vector c2nodes=xmlw.getAllNodesOfType("c2");
       Node a=(Node)anodes.firstElement();
       Node b=(Node)bnodes.firstElement();
       Node x=(Node)xnodes.firstElement();
       Node d=(Node)dnodes.firstElement();
       Node c1=(Node)c1nodes.firstElement();
       Node c2=(Node)c2nodes.firstElement();
       Vector siblings=new Vector();
       //siblings.addElement(a);
       siblings.addElement(b);
       //siblings.addElement(c1);
       siblings.addElement(d);
       //System.out.println(xmlw);

       System.out.println("first x="+xmlw.getFirstNodeOfType("x"));
       xmlw.keepAllNodesOfType(new String[]{TEXT,"root"});
       System.out.println(xmlw);
       xmlw.lowerSubtree(xmlw.createElement("p"),siblings);
       //xmlw.lowerSiblings(xmlw.createElement("p"),siblings);
       Vector cp=xmlw.findCommonParent(siblings);
       for(int i=0;i<cp.size();i++)
	 System.out.println("res sib="+cp.elementAt(i));
       xmlw.lowerSiblings(xmlw.createElement("p"),siblings);
       xmlw.spliceParent(x,xmlw.createElement("p"));
       xmlw.pruneAllNodesOfType(new String[]{"f",TEXT});
       xmlw.prune(todie);       Vector nodes=xmlw.getAllNodesOfType("x");
       System.out.println("idx(x)="+xmlw.siblingIndex((Node)nodes.firstElement()));
       Node a=xmlw.getAncestorOfType((Node)nodes.firstElement(),"rot");
       Node root=(Node)nodes.firstElement();
       Vector nodes=xmlw.getAllNodesOfType(new String[]{TEXT,"a"});
       for(int i=0;i<nodes.size();i++)
	 System.out.println("ANOT="+nodes.elementAt(i));
       xmlw.addChild(root,xmlw.createText("xxx"),1);
       System.out.println(xmlw);
      XMLWrapper xmlw=new XMLWrapper(new File("test.xml"));
      xmlw.dump(System.out);
      System.out.println("\n------------------------------\n");
      Vector nodes=xmlw.getAllNodesOfType("foo");
      Element foo=(Element)nodes.firstElement();
      //xmlw.disconnect(foo);
      //xmlw.addChild(foo,xmlw.createNode("cow",new String[]{"att"},new String[]{"val"}),LAST);
      xmlw.computeWordIndex();
      System.out.println("did indexing..");
      System.out.println("\n------------------------------\n");
      xmlw.dumpIndices(System.out);
      //xmlw.dump(System.out);
      //System.out.println(xmlw);
      xmlw.popupJTree(500,200);
			 */
		}catch(Exception e) {
			System.out.println("ex: "+e);
		};
	}

}



