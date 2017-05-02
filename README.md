# MUF-inMind-April2017
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

-	Open the file edu.cmu.inmind.multiuser.sara.examples.Ex15_WholePipeline and make sure that only these lines are uncommented.
.addPlugin(NLUComponent.class, SaraCons.ID_NLU)
.addPlugin(TaskReasonerComponent.class, SaraCons.ID_DM)
.addPlugin(SocialReasonerComponent.class, SaraCons.ID_SR)
.addPlugin(NLGComponent.class, SaraCons.ID_NLG)
-	Run the main class edu.cmu.inmind.multiuser.sara.examples.Ex15_WholePipeline.
-	You should see the following message in the console, meaning that the server is running and waiting for clients to connect:

Type "shutdown" to stop:
2017-05-01 17:06:36,663 INFO    SessionManager                 - Starting Multiuser framework...



    
