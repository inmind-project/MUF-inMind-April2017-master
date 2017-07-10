package beat.languageProcessing;


import java.io.StreamTokenizer;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import beat.Config;
import beat.utilities.XMLWrapper;
import org.w3c.dom.Element;

import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLPClient;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.util.CoreMap;

public class StanfordTagger implements POSTagger {
	public StanfordCoreNLPClient pipeline;
	
	public StanfordTagger(String server, int port) {
		// creates a StanfordCoreNLP object with POS tagging, lemmatization, NER, parsing, and coreference resolution
		Properties props = new Properties();
		props.setProperty("annotators", "tokenize, ssplit, pos, lemma, parse");
		pipeline = new StanfordCoreNLPClient(props, Config.POSTAGGER_HOST, Config.POSTAGGER_PORT, 1);
	}
	
	public static void main(String[] args) {
		
		
	}

	@Override
	public Word readWord(StreamTokenizer ST) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
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
			    alltext.append(spantext+" ");
			}
		    }
		    if(Config.logging)System.out.println("alltext: "+alltext.toString());
		 //   System.out.println("spantext: "+spantext.toString());
		   
		    // Send the string to the POS server and read the returned words
	
			try {
//			    mPosOut.println("all::"+alltext.toString());
//			    StreamTokenizer ST = new StreamTokenizer(mPosIn);
//			    ST.wordChars('a', 'z'); ST.wordChars('A', 'Z'); ST.wordChars('?','?'); 
//			    ST.wordChars('<','>'); ST.wordChars('&','&'); ST.wordChars('_','_'); ST.wordChars(' ',' ');
//			    ST.ordinaryChar('['); ST.ordinaryChar(']'); ST.ordinaryChar('*');
//			    ST.whitespaceChars(':',':');
//			    ST.eolIsSignificant(false);
//			    ST.lowerCaseMode(true);
//			    ST.parseNumbers();
			    
				// read some text in the text variable
//				String text = "Did you love me or he loves me?"; // Add your text here!
				// create an empty Annotation just with the given text
				Annotation document = new Annotation(alltext.toString());
				// run all Annotators on this text
				pipeline.annotate(document);
				// these are all the sentences in this document
				// a CoreMap is essentially a Map that uses class objects as keys and has values with custom types
				List<CoreMap> sentences = document.get(SentencesAnnotation.class);

				int nodeindex=0;
				for(CoreMap sentence: sentences) {
				  // traversing the words in the current sentence
				  // a CoreLabel is a CoreMap with additional token-specific methods
				  for (CoreLabel token: sentence.get(TokensAnnotation.class)) {
				    // this is the text of the token
				    String word = token.get(TextAnnotation.class);
				    // this is the POS tag of the token
				    String pos = token.get(PartOfSpeechAnnotation.class);
				    // this is the NER label of the token
				    String ne = token.get(NamedEntityTagAnnotation.class);
				    
				    String lemma=token.get(LemmaAnnotation.class);
				  //  System.out.println("word:"+word+"\n"+"pos:"+pos+"\n"+"lemma:"+lemma+"\n");
				    ((Element)wordnodes.elementAt(nodeindex)).setAttribute("POS",pos);				
				    ((Element)wordnodes.elementAt(nodeindex)).setAttribute("LEM",lemma);	
				    nodeindex++;
				  }
				  
				  // this is the parse tree of the current sentence
				  Tree tree = sentence.get(TreeAnnotation.class);

				  // this is the Stanford dependency graph of the current sentence
				  SemanticGraph dependencies = sentence.get(CollapsedCCProcessedDependenciesAnnotation.class);
				}

//			    StringTokenizer wordparts;
//			    String wordpart;
//			    
//			    POSTagger.Word word;
//			    int nodeindex = -1;
//			    boolean found;
//			    while((ST.nextToken()!=ST.TT_EOF) && (ST.ttype!='*')) {
//				if(ST.ttype=='[') {
//				    word = readWord(ST);
//				    System.out.print("["+word.mForm+":"+word.mLemma+"]");
//				    wordparts = new StringTokenizer(word.mForm,"_ ");
//				    while(wordparts.hasMoreTokens()) {
//					found=false;
//					wordpart  = wordparts.nextToken();
//					System.out.print("."+wordpart);
//					while((!found)&&(++nodeindex<textnodes.size())) 
//					    found = ((((String)textnodes.elementAt(nodeindex)).toLowerCase()).indexOf(wordpart)>=0);
//					if(found) {
//					    ((Element)wordnodes.elementAt(nodeindex)).setAttribute("POS",word.mPOS);				
//					    ((Element)wordnodes.elementAt(nodeindex)).setAttribute("LEM",word.mLemma);				
//					    ((Element)wordnodes.elementAt(nodeindex)).setAttribute("SYN",word.mSyntax);				
//					}
//				    }
//				    
//				}
//			    }		    
			} catch (Exception e) {}


		if(Config.logging)System.out.println(xml);
		    return xml;
	}
}
