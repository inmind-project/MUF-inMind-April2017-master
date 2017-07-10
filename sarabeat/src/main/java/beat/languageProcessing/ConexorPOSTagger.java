/* -------------------------------------------------------------------------

   ConexorPOSTagger.java
     - Implements POS tagging for the BEAT gesture toolkit

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
import java.net.*;

// XML DOM Classes
import org.w3c.dom.*;

import beat.utilities.XMLWrapper;


    /* -------------------- CONEXOR POS TAGGER  ------------------*/

    /** The ConexorPOSTagger is a working POSTagger that interfaces with a Conexor EngLite POS
        Server to retreive POS information.  */
    public class ConexorPOSTagger implements POSTagger {

		Socket mPosSocket = null;
		PrintWriter mPosOut = null;
		InputStreamReader mPosIn = null;

		/** Translates Conexor POS (part-of-speech) tags to the Penn Treebank tagset */
		protected final String[][] mConexorPOS= { 
			{"A","JJ"},{"ABBR","NN"},{"ADV","RB"},{"CC","CC"},{"CS","IN"},{"DET","DT"},{"EN","RB"},{"INFMARK>","TO"},
			{"ING","VB"},{"INTERJ","UH"},{"N","NN"},{"NEG-PART","RB"},{"NUM","CD"},{"PREP","IN"},{"PRON","PR"},{"V","VB"}};

		/** Translates Conexor SYN (syntax) tags to a neutral tagset */
		protected final String[][] mConexorSYN= {
			{"&>N","NNPE"},{"&NH","NNHD"},{"&N<","NNPO"},{"&>A","RBPE"},{"&AH","RBHD"},{"&A<","RBPO"},
			{"&AUX","VBX"},{"&VP","VBP"},{"&VA","VBA"},{"&>CC","CCHD"},{"&CC","CCPO"},{"&CS","INSUB"}};
		
		/** Creates an interface to an EngLite server wrapper that is running on <i>server</i> and listening to <i>port</i> */
		public ConexorPOSTagger(String server, int port) {
			try {
				for(int i=0;i<mConexorPOS.length;i++) mPOSCodes.put(mConexorPOS[i][0],mConexorPOS[i][1]);
				for(int j=0;j<mConexorSYN.length;j++) mSYNCodes.put(mConexorSYN[j][0],mConexorSYN[j][1]);
				System.out.print("[IConexorPOSTagger] Connecting to Conexor POS server at "+server+":"+port+"...");
				mPosSocket = new Socket(server,port);
				mPosOut = new PrintWriter(mPosSocket.getOutputStream(),true);
				mPosIn = new InputStreamReader(mPosSocket.getInputStream());
				System.out.println("success!");
			} catch(IOException e) {
				System.err.println("failed!!\nIOException: "+e); System.exit(1);
			}
		}
		
		/** Reads in information for one word, assuming the format is "[form:lemma:syntax:pos]" */
		public POSTagger.Word readWord(StreamTokenizer ST) {
            if(ST.ttype=='[') {
				try {
					if(ST.nextToken()!=ST.TT_WORD) throw new Exception("Expected Form");
					String form = ST.sval.toString();                
					if(ST.nextToken()!=ST.TT_WORD) throw new Exception("Expected Lemma");
					String lemma = ST.sval.toString();
					if(ST.nextToken()!=ST.TT_WORD) throw new Exception("Expected Syntax");
					String syntax = (String)mSYNCodes.get((ST.sval.toString()).toUpperCase());
					if(ST.nextToken()!=ST.TT_WORD) throw new Exception("Expected Pos");
					String posread = (ST.sval.toString()).toUpperCase();

					//System.out.println("[readWord] POS = "+posread);

					if(posread.startsWith("&")) posread=posread.substring(1);
					if(posread.equals("<?>")) posread=syntax.substring(0,1); // use syntax guess
					String pos = (String)mPOSCodes.get(posread);
					if(pos==null) pos = new String("");
					return new POSTagger.Word(form,lemma,syntax,pos);
				} catch(Exception e) {
					System.out.println("[ConexorPOSTagger] Exception in ConexorPOSTagger.readWord() on reading "+ST.sval+" : "+e);
				}
			}
			return null;
		}

	    /** Evokes the POS Tagger, processing the supplied <i>xml</i> */
		public XMLWrapper process(XMLWrapper xml) throws Exception {
			
		    Vector wordnodes = xml.getAllNodesOfType("W");
		    Vector textnodes = new Vector();
		    StringBuffer alltext = new StringBuffer();
		    String spantext;
		    Vector allwords = new Vector();
		    org.w3c.dom.Node textnode;
		    
		    // Create one continuous string from all text nodes
		    for(int i=0;i<wordnodes.size();i++) {
			textnode = ((org.w3c.dom.Node)(wordnodes.elementAt(i))).getFirstChild();
			if(textnode!=null) {
			    spantext = (textnode.getNodeValue()).replace('\n',' ');
			    textnodes.addElement(spantext);
			    alltext.append(spantext);
			}
		    }
		    
		    // Send the string to the POS server and read the returned words
		    if((mPosOut!=null)&&(mPosIn!=null)) {
			try {
			    mPosOut.println("all::"+alltext.toString());
			    StreamTokenizer ST = new StreamTokenizer(mPosIn);
			    ST.wordChars('a', 'z'); ST.wordChars('A', 'Z'); ST.wordChars('?','?'); 
			    ST.wordChars('<','>'); ST.wordChars('&','&'); ST.wordChars('_','_'); ST.wordChars(' ',' ');
			    ST.ordinaryChar('['); ST.ordinaryChar(']'); ST.ordinaryChar('*');
			    ST.whitespaceChars(':',':');
			    ST.eolIsSignificant(false);
			    ST.lowerCaseMode(true);
			    ST.parseNumbers();
			    
			    StringTokenizer wordparts;
			    String wordpart;
			    
			    POSTagger.Word word;
			    int nodeindex = -1;
			    boolean found;
			    while((ST.nextToken()!=ST.TT_EOF) && (ST.ttype!='*')) {
				if(ST.ttype=='[') {
				    word = readWord(ST);
				    System.out.print("["+word.mForm+":"+word.mLemma+"]");
				    wordparts = new StringTokenizer(word.mForm,"_ ");
				    while(wordparts.hasMoreTokens()) {
					found=false;
					wordpart  = wordparts.nextToken();
					System.out.print("."+wordpart);
					while((!found)&&(++nodeindex<textnodes.size())) 
					    found = ((((String)textnodes.elementAt(nodeindex)).toLowerCase()).indexOf(wordpart)>=0);
					if(found) {
					    ((Element)wordnodes.elementAt(nodeindex)).setAttribute("POS",word.mPOS);				
					    ((Element)wordnodes.elementAt(nodeindex)).setAttribute("LEM",word.mLemma);				
					    ((Element)wordnodes.elementAt(nodeindex)).setAttribute("SYN",word.mSyntax);				
					}
				    }
				    
				}
			    }		    
			} catch (Exception e) { System.out.println("[ConexorPOSTagger] Exception while POS tagging: "+e); }				
			
		    }
		    System.out.println(xml);
		    return xml;
		    
		}
    }

