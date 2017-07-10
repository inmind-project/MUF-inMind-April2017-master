/* -------------------------------------------------------------------------

   ParticipationFramework.java
     - Keeps track of participation state for all participants in a scene

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
package beat.nvbgenerators;

import java.util.*;

/** Keeps track of participation state for a group of participants.  Takes in a list of
    names and initially puts them all in a <code>HEARER</code> state.  A <code>SPEAKER</code>
    can be explicitly selected.  If no <code>ADDRESSEE</code> is explicitly chosen at the
    same time, the ParticipationFramework will set the state of the last speaker as <code>ADDRESSEE</code>.

    @author Hannes
    @version 1.0, 02/21/2002
 */
public class ParticipationFramework {

    Hashtable mParticipants = new Hashtable();

    Participant mSpeaker = null;
    Participant mAddressee = null;

    public static final int HEARER = 0;
    public static final int ADDRESSEE = 1;
    public static final int SPEAKER = 2;

    /** Represents each participant */
    public class Participant {
	String mName;
	int mState;
	public Participant(String name) { mName=name; mState=HEARER; }
	public Participant(String name, int state) { mName=name; mState=state; }
	public String getName() { return mName; }
	public int getState() { return mState; }
	public Participant setState(int state) { mState=state; return this; }
	public String toString() { return "<< "+mName+" ["+mState+"] >>"; }
    };

    public ParticipationFramework() {
    }

    /** Expects a vector of participant names as Strings */
    public ParticipationFramework(Vector participants) {
	for(int i=0;i<participants.size();i++)
	    mParticipants.put((String)participants.elementAt(i),new Participant((String)participants.elementAt(i)));
    }
    
    /** Expects a String array of participant names */
    public ParticipationFramework(String[] participants) {
	for(int i=0;i<participants.length;i++)
	    mParticipants.put(participants[i],new Participant(participants[i]));
    }

    /** Expects the name of the current speaker and the name of the person being addressed */
    public void setSpeakerAddressing(String speaker, String addressee) {
	if(mSpeaker!=null)
	    mSpeaker.setState(HEARER);
	if(mAddressee!=null)
	    mAddressee.setState(HEARER);
	Participant newspeaker = (Participant)mParticipants.get(speaker);
	if(newspeaker!=null)
	    mSpeaker = newspeaker.setState(SPEAKER);
	Participant newaddressee = (Participant)mParticipants.get(addressee);
	if(newaddressee!=null)
	    mAddressee = newaddressee.setState(ADDRESSEE);
    }

    /** Expects the name of the current speaker without explicitly choosing an addressee (that's left up to this class) */
    public void setSpeaker(String speaker) {
	if(mAddressee!=null) 
	    mAddressee.setState(HEARER);	
	if(mSpeaker!=null) {
	    mSpeaker.setState(ADDRESSEE);
	    mAddressee = mSpeaker;
	} else mAddressee = null;

	if(speaker!=null) { 
	    Participant newspeaker = (Participant)mParticipants.get(speaker);
	    if(newspeaker!=null)
		mSpeaker = newspeaker.setState(SPEAKER);
	} else
	    mSpeaker=null;
    }

    /** Expects the name of a new participant */
    public void addParticipant(String participant) {
	mParticipants.put(participant, new Participant(participant));
    }

    /** Expects the name of a participant to be removed from the current framework */
    public void removeParticipant(String participant) {
	Participant removed = (Participant)mParticipants.get(participant);
	if(mSpeaker==removed) mSpeaker = null;
	if(mAddressee==removed) mAddressee = null;
	mParticipants.remove(participant);
    }

    /** Returns the name of the current speaker */
    public String getSpeaker() {
	if(mSpeaker!=null)
	    return mSpeaker.getName();
	else
	    return "";
    }
    
    /** Returns the name of the current addressee */
    public String getAddressee() {
	if(mAddressee!=null)
	    return mAddressee.getName();
	else
	    return "";
    }

    /** Returns a vector of the names of all hearers as strings */
    public Vector getHearers() {
	Vector hearers = new Vector();
	Enumeration e;
	Participant p;
	for(e=mParticipants.elements();e.hasMoreElements();) {
	    p = (Participant)e.nextElement();
	    if(p!=mSpeaker) hearers.add(p.getName());	    
	}
	return hearers;
    }
    
    /** Returns a string representation of the entire framework */
    public String toString() {
	return mParticipants.toString();
    }

    /* Test stub */
    public static void main(String[] a) {
	ParticipationFramework pf = new ParticipationFramework(new String[]{"NED1","PETER1","OLAF1"});
	System.out.println(pf);
	pf.setSpeakerAddressing("NED1","PETER1");
	System.out.println(pf);
	pf.setSpeaker("PETER1");
	System.out.println(pf);
	pf.setSpeaker("NED1");
	System.out.println(pf);
	pf.setSpeaker("PETER1");
	System.out.println(pf);
	pf.setSpeaker("OLAF1");
	System.out.println(pf);
	pf.setSpeaker("NED1");
	System.out.println(pf);
	pf.setSpeakerAddressing("PETER1","OLAF1");
	System.out.println(pf);

    }


}
