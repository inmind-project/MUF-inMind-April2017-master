# MUF-inMind-April2017

## Cloning with Submodules
This repository contains submodules which you need to checkout in addition to the main respository itself, 
and one of the submodules uses large-file-support. 

 * before cloning, please make sure you have installed git large-file-support: https://git-lfs.github.com/

 * to be able to clone the dialogOS submodule, you'll need to create a bitbucket account, and send a request to @tbaumann. You might have to enter your credentials during the cloning process.
 
 * clone by saying ```git clone --recursive git@github.com:fpecune/MUF-inMind-April2017-master.git```
   (or via https, whichever you prefer)

 * or after cloning say the following to populate the submodules:
```bash
git submodule init
git submodule update
```

 * the latest stable branch should always be the master branch. To be sure you are up-to-date and on the correct branch, use ```git status```
 
## Building with Gradle
Once you cloned the main repo and all its submodules (sara-csc, sara-beat, and dialoOS), you'll have to build the project with all its dependencies. 

 * build by saying ```gradlew build -x test```
 
 * for Windows users, you might need to check the path to your jdk in your environment variables so it actually matches your installation folder. This folder should be something like "C:\Program Files\Java\jdk1.8.0_121\bin".
 
 * if there are errors with the parser in DialogOS, compile DialogOS independently once (gradlew.bat in DialogOS directory, then gradlew.bat build from main MUF directory to create the missing classes). For Windows users, copy the XMLData folder from BEAT to main directory. This should replace the XMLData file already existing.
 
## Starting the CSC and NLU
To run the CSC and NLU, you will need to receive a .pem key to access the AWS. For windows users, you will need to transform .pem key into .ppk using PuttyGen using the following tutorial: https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/putty.html.

After transforming the key into .ppk format, open Putty. Under the category pane > Auth, browse for your key that prompts for a "Private key file for authentification". Return to the session pane and press "open".

 * for Windows users, in order to simultaneously run the CSC and NLU through SSH, you will need to use TMux to have two SSH windows running at the same time.
 
 * the CSC exists in the deployment folder. Once you navigate to the CSCPython directory, say ```./run.sh```
 
 * the NLU also exists in the deployment folder. Navigate to dialogue-system and the DialoguePython directory. Once you are in the correct directory, say ```./run.sh```
 
 ## Starting the MUF
Once you cloned the main repo and all its submodules, and built the project succesfully, you should be able to start the server.

 * start the MUF server by saying ```gradlew run```
 
## Contents:

This repository contains:
1.	SARA Commons, a library defining:

o	List of constants used amongst all projects 

o	The different objects used to communicate between components:

2.	The Multi-User Framework version used during the inMind retreat on 28th of April.

o	A list of Orchestrators managing the flow of information amongst the different components integrated in the architecture

o	A list of components:

	Fake NLU
•	Input: Msg_ASR
•	Output: Msg_NLU
	NLU_DM component (used to connect to Yulun`s AWS server)
•	Input: Msg_ASR
•	Output: Msg_DM
	Rapport Estimator (used to connect to Conversational Classifier via VHT)
•	Input: Msg_ASR
•	Output: Msg_RPT
	Fake Task reasoner
•	Input: Msg_NLU
•	Output: Msg_DM
	Social Reasoner
•	Input; Msg_DM, Msg_RPT, Msg_NVB)
•	Output: Msg_SR
	NLG
•	Input; Msg_SR
•	Output: Msg_NLG
	OpenFace
•	Input; Msg_DM, Msg_RPT, Msg_NVB)
•	Output: Msg_SR
	User Model
•	Input; Msg_DM, Msg_RPT, Msg_NVB)
•	Output: Msg_SR

3.	The OpenFace qualifier version used during the retreat and integrated within the MUF.
o	Actually runs OpenFace features extraction executable file.
o	Analyses the features and returns higher level information:
	Whether the user is smiling or not.

4.	The Social Reasoner version used during the retreat and integrated within the MUF.
o	Gets inputs from:
	DM
	OpenFace
	Rapport Estimator
o	Computes conversational strategy and sends it through MUF blackboard.

5.	Rapport estimator client


How to run a minimal configuration:

Ensure the file `connection.properties` exists in the directory in which the application is found, wherein the property `hostname` denotes the host IP address the client should connect to.

-	Open the file `edu.cmu.inmind.multiuser.sara.SaraServerMainClass` and make sure that only these lines are uncommented.
`.addPlugin(NLUComponent.class, SaraCons.ID_NLU)`
`.addPlugin(TaskReasonerComponent.class, SaraCons.ID_DM)`
`.addPlugin(SocialReasonerComponent.class, SaraCons.ID_SR)`
`.addPlugin(NLGComponent.class, SaraCons.ID_NLG)`
-	Run the main class `edu.cmu.inmind.multiuser.sara.SaraServerMainClass`.
-	You should see the following message in the console, meaning that the server is running and waiting for clients to connect:

Type "shutdown" to stop:
2017-05-01 17:06:36,663 INFO    SessionManager                 - Starting Multiuser framework...
