package edu.cmu.inmind.multiuser.sara.component.nlg;

import edu.cmu.inmind.multiuser.common.model.SROutput;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by yoichimatsuyama on 4/12/17.
 *
 * The structure and functionality is as follows:
 * - given a task intent and conversational strategy, we select a sentence from the DB and fill in all templates in that sentence.
 * - if there is no specific conv. strategy for this intent, we select the NONE sentence, and if that does not exist, we back off to any strategy.
 * - if the intent does not exist, we fail.
 * -
 */
public class SentenceGeneratorTemplate implements SentenceGenerator {

    // the outer map maps from task intent to map of social intent to a list of candidate sentence templates to be filled in
    private final Map<String,Map<String, List<Template>>> templateMap = new HashMap<>();
    // maps from entity to list of SARA's preferences for that entity
    private final Map<String,ArrayList<String>> preferencesMap = new HashMap<>();
    private static final String ANY_STRATEGY = "ANY";

    /** sentence DB to be loaded by default */
    public SentenceGeneratorTemplate() throws FileNotFoundException {
        this("resources/nlg/sentence_db.tsv", "resources/nlg/sara_preferences_db.tsv");
    }

    public SentenceGeneratorTemplate(String sentenceDBName, String saraDBName) throws FileNotFoundException {
        this(new FileInputStream(sentenceDBName), new FileInputStream(saraDBName));
    }

    public SentenceGeneratorTemplate(InputStream sentenceDB, InputStream saraDB) {
        assert sentenceDB != null;
        loadSentenceList(sentenceDB);
System.out.println("load SARA's preferences");
        loadSARAPreferences(saraDB);
    }

    /** generate the final NLG output string for this task intent and social strategy */
    @Override
    public String generate(SROutput srOutput){
        List<WeightedCandidate> candidates = selectCandidates(srOutput);
        String sentence = selectByWeight(candidates);
        return sentence;
    }

    /* select candidates from the DB based on the task intent and conversational strategy */
    public List<WeightedCandidate> selectCandidates(SROutput srOutput) {
        String intent = srOutput.getAction();
        // srOutput strategy is formatted as [STRATEGY]_CS_SYSTEM; we only want [STRATEGY]
        String strategy = srOutput.getStrategy().split("_")[0];
        Map<String, List<Template>> intentMap = templateMap.get(intent);
        if (intentMap == null)
            throw new IllegalArgumentException("you're querying an unknown intent: " + intent);
        // get the sentence candidates that match the intent and strategy
        List<WeightedCandidate> candidates = filterFillable(intentMap.get(strategy), srOutput, preferencesMap);
        // if no strategy is known, try "NONE" as strategy
        if (candidates == null || candidates.isEmpty()) {
            candidates = filterFillable(intentMap.get("NONE"), srOutput, preferencesMap);
        }
        // if none does not exist either, use all available social strategies
        if (candidates == null || candidates.isEmpty()) {
            candidates = filterFillable(intentMap.get(ANY_STRATEGY), srOutput, preferencesMap);
        }
        assert candidates != null && !candidates.isEmpty() : "could not find any intent for " + intent + "+" + strategy;
        return candidates;
    }

    public static List<WeightedCandidate> filterFillable(List<Template> candidates, SROutput srOutput, Map<String,ArrayList<String>> preferencesMap) {
        List<WeightedCandidate> filteredCandidates = new ArrayList<>();
        if (candidates != null) {
            for (Template template : candidates) {
                WeightedCandidate match = template.match(srOutput, preferencesMap);
                if (match != null) {
                    // add to head if has greater weight (else add to tail)
                    if (filteredCandidates.size() > 0 && match.weight >= filteredCandidates.get(0).weight) {
                        filteredCandidates.add(0,match);
                    } else {
                        filteredCandidates.add(match);
                    }
                }
            }
        }
        return filteredCandidates;
    }

    public static String selectByWeight(List<WeightedCandidate> candidates) {
        assert candidates.size() > 0 : "filtering ended without any options.";
        // FIXME: to be implemented
        // temporarily implemented in filterFillable (when adding candidates to list)
        return candidates.get(0).sentence;
    }

    /*
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
        return out;
    }
    */

    private static class WeightedCandidate {
        String sentence;
        double weight;
        WeightedCandidate(String sentence, double weight) {
            this.sentence = sentence;
            this.weight = weight;
        }
    }


    /** a template that can return a string with values filled in from a frame/explanation/... */
    private static class Template {
        private final static Pattern slotMarker = Pattern.compile(".*?(#[^ \\.,\\?!]*).*");

        final String template;
        final double weight;
        /* Template(String template) {
            this.template = template;
        } */
        Template(String template, double weight) {
            this.template = template;
            this.weight = weight;
        }

        WeightedCandidate match(SROutput srOutput, Map<String,ArrayList<String>> preferencesMap) {
            String instantiation = template;
            Matcher m = slotMarker.matcher(instantiation);
            while (m.matches()) {
                String slotName = m.group(1);
                String value = null, latestEntityValue = null, latestEntityType = null, latestUtterance = null;
                try {
                    switch (slotName) {
                        case "#title":
                            value = srOutput.getRecommendation().getRexplanations().get(0).getRecommendation();
                            break;
                        case "#reason":
                            value = srOutput.getRecommendation().getRexplanations().get(0).getExplanations().get(0);
                            break;
                        case "#previousMovie":
                            // Gets the latest movie that the user liked. Supposedly used to change greeting phase.
                            int size = srOutput.getUserFrame().getFrame().getMovies().getLike().size();
                            value = srOutput.getUserFrame().getFrame().getMovies().getLike().get(size-1);
                            break;
                        case "#actor":
                            value = srOutput.getUserFrame().getFrame().getActors().getLike().get(0).getValue();
                            break;
                        case "#dislikedActor":
                            value = srOutput.getUserFrame().getFrame().getActors().getDislike().get(0).getValue();
                            break;
                        case "#genre":
                            value = srOutput.getUserFrame().getFrame().getGenres().getLike().get(0).getValue();
                            break;
                        case "#dislikedGenre":
                            value = srOutput.getUserFrame().getFrame().getGenres().getDislike().get(0).getValue();
                            break;
                        case "#director":
                            value = srOutput.getUserFrame().getFrame().getDirectors().getLike().get(0).getValue();
                            break;
                        case "#dislikedDirector":
                            value = srOutput.getUserFrame().getFrame().getDirectors().getDislike().get(0).getValue();
                            break;
                        case "#latest":
                            latestUtterance = srOutput.getUserFrame().getLatestUtterance();
                            latestEntityValue = getLatestEntityValue(srOutput); // latest entity
                            // If entity is from user's last utterance
                            if (latestUtterance.contains(latestEntityValue)) {
                                value = latestEntityValue;
                            }
                            break;
                        case "#agree":
                            latestUtterance = srOutput.getUserFrame().getLatestUtterance();
                            latestEntityValue = getLatestEntityValue(srOutput); // latest entity value
                            latestEntityType = getLatestEntityType(srOutput);
                            // If SARA shares the same preference
                            if (preferencesMap.get(latestEntityType).contains(latestEntityValue)) {
                                value = latestEntityValue;
                            }
                            break;
                        case "#disagree":
                            latestUtterance = srOutput.getUserFrame().getLatestUtterance();
                            latestEntityValue = getLatestEntityValue(srOutput); // latest entity value
                            latestEntityType = getLatestEntityType(srOutput);
                            // If SARA have opposite preference
                            if (!preferencesMap.get(latestEntityType).contains(latestEntityValue)) {
                                value = latestEntityValue;
                            }
                            break;
                        case "#different":
                            latestUtterance = srOutput.getUserFrame().getLatestUtterance();
                            latestEntityValue = getLatestEntityValue(srOutput); // latest entity value
                            latestEntityType = getLatestEntityType(srOutput);
                            // If SARA has other preference
                            if (!preferencesMap.get(latestEntityType).contains(latestEntityValue)) {
                                value = preferencesMap.get(latestEntityValue).get(0);
                            }
                            break;
                        default:
                            throw new RuntimeException("SentenceGeneratorTemplate.Template#match() found a marker that I do not understand: " + slotName);
                    }
                } catch (NullPointerException | IndexOutOfBoundsException npe) {
                    // null pointer exceptions occur when we're unable to access the field
                    // it's much simpler to catch them than to handle them individually for all cases
                    value = null;
                }
                if (value == null) {
                    instantiation = null;
                    break;
                }
                instantiation = instantiation.replaceAll(slotName, value);
                m = slotMarker.matcher(instantiation);
            }
            // return instantiation;
            return (instantiation == null) ? null : new WeightedCandidate(instantiation, weight);
        }
    }

    /**
     * Uses latest intent to get latest entity value.
     */
    private static String getLatestEntityValue(SROutput srOutput) {
        int latestEntityIndex = 0;
        try {
            switch(srOutput.getAction()) {
                case "ask_directors": // refer to genre
                    latestEntityIndex = srOutput.getUserFrame().getFrame().getGenres().getLike().size() - 1;
                    return srOutput.getUserFrame().getFrame().getGenres().getLike().get(latestEntityIndex).getValue();
                case "ask_actors": // refer to director
                    latestEntityIndex = srOutput.getUserFrame().getFrame().getDirectors().getLike().size() - 1;
                    return srOutput.getUserFrame().getFrame().getDirectors().getLike().get(latestEntityIndex).getValue();
                case "recommend": // refer to actor
                    latestEntityIndex = srOutput.getUserFrame().getFrame().getActors().getLike().size() - 1;
                    return srOutput.getUserFrame().getFrame().getActors().getLike().get(latestEntityIndex).getValue();
                default:
                    return null;
            }
        } catch (NullPointerException npe) {
            throw new NullPointerException();
        }
    }

    /**
     * Uses latest intent to get latest entity type.
     */
    private static String getLatestEntityType(SROutput srOutput) {
        int latestEntityIndex = 0;
        try {
            switch(srOutput.getAction()) {
                case "ask_directors": // refer to genre
                    return "genre";
                case "ask_actors": // refer to director
                    return "director";
                case "recommend": // refer to actor
                    return "actor";
                default:
                    return null;
            }
        } catch (NullPointerException npe) {
            throw new NullPointerException();
        }
    }



    /**
     * Created by yoichimatsuyama on 4/12/17.
     */
    private static class Sentence {
        String intent;
        String strategy;
        String sentence;
        double weight;

        /**
         * turn a line formatted in the following way into a sentence object:
         * - empty lines are OK, lines starting with a # character are OK (both are disregarded)
         * - tab-separated fields: phase \t intent \t conversational strategy \t weight (as a double) \t sentence (possibly with attribute markers)
         * @return the Sentence object or NULL if comment
         * @throws IllegalArgumentException for lines that are neither comments nor properly formatted sentenceDB entries
         */
        public static Sentence fromLine(String line) {
            Sentence sentence = null;
            if(!line.startsWith("#") && !line.trim().isEmpty()){
                String[] tokens = line.split("\t");
                if(tokens.length > 3){
                    // assumes that weight (token[3]) is a double
                    sentence = new Sentence(tokens[1], tokens[2], Double.parseDouble(tokens[3]), tokens[4]);
                } else {
                    throw new IllegalArgumentException("line was not a comment and did not have the right format: " + line);
                }
            }
            return sentence;
        }

        Sentence(String intent, String strategy, double weight, String sentence) {
            this.intent = intent;
            this.strategy = strategy;
            this.weight = weight;
            this.sentence = sentence.trim();
        }

        String getIntent() { return intent; }
        String getStrategy() { return strategy; }
        String getSentence() { return sentence; }
        double getWeight() { return weight; }
        Template getTemplate() {return new Template(sentence, weight);}
    }

    /**
     * load sentence database
     */
    private void loadSentenceList(InputStream sentenceDB){
        templateMap.clear();
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(sentenceDB));
            String line = null;
            while((line = in.readLine()) != null) {
                Sentence sentence = Sentence.fromLine(line);
                if (sentence != null) {
                    String intent = sentence.getIntent();
                    String strategy = sentence.getStrategy();
                    // create the map objects as needed
                    templateMap.putIfAbsent(intent, new HashMap<>());
                    templateMap.get(intent).putIfAbsent(strategy, new ArrayList<>());
                    templateMap.get(intent).get(strategy).add(sentence.getTemplate());
                    // also store in "ANY" bucket for easy access when we need them
                    templateMap.get(intent).putIfAbsent(ANY_STRATEGY, new ArrayList<>());
                    templateMap.get(intent).get(ANY_STRATEGY).add(sentence.getTemplate());
                }
            }
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * load SARA's preferences
     */
    private void loadSARAPreferences(InputStream saraDB) {
        preferencesMap.clear();
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(saraDB));
            String line = null;
            while((line = in.readLine()) != null && !line.startsWith("#") && !line.trim().isEmpty()) {
                String[] tokens = line.split("\t");
                if (tokens.length == 2) {
                    String entity = tokens[0], preference = tokens[1];
                    // create map objects if entity doesn't already exist
                    preferencesMap.putIfAbsent(entity, new ArrayList<>());
                    preferencesMap.get(entity).add(preference);
                }
            }
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } // end try
    }
}
