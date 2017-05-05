package edu.cmu.inmind.multiuser.sara.component.nlg;

import edu.cmu.inmind.multiuser.common.Utils;
import edu.cmu.inmind.multiuser.common.model.Recommendation;
import edu.cmu.inmind.multiuser.common.model.Rexplanation;
import edu.cmu.inmind.multiuser.common.model.SROutput;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by yoichimatsuyama on 4/12/17.
 */
public class SentenceGeneratorTemplate implements SentenceGenerator {
    private List<Sentence> sentenceList;
    private List<String> templateUseIntents;
    private final String sentenceDBPath = Utils.getProperty("sentenceDBPath");
    private final String templateUsePath = Utils.getProperty("templateUsePath");

    public SentenceGeneratorTemplate(){
        sentenceList = loadSentenceList();
        templateUseIntents = loadTemplateUseIntent();
    }

    @Override
    public String genenete(SROutput srOutput){
        String sentence = "";
        List<Sentence> sentenceCandidates = new ArrayList<Sentence>();

        for(String tempIntent : templateUseIntents){
            sentenceCandidates = selectTemplate(srOutput);
        }
        //final ranking
        Random r = new Random();
        int rInt = r.nextInt(sentenceCandidates.size());
        sentence = sentenceCandidates.get(rInt).getSentence();
        //ignore special cases
        sentence = Util.ignoreSpecialCases(sentence);
        //fill values
        sentence = fillValues(sentence, srOutput);

        return sentence;
    }

    public List<Sentence> selectTemplate(SROutput intent){
        List<Sentence> sentenceTemplateList = new ArrayList<Sentence>();
        for(Sentence s : sentenceList){
            if(intent.getAction().equals(s.getIntent()) && intent.getStrategy().equals(s.getStrategy())){
                sentenceTemplateList.add(s);
            }
        }

        //select NONE
//        if(sentenceTemplateList.size()==0){
//            for(Sentence s : sentenceList){
//                if(intent.getAction().equals(s.getIntent()) && s.getStrategy().equals("NONE")){
//                    sentenceTemplateList.add(s);
//                }
//            }
//        }

        //select something
        if(sentenceTemplateList.size()==0){
            for(Sentence s : sentenceList){
                if(intent.getAction().equals(s.getIntent())){
                    sentenceTemplateList.add(s);
                }
            }
        }

        return sentenceTemplateList;
    }

    public String fillValues(String sentence, SROutput srOutput){
        String out = sentence;
        if(sentence.contains("#title")) {
            if (srOutput.getRecommendation().getRexplanations().get(0).getRecommendation() != null) {
                String title = srOutput.getRecommendation().getRexplanations().get(0).getRecommendation();
                out = sentence.replaceAll("#title", title);
            }
        } if(sentence.contains("#entity")) {
            if (srOutput.getEntities().size()>0) {
                String entity = srOutput.getEntities().get(0).getValue();
                out = sentence.replaceAll("#entity", entity);
            }
        }
        /*
        }else if(sentence.contains("#genre")){
            if(srOutput.getUserFrame().getFrame().getGenres().getLike()!=null){
                int size = srOutput.getUserFrame().getFrame().getGenres().getLike().size();
                String genre = srOutput.getUserFrame().getFrame().getGenres().getLike().get(size-1);
                out = sentence.replaceAll("#genre", genre);
            }
        }else if(sentence.contains("#actor")){
            if(srOutput.getUserFrame().getFrame().getGenres().getLike()!=null){
                int size = srOutput.getUserFrame().getFrame().getActors().getLike().size();
                String actor = srOutput.getUserFrame().getFrame().getActors().getLike().get(size-1);
                out = sentence.replaceAll("#actor", actor);
            }
        }else if(sentence.contains("#director")){
            if(srOutput.getUserFrame().getFrame().getGenres().getLike()!=null){
                int size = srOutput.getUserFrame().getFrame().getDirectors().getLike().size();
                String director = srOutput.getUserFrame().getFrame().getDirectors().getLike().get(size-1);
                out = sentence.replaceAll("#director", director);
            }
        }*/
        return out;
    }

    /**
     * load sentence
     */
    public List<Sentence> loadSentenceList(){
        List<Sentence> sentenceList = new ArrayList<Sentence>();
        try {
            BufferedReader in = new BufferedReader(new FileReader(sentenceDBPath));
            String line = null;
            while((line = in.readLine()) != null) {
                if(line != ""){
                    String[] tokens = line.split("\t");
                    if(tokens.length > 3){
                        Sentence sentence = new Sentence();
                        sentence.setPhase(tokens[0]);
                        sentence.setIntent(tokens[1]);
                        sentence.setStrategy(tokens[2]);
                        sentence.setSentence(tokens[3]);
                        sentenceList.add(sentence);
                    }
                }
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return sentenceList;
    }

    public List<String> loadTemplateUseIntent(){
        List<String> list = Util.getLines(templateUsePath);
        return list;
    }

    public static void main(String[] args){
        SentenceGeneratorTemplate template = new SentenceGeneratorTemplate();
        template.loadTemplateUseIntent();
        SROutput srOutput = new SROutput();
        srOutput.setAction("recommend");
        srOutput.setStrategy("SD");
        Recommendation rec = new Recommendation();
        Rexplanation rexplanation = new Rexplanation();
        List<Rexplanation> rexplanations = new ArrayList<Rexplanation>();
        rexplanations.add(rexplanation);
        rec.setRexplanations(rexplanations);
        srOutput.setRecommendation(rec);

        System.out.println(template.genenete(srOutput));
    }
}
