/* -------------------------------------------------------------------------

   POSTagger.java
     - POS tagger for the BEAT gesture toolkit

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

import beat.utilities.XMLWrapper;

/************************* POS TAGGER *************************/

/** POSTagger is an interface that inserts Part-of-Speech information 
    into all &lt;W&gt; nodes found in the XML passed to its processing 
    method. Usually this is acheived by communicating with an external 
    POS Tagger, so this interface effectively acts as a glue between 
    the Language Module and a POS engine.
*/ 
public interface POSTagger {
	
	/** A simple structure that holds all the information received from a Part-of-Speech Tagger */
	public class Word {
		/** The original form of the word */
		public String mForm;
		/** The lemmatized or standard form of the word */
		public String mLemma;
		/** Information about the role of the word within the sentence */ 
		public String mSyntax;
		/** The part-of-speech tag itself */
		public String mPOS;
		/** Creates a new word containing the supplied information */
		public Word(String form, String lemma, String syntax, String pos) {
			mForm   = form;
			mLemma  = lemma;
			mSyntax = syntax;
			mPOS    = pos;
		}	
	}
	
		/** Maps POS (Part-of-Speech) codes to the standardized Penn Treebank POS codes */
	public Hashtable mPOSCodes = new Hashtable();
		/** Maps SYN codes (Syntax) codes to a BEAT set of codes */
	public Hashtable mSYNCodes = new Hashtable();

		/** Reads and interprets from the supplied stream	the POS information for a 
        single word.  The stream is usually the entire output from an external 
        POS engine and this method tokenizes a section belonging to one word,
        returning the results as a proper Word structure. */
	public Word readWord(StreamTokenizer ST);

		/** Takes in XML, adds POS attributes to all &lt;W&gt; elements and returns 
        the results */
	public XMLWrapper process(XMLWrapper xml) throws Exception; 
}
