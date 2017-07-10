/* -------------------------------------------------------------------------

   ParticipationFrameworkBase
     - Holds a collection of participation frameworks of different scenes

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

import beat.kb.KnowledgeBase;

/** Extracts participation frameworks from <code>SCENE</code> elements as they are stored
    in the KnowledgeBase.  Allows those frameworks to be retreived by <code>SCENE ID</code> 
    
    @author Hannes
    @version 1.0, 02/21/2002
 */
public class ParticipationFrameworkBase {

    private Hashtable mSceneList = new Hashtable();

    /** Expects an enumeration of KnowledgeBase Scene objects as returned by the 
	<code>KnowledgeBase.getAllScenes()</code> */
    public ParticipationFrameworkBase(Enumeration scenes) {
	KnowledgeBase.Scene scene;
	while(scenes.hasMoreElements()) {
	    scene = (KnowledgeBase.Scene)scenes.nextElement();
	    if(scene!=null) {
		mSceneList.put(scene.getID(),new ParticipationFramework(scene.getParticipants()));
	    }
	}
    }

    /** Returns a participation framework by the passed <code>SCENE ID</code> */
    public ParticipationFramework getParticipationFramework(String sceneid) {
	return (ParticipationFramework)mSceneList.get(sceneid);
    }

    /** Returns a string representation of the entire ParticipationFrameworkBase */
    public String toString() {
	return mSceneList.toString();
    }

}
