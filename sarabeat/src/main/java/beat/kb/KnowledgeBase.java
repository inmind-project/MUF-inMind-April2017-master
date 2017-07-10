/* -------------------------------------------------------------------------

   KnowledgeBase.java
     - Implements a domain Knowledge Base for the BEAT gesture toolkit

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
package beat.kb;

import java.util.*;
import java.io.File;

import beat.Config;
import org.w3c.dom.*;

import beat.utilities.XMLWrapper;

/** The KnowledgeBase is an interface to the domain database that contains definitions
    of object types, instances and scenes as well as gesture definitions that associate
    hand and arm configurations with lexical values. 

		The database is stored in the following XML format:
	<code>
	<ul>
	<li> file ::= &lt;DATA&gt; { entry }* &lt;DATA&gt;
	<li> entry ::= <b>type</b> | <b>instance</b> | <b>gesture</b> | <b>scene</b>
	<li> <b>type</b> ::= &lt;TYPE NAME="string" CLASS="class"&gt; { feature }* &lt;TYPE&gt;
	<li> class ::= OBJECT | PERSON | PLACE
	<li> feature ::= symfeature | numfeature
	<li> symfeature ::= &lt;SYMFEATURE NAME="string" TYPICAL="typicalsym"/&gt;
	<li> typicalsym ::= string{,string}* | ANY
	<li> numfeature ::= &lt;NUMFEATURE NAME="string" TYPICAL="typicalnum"/&gt;
	<li> typicalnum ::= float{-float}
	<li> <b>instance</b> ::= &lt;INSTANCE OF="typename" ID="string" {featurename=featurevalue}* /&gt;
	<li> typename ::= <i>name of a previously defined type</i>
	<li> featurename ::= <i>name of a feature defined for this particular type</i>
	<li> featurevalue ::= string | float
	<li> <b>gesture</b> ::= &lt;GESTURE TYPE="gesturetype" VALUE="string"&gt; { rightarm } { leftarm } &lt;/GESTURE&gt;
	<li> rightarm ::= &lt;RIGHTARM HANDSHAPE="string" TRAJECTORY="string"/&gt;
	<li> leftarm ::= &lt;LEFTARM HANDSHAPE="string" TRAJECTORY="string"/&gt;
	<li> <b>scene</b> ::= &lt;SCENE ID="string"&gt; { object | person }* &lt;/SCENE&gt;
	<li> object ::= &lt;OBJECT ID="string"/&gt;
	<li> person ::= &lt;PERSON ID="string" ROLE="role"/&gt;
	<li> role ::= PARTICIPANT | BYSTANDER | string
	</ul>
	</code>
*/

public class KnowledgeBase {

    public XMLWrapper mDoc;
    
    protected Hashtable mAllTypes = new Hashtable();
    protected Hashtable mAllInstances = new Hashtable();
    protected Hashtable mAllGestures = new Hashtable();
    protected Hashtable mAllScenes = new Hashtable();

	// -----------:: INTERNAL CLASSES ::--------------

	// ----------------- FEATURES --------------------
 
    /** Generic feature asociated with types */
    abstract public class Feature {
	protected String mName;
	public Feature(String name) { mName = name; }
	public String getName() { return mName; }
	public String toString() { return mName; }
	/** Returns true if the value passed falls within this feature's typical range */
	abstract public boolean isTypical(String value);
    }
    
    /** Symbolic or textual features asociated with types */
    public class SymbolicFeature extends Feature {
	protected Vector mTypicalValues = new Vector();
	public SymbolicFeature(String name, String typicalvalues) {
	    super(name);
	    StringTokenizer ST = new StringTokenizer(typicalvalues,",");
	    while(ST.hasMoreTokens()) mTypicalValues.addElement(ST.nextToken());
	}
	public String toString() {
	    return ("<"+mName+" "+mTypicalValues+">");
	}
	/** Returns true if the value passed falls within this feature's typical range */
	public boolean isTypical(String value) {
	    if(value==null) return true;
	    if(mTypicalValues.size()==0) return false;
	    if(((String)(mTypicalValues.firstElement())).length()==0) return false;
	    if(((String)(mTypicalValues.firstElement())).equals("ANY")) return true;

	    for(int i=0; i<mTypicalValues.size(); i++) 
		if(value.equals(mTypicalValues.elementAt(i)))
		    return true;
	    return false;
	}	
    }
    
    /** Numeric range features asociated with types */
    public class NumericFeature extends Feature {
	protected double mMinValue;
	protected double mMaxValue;
	public NumericFeature(String name, String valuerange) {
	    super(name);
	    StringTokenizer ST = new StringTokenizer(valuerange,", -");
	    String first = ST.nextToken();
	    String last = first;
	    if(ST.hasMoreTokens()) last = ST.nextToken(); 
	    mMinValue = (new Double(first)).doubleValue();
	    mMaxValue = (new Double(last)).doubleValue();
	}
	public String toString() {
	    return ("<"+mName+" "+mMinValue+"-"+mMaxValue+">"); 
	}
	/** Returns true if the value passed falls within this feature's typical range */
	public boolean isTypical(String value) {
	    if(value==null) return true;
	    double dvalue = (new Double(value)).doubleValue();
	    if((dvalue>=mMinValue)&&(dvalue<=mMaxValue)) 
		return true; 
	    else 
		return false;
	}
    }
    
    // ----------------- TYPE --------------------
    
    /** Datatype defined in the Knowledge Base */
    public class Type {
	protected String mName;
	protected String mClass;
	protected Vector mFeatures = new Vector();
	public Type(Element typenode) throws Exception {
	    mName = typenode.getAttribute("NAME");
	    mClass = typenode.getAttribute("CLASS");
	    Element feature;
	    String ftype; String fname; String typical;
	    Vector features = mDoc.getAllNodesOfType(typenode,new String[]{"SYMFEATURE","NUMFEATURE"});
	    for(int i=0; i<features.size(); i++) {
		feature = (Element)features.elementAt(i);
		fname = feature.getAttribute("NAME");
		typical = feature.getAttribute("TYPICAL");
		if((fname!=null)&&(typical!=null)) {
		    if((feature.getTagName()).equals("NUMFEATURE")) 
			mFeatures.add(new NumericFeature(fname,typical));
		    else
			mFeatures.add(new SymbolicFeature(fname,typical));
		} else
		    throw new Exception("NAME or TYPICAL field missing from feature");
	    }
	    
	}
	public String getName() { return mName; }
	public String getTypeClass() { return mClass; }
	public Vector getFeatures() { return mFeatures; }
	public String toString() {
	    StringBuffer SB = new StringBuffer();
	    SB.append("[TYPE "+mName+"/"+mClass+" ");
	    for(int i=0;i<mFeatures.size();i++) SB.append((mFeatures.elementAt(i)).toString());
	    return SB.toString();
	}
    }
    
    // ----------------- INSTANCE --------------------
    
    /** Instance of a Datatype created in the Knowledge Base */
    public class Instance {
	protected String mID;
	protected Type mType;
	protected Hashtable mValues = new Hashtable();

	public Instance(Element instancenode) throws Exception {
	    mID = instancenode.getAttribute("ID");
	    String type = instancenode.getAttribute("OF");
	    Vector features;
	    Feature feature; String value; 
	    if((mID!=null)&&(type!=null)) {
		mType = (Type)mAllTypes.get(type);
		if(mType!=null) {
		    features = mType.getFeatures();
		    for(int i=0;i<features.size();i++) {
			feature = (Feature)features.elementAt(i);
			value = instancenode.getAttribute(feature.getName());
			if(value!=null) {
			    mValues.put(feature.getName(), value);
			}
		    }
		} else
		    throw new Exception("TYPE is not known in INSTANCE "+mID);
		
	    } else
		throw new Exception("ID or OF values missing for INSTANCE");
	}
	public String getID() { return mID; }
	public Type getType() { return mType; }
	public String getValue(String feature) {
	    return (String)mValues.get(feature);
	}
	public Enumeration getAllValues() {
	    return mValues.elements();
	}
	/** Returns the first value that is not typical for an instance of this type */
	public String getSurprisingValue() {

	    //System.out.println("getSurprisingValue - mType="+mType);

	    if(mType!=null) {
		Vector features = mType.getFeatures();
		Feature feature;
		for(int i=0; i<features.size();i++) {
		    feature = (Feature)features.elementAt(i);
		    if(feature!=null) {

			//System.out.println("getSurprisingValue - Checking if "+feature+" is typical");

			if(!(feature.isTypical((String)mValues.get(feature.getName()))))
			    return (String)mValues.get(feature.getName());
		    }
		}
		
	    }
	    return null;
	    
	}
	public String toString() {
	    return (mID+"/"+mType.getName()+" "+mValues);
	}
    }
    
    // ----------------- SCENE --------------------
    
    /** Scenes group objects that can be seen together */
    public class Scene {
	protected String mID;
	protected Hashtable mAllObjects = new Hashtable();
	
	/** Builds a new scene object from the supplied XML representation */
	public Scene(Element scenenode) {
	    mID = scenenode.getAttribute("ID");
	    Element sceneobject;
	    Vector sceneobjects = mDoc.getAllNodesOfType(scenenode,new String[]{"OBJECT","PERSON","PARTICIPANT"});
	    for(int i=0; i<sceneobjects.size(); i++) {
		sceneobject = (Element)sceneobjects.elementAt(i);
		if(sceneobject!=null) addObject(sceneobject);
	    }  
	}
	
	/** Adds a new object to the scene */
	public void addObject(Element objectnode) {
	    String id = (String)objectnode.getAttribute("ID");
	    if(id!=null) mAllObjects.put(id,objectnode);
	}
	
	/** Returns true if an object with the supplied id is found in the scene */
	public boolean containsObject(String id) {
	    return (mAllObjects.get(id)!=null);
	}
	
	/** Returns the IDs of all <code>PERSON</code>s that have the <code>ROLE</code> of a <code>"PARTICIPANT"<code> */
	public Vector getParticipants() {
	    Enumeration e;
	    Element element;
	    Instance instance;
	    String id;
	    String role;
	    Vector s = new Vector();
	    int i=0;
	    for(e = mAllObjects.elements(); e.hasMoreElements();) {
		element = (Element)e.nextElement();
		if((element.getTagName()).equals("PERSON")) {
		    role = (String)element.getAttribute("ROLE");
		    if(role!=null) {
			if(role.equals("PARTICIPANT")) {
			    id = (String)element.getAttribute("ID");
			    if(id!=null)
				s.add(id);
			}
		    }
		}
	    }
	    return s;
	}

	/** Returns the ID of the Scene */
	public String getID() { return mID; }

       
	
    }
    
    // -----------------------------------------------------
    
    // ----------------- KNOWLEDGE BASE --------------------
    
    /** Constructor takes in the XML database filename */
    public KnowledgeBase(String filename) {
	try {
	    load(new File(filename));
	} catch(Exception e) {
	    System.out.println("[KnowledgeBase] "+e);
	}
    }
    
    /** Loads in the XML database file and sets up all the internal 
        data structures, ready to be queried */
    public void load(File file) throws Exception {
	mDoc=new XMLWrapper(file);
	mDoc.pruneAllNodesOfType(new String[]{mDoc.TEXT});

		if(Config.logging)System.out.println("\n--- Initializing domain knowledge ---");
	
	//Types
	Vector types = mDoc.getAllNodesOfType("TYPE");
	Type type;
	for(int i=0;i<types.size();i++) {
	    type = new Type((Element)types.elementAt(i));
		if(Config.logging)System.out.println("[KnowledgeBase] Read type "+type.getName());
	    mAllTypes.put(type.getName(),type);	    
	}
	//Instances
	Vector instances = mDoc.getAllNodesOfType("INSTANCE");
	Instance instance;
	for(int j=0;j<instances.size();j++) {
	    instance = new Instance((Element)instances.elementAt(j));
		if(Config.logging)System.out.println("[KnowledgeBase] Read instance "+instance.getID());
	    mAllInstances.put(instance.getID(),instance);
	}
	//Gestures
	Vector gestures = mDoc.getAllNodesOfType("GESTURE");
	String value, shape;
	for(int k=0;k<gestures.size();k++) {
	    value = ((Element)gestures.elementAt(k)).getAttribute("VALUE");
	    if(value!=null) {
			if(Config.logging)System.out.println("[KnowledgeBase] Read gesture for value "+value);
		mAllGestures.put(value,(Element)gestures.elementAt(k));
	    } else
		throw new Exception("VALUE or SHAPE missing for gesture");
	}
	//Scenes
	Vector scenes = mDoc.getAllNodesOfType("SCENE");
	Scene scene;
	String id;
	for(int l=0;l<scenes.size();l++) {
	    scene = new Scene((Element)scenes.elementAt(l));
	    id = scene.getID();
		if(Config.logging)System.out.println("[KnowledgeBase] Read scene "+id);
	    mAllScenes.put(id,scene);
	}		
	System.out.println();
    }

    /** Returns all elements identified with the <code>SCENE</code> tag */
    public Enumeration getAllScenes() {
	return mAllScenes.elements();
    }
    
	/** Returns a single instance that matches the supplied id */
    public Instance getInstance(String id) {
	return (Instance)mAllInstances.get(id);
    }
    
    /** Returns an enumeration of all instances found in the knowledge base */
    public Enumeration getAllInstances() {
	return mAllInstances.elements();
    }
    
    /** Returns the instance in the KnowledgeBase that has values that
        correspond most closely to the words found in the passed description.
        null is returned if there is no clear best match or if there is
        no match at all */
    public Instance getBestInstanceMatch(String description) {
	description = description.toLowerCase();
	int[] matchcount = new int[mAllInstances.size()];
	int matches = 0, maxmatches=0;
	int i, j, k;
	Instance bestinstance = null;
	Enumeration ei, ev;
	Instance instance;
	String value;
	for(i=0, ei=mAllInstances.elements();ei.hasMoreElements();i++) {
	    instance = (Instance)ei.nextElement();
	    for(ev=instance.getAllValues();ev.hasMoreElements();) {
		value = (String)ev.nextElement();
		if((description.indexOf(value.toLowerCase())>-1)&&(value.length()>0)) {
		    matchcount[i]++;
		}
	    }
	    if(matchcount[i]>maxmatches) {
		bestinstance=instance; 
		maxmatches=matchcount[i];
	    }
	}
	for(k=0, j=0;k<matchcount.length;k++) if(matchcount[k]==maxmatches) j++;
	if((j<2)&&(bestinstance!=null)) return bestinstance; else return null;
    }
    
    /** Returns the entire gesture definition element as it appears in the
        KnowledgeBase file */
    public Element getGesture(String value) {
	return (Element)mAllGestures.get(value);
    }
    
    /** Returns the gesture as a single compact gesture in a form suitable for 
        a single behavior suggestion element. 
	<br>The format is as follows:
	<pre>	
	&lt;GESTURE_XXXX TYPE="" RIGHT_HANDSHAPE="" RIGHT_TRAJECTORY="" LEFT_HANDSHAPE="" LEFT_TRAJECTORY=""/&gt;
	</pre>	
	<br>Where "XXXX" is "BOTH","RIGHT" or "LEFT" (and then corresponding attributes are dropped) */
    public Element getCompactGestureElement(Document target, String value) {
	Element gesture = (Element)mAllGestures.get(value);
	Element left,right;
	Element compact=null;
	if(gesture!=null) {
	    left = (Element)mDoc.getFirstNodeOfType(gesture,"LEFTARM");
	    right = (Element)mDoc.getFirstNodeOfType(gesture,"RIGHTARM");
	    if((left!=null)&&(right!=null))
		compact = target.createElement("GESTURE_BOTH");
	    else if(left!=null)
		compact = target.createElement("GESTURE_LEFT");
	    else 
		compact = target.createElement("GESTURE_RIGHT");
	    compact.setAttribute("TYPE",gesture.getAttribute("TYPE"));
	    if(right!=null) {
		compact.setAttribute("RIGHT_HANDSHAPE",right.getAttribute("HANDSHAPE"));
		compact.setAttribute("RIGHT_TRAJECTORY",right.getAttribute("TRAJECTORY"));
	    }
	    if(left!=null) {
		compact.setAttribute("LEFT_HANDSHAPE",left.getAttribute("HANDSHAPE"));
		compact.setAttribute("LEFT_TRAJECTORY",left.getAttribute("TRAJECTORY"));
	    }	    
	    
	}
	return compact;
    }
    
    /** Returns <b>true</b> if an object with an id of <i>objectID</i> is found 
        inside the scene with id of <i>sceneID</i>, returns <b>false</b> otherwise */
    public boolean isObservable(String sceneID, String objectID) {
	Scene scene = (Scene)mAllScenes.get(sceneID);
	if(scene!=null) {
	    return scene.containsObject(objectID);
	}
	return false;
    } 
    
    /** TEST STUB */
    public static void main(String[] a) {
	try {
	    KnowledgeBase KB = new KnowledgeBase("XMLData/database.xml");

		if(Config.logging) {
			System.out.println("- matches -");
			System.out.println("Virtual Actor:" + KB.getBestInstanceMatch("Virtual Actor"));
			System.out.println("Actor: " + KB.getBestInstanceMatch("Actor"));
			System.out.println("Box: " + KB.getBestInstanceMatch("Box"));
			System.out.println("Box of chocolates: " + KB.getBestInstanceMatch("Box of chocolates"));
			System.out.println("Just something:" + KB.getBestInstanceMatch("Just something"));
			//System.out.println("AC1:" + KB.getBestInstanceMatch("AC1"));
			System.out.println("hate:" + KB.getBestInstanceMatch("hate"));

			System.out.println("- surprises -");
			System.out.println("BOX1: " + (KB.getInstance("BOX1")).getSurprisingValue());
			System.out.println("PUNK1: " + (KB.getInstance("PUNK1")).getSurprisingValue());
			//System.out.println((KB.getInstance("GENIUS1")).getSurprisingValue());

			System.out.println("- gestures -");
			//System.out.println("HATE: " + KB.getCompactGestureElement(KB.mDoc.getDocument(),"HATE"));
			System.out.println("HATE: " + KB.getGesture("MOVE"));
			System.out.println("PUNK1: " + KB.getCompactGestureElement(KB.mDoc.getDocument(), (KB.getInstance("PUNK1")).getSurprisingValue()));

			System.out.println("- observable -");
			System.out.println(KB.isObservable("MIT", "ML1"));
			System.out.println(KB.isObservable("MIT", "BOX"));
		}
	    
	}catch(Exception e) {
	    System.out.println("ex: "+e);
	};
    }
    
    
}
