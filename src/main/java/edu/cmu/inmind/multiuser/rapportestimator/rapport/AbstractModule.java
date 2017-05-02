/**
 * Copyright (C) Carnegie Mellon University - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * This is proprietary and confidential.
 * Written by members of the ArticuLab, directed by Justine Cassell, 2014.
 * 
 * @author Yoichi Matsuyama <yoichim@cs.cmu.edu>
 * 
 */
package edu.cmu.inmind.multiuser.rapportestimator.rapport;

public interface AbstractModule {
	public void setAgentId(int id);
	public int getAgentId();
	public void initializeVHMsg();
}
