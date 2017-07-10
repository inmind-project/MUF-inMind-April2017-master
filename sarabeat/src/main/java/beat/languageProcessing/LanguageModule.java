/* -------------------------------------------------------------------------

   LanguageModule.java
     - Implements language tagging for the BEAT gesture toolkit

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
package beat.languageProcessing;

import java.io.*;
import java.util.*;

// XML DOM Classes
import beat.Config;
import org.w3c.dom.*;

import beat.kb.KnowledgeBase;
import beat.nvbgenerators.ParticipationFramework;
import beat.nvbgenerators.ParticipationFrameworkBase;
import beat.utilities.BeatModule;
import beat.utilities.XMLWrapper;
// WordNet Classes
import jwnl.word.*;
import jwnl.data.*;
// Grok OpenNLP
import opennlp.common.*;


/** 

    The LanguageModule is a BeatModule that analyzes incoming text and enriches it with
    XML tags that describe its linguistic elements and discourse structure.  The input text
    should be in XML format, where each text section to be analyzed is contained within an
    <code>&lt;UTTERANCE&gt;</code> tag.
    <p>
    The LanguageModule applies a series of internal transducers to generate the language
    tags.  The transducers and the tags they generate are as follows:
    <p>
    <table border=1>
    <tr><th>Transducer</th><th>Function</th><th>Tags</th></tr>
    <tr><td><i>Tokenizer</i></td><td>Encloses words and punctuations in <code>&lt;W&gt;</code> tags</td><td><code>&lt;W&gt;</code></td></tr>
    <tr><td><i>PosTagger</i></td><td>Adds Part-of-Speech information to existing <code>&lt;W&gt;</code> tags &nbsp;</td><td><code>&lt;W LEM="" POS=""&gt;</code></td></tr>
    <tr><td><i>Chunker</i></td><td>Chunks utterance into phrases and clauses</td><td><code>&lt;CLAUSE&gt;,&lt;ACTION&gt;,&lt;OBJECT&gt;</code></td></tr>
    <tr><td><i>DiscourseTagger</i></td><td>Adds information that relies on discourse history and knowledge bases &nbsp;</td><td><code>&lt;NEW&gt;,&lt;THEME&gt;,&lt;RHEME&gt;,&lt;CONTRAST&gt; &nbsp;<br>&lt;ACTION ID=""&gt;,&lt;OBJECT ID=""&gt;</code></td></tr>
    </table>
    <p>
    The LanguageModule relies on the following external structures and services:
    <p>
    <ul>
    <li><i>Part-of-Speech tagger</i>: Currently interfacing with the EngLite POS engine from <a href="http://www.conexor.fi">Conexor</a></li>
    <li><i>Lexial database</i>: Currently incorporating the <A HREF="http://www.cogsci.princeton.edu/~wn/">WordNet</A> database</li>
    <li><i>Domain database</i>: A custom built domain KnowledgeBase, supplied to the module constructor</li>
    </ul>

 */

public class LanguageModule extends BeatModule {

	/** The XML representation of the utterance currently being processed */
	protected XMLWrapper mUtterance;
	protected String mStringUtterance;

	// Internal Transducers
	/** <B>XML Transducer:</B> Tokenizes, Tags and Chunks */
	protected Pipeline mPreprocessor;
	/** <B>XML Transducer:</B> Encloses words and punctuations in <code>W</code> tags */
	protected BEATTokenizer mTokenizer;
	/** <B>XML Transducer:</B> Adds Part-of-Speech information to existing <code>W</code> tags */
	protected POSTagger mPosTagger;
	/** <B>XML Transducer:</B> Chunks utterances into clauses and then actions and objects */
	protected Chunker mChunker;
	/** <B>XML Transducer:</B> Adds high level discourse information, such as the placement of theme and rheme */
	protected DiscourseTagger mDiscourseTagger;

	// Knowledge Bases
	/** <B>Static Knowledge:</B> The <A HREF="http://www.cogsci.princeton.edu/~wn/">WordNet</A> lexical database */
	protected DictionaryDatabase mWordDB;
	/** <B>Static Knowledge:</B> The knowledge used for gesture generation in the current domain */
	protected KnowledgeBase mKnowledgeBase;
	/** <B>Dynamic Knowledge:</B> The knowledge built over the course of the current discourse */
	protected DiscourseModel mDiscourseModel;
	/** <B>Participation Framework:</B> Keeps track of what role each participant is playing in each scene */
	protected ParticipationFrameworkBase mParticipationFrameworkBase;

	/************************* TOKENIZER *************************/

	/** A Tokenizer is an XML transducer that chops us all TEXT nodes so that 
        each token becomes the single child of a <code>W</code> element. */  
	abstract public class BEATTokenizer { 
		protected Hashtable mDELCodes = new Hashtable(); 
		public BEATTokenizer() {}
		public XMLWrapper process(XMLWrapper xml) throws Exception {return xml;}
	}

	/* -------------------- SIMPLE TOKENIZER --------------------*/

	public class GrokTokenizer extends BEATTokenizer {

		public XMLWrapper process(XMLWrapper xml) throws Exception {
			Vector textnodes = xml.getAllNodesOfType("TEXT");
			String chunk = "";
			for(int i=0;i<textnodes.size();i++) {
				if(xml.getAncestorOfType((org.w3c.dom.Node)textnodes.elementAt(i),"W")==null) {
					chunk = chunk+" "+((org.w3c.dom.Node)(textnodes.elementAt(i))).getNodeValue();
					if(Config.logging)System.out.println(chunk);
				}
			}

			if(Config.logging)System.out.println();
			return xml;
		}
	}

	/** The SimpleTokenizer is a working Tokenizer that also adds a <code>SYN</code> attribute
        to all <code>W</code> elements that contain punctuation.  The <code>SYN</code> value reflects the type
        of punctuation (see mSimpleDEL below). */
	public class SimpleTokenizer extends BEATTokenizer {
		private Element mLast=null;
		protected final String mSimpleDEL[][] = {{",","C"},{".","PP"},{"!","PE"},{"?","PQ"},{"\"","Q"},{":","PC"}};
		public SimpleTokenizer() {
			for(int i=0;i<mSimpleDEL.length;i++) mDELCodes.put(mSimpleDEL[i][0],mSimpleDEL[i][1]);
		}

		/** Creates space before and after each punctuation mark if there isn't any already */
		public String separatePunctuation(String s) {
			s = s.replace('\n',' ')+" ";
			int p = -1;
			int index = s.length();
			while(index>=0) {
				p = s.lastIndexOf(". ",index);  // NOTE: should also look at capitalization because of abbreviations
				if(p>1) {
					if(s.charAt(p-2)=='.') p=-1;  // Abbreviation? (another preceding period)
				}
				p = Math.max(p,s.lastIndexOf(",",index));
				p = Math.max(p,s.lastIndexOf("!",index));
				p = Math.max(p,s.lastIndexOf("?",index));
				p = Math.max(p,s.lastIndexOf("\"",index));
				p = Math.max(p,s.lastIndexOf(":",index));
				p = Math.max(p,s.lastIndexOf(";",index));
				if(p>=0) {
					s = s.substring(0,p)+" "+s.charAt(p)+" "+s.substring(p+1);
				}
				index = p-1;
			}
			return s;
		}


		// In a string replace one substring with another	
		public String replaceString(String s, String from, String to) {
			if (s.equals("")) return "";
			String res = "";
			int i = (s.toLowerCase()).indexOf(from,0);
			int lastpos = 0;
			while (i != -1) {
				res += s.substring(lastpos,i) + to;
				lastpos = i + from.length();
				i = (s.toLowerCase()).indexOf(from,lastpos);
			}
			res += s.substring(lastpos);  // the rest
			return res;  
		}

		protected final String mAbbreviation[][] = {
				{"that's","that is"},{"here's","here is"},{"there's","there is"},{"where's","where is"},
				{"he's","he is"},{"she's", "she is"},{"what's","what is"},
				{"'m", " am"},{"'re"," are"},{"'ve"," have"},
				{"don't", "do not"}, {"can't", "can not"}, {"won't", "will not"}, 
				{"hasn't", "has not"}, {"wasn't", "was not"},{"isn't","is it not"},{"let's","let us"},
				{"m.i.t.","emmighty"},{"mit","emmighty"},{"media lab","medialab"},{"media laboratory","medialab"},
				{"'s"," "}, {"'"," "}, {"["," "}, {"]", " "}, {"&","and"},{"-"," "}
		};

		/** Removes abbreviating apostrophies and replaces them with full words where possible */
		public String expandAbbreviations(String s) {
			for(int i=0;i<mAbbreviation.length;i++) {
				s = replaceString(s,mAbbreviation[i][0],mAbbreviation[i][1]);
			}
			return s;
		}

		/** Separates all the words and punctuations found inside <i>node</i> by enclosing them in <code>&lt;W&gt;</code> tags.
            Also sets PREV and NEXT XMLWrapper non-xml attributes for each <code>&lt;W&gt;</code> to point to the previous and the next token. */
		public void expandNode(XMLWrapper xml, org.w3c.dom.Node node) {
			String chunk = node.getNodeValue();
			chunk=separatePunctuation(chunk);
			chunk=expandAbbreviations(chunk);
			StringTokenizer ST = new StringTokenizer(chunk);
			Vector tokens = new Vector();
			org.w3c.dom.Node parent = xml.getParent(node);
			Element wordnode;
			org.w3c.dom.Node nextsibling = node.getNextSibling();
			xml.disconnect(node);
			String token;
			String del;
			while(ST.hasMoreTokens()) {
				token = ST.nextToken();
				tokens.addElement(token);
			}
			for(int i=0;i<tokens.size();i++) {
				wordnode = xml.createElement("W");
				if(mLast!=null) {
					xml.setNXMLAttribute(wordnode,"PREV",mLast);
					xml.setNXMLAttribute(mLast,"NEXT",wordnode);
				}
				parent.insertBefore(wordnode,nextsibling);
				token = (String)tokens.elementAt(i);
				xml.addChild(wordnode,xml.createText(token+" "),xml.LAST);
				if((del=(String)mDELCodes.get(token))!=null) wordnode.setAttribute("SYN",del);
				mLast=wordnode;
			}		
		}

		/** Separates all the words and punctuations found inside all TEXT nodes of <i>xml</i> by enclosing them in <code>&lt;W&gt;</code> tags */		
		public XMLWrapper process(XMLWrapper xml) throws Exception {
			Vector textnodes = xml.getAllNodesOfType("TEXT");
			for(int i=0;i<textnodes.size();i++) {
				if(xml.getAncestorOfType((org.w3c.dom.Node)textnodes.elementAt(i),"W")==null)
					expandNode(xml, (org.w3c.dom.Node)textnodes.elementAt(i));
			}
			return xml;
		}
	}





	/************************* CHUNKER *************************/

	/** A Cunker is an XML transducer that groups together <code>&lt;W&gt;</code> elements, first into
        <code>&lt;OBJECT&gt;</code>s and <code>&lt;ACTION&gt;</code>s (corresponding to noun groups and verb groups), and then
        into <code>&lt;CLAUSE&gt;</code>s. */
	abstract public class Chunker {
		public Chunker() {}
		public XMLWrapper process(XMLWrapper xml) throws Exception {return xml;} 
	}

	/* -------------------- SIMPLE CHUNKER --------------------*/

	/** The SimpleChunker is a working Chunker that relies on SYN (syntax) attributes of <code>&lt;W&gt;</code> nodes
        to identify the noun and verb groups.  It then uses punctuation to assign clauses. */
	public class SimpleChunker extends Chunker {
		public SimpleChunker() {}

		/** A utility function that creates a new element called <i>name</i> spanning all nodes in <i>group</i>, returning the newly created element */
		protected Element groupElements(XMLWrapper xml, String name, Vector group) {
			Element parent = xml.createElement(name);
			xml.lowerSiblings(parent,xml.findCommonParent(group));	 
			return parent;
		}

		/** Evokes the chunker, processing the supplied <i>xml</i> */
		public XMLWrapper process(XMLWrapper xml) throws Exception {
			Vector wordnodes = xml.getAllNodesOfType("W");
			Element firstword = (Element)wordnodes.firstElement();
			Element currentword = firstword;
			String syn;
			Vector allgroups = new Vector();
			Vector group = new Vector();
			boolean NG = false; boolean VG = false; boolean NH = false;
			while(currentword!=null) {
				syn = currentword.getAttribute("SYN");
				if(syn!=null) {
					if(syn.startsWith("NN")) {
						if(VG) { allgroups.addElement(groupElements(xml,"ACTION",group)); VG=false; }
						if(!NG) { group.clear(); NG=true; }
						if((syn.endsWith("HD")||syn.endsWith("PE"))&&NH) {
							allgroups.addElement(groupElements(xml,"OBJECT",group)); group.clear(); NH=false; }
						if(syn.endsWith("HD")) NH=true;
						group.addElement(currentword);
					} else if(syn.startsWith("VB")||syn.startsWith("RB")) {
						if(NG) { allgroups.addElement(groupElements(xml,"OBJECT",group)); NG=false; NH=false; }
						if(!VG) { group.clear(); VG=true; }
						group.addElement(currentword);
					} else if(syn.startsWith("C")||syn.startsWith("P")||syn.startsWith("IN")) {
						if(VG) { allgroups.addElement(groupElements(xml,"ACTION",group)); VG=false; }
						if(NG) { allgroups.addElement(groupElements(xml,"OBJECT",group)); NG=false; NH=false; }
						group.clear();
						allgroups.addElement(currentword);
					}
				} 
				if((currentword = (Element)xml.getNXMLAttribute(currentword,"NEXT"))==null) {
					if(VG) allgroups.addElement(groupElements(xml,"ACTION",group));
					else if(NG) allgroups.addElement(groupElements(xml,"OBJECT",group));
				}
			}
			group.clear();
			Element currentelement;
			String type;
			Element next1=null; Element next2=null; Element prev1=null;
			String nextsyn = "";
			boolean anothercoming = false;
			for(int index=0;index<allgroups.size();index++) {
				currentelement=(Element)allgroups.elementAt(index);
				group.addElement(currentelement);
				//		System.out.println(group);
				type = currentelement.getTagName();
				if(type.equals("W")) {
					syn = currentelement.getAttribute("SYN");

					next1 = null;
					next2 = null;
					if((index+1)<allgroups.size()) next1 = (Element)allgroups.elementAt(index+1);
					if((index+2)<allgroups.size()) next2 = (Element)allgroups.elementAt(index+2);		    

					prev1 = null;
					if((index-1)>=0) prev1 = (Element)allgroups.elementAt(index-1);

					if(next1!=null) {
						nextsyn = next1.getAttribute("SYN");
						if(nextsyn!=null) 
							anothercoming = (nextsyn.startsWith("P")||nextsyn.startsWith("C"));
					}
					if((syn.startsWith("P"))&&(!anothercoming)) {

						if(next1!=null) group.removeElement(currentelement);
						groupElements(xml,"CLAUSE",group); 
						group.clear();
						if(next1!=null) group.addElement(currentelement);

					} else if(syn.startsWith("C")&&(!anothercoming)&&(prev1!=null)) {
						if((next1!=null)&&(next2!=null)) {
							if(!(next2.getTagName()).equals("W")) {
								if(!next1.getTagName().equals(next2.getTagName())) {

									group.removeElement(currentelement);
									groupElements(xml,"CLAUSE",group); 
									group.clear(); 
									group.addElement(currentelement);

								}
							}
						}
					}
				}
			}
			if(group.size()>0) 
				groupElements(xml,"CLAUSE",group);
			return xml;
		};
	}

	/************************* DISCOURSE MODEL *************************/

	/** The DiscourseModel contains the history of the interaction and can answer
        questions that involve the status of new <code>&lt;W&gt;</code> elements with respect to this
        history.  For example, it can tell you whether a <code>&lt;W&gt;</code> has been seen before, 
        or whether it contrasts with earlier words. */
	public class DiscourseModel {

		Vector mHistory = new Vector();

		public DiscourseModel() {
		}

		/** Adds the supplied utterance at the top of the history stack */
		public void addUtterance(XMLWrapper xml) {
			mHistory.addElement(xml);
		}

		/** Removes all entries from the history stack */
		public void clearHistory() {
			mHistory.clear();
		}

		/** Returns <b>true</b> if the lemma of the supplied <i>word</i> does not match 
            the lemma of any words stored in the history so far, returns <b>false</b> otherwise */
		public boolean isNew(Element word) {
			XMLWrapper u;
			String lemma = word.getAttribute("LEM");
			Element tempword;
			String templemma;
			for(int i=(mHistory.size()-1);i>=0;i--) {
				u=(XMLWrapper)mHistory.elementAt(i);
				tempword=(Element)u.getFirstNodeOfType("W");
				while(tempword!=null) {
					templemma = tempword.getAttribute("LEM");
					if(templemma!=null)
						if(lemma.equals(templemma)) return false;
					tempword = (Element)u.getNXMLAttribute(tempword,"NEXT");
				}
			}
			return true;
		}

		/** Checks to see if the lemma of the supplied <i>word</i> is on the "contrastlist" of any of 
            the words stored in the history so far, if so returns that contrasting word, returns null otherwise */
		public Element isContrasting(Element word) {
			XMLWrapper u;
			Element tempword;
			String templemma;
			Vector tempcontrastlist;
			String lemma = word.getAttribute("LEM");
			for(int i=(mHistory.size()-1);i>=0;i--) {
				u=(XMLWrapper)mHistory.elementAt(i);
				tempword=(Element)u.getFirstNodeOfType("W");
				while(tempword!=null) {
					tempcontrastlist = (Vector)u.getNXMLAttribute(tempword,"CONTRASTWORDS");
					if((tempcontrastlist!=null)&&(lemma!=null)) {
						for(int j=0; j<tempcontrastlist.size();j++) {
							if(lemma.equals(tempcontrastlist.elementAt(j))) {
								return tempword;
							}
						}
					}
					tempword = (Element)u.getNXMLAttribute(tempword,"NEXT");
				}
			}
			return null;
		}
	}

	/************************* DISCOURSE TAGGER *************************/

	/** The DiscourseTagger is an XML transducer that adds <code>&lt;NEW&gt;</code>, <code>&lt;RHEME&gt;</code> and <code>&lt;THEME&gt;</code> tags 
        to the incoming XML based on already existing <code>&lt;W&gt;</code>, <code>&lt;OBJECT&gt;</code>, <code>&lt;ACTION&gt;</code> 
	and <code>&lt;CLAUSE&gt;</code> tags and the information provided by the DiscourseModel.  The DiscourseTagger also
        attempts to identify <code>&lt;OBJECT&gt;</code>s and <code>&lt;ACTION&gt;</code>s by referring to the domain KnowledeBase, and to add
        <code>&lt;TOPICSHIFT&gt;<code> tags based on the presence of discoruse markers at the beginning of clauses.*/
	public class DiscourseTagger {

		DiscourseModel mDM;

		/** List of POS tags that represent open classed words */
		protected final String mOpenClass[] = {"JJ","NN","VB"};  // excluding RB=adverbs!


		/** List of words that should not be considered NEW at all */
		protected final String mGivenWords[] = {"be","have"};

		public DiscourseTagger(DiscourseModel dm) { mDM=dm; }

		/** Tags open classed words that have not been seen before in the discourse as <code>NEW</code> */
		public void markNew(XMLWrapper xml) {
			Element word; 
			String lemma;
			String pos;

			boolean repeated = false;
			Vector current = new Vector();

			// Marking new open classed words
			boolean open = false;
			boolean given = false;
			if(Config.logging)System.out.println("mark new: "+xml);
			//word = (Element)xml.getFirstNodeOfType("W");
			Vector words = xml.getAllNodesOfType("W");
			for(int z=0;z<words.size();z++){
				word=(Element)words.get(z);
				while(word!=null) {
					open = false;
					given = false;
					pos = word.getAttribute("POS");
					lemma = word.getAttribute("LEM");
				
					if(pos!=null) {
						for(int j=0;j<mOpenClass.length;j++) {
							if(pos.equals(mOpenClass[j])) {
								
								open=true;
								current.add(lemma);
							}
						}
					}
					if(lemma!=null) {
						for(int k=0;k<mGivenWords.length;k++) {
							if(lemma.equals(mGivenWords[k])) given=true;
						}
					}
					if((lemma!=null)&&open&&(!given)) {
						repeated=false;
						for(int i=0;i<(current.size()-1);i++)
							if(lemma.equals(current.elementAt(i))) repeated=true;
						if(mDM.isNew(word)&&(!repeated)) 
							xml.spliceParent(word,xml.createElement("NEW"));
					}
					word = (Element)xml.getNXMLAttribute(word,"NEXT");
				}
			}
		


		}

		/** Places a single <code>TOPICSHIFT</code> element at the beginning of clauses that start with 
            a possible topic shifting discoruse marker */
		public void markTopicShifts(XMLWrapper xml) {

			/** From Clark, H. (1996) "Using Language" page 345

                Next - and,but,so,now,then;speaking of that, that reminds me, one more thing, before I forget
                Push - now, like
                Pop  - anyway, but anyway, so, as I was saying
                Digress - incidentally, by the way
                Return  - anyway, what were we saying

			 */

			String mShiftMarker[][] = {
					{"POP","anyway"},{"POP","but","anyway"},{"POP","so"},{"POP","as","i","was","saying"},
					{"PUSH","now"},{"PUSH","like"},
					{"NEXT","and"},{"NEXT","but"},{"NEXT","so"},{"NEXT","now"},{"NEXT","then"},{"NEXT","speaking","of","that"},
					{"NEXT","that","reminds","me"},{"NEXT","one","more","thing"},{"NEXT","before","i","forget",},
					{"DIGRESS","incidentally"},{"DIGRESS","by","the","way"},
					{"RETURN","anyway"},{"RETURN","what","were","we","saying"}
			};

			Vector clauses = xml.getAllNodesOfType("CLAUSE");
			Element clause;
			Element firstword;
			Element word;
			String syn,lem;
			Vector clauseparts;
			Vector words;
			StringBuffer SB;
			int index;
			boolean found;
			for(int i=0; i<clauses.size(); i++) {
				clause = (Element)clauses.elementAt(i);
				//clauseparts = xml.getAllNodesOfType(clause,"W");
				firstword = (Element)xml.getFirstNodeOfType(clause,"W");

				//System.out.println("mShiftMarker length="+mShiftMarker.length);
				for(index=0; index<mShiftMarker.length;index++) {
					word = firstword; words = new Vector(); found=false;
					//System.out.println("word = "+word);
					for(int j=1;(j<mShiftMarker[index].length);j++) {
						//System.out.println("mShiftMarker length_marker="+mShiftMarker[index][j]);
						//System.out.println("word_value = "+word.getFirstChild());
						//System.out.println("["+index+"] Comparing "+((word.getFirstChild()).toString()).toLowerCase()+" with "+ mShiftMarker[index][j]);
						found = (((word.getFirstChild()).toString()).toLowerCase()).equals(mShiftMarker[index][j]+" ");
						if(found) {
							words.add(word);
							word = (Element)xml.getNXMLAttribute(word,"NEXT");
						}
						else break;
					}
					if(found) {
						//System.out.println("MATCH FOUND");
						//System.out.println("Creating element");
						Element topicshift = xml.createElement("TOPICSHIFT");
						topicshift.setAttribute("TYPE",mShiftMarker[index][0]); 
						//System.out.println("Creating parent for: "+words);
						xml.spliceParent((Element)words.elementAt(0),topicshift);

						//		    xml.lowerSiblings(topicshift,words);
						break;
					}
				}			
			}
			/*


		SB = new StringBuffer();
		for(int j=0; j<clauseparts.size(); j++) {
		    word = (Element)clauseparts.elementAt(j);
		    if(word!=null) {
		      syn = word.getAttribute("SYN");
		      if(syn!=null) {
			if(!syn.startsWith("P")) 
			  SB.append((word.getNodeValue()).toLowerCase());
			SB.append(" ")
		      }
		    }
		}
		for(int k=0; k<mShiftMarker.length; k++) 
		  if((SB.toString()).startsWith(mShiftMarker[k]))
		    xml.spliceParent(word,xml.createElement("TOPICSHIFT"));
		    } else // skip over any initial non-words 
			scope++;
		    if(syn!=null)
			if(syn.startsWith("P")) scope++; // skip over any initial punctuation
		}
	    }

			 */
		}

		/** Divides each <code>CLAUSE</code> into a <code>RHEME</code> and a <code>THEME</code> according to
            heuristic rules that look at the location of <code>NEW</code> tagged words with respect to the
            verb head of the clause */
		public void markInformationStructure(XMLWrapper xml) {
			// Marking THEME and RHEME
			Vector  clauses = xml.getAllNodesOfType("CLAUSE");
			Element clause;
			Vector  clauseparts;

			Element temp;
			boolean isfocused = false;

			Element V = null; boolean Vfocus = false;
			Vector  preV  = new Vector(); boolean preVfocus  = false;
			Vector  postV = new Vector(); boolean postVfocus = false;

			for(int i=0; i<clauses.size(); i++) {

				//System.out.println("Checking clause "+i);

				clause = (Element)clauses.elementAt(i);
				clauseparts = xml.getAllNodesOfType(clause,new String[]{"ACTION","OBJECT"});
				preV.clear(); 
				postV.clear();
				V=null;
				Vfocus = false;
				preVfocus = false;
				postVfocus = false;
				isfocused = false;

				if(clauseparts!=null) {

					//System.out.println("Found ACTION or OBJECT");

					for(int j=0; j<clauseparts.size(); j++) {

						//System.out.println("Checking clause part "+j);

						temp = (Element)clauseparts.elementAt(j);

						//System.out.println("The part is: "+temp);

						//System.out.print("Checking if it contains focused items...");
						isfocused = (xml.getFirstNodeOfType(temp,"NEW")!=null);
						//System.out.println(isfocused);
						//System.out.println("Getting tagname: "+temp.getTagName());
						if((temp.getTagName()).equals("ACTION")&&(V==null)) {
							V=temp; 
							Vfocus = isfocused;

						} else {
							if(V==null) { 
								//System.out.println("Adding element to preV");
								preV.add(temp); 
								preVfocus=(preVfocus||isfocused);
							} else {
								//System.out.println("Adding element to postV");
								postV.add(temp);
								postVfocus=(postVfocus||isfocused);
							}
						}
					}

					//System.out.println("Done checking clauseparts, now assigning tags");

					if(V!=null) {

						// The following are the heuristics described in 
						// (Hiyakamoto,Prevost & Cassell, 1997)
						if(postVfocus) preV.add(V); else postV.add(0,V);

						if((preV.size()>0)&&(postV.size()>0)) {			    

							if(!preVfocus&&!Vfocus&&!postVfocus) {        // (1)
								xml.lowerSubtree(xml.createElement("THEME"),preV);
								xml.lowerSubtree(xml.createElement("RHEME"),postV);
							} else if(preVfocus&&Vfocus&&!postVfocus) {   // (2)
								xml.lowerSubtree(xml.createElement("THEME"),preV);
								xml.lowerSubtree(xml.createElement("RHEME"),postV);
							} else if(preVfocus&&!Vfocus&&postVfocus) {   // (3)
								xml.lowerSubtree(xml.createElement("THEME"),preV);
								xml.lowerSubtree(xml.createElement("RHEME"),postV);
							} else if(!preVfocus&&Vfocus&&postVfocus) {   // (4)
								xml.lowerSubtree(xml.createElement("THEME"),preV);
								xml.lowerSubtree(xml.createElement("RHEME"),postV);
							} else if(preVfocus&&Vfocus&&postVfocus) {    // (5)
								xml.lowerSubtree(xml.createElement("THEME"),preV);
								xml.lowerSubtree(xml.createElement("RHEME"),postV);
							} else if(preVfocus&&!Vfocus&&!postVfocus) {  // (6)
								xml.lowerSubtree(xml.createElement("RHEME"),preV);
								xml.lowerSubtree(xml.createElement("THEME"),postV);
							} else if(!preVfocus&&Vfocus&&!postVfocus) {  // (7)
								xml.lowerSubtree(xml.createElement("THEME"),preV);
								xml.lowerSubtree(xml.createElement("RHEME"),postV);
							} else if(!preVfocus&&!Vfocus&&postVfocus) {  // (8)
								xml.lowerSubtree(xml.createElement("THEME"),preV);
								xml.lowerSubtree(xml.createElement("RHEME"),postV);
							}  		    
						} else if(preV.size()>0) {
							xml.lowerSubtree(xml.createElement("RHEME"),preV);
						} else if(postV.size()>0) {
							xml.lowerSubtree(xml.createElement("RHEME"),postV);
						}

					} else if(preV.size()>0) {
						xml.lowerSubtree(xml.createElement("RHEME"),preV);
					}
				}
			}		
		}

		/** Inserts <code>CONTRAST</code> tags to reflect (1) a contrast between two words within
            the utterance, and (2) a contrast between a word in the utterance and a word in some earlier
            utterance.  In the former case, each contrast pair will have the <code>ID</code> attribute of 
	    the <code>CONTRAST</code> element set to the same unique value */ 
		public void markContrast(XMLWrapper xml) {
			Element word = (Element)xml.getFirstNodeOfType("W");
			Element tempword;
			Vector contrastwords;
			Vector prevcontrastwords;
			Vector prevwords = new Vector();
			String pos;
			String lemma;
			String templemma;
			Element newnode;
			boolean found;
			Element localcontrast;
			Element globalcontrast;
			int contrastgroup=0;
			while(word!=null) {
				pos = word.getAttribute("POS");
				lemma = word.getAttribute("LEM");
				if((pos==null)||(lemma==null)) return;
				if(pos.equals("JJ")) {
					contrastwords = new Vector();
					IndexWord iw = mWordDB.lookupIndexWord(POS.ADJECTIVE, lemma);
					if(iw!=null) {
						//System.out.println("[DiscourseTagger] Adjective "+lemma+" found in WN!");
						PointerTargetList antonymslist = iw.getAntonyms(1);
						PointerTargetListItr itr = antonymslist.getIterator();
						jwnl.word.Word w2,w3;
						IndexWord iw2;
						PointerTargetListItr itr2;
						PointerTargetList synonymslist;
						while(itr.hasNext()) {
							w2 = itr.nextWord();
							if(w2!=null) {
								contrastwords.addElement(w2.getLemma());
								iw2 = mWordDB.lookupIndexWord(POS.ADJECTIVE,w2.getLemma());
								if(iw2!=null) {
									synonymslist = iw2.getSynonyms(1);
									if(synonymslist!=null) {
										itr2 = synonymslist.getIterator();
										while(itr2.hasNext()) {
											w3 = itr2.nextWord();
											if(w3!=null) contrastwords.addElement(w3.getLemma());
										}
									}
								}
							}
						}
					}
					xml.setNXMLAttribute(word,"CONTRASTWORDS",contrastwords);

				}

				globalcontrast = mDM.isContrasting(word);
				found=false;
				localcontrast=null;
				for(int i=0;(i<prevwords.size())&&!found;i++) {
					tempword = (Element)prevwords.elementAt(i);
					prevcontrastwords = (Vector)xml.getNXMLAttribute(tempword,"CONTRASTWORDS");
					if(prevcontrastwords!=null) {
						for(int j=0;(j<prevcontrastwords.size())&&!found; j++) {
							if(lemma.equals(prevcontrastwords.elementAt(j))) {
								localcontrast=tempword;
								found=true;
							}
						}
					}
				}
				if(localcontrast!=null) {
					contrastgroup++;
					newnode = xml.createElement("CONTRAST");
					newnode.setAttribute("ID",String.valueOf(contrastgroup));
					xml.spliceParent(word,newnode);
					newnode = xml.createElement("CONTRAST");
					newnode.setAttribute("ID",String.valueOf(contrastgroup));
					xml.spliceParent(localcontrast,newnode);		    
				} else if(globalcontrast!=null) {
					xml.spliceParent(word,xml.createElement("CONTRAST"));
				}

				prevwords.add(word);

				word = (Element)xml.getNXMLAttribute(word,"NEXT");

			}

		}

		/** For each <code>OBJECT</code> element found in the utterance, tries to
            find an instance in the domain knowledge base that is a likely referent */
		public XMLWrapper identifyObjects(XMLWrapper xmlw) throws Exception {
			Vector objects = xmlw.getAllNodesOfType("OBJECT");
			Element word;
			Element object;
			String lemma;
			StringBuffer SB;
			Vector words;
			KnowledgeBase.Instance objectinstance;

			if(Config.logging)System.out.println("identifyObjects: " + xmlw.pprint2StringAll());
			Document xml = xmlw.getDocument();

			for(int i=0;i<objects.size();i++) {
				object = (Element)objects.elementAt(i);
				SB = new StringBuffer();
				words = xmlw.getAllNodesOfType(object,"W");
				if(words!=null) {
					for(int j=0;j<words.size();j++) {
						word = (Element)words.elementAt(j);
						lemma = word.getChildNodes().item(0).getNodeValue();
						if(lemma!=null)
							SB.append(lemma+" ");
					}
					objectinstance = mKnowledgeBase.getBestInstanceMatch(SB.toString());
					if(objectinstance!=null){
						object.setAttribute("ID", objectinstance.getID());
						if(Config.logging)System.out.println("SB: " + SB + " objectinstance:" + objectinstance + " ID: " + objectinstance.getID());
					}
				}
			}
			return xmlw;
		}

		/** For each <code>ACTION</code> element found in the utterance, tries to 
	    find a corresponding defined action class in the domain knowledge base */
		public XMLWrapper identifyActions(XMLWrapper xml) throws Exception {
			Vector actions = xml.getAllNodesOfType("ACTION");
			Element action, word;
			String pos, lemma, identifier, hypervalue;
			Element gesture;
			Vector words;
			boolean found=false;
			for(int i=0;i<actions.size();i++) {
				action = (Element)actions.elementAt(i);
				words = xml.getAllNodesOfType(action,"W");
				if(words!=null) {
					for(int j=0;j<words.size();j++) {
						word = (Element)words.elementAt(j);
						pos = word.getAttribute("pos");
						//lemma = word.getAttribute("LEM");
						lemma = word.getChildNodes().item(0).getNodeValue(); //Yoichi 2015.7.7
						if((pos!=null)&&(lemma!=null)) {
							if(pos.contains("VB")) {
								identifier = lemma.toUpperCase();
								//System.out.println("[identifyActions] Action lemma: "+identifier);
								gesture = mKnowledgeBase.getGesture(identifier);
								//gesture = mKnowledgeBase.getCompactGestureElement(mKnowledgeBase.mDoc.getDocument(),identifier); //Yoichi 2015.7.7

								if(DEBUG)System.out.println("###Action lemma identifier: " + identifier + " gesture: " + gesture);
								if(gesture!=null) 
									action.setAttribute("ID",identifier);
								else {
									if(identifier.equals("KNOW")||identifier.equals("LIKE")) found=true; // avoid a bug in WN!!
									IndexWord iw = mWordDB.lookupIndexWord(POS.VERB, identifier);
									if((iw!=null)&&!found) {
										//System.out.println("Verb "+identifier+" found in WN!");
										PointerTargetList hypernymlist;
										PointerTargetListItr itr;
										jwnl.word.Word w;
										jwnl.word.Synset s;
										jwnl.word.Word wordset[];
										int sensecount = iw.getSenseCount();
										if(sensecount>10) sensecount=10;
										for(int k=1;(k<=sensecount)&&!found;k++) {
											hypernymlist = iw.getDirectHypernyms(k);
											if(hypernymlist!=null) {
												itr = hypernymlist.getIterator();
												if(itr!=null) {
													while(itr.hasNext()&&!found) {
														s = itr.nextSynset();
														if(s!=null) {
															wordset=s.getWords();
															for(int l=0;(l<wordset.length)&&!found;l++) {
																w = wordset[l];
																if(w!=null) {
																	hypervalue = w.getLemma().toUpperCase();
																	if(Config.logging)System.out.println("hypervalue: " + hypervalue);
																	gesture = mKnowledgeBase.getGesture(hypervalue);
																	if(gesture!=null) {
																		//System.out.println("Found in VerbDatabase!");
																		identifier = hypervalue;
																		found = true;
																	}
																}
															}
														}
													}
												}
											}
										}
										if(found)
											action.setAttribute("ID",identifier);
									}
								}
							}
						}
					}
				}
			}
			return xml;

		}

		/** Updates ParticipationFramework with information contained in the 
            <code>UTTERANCE</code> tag.  It looks for a <code>SPEAKER</code>,
            <code>HEARER</code> and a <code>SCENE</code> attributes.  The <code>HEARER</code> 
            attribute is optional, if none is found, the ParticipationFramwork object will
            choose an addressee.  If no <code>SCENE</code> is found, it creates it 
            and sets its value to <code>"LOCAL"</code> */
		public void markInteractionStructure(XMLWrapper xml) throws Exception {
			Element utterance = (Element)xml.getFirstNodeOfType("UTTERANCE");
			if(utterance==null) return;
			String speaker = utterance.getAttribute("SPEAKER");
			String hearer = utterance.getAttribute("HEARER");
			String scene = utterance.getAttribute("SCENE");
			if(scene==null) { utterance.setAttribute("SCENE","LOCAL"); scene="LOCAL"; }

			if(mParticipationFrameworkBase!=null) {
				ParticipationFramework pf = mParticipationFrameworkBase.getParticipationFramework(scene);
				if(pf!=null) {
					//System.out.println("[LanguageModule] Scene: "+scene);
					if((speaker!=null)&&(hearer!=null)) {
						pf.setSpeakerAddressing(speaker,hearer);
					} else if(speaker!=null) {
						pf.setSpeaker(speaker);
					}
					//System.out.println("[LanguageModule] PF: "+pf);
				}
			}
		}

		/** Evokdes the Discourse Tagger on the supplied <i>xml</i> utterance */
		public XMLWrapper process(XMLWrapper xml) throws Exception {
			if(DEBUG)System.out.print("...done\nMarking New Items...");
			markNew(xml);
			if(DEBUG)System.out.print("..done\nMarking Topic Shifts...");
			markTopicShifts(xml);
			if(DEBUG)System.out.print("...done\nMarking Information Structure...");
			markInformationStructure(xml);
			if(DEBUG)System.out.print("...done\nMarking Contrast...");
			markContrast(xml);
			if(DEBUG)System.out.print("...done\nIdentifying Objects...");
			identifyObjects(xml);
			if(DEBUG)System.out.print("...done\nIdentifying Actions...");
			identifyActions(xml);
			if(DEBUG)System.out.print("...done\nMarking Interaction Structure...");
			markInteractionStructure(xml);
			if(DEBUG)System.out.print("...done\n");
			return xml;
		} 	
	}


	/***************************************************/

	/** Constructs a new language module, setting up all the transducers and knowledge bases */
	public LanguageModule(KnowledgeBase kb, POSTagger postagger, BeatModule output) throws Exception { 
		this(kb,null,postagger,output);
	}

	/** Constructs a new language module, setting up all the transducers and knowledge bases */
	public LanguageModule(KnowledgeBase kb, ParticipationFrameworkBase pfb, POSTagger postagger, BeatModule output) throws Exception { 
		super(output);
		String[] ppLinks = {
				//"SimpleLink"
				"opennlp.grok.preprocess.sentdetect.EnglishSentenceDetectorME",
				"opennlp.grok.preprocess.tokenize.EnglishTokenizerME",
				"opennlp.grok.preprocess.postag.EnglishPOSTaggerME",
				"opennlp.grok.preprocess.chunk.EnglishChunkerME",
		}; 
		mKnowledgeBase = kb;
		mParticipationFrameworkBase = pfb;
		mPreprocessor = new Pipeline(ppLinks);
		mTokenizer = new SimpleTokenizer();
		//mPosTagger = new ConexorPOSTagger("arrack.media.mit.edu",9090);
		mPosTagger = postagger;
		
		//new ConexorPOSTagger("aragog.media.mit.edu",9090);
		mChunker = new SimpleChunker();
		mWordDB = new FileBackedDictionary();	
		mDiscourseModel = new DiscourseModel();
		mDiscourseTagger = new DiscourseTagger(mDiscourseModel);

		//mUtterance = new XMLWrapper("<UTTERANCE>have is Final</UTTERANCE>");
		//annotate();
	}

	/** Clears the discourse history */
	public void reset() {
		if(mDiscourseModel!=null) mDiscourseModel.clearHistory();
	}

	public XMLWrapper replaceXML(XMLWrapper xml, String from, String to) {
		String xslA = "<?xml version=\"1.0\"?><xsl:stylesheet xmlns:xsl = \"http://www.w3.org/1999/XSL/Transform\"><xsl:template match=/|*|@*|text()\" priority=\"1\"><xsl:copy><xsl:apply-templates select=\"@*\"/><xsl:apply-templates/></xsl:copy></xsl:template><xsl:template match=\"";
		String xslB = "\" priority=\"2\"><";
		String xslC = "><xsl:apply-templates/></";
		String xslD = "></xsl:template></xsl:stylesheet>";
		XMLWrapper newxml = null;
		try {

			XMLWrapper.Transform xsl = new XMLWrapper.Transform(xslA+from+xslB+to+xslC+to+xslD);
			newxml = new XMLWrapper(xml.applyTransform(xsl,xml.getDocument()));
		}  catch(Exception e) {
			if(Config.logging)System.out.println("[LanguageModule] 'replaceXML' Exception: "+e);
		}

		return newxml;

	} 

	public XMLWrapper convertFromNLPDocument(opennlp.common.xml.NLPDocument xml) {
		XMLWrapper newxml = null;
		try {
			if(Config.logging)System.out.println("INPUT OF POS: "+xml);
			XMLWrapper.Transform xsl = new XMLWrapper.Transform(new File("XMLData/nlpConverter.xsl"));
			XMLWrapper oldxml = new XMLWrapper(xml.toXml());
			newxml = new XMLWrapper(XMLWrapper.applyTransform(xsl,oldxml.getDocument()));
			if(Config.logging)System.out.println("OUTPUT OF POS new xml: "+newxml);
			if(Config.logging)System.out.println("OUTPUT OF POS xsl: "+xsl);
			if(Config.logging)System.out.println("OUTPUT OF POS oldxml: "+oldxml);
		} catch(Exception e) {
			if(Config.logging)System.out.println("[LanguageModule] 'replaceXML' Exception: "+e);
		}

		return newxml;

	}

	/** Runs the series of internal transducers on the current utterance */
	public void annotate() {
		Vector textnodes = mUtterance.getAllNodesOfType("TEXT");
		String text = "";
		for(int i=0;i<textnodes.size();i++) {
			if(mUtterance.getAncestorOfType((org.w3c.dom.Node)textnodes.elementAt(i),"W")==null) {
				text = text+" "+((org.w3c.dom.Node)(textnodes.elementAt(i))).getNodeValue();
				if(Config.logging)System.out.println(text);
			}
		}	

		try {

			if(DEBUG || Config.logging) System.out.println("\n\n=== Output from Preprocessor ===\n");
			if(Config.logging)System.out.println("Processing: "+text);
			opennlp.common.xml.NLPDocument utt = mPreprocessor.run(text);
			if(Config.logging){
				System.out.println("Displaying the output");
				System.out.println(utt.toXml());
				//System.out.println("Done");
			}

			mUtterance = new XMLWrapper(utt.toXml());
			if(Config.logging){
				System.out.println("pprinting...");
				mUtterance.pprint();
			}
			if(Config.logging)System.out.println("Done printing");
			if(Config.logging)System.out.println("Translating");
			mUtterance = convertFromNLPDocument(utt);
			if(mUtterance==null) return;
			if(Config.logging) {
				System.out.println("Done translating");
				mUtterance.pprint();
			}
			
			  if(DEBUG) System.out.println("\n\n=== Output from POS Tagger ===\n");
			    if(mPosTagger!=null) mPosTagger.process(mUtterance);
			/*
	    if(DEBUG) System.out.println("=== Input ===\n");
	    if(DEBUG) mUtterance.pprint();
	    //mUtterance.dump(System.out);
	    if(DEBUG) System.out.println("\n\n=== Output from Tokenizer ===\n");
	    if(mTokenizer!=null) mTokenizer.process(mUtterance);
	    if(DEBUG) mUtterance.pprint();
	    //System.out.println(mUtterance);replaceXML(mUtterance,"s","CLAUSE");
	    if(DEBUG) System.out.println("\n\n=== Output from POS Tagger ===\n");
	    if(mPosTagger!=null) mPosTagger.process(mUtterance);
	    if(DEBUG) mUtterance.pprint();
	    //System.out.println(mUtterance);
	    if(DEBUG) System.out.println("\n\n=== Output from Chunker ===\n");
	    if(mChunker!=null) mChunker.process(mUtterance);
	    if(DEBUG) mUtterance.pprint();
			 */



			if(DEBUG) System.out.println("mUtterance: "+mUtterance);
			if(DEBUG) System.out.println("\n\n=== Output from Discourse Tagger ===\n");
			if(mDiscourseTagger!=null) mDiscourseTagger.process(mUtterance);
			if(DEBUG) mUtterance.pprint();
			//System.out.println(mUtterance);
			if(DEBUG) System.out.println("\n=== done ===\n");

			mDiscourseModel.addUtterance(mUtterance);

		} catch(Exception e) {
			if(Config.logging)System.out.println("[LanguageModule] Exception: "+e);
		}
	}

	protected String transduce(String xml) throws Exception {
		if(Config.logging)System.out.println("creating wrapper");
		mUtterance = new XMLWrapper(xml);
		annotate();
		return mUtterance.toString(); 
	}  //XML transducer

	protected Document transduce(Document xml) throws Exception {
		mUtterance = new XMLWrapper(xml);
		annotate();
		return mUtterance.getDocument(); 
	}  //XML transducer



	/** ---- TEST STUB ---- */
	public static void main(String[] arg) throws IOException {

		String test = "<?xml version='1.0' encoding='utf-8'?><DATA><UTTERANCE SPEAKER=\"AGENT\" HEARER=\"USER\" SCENE=\"MIT\">I am reporting from the Medialab!</UTTERANCE><UTTERANCE SPEAKER=\"AGENT\" HEARER=\"USER\">They have both very big and very small projects here.</UTTERANCE><UTTERANCE SPEAKER=\"AGENT\" HEARER=\"USER\">One project is some kind of an actor.</UTTERANCE><UTTERANCE SPEAKER=\"AGENT\" HEARER=\"USER\">To make it talk, you just have to type something!</UTTERANCE></DATA>";

		try {
			KnowledgeBase KB = new KnowledgeBase("XMLData/database.xml");
			ParticipationFrameworkBase PFB = new ParticipationFrameworkBase(KB.getAllScenes());
			LanguageModule lm = new LanguageModule(KB,PFB,null,null);
			lm.setDEBUG(true);

			File f = new File("demotext.xml");
			//System.out.println(f);

			XMLWrapper file = new XMLWrapper(test);
			//System.out.println(file.toString());
			file.pprint();
			//System.out.println(file);
			Vector utterances = file.getAllNodesOfType("UTTERANCE");
			String output; String input;

			//for(int i=0;i<utterances.size();i++) {
			//	System.out.println("\n=========== Utterance "+(i+1)+" ===========\n");
			//	((Element)utterances.elementAt(i)).normalize();
			//	input = ((Element)utterances.elementAt(i)).toString();
			//	System.out.println("input="+input);
			//output = lm.transduce(input);
			//System.out.println(output);
			//}

			output = lm.transduce("<UTTERANCE SPEAKER=\"AGENT\" HEARER=\"USER\" SCENE=\"MIT\">I am reporting from the Medialab!</UTTERANCE>");
			System.out.println("output: " + output);

		} catch(Exception e) {
			System.out.println("Exception: "+e);
		}
	}


}
