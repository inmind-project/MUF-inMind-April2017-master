package beat.main;
/* -------------------------------------------------------------------------

   FrameExecutive.java

     - A simple GUI application that demonstrates the BEAT pipeline

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
   -------------------------------------------------------------------------*/
import beat.nvbgenerators.*;
import beat.Config;
import beat.filter.ContrastBeatFilter;
import beat.filter.NVBConflictFilter;
import beat.filter.NVBFilterModule;
import beat.kb.KnowledgeBase;
import beat.languageProcessing.LanguageModule;
import beat.scheduler.SchedulerModule;
import beat.speechTiming.FixedTimingSource;
import beat.compiler.GanttChartCompiler;
import beat.compiler.PantomimeCompiler;
import beat.filter.ActionBeatFilter;
import beat.speechTiming.TimingSource;
import beat.utilities.BeatModuleTracer;
import beat.utilities.FlattenTreeModule;
import beat.utilities.ModuleTags;
import beat.utilities.NVBTypes;
import beat.utilities.PruneTagModule;
import beat.utilities.XMLWrapper;

import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

/** 
  A sample application that constructs a simpel GUI where a user can type an
  utterance into an edit field and send it through BEAT by pressing a button.

  Separate windows show the state of the XML after passing through the various 
  BEAT Modules.  At the last stage the application tries to send an animation
  script to the Pantomime Animator (not finding Pantomime won't break the
  system).

  "SAY" Button   - processes the utterance in the input box
  "RESET" Button - reset the discourse model
  "CLEAR" Button - clear content of all output windows and the input edit field

  "SCENE", "SPEAKER", "HEARER" Fields - set these attributes in the input 
                                        UTTERANCE tag

    <h3>Change log:</h3>
    <table border="1">
    <tr><th>Date<th>Who<th>What</tr>
    <tr><td>7/17/01<td>T. Bickmore<td> Created. </tr>
    <tr><td>1/12/02<td>T. Bickmore<td> Added posture shift. </tr>
    </table>
**/
public class FrameExecutive extends Frame {
    
    // Layout parameters
    public final int TEXTAREA_ROWS = 10;
    public final int TEXTAREA_COLUMNS = 80;

    // Interface elements
    Label mInputLabel = new Label("Input");
    Label mSpeakerLabel = new Label("Speaker");
    Label mListenerLabel = new Label("Hearer");
    Label mSceneLabel = new Label("Scene");
    Label mLanguageModuleLabel = new Label("Language Tagging Output");
    Label mSchedulerModuleLabel = new Label("Behavior Generation Output");
    Label mEventsModuleLabel = new Label("Scheduling Output");
    TextField mInputField = new TextField();
    TextField mSpeakerField = new TextField("AGENT");
    TextField mListenerField = new TextField("USER");
    TextField mSceneField = new TextField("MIT");
    Button mExecuteButton = new Button();
    Button mResetButton = new Button();
    Button mClearButton = new Button();
    Button mmmmGanttButton = new Button("Gantt");
    TextArea mLanguageModuleArea = new TextArea("",TEXTAREA_ROWS, TEXTAREA_COLUMNS);
    TextArea mSchedulerModuleArea = new TextArea("",TEXTAREA_ROWS, TEXTAREA_COLUMNS);
    TextArea mPantomimeModuleArea = new TextArea("",TEXTAREA_ROWS, TEXTAREA_COLUMNS);
    TextArea mEventsModuleArea = new TextArea("",TEXTAREA_ROWS, TEXTAREA_COLUMNS);
    
    /** 
	A Beat Module Tracer that prints the xml tree inside a given TextArea
    */
    private class TextAreaTracer implements BeatModuleTracer {
	private TextArea mOutputArea;
	public TextAreaTracer(TextArea outputarea) { mOutputArea = outputarea; }
	public void trace(String xml) {
	    try { 
		mOutputArea.setText((new XMLWrapper(xml)).pprint2String());
	    }catch(Exception efoo){
		System.out.println("Error in TextAreaTracer!\n"+efoo.getMessage());
	    };
	}
    }
    
    // Beat paramters
//    public static final String PANTOMIME_HOST = "quaffle";
//    public static final int    PANTOMIME_PORT = 5678;
    
//    public static final String FESTIVALTIMESOURCE_HOST = "aragog";
//    public static final int    FESTIVALTIMESOURCE_PORT = 8022;  //BEAT=8022 REA=8012
    
//    public static final String POSTAGGER_HOST = "aragog";
//    public static final int    POSTAGGER_PORT = 11111; //9090;
    
    public static final String XMLDIR="XMLData/";
    
    // Beat elements
    PantomimeCompiler compiler;
    SchedulerModule scheduler;
    NVBFilterModule filter;
    PruneTagModule filterLinguisticTags;
    NVBGeneratorModule generator;
    LanguageModule tagger;
    GanttChartCompiler mmmmGanttCompiler;
    
    public FrameExecutive() {
	setLayout(null);
	setBackground(java.awt.Color.white);
	setLocation(0,0);
	setSize(1024,748);
	setVisible(false);
	
	int cpy=46;
	
				// Control panel
	mInputField.setBounds(20,cpy,390,20);
	add(mInputField);
	mInputLabel.setBounds(20,cpy-18,390,20);
	add(mInputLabel);
	
	mExecuteButton.setBounds(420,cpy,80,20);
	mExecuteButton.setLabel("Say!");
	SymAction lSymAction = new SymAction();
	mExecuteButton.addActionListener(lSymAction);
	add(mExecuteButton);
	
	mResetButton.setBounds(520,cpy,80,20);
	mResetButton.setLabel("Reset");
	mResetButton.addActionListener(lSymAction);
	add(mResetButton);	
	
	mClearButton.setBounds(620,cpy,80,20);
	mClearButton.setLabel("Clear");
	mClearButton.addActionListener(lSymAction);
	add(mClearButton);	
	
	mmmmGanttButton.setBounds(880,715,80,20);
	mmmmGanttButton.addActionListener(lSymAction);
	add(mmmmGanttButton);	

	mSceneField.setBounds(720,cpy,80,20);
	add(mSceneField);
	mSceneLabel.setBounds(720,cpy-18,80,20);
	add(mSceneLabel);
	
	mSpeakerField.setBounds(820,cpy,80,20);
	add(mSpeakerField);
	mSpeakerLabel.setBounds(820,cpy-18,80,20);
	add(mSpeakerLabel);
	
	mListenerField.setBounds(920,cpy,80,20);
	add(mListenerField);
	mListenerLabel.setBounds(920,cpy-18,80,20);
	add(mListenerLabel);
	
				// Output Areas
	mLanguageModuleArea.setBounds(20,100,390,280);
	add(mLanguageModuleArea);
	mLanguageModuleLabel.setBounds(20,82,390,20);
	add(mLanguageModuleLabel);
	
	mSchedulerModuleArea.setBounds(420,100,580,280);
	add(mSchedulerModuleArea);
	mSchedulerModuleLabel.setBounds(420,82,580,20);
	add(mSchedulerModuleLabel);
	
	mEventsModuleArea.setBounds(20,410,980,300);
	add(mEventsModuleArea);
	mEventsModuleLabel.setBounds(20,392,980,20);
	add(mEventsModuleLabel);
	
	setTitle("BEAT Frame Tester");
				
	SymWindow aSymWindow = new SymWindow();
	this.addWindowListener(aSymWindow);
	
	try {
	    
	    String myHost = (InetAddress.getLocalHost()).getHostName();
	    System.out.print("BEAT is starting up on "+myHost+"...");
	    
	    //Load DBs:
	    NVBTypes nvbTypes=new NVBTypes(new File(XMLDIR+"NVBTypes.xml"));
	    ModuleTags moduleTags=new ModuleTags(new File(XMLDIR+"ModuleTags.xml"));
	    KnowledgeBase KB = new KnowledgeBase(XMLDIR+"database.xml");
	    
	    System.out.println("--- Building BEAT pipeline ---");
	    
	    //Build compiler...
	    System.out.println("Making PantomimeCompiler...");
	    //compiler=new PantomimeCompiler(PANTOMIME_HOST,PANTOMIME_PORT);
//	    compiler.setDEBUG(true);
	    
	    
	//    ArmManagementModule armm=new ArmManagementModule(compiler);
//	    armm.setOutputTracer(new TextAreaTracer(mEventsModuleArea));
//	    armm.setDEBUG(true);
	    FlattenTreeModule treeFlattener=new FlattenTreeModule(nvbTypes,null);
	   // FlattenTreeModule treeFlattener=new FlattenTreeModule(nvbTypes,armm);
		treeFlattener.setOutputTracer(new TextAreaTracer(mEventsModuleArea));
	    
	    //Build scheduler...
	    System.out.println("Making Scheduler...");
	    
	    TimingSource fixed=new FixedTimingSource(nvbTypes,2.0);
		SchedulerModule scheduler=new SchedulerModule(nvbTypes,fixed,treeFlattener);
//	    TimingSource festival=new FestivalTimingSource(myHost,8030,FESTIVALTIMESOURCE_HOST,FESTIVALTIMESOURCE_PORT); 
//	    scheduler=new SchedulerModule(nvbTypes,festival,treeFlattener);
	    scheduler.setInputTracer(new TextAreaTracer(mSchedulerModuleArea));
	    scheduler.setDEBUG(Config.logging);
	    
	    mmmmGanttCompiler=new GanttChartCompiler();
	    ForkModule fork1=new ForkModule(mmmmGanttCompiler,scheduler);
						
	    PruneTagModule filterLinguisticTags=new PruneTagModule(moduleTags.getModuleTags("Tagger"),fork1);

	    //Build filters...
	    System.out.println("Making NVBFilter...");
	    filter=new NVBFilterModule(nvbTypes,filterLinguisticTags);
	    //filter.setDEBUG(true);
	    //filter.register(new NVBPriorityFilter(1));
	    filter.register(new NVBConflictFilter());
	    filter.register(new ActionBeatFilter());
	    filter.register(new ContrastBeatFilter());
						
	    //Build generator...
	    System.out.println("Making NVBGenerator...");
	    generator=new NVBGeneratorModule(nvbTypes,KB,filter);
	    generator.setDEBUG(Config.logging);
	    generator.register(new GazeGenerator());
	    generator.register(new ContrastGestureGenerator());
	    generator.register(new IconicGestureGenerator());
	    generator.register(new DeicticGestureGenerator());
	    generator.register(new MonologuePostureShiftGenerator());
	    //Not used: generator.register(new PostureShiftBreakGenerator());
	    generator.register(new NVBXSLGenerator(new File(XMLDIR+"BeatGenerator.xsl")));
	    generator.register(new NVBXSLGenerator(new File(XMLDIR+"EyebrowsGenerator.xsl")));
	    generator.register(new NVBXSLGenerator(new File(XMLDIR+"IntonationGenerator.xsl")));
	    generator.register(new NVBXSLGenerator(new File(XMLDIR+"MetaGenerator.xsl")));
	    generator.register(new NVBXSLGenerator(new File(XMLDIR+"HeadnodGenerator.xsl")));
	    generator.register(new NVBXSLGenerator(new File(XMLDIR+"PunctuationGenerator.xsl")));
	    
	    //Build tagger...
	    System.out.println("Making Language Tagger...");
	 //   POSTagger postagger = new ConexorPOSTaggerServer(POSTAGGER_HOST,POSTAGGER_PORT); // Modified by G. Poncin, to use Conexor POSTagger as a server... JDK 1.3+ Required !
	  tagger=new LanguageModule(KB,null,generator);
	 //   tagger=new LanguageModule(KB,postagger,generator);
	    tagger.setOutputTracer(new TextAreaTracer(mLanguageModuleArea));
	    tagger.setDEBUG(Config.logging);
	    System.out.println("Pipeline built!");
	    
	} catch(Exception e) {
	    System.err.println("Failed to set up processing pipeline:\n"+e.getMessage());
	}
	
    }
    
    /**
       Prepares the input and sends it off into the pipeline */
    public void process(String input) {
	try {
	    //if(input.indexOf('\'')>=0)
	    //	throw new Exception("BEAT does not handle apostrophes.");
	    tagger.process(XMLWrapper.parseXML("<UTTERANCE SCENE=\""+mSceneField.getText()+"\" SPEAKER=\""+mSpeakerField.getText()+"\" HEARER=\""+mListenerField.getText()+"\">"+input+"</UTTERANCE>"));
	} catch(Exception e) {
	    System.err.println("Failed to process utterance...\n"+e.getMessage());
	    JOptionPane.showMessageDialog(this,e.getMessage(),"BEAT Error",JOptionPane.INFORMATION_MESSAGE);
	};
    }
    

    /**
       All button presses handled here */
    class SymAction implements ActionListener {
	public void actionPerformed(ActionEvent event) {
	    Object object = event.getSource();
	    if (object == mExecuteButton) {
		process(mInputField.getText());
	    } else if(object==mResetButton) {
		tagger.reset();
		mLanguageModuleArea.setText("");
		mSchedulerModuleArea.setText("");
		mEventsModuleArea.setText("");
	    } else if(object==mClearButton) {
		mLanguageModuleArea.setText("");
		mSchedulerModuleArea.setText("");
		mEventsModuleArea.setText("");
		mInputField.setText("");
	    } else if(object==mmmmGanttButton) {
	      mmmmGanttCompiler.show();
	    };
	}
    }
    
    class SymWindow extends java.awt.event.WindowAdapter {
	public void windowClosing(java.awt.event.WindowEvent event) {
	    Object object = event.getSource();
	    if (object == FrameExecutive.this)
		System.exit(0);
	}
    }
    
    
    public void setVisible(boolean b) {
	if(b) {
	    setLocation(50, 50);
	}	
	super.setVisible(b);
    }
    
    
    public static void main(String args[]) {
	
	try {
	    (new FrameExecutive()).setVisible(true);
	} catch (Throwable t) {
	    System.err.println(t);
	    t.printStackTrace();
	    //Ensure the application exits with an error condition.
	    System.exit(1);
	}
    }
    
    
}


