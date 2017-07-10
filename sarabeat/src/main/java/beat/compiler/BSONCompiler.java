package beat.compiler;

import java.io.*;
import java.lang.*;
import java.util.*;

import beat.BEAT;
import beat.bson.Viseme;
import org.w3c.dom.*;

import com.google.gson.Gson;

import beat.Config;
import beat.bson.BSON;
import beat.bson.Behavior;
import beat.bson.Word;

public class BSONCompiler extends Compiler {
	private BEAT beat;

	private boolean useTTS = false;
	private String ssml;
	private String plainText;
	private List<SentenceFrame> sentenceFrameList;
	private boolean cancelGaze;
	private HashMap<String,String[]> BML_database;
	private List<String> beatGestureList;
	//
	private  double nodAmount = 0.1;
	private int nodRepeat = 1;
	private float eyeBrowAmount = 5.0f;
	private int gazeAngle = 0;
	private int behaviorTimeShift = 12;
	//
	private String voice = "Salli";
	private String mp3Path = "output/speech.mp3";
	private String speechMarkPath = "output/speech.mark";

	public BSONCompiler(BEAT beat) throws Exception {
		super();
		this.beat = beat;
		cancelGaze = false;
		BML_database = new HashMap<String, String[]>();
		beatGestureList = new ArrayList<String>();
		loadBeatGesture();
	}

	public String compile(Document xml) throws Exception {
		Random r = new Random();
		BSON bson = new BSON();
		/**
		 * Set speech
		 */
		String plainText = getPlainText();
		bson.setSpeech(plainText);
		String[] tokens = plainText.replaceAll("\\.", "").split(" ");
		List<Word> words = new ArrayList<Word>();
		List<Behavior> behaviors = new ArrayList<Behavior>();
		//
		int finalId = 0;
		/**
		 * Set words
		 */
		for(int i = 0; i< tokens.length; i++){
			float timeInterval = 0.2f;
			Word word = new Word();
			word.setWord(tokens[i]);
			word.setStart_id(i);
			word.setEnd_id(i+1);
			word.setStart_time(i * timeInterval);
			word.setEnd_time((i+1) * timeInterval);
			words.add(word);
		}
		/**
		 * Behaviors
		 */
		int actionNum = xml.getElementsByTagName("STOP").getLength(); // number of actions
		for(int i = 0; i < actionNum; i++){
			String actionName = xml.getElementsByTagName("START").item(i+1).getAttributes().getNamedItem("ACTION").getNodeValue();
			int startId = Integer.valueOf(xml.getElementsByTagName("START").item(i+1).getAttributes().getNamedItem("WI")
					.getNodeValue());
			int endId = Integer.valueOf(xml.getElementsByTagName("STOP").item(i).getAttributes().getNamedItem("WI")
					.getNodeValue());

			finalId = endId;
			/**
			 * Nod
			 */
			if(actionName.equals("HEADNOD")){
				int n = r.nextInt(2) + 1;
				nodAmount = n * 0.1;
				int strokeId = startId - behaviorTimeShift;
				if(strokeId<0) strokeId = 1;
				Behavior b = new Behavior();
				b.setType("animation");
				b.setName("head_nod_sara");
				b.setAmount(nodAmount);
				b.setStart(strokeId);
				b.setEnd(endId);
				if(xml.getElementsByTagName("START").item(i+1).getAttributes().getNamedItem("PRIORITY")!=null){
					int priority = Integer.valueOf(xml.getElementsByTagName("START").item(i+1)
							.getAttributes().getNamedItem("PRIORITY").getNodeValue());
					b.setPriority(priority);
				}else{
					b.setPriority(1);
				}
				behaviors.add(b);
				/**
				 * Gaze
				 */
			}else if(actionName.equals("GAZE")){
				if(!cancelGaze){
					int ir = r.nextInt(2);
					String target = "GazeUp";
					switch (ir){
						case 0: target = "GazeUp";
						case 1: target = "GazeDown";
							//case 2: target = "GazeRight";
							//case 3: target = "GazeLeft";
					}
					//
					gazeAngle = r.nextInt(3) + 3;
					Behavior b = new Behavior();
					b.setType("gaze");
					b.setStart(startId);
					if(xml.getElementsByTagName("START").item(i+1).getAttributes().getNamedItem("DIRECTION").getNodeValue().equals("AWAY_FROM_HEARER")){
						b.setTarget(target);
						b.setDirection("up");
						b.setInfluence("head");
						b.setAngle(gazeAngle);
						if(xml.getElementsByTagName("START").item(i+1).getAttributes().getNamedItem("PRIORITY")!=null){
							int priority = Integer.valueOf(xml.getElementsByTagName("START").item(i+1)
									.getAttributes().getNamedItem("PRIORITY").getNodeValue());
							b.setPriority(priority);
						}else{
							b.setPriority(1);
						}
						behaviors.add(b);

						Behavior b1 = new Behavior();
						b1.setType("gaze");
						b1.setStart(startId+1);
						b1.setTarget("GazeTarget");
						b1.setDirection("up");
						b1.setInfluence("head");
						b1.setAngle(gazeAngle);
						behaviors.add(b1);

					}else if(xml.getElementsByTagName("START").item(i+1).getAttributes().getNamedItem("DIRECTION").getNodeValue().equals("TOWARDS_HEARER")){
						b.setTarget("GazeTarget");
						b.setDirection("up");
						b.setInfluence("head");
						b.setAngle(0);
						behaviors.add(b);
					}
				}
				/**
				 * Eye brow
				 */
			}else if(actionName.equals("EYEBROWS")){
				/**
				 * Beat gesture
				 */
			}else if(actionName.equals("GESTURE_BOTH") ||
					actionName.equals("GESTURE_RIGHT") ||
					actionName.equals("GESTURE_LEFT")) {
				int strokeId = startId - behaviorTimeShift;
				if(strokeId<0) strokeId = 1;
				String gesture = getBeatGesture();
				Behavior b = new Behavior();
				b.setType("animation");
				b.setStart(strokeId);
				b.setName(gesture);
				if(xml.getElementsByTagName("START").item(i+1).getAttributes().getNamedItem("PRIORITY")!=null){
					int priority = Integer.valueOf(xml.getElementsByTagName("START").item(i+1)
							.getAttributes().getNamedItem("PRIORITY").getNodeValue());
					b.setPriority(priority);
				}else{
					b.setPriority(1);
				}
				behaviors.add(b);
				/**
				 * Intonation
				 */
			}else if(actionName.equals("INTONATION_TONE")){
				//<INTONATION_ACCENT>
				//<INTONATION_TONE>
				//<INTONATION_BREAK>
				//<START ACTION="INTONATION_TONE" AID="A11" ENDTONE="L-H%" SRT="0.0" WI="0">   </START>
				//<STOP ACTION="INTONATION_TONE" AID="A11" ENDTONE="L-H%" SRT="4.0" WI="2">   </STOP>
				//String endtone_symbol = xml.getElementsByTagName("START").item(i+1).getAttributes().getNamedItem
				//		("ENDTONE").getNodeValue();
				//vrSpeakMessage += "<intonation_tone endtone=\"" + endtone_symbol + "\" start=\"sp1:T" + startId +
				//		"\" end=\"sp1:T" + endId + "\"/>\n";

			}else if(actionName.equals("INTONATION_BREAK")){
				//vrSpeakMessage += "<intonation_break start=\"sp1:T" + startId + "\" end=\"sp1:T" + endId + "\"/>\n";
			}
		}
		/**
		 * Gaze in the end of turn
		 */
		Behavior b = new Behavior();
		b.setType("gaze");
		b.setStart(finalId);
		b.setTarget("GazeTarget");
		b.setDirection("up");
		b.setInfluence("head");
		b.setAngle(0);
		behaviors.add(b);
		/**
		 * Smile in the begining
		 */
		Behavior bs = new Behavior();
		bs.setType("facs");
		bs.setStart(0);
		bs.setEnd(1);
		bs.setAu("105");
		bs.setAmount(0.5);
		behaviors.add(bs);
		/**
		 * Set words and behaviors
		 */
		if(!useTTS) bson.setWords(words);
		//Behavior bs = prioritizeBehaviors(behaviors);
		bson.setBehaviors(behaviors);
		/**
		 * BSON
		 */
		Gson gson = new Gson();
		String messageStr = gson.toJson(bson);
		beat.receiveBSON(messageStr);
		/**
		 * Sender
		 */
		if(Config.isConnectVHMsg) {
			//vrSpeakSender.sendMessage(messageStr);
			//vrSSMLSender.sendMessage("0 <s>" + plainText + "</s>");
		}

		return null;
	}

	public List<Behavior> prioritizeBehaviors(List<Behavior> behaviors) {
		List<Behavior> tempBehaviors = new ArrayList<Behavior>();
		tempBehaviors.addAll(behaviors);

		for (Behavior b : behaviors) {
			for (Behavior tb : tempBehaviors) {
				if (b.getType().equals(tb.getType())) {

				}
			}
		}
		return null;
	}

	public void loadBeatGesture(){
		beatGestureList.add("beat_low_right_sara");
		beatGestureList.add("beat_middle_right_sara");
		beatGestureList.add("both_hands_cont");
	}

	public String getSsml() {
		return ssml;
	}

	public void setSsml(String ssml) {
		this.ssml = ssml;
	}

	public void setPlainText(String text) {
		this.plainText = text;
	}

	public String getPlainText() {
		return plainText;
	}

	public String getBeatGesture(){
		Random r = new Random();
		String gesture = "";
		//int rand = r.nextInt(2);
		//if (rand == 0) {
		int id = r.nextInt(beatGestureList.size());
		gesture = beatGestureList.get(id);
		//}
		return gesture;
	}

	public BSON getSpeechMarks(BSON bson){
		ArrayList<String> resultList = new ArrayList<String>();
		File file = new File(speechMarkPath);
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line = "";
			try {
				while ((line = br.readLine()) != null){
					resultList.add(line);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		List<Word> words = new ArrayList<Word>();
		List<Viseme> visemes = new ArrayList<Viseme>();
		List<Viseme> visemesTmp = new ArrayList<Viseme>();
		float wordStartTime = 0.0f;
		float wordEndTime = 0.0f;
		int wordId = 0;
		String wordStr = "";
		for(String line : resultList) {
			String[] markArray = line.split(" ");
			/**
			 * get word list
			 */
			if (markArray[1].equals("word")) {
				Word word = new Word();
				if (visemes.size() > 0) {
					word.setWord(wordStr);
					word.setStart_time(wordStartTime);
					word.setEnd_time(wordEndTime);
					word.setVisemes(visemes);
					word.setStart_id(wordId);
					word.setEnd_id(wordId+1);
					words.add(word);
					//visemes.removeAll(visemes);
					visemes = new ArrayList<Viseme>();
					wordId++;
				}
				wordStartTime = Float.valueOf(markArray[0]);
				wordStr = markArray[4];

			} else if (markArray[1].equals("viseme")) {
				Viseme viseme = new Viseme();
				viseme.setStartTime(Float.valueOf(markArray[0]));
				viseme.setSymbol(markArray[4]);
				wordEndTime = Float.valueOf(markArray[0]) + 0.15f;
				visemes.add(viseme);
			}
		}
		Word word = new Word();
		word.setWord(wordStr);
		word.setVisemes(visemes);
		word.setStart_time(wordStartTime);
		word.setEnd_time(wordEndTime);
		//
		words.add(word);
		/**
		 * make BSON
		 */
		bson.setWords(words);

		return bson;
	}


}