/* -------------------------------------------------------------------------

   FestivalWinTimingSource.java
     - Obtains timing information from Festival for the BEAT gesture toolkit
     - Works with FestivalServer for Windows!

   BEAT is Copyright(C) 2000-2002 by the MIT Media Laboratory.  
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
package beat.speechTiming;

import java.util.*;
import java.io.*;
import java.net.*;
import KQML.*;

import org.w3c.dom.*;

/** Obtains timing information in real-time from the Festival TTS using a
    custom KQML-based interface.
    The primary method--getTiming()--takes the XML tree
    to be produced (after generation and filtering)--and returns the timing information
    for the speech in the form of a Vector of TimingSource.TimedEvent objects.
    Each such object has a time stamp (relative to the start of speech) and an 
    event which specifies either the start of a word or a viseme. Note that
    Festival actually generates the audio file in the process of obtaining
    the timing information.

    <h3>Change log:</h3>
    <table border="1">
    <tr><th>Date<th>Who<th>What</tr>
    <tr><td>4/1/01<td>T. Bickmore<td>Created.</tr>
	<tr><td>7/10/01<td>H. Vilhjalmsson<td>Modified constructor to take client info.<br>Added a voice parameter to festival command</tr>
	<tr><td>6/24/02<td>H. Vilhjalmsson<td>Forking off as FestivalWinTimingSource</tr>
	<tr><td>6/24/02<td>H. Vilhjalmsson<td>Replaced COM.receivePerformative(0) with a BufferedReader directly on socket</tr>
    </table>
    */

// TODO!! Have to move INTONATION_BREAK to *start* of clause, and must implement DURATION!

public class FestivalWinTimingSource extends TimingSource {
  private KQMLCommunication COM=null; //used for input only
  private OutputStream out;
    private BufferedReader in;
  private Socket socket;
  private String myHost="quaffle";
  private int myPort=8030;
  private String TTSHost="aragog";
  private int TTSPort=8022; //  8022=BEAT  8012=REA  (for when they're running at the same time)

	/** Translates speaker names to Festival voice names */
	protected final String[][] voices= { 
	  {"DEFAULT","rab_diphone"},
	  {"AGENT","rab_diphone"},
	  {"MACK","ked_diphone"},
	  {"REA","tll_diphone"},
	  {"DILBERT","kal_diphone"},
	  {"PETER","rab_diphone"},
	  {"NED","ked_diphone"},
	  {"OLAF","kal_diphone"},
	  {"SPACY","kal_diphone"},
	  {"ODEO","rab_diphone"}
	};

  private Hashtable mNameToVoice = new Hashtable();

  /** Constructs a FestivalTimingSource given the specified host and port that
      the Festival client and server are listening on. */
  public FestivalWinTimingSource(String clientHost, int clientPort, String festivalHost,int festivalPort) throws Exception {
			this.myHost=clientHost;
			this.myPort=clientPort;
			this.TTSHost=festivalHost;
			this.TTSPort=festivalPort;
			System.out.print("[FestivalWinTimingSource] Connecting to Festival at "+TTSHost+":"+TTSPort+"...");
			try {
					socket = new Socket(TTSHost,TTSPort); // connect to festival
					out = socket.getOutputStream();
					in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					System.out.println("success!");
					System.out.print("[FestivalWinTimingSource] ");
					COM=new KQMLCommunication ("GBOX", new String[]{"GBOX",myHost+":"+myPort});
					COM.debug = true;
			} catch(IOException e) {
					System.err.println("failed!!\nIOException: "+e); System.exit(1);
			}
			for(int i=0;i<voices.length;i++) mNameToVoice.put(voices[i][0],voices[i][1]);
  }
		
    /** Returns Vector of TimedEvent sorted by time. */
  public Vector getTiming(Document xml) throws Exception {
	String speaker = getSpeaker(xml);
    //First extract speech+intonation string for Festival...
    StringBuffer output=new StringBuffer();
    extractSpeechR(null,null,false,xml,output);
    requestTTSTimings(speaker,output.toString());
    return retrieveTTSTimings();
  }

  /** Assumes you can (and do!) have situations like node P has
      children A and B, and there are accents on P and B (P & A conflicts
      should be eliminated by the filter). Then the output should be:
      {Paccent A} {Baccent B} -- that is, the phrase (P) accent should 
      prefix everything until a lower-level accent is detected. */

  // <INTONATION_ACCENT ACCENT="type">, <INTONATION_TONE ENDTONE="type">, <INTONATION_BREAK ["DURATION=nseconds"]>

  private void extractSpeechR(String accent,String tone,boolean isBreak,Node node,StringBuffer output) {
    if(node instanceof Text) {
      String text=((Text)node).getData().trim();
      if(text.length()>0) {
	output.append(makeUtterance(accent,tone,isBreak,formatSpeechString(text)));
      };
    } else { //non-leaf node
      if(node.getNodeName().equals("INTONATION_ACCENT")) { //update params
	accent=((Element)node).getAttribute("ACCENT");
      } else if(node.getNodeName().equals("INTONATION_TONE")) {
	tone=((Element)node).getAttribute("ENDTONE");
      } else if(node.getNodeName().equals("INTONATION_BREAK")) {
	isBreak=true;
      };
      NodeList children=node.getChildNodes();
      for(int i=0;i<children.getLength();i++) {
	String cAccent=(i==0?accent:null);
	String cTone=(i==children.getLength()-1?tone:null);
	boolean cBreak=(i==children.getLength()-1?isBreak:false);
	extractSpeechR(cAccent,cTone,cBreak,children.item(i),output);       
      };
    };
  }

  //Note: accent goes on first word; break & tone go on last word
  private String makeUtterance(String accent,String tone,boolean isBreak,String text) {
    if(accent==null && tone==null && !isBreak)
      return " "+text+" ";
    StringTokenizer tok=new StringTokenizer(text);
    int numTokens=tok.countTokens();
    if(numTokens==0) return "";
    //Format a Festival Utterance:
    String firstWordAttributes="";
    String lastWordAttributes="";
    if(accent!=null) firstWordAttributes+=("(accent "+accent+")");
    if(tone!=null)   lastWordAttributes+=("(tone "+tone+")");
    if(isBreak)      lastWordAttributes+=("(pbreak BB)"); //BB for 'big break'
    StringBuffer buf=new StringBuffer();
    for(int i=0;i<numTokens;i++) {
      String word=tok.nextToken();
      String wordAttributes="";
      if(i==0) wordAttributes+=firstWordAttributes;
      if(i==numTokens-1) wordAttributes+=lastWordAttributes;
      if(wordAttributes.length()==0)
	buf.append(" "+word);
      else
	buf.append(" ( "+word+" ("+wordAttributes+"))");
    };
    return buf.toString();
  }
  
  private void requestTTSTimings(String speech) throws Exception {
	requestTTSTimings("DEFAULT",speech);
  }

  private void requestTTSTimings(String speaker, String speech) throws Exception {
    //speech=formatSpeechString(speech);
    String KQML="(tell :sender \""+myHost+":"+myPort+"\" :receiver TTS :content "+"(timereq :id 1 :voice \""+mNameToVoice.get(speaker)+"\" :text \"( "+speech+" )\" ))";
    String msg=(KQML.length()+1)+"\n"+KQML+"\n ";
    byte[] bytes=msg.getBytes();
    
    if(DEBUG) System.out.println("\nRequest to Festival:\n"+msg);
    
    out.write(bytes);
    out.flush();
  }
    
  private Vector retrieveTTSTimings() throws Exception { //Vector of TimedEvent
    //resp: (tell :content (timeest :id <int> :content " {<start> <event>}* "))
    //where: <event> ::= WORD | <phoneme>

      String length = in.readLine();
      if(DEBUG) System.out.println("Response length: "+length);
      String response = in.readLine();
      if(DEBUG) System.out.println("Response: "+response);
      
      KQMLPerformative perf = (KQMLPerformative)KQMLPerformative.parse(response.getBytes());
      //    KQMLPerformative perf =COM.receivePerformative(0); //0=blocking
    if(DEBUG) {
      System.out.println("\nResults from Festival:\n");
      perf.print(System.out);
      System.out.println("");
    };
    //if(GBModule.DEMO) try { System.in.read(); System.in.read(); }catch(Exception e) {};
    
    if(!perf.getType().toString().equals("tell")) throw new Exception("BEAT.Festival: Expected TELL from TTS.");
    KQMLPerformative content=(KQMLPerformative)perf.getProperty(KQMLWord.KEY_CONTENT);
    if(content==null) throw new Exception("BEAT.Festival: Expected :content from TTS.");
    if(!content.getType().toString().equals("timeest")) throw new Exception("BEAT.Festival: Expected timeest from TTS.");
    KQMLString eventKQMLString=(KQMLString)content.getProperty(KQMLWord.KEY_CONTENT);
    if(eventKQMLString==null) throw new Exception("BEAT.Festival: Expected timeest :content from TTS.");
    String eventString=eventKQMLString.toString();
    Vector results=new Vector();
    StringTokenizer tok=new StringTokenizer(eventString);
    TimedEvent lastphoneme=null;
    while(tok.hasMoreTokens()) {
      String time=tok.nextToken();
      String event=tok.nextToken();
      TimedEvent te=new TimedEvent();
      te.time=new Double(time).doubleValue()/1000.0;
      if(event.equals("WORD")) 
	te.event=WORD;
      else {
	te.event=goofballTranslate(mapPhoneme(event));
	lastphoneme = te;
      }
      results.addElement(te);
    };

    /* There has to be a better way to do this - let's start with adding a column in Gantt Compiler -hhv(2)
    // Need to add one more word placeholder to represent the end of an utterance -hhv(1)
    if(lastphoneme!=null) {
	TimedEvent te=new TimedEvent();
	te.time = lastphoneme.time + 0.04; // WAG!
	te.event=WORD;
	results.addElement(te);
    }
    */

    return results;
  }
    
  private String formatSpeechString(String speech) {
    //Remove characters offensive to Festival...
    StringBuffer buf=new StringBuffer(speech);
    for(int i=0;i<buf.length();i++)
      if(!(Character.isLetterOrDigit(buf.charAt(i))))
	/* buf.charAt(i)=='(' ||
	   buf.charAt(i)==')' ||
	   buf.charAt(i)=='*' ||
	   buf.charAt(i)=='+' ||
	   buf.charAt(i)=='%'
	   )) */
	buf.setCharAt(i,' ');
    //return "( "+buf.toString()+" )";
    return buf.toString();
  }

  /** Maps a Festival phoneme into a character viseme. */
  public int mapPhoneme(String phoneme) {
    if(phoneme.equals("xxx")) //remove for op
      return VISEME_DEBUG;
    else if(
	    phoneme.equals("uh") ||
	    phoneme.equals("@") ||
	    phoneme.equals("A") ||
	    phoneme.equals("^") ||
	    phoneme.equals(">") ||
	    phoneme.equals("aI") ||
	    phoneme.equals("ai") ||
	    phoneme.equals("&") ||
	    phoneme.equals("3r") ||
	    phoneme.equals("ei"))
      return VISEME_A;
    else if(
	    phoneme.equals("b") ||
	    phoneme.equals("m") ||
	    phoneme.equals("p"))
      return VISEME_B;
    else if(
	    phoneme.equals("jh") ||
	    phoneme.equals("d") ||
	    phoneme.equals("t") ||
	    phoneme.equals("g") ||
	    phoneme.equals("n") ||
	    phoneme.equals("N") ||
	    phoneme.equals("k") ||
	    phoneme.equals("s") ||
	    phoneme.equals("S") ||
	    phoneme.equals("z") ||
	    phoneme.equals("T") ||
	    phoneme.equals("D") ||
	    phoneme.equals("l") ||
	    phoneme.equals("Z"))
      return VISEME_D;
    else if(
	    phoneme.equals("i:") ||
	    phoneme.equals("j") ||
	    phoneme.equals("I") ||
	    phoneme.equals("ng") ||
	    phoneme.equals("i") ||
	    phoneme.equals("h") ||
	    phoneme.equals("ii") ||
	    phoneme.equals("e") ||
	    phoneme.equals("E"))
      return VISEME_E;
    else if(
	    phoneme.equals("f") ||
	    phoneme.equals("v"))
      return VISEME_F;
    else if(
	    phoneme.equals("o") ||
	    phoneme.equals("oi") ||
	    phoneme.equals("oU") ||
	    phoneme.equals("oo") ||
	    phoneme.equals("aU") ||
	    phoneme.equals("au") ||
	    phoneme.equals(">i"))
      return VISEME_O;
    else if(
	    phoneme.equals("U") ||
	    phoneme.equals("uu") ||
	    phoneme.equals("u"))
      return VISEME_OO;
    else if(
	    phoneme.equals("sh") ||
	    phoneme.equals("dh") ||
	    phoneme.equals("ch") ||
	    phoneme.equals("th") ||
	    phoneme.equals("tS") ||
	    phoneme.equals("ts") ||
	    phoneme.equals("dZ") ||
	    phoneme.equals("dz"))
      return VISEME_TH;
    else if(
	    phoneme.equals("@@") || 
	    phoneme.equals("r") || 
	    phoneme.equals("w") || 
	    phoneme.equals("9r"))
      return VISEME_R;
     else if(
	     phoneme.equals("#"))
       return VISEME_CLOSED;
    else
      return VISEME_E;
  }


    /*
;  (pmap "CLOSED" 		CLOSED 	CLOSED)
;  (pmap "LATIN AE" 		@ 	A)
;  (pmap "LATIN ALPHA" 		A 	A)
;  (pmap "LATIN TURNED V"	^	A)
;  (pmap "LATIN OPEN O"		>	A)
;  (pmap "dipthong aa+ih"	aI	A)
;  (pmap "LATIN SCHWA"		"&"	A)
;  (pmap "LATIN SCHWA W/HOOK"	3r	A)
;  (pmap "LATIN E"		ei	A)

;  (pmap "LATIN B"		b	B)
;  (pmap "LATIN M"		m	B)
;  (pmap "LATIN P"		p	B)

;  (pmap "LATIN D"		d	D)
;  (pmap "LATIN T"		t	D)
;  (pmap "LATIN SCRIPT G"	g	D)
;  (pmap "LATIN N"		n	D)
;  (pmap "LATIN ENG"		N	D)
;  (pmap "t+sh"			tS	D)
;  (pmap "d+zh"			dZ	D)
;  (pmap "LATIN K"		k	D)
;  (pmap "LATIN S"		s	D)
;  (pmap "LATIN Z"		z	D)
;  (pmap "LATIN ESCH"		S	D)
;  (pmap "LATIN EZH"		Z	D)

;  (pmap "LATIN I" 		i: 	E)
;  (pmap "LATIN Y"		j	E)
;  (pmap "LATIN CAPITAL I" 	I 	E)
;  (pmap "LATIN H"		h	E)
;  (pmap "LATIN OPEN E"		E	E)

;  (pmap "LATIN F"		f	F)
;  (pmap "LATIN V"		v	F)

;  (pmap "LATIN O"		oU	O)
;  (pmap "dipthong aa+uh"	aU	O)
;  (pmap "dipthong ao+ih"	>i	O)

;  (pmap "LATIN UPSILON"        U	OO)
;  (pmap "LATIN U"		u	OO)

;  (pmap "GREEK THETA"		T	TH)
;  (pmap "LATIN ETH"		D	TH)
;  (pmap "LATIN L"		l	TH)
;  (pmap "LATIN R"		9r	R)
;  (pmap "LATIN W"		w	R)

    */    


/* 3-phones only - try 1
    public int mapPhoneme(String phoneme) {
        if(phoneme.equals("i:") ||
        phoneme.equals("I") ||
        phoneme.equals("@") ||
        phoneme.equals("A") ||
        phoneme.equals("^") ||
        phoneme.equals(">") ||
        phoneme.equals("aI") ||
        phoneme.equals("&") ||
        phoneme.equals("ei") ||
        phoneme.equals("E") ||
        phoneme.equals("3r") ||
        phoneme.equals("d") ||
        phoneme.equals("t") ||
        phoneme.equals("g") ||
        phoneme.equals("k") ||
        phoneme.equals("s") ||
        phoneme.equals("z") ||
        phoneme.equals("T") ||
        phoneme.equals("D") ||
        phoneme.equals("S") ||
        phoneme.equals("Z") ||
        phoneme.equals("l") ||
        phoneme.equals("j") ||
        phoneme.equals("h") ||
        phoneme.equals("n") ||
        phoneme.equals("N") ||
        phoneme.equals("tS") ||
        phoneme.equals("dZ"))
            return VISEME_A;
       else if(phoneme.equals("w") || 
        phoneme.equals("9r") ||
        phoneme.equals("oU") ||
        phoneme.equals("aU") ||
        phoneme.equals(">i") ||
        phoneme.equals("U") ||
        phoneme.equals("u"))
            return VISEME_O;
        else
            return VISEME_CLOSED;
	    } */

    /*
    public int mapPhoneme(String phoneme) {
	if(phoneme.equals("ng")) //remove for op
	   return VISEME_DEBUG;
       else if(phoneme.equals("i:") ||
        phoneme.equals("I") ||
        phoneme.equals("@") ||
        phoneme.equals("A") ||
        phoneme.equals("^") ||
        phoneme.equals(">") ||
        phoneme.equals("aI") ||
        phoneme.equals("&") ||
        phoneme.equals("i") ||
        phoneme.equals("ei") ||
        phoneme.equals("E") ||
        phoneme.equals("3r") ||
        phoneme.equals("k"))
            return VISEME_A;
       else if(phoneme.equals("w") || 
        phoneme.equals("9r") ||
        phoneme.equals("oU") ||
        phoneme.equals("aU") ||
        phoneme.equals(">i") ||
        phoneme.equals("U") ||
        phoneme.equals("u"))
            return VISEME_O;
        else if(phoneme.equals("s") ||
        phoneme.equals("z") ||
        phoneme.equals("T") ||
        phoneme.equals("D") ||
        phoneme.equals("S") ||
        phoneme.equals("ch") ||
        phoneme.equals("dh") ||
        phoneme.equals("th") ||
        phoneme.equals("Z") ||
        phoneme.equals("tS") ||
        phoneme.equals("dZ"))
	    return VISEME_TH;
	else
            return VISEME_E;
    }
    */

    //Mappings for Ian's PUNK... given the desired viseme, returns the viseme to use for the PUNK:
    private int goofballTranslate(int correctViseme) {
	switch(correctViseme) {
	case VISEME_A: return VISEME_E;
	case VISEME_B: return VISEME_TH;
	case VISEME_D: return VISEME_A;
	case VISEME_E: return VISEME_A;
	case VISEME_F: return VISEME_TH;
	case VISEME_O: return VISEME_O;
	case VISEME_OO: return VISEME_OO;
	case VISEME_TH: return VISEME_B;
	default: return correctViseme;
	}
    }
}

