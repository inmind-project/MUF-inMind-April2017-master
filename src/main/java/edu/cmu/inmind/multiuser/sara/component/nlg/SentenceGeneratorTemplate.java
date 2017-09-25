package edu.cmu.inmind.multiuser.sara.component.nlg;
import edu.cmu.inmind.multiuser.common.model.*;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by yoichimatsuyama on 4/12/17.
 *
 * The structure and functionality is as follows:
 * - given a task intent and conversational strategy, we select a sentence (or sentences) from the DB and fill in all templates in it.
 * - if there is no specific conv. strategy for this intent, we select the NONE sentence, and if that does not exist, we back off to any strategy.
 * - if the intent does not exist, we fail.
 */
public class SentenceGeneratorTemplate implements SentenceGenerator {

    // the outer map maps from task intent to map of social intent to a list of candidate sentence templates to be filled in
    private final Map<String,Map<String, List<Template>>> templateMap = new HashMap<>();
    // maps from entity to list of SARA's preferences for that entity
    //private final Map<String,ArrayList<String>> preferencesMap = new HashMap<>();
    private final UserFrame.Frame systemPreferences;
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
        System.out.println("Load SARA's preferences");
        systemPreferences = loadSARAPreferences(saraDB);
    }

    /** generate the final NLG output string for this task intent and social strategy */
    @Override
    public String generate(SROutput srOutput) {
        List<String> sentences = generateAsList(srOutput);
        return String.join("", sentences.stream().map(s -> replacePatterns(s, srOutput)).collect(Collectors.toList()));
    }

    /** generate a list of sentences */
    public List<String> generateAsList(SROutput srOutput) {
        List<WeightedCandidate> candidates = selectCandidates(srOutput);
        assert candidates != null && candidates.size() > 0;
        String allText = selectByWeight(candidates);
        return splitIntoSentences(allText);
    }

    /** split text into a list of sentences */
    private List<String> splitIntoSentences(String allText) {
        return Arrays.asList(allText.split("\\|"));
    }

    /* select candidates from the DB based on the task intent and conversational strategy */
    public List<WeightedCandidate> selectCandidates(SROutput srOutput) {
        String intent = srOutput.getDMOutput().getAction();
        // srOutput strategy is formatted as [STRATEGY]_CS_SYSTEM; we only want [STRATEGY]
        String strategy = srOutput.getStrategy().split("_")[0];
        Map<String, List<Template>> intentMap = templateMap.get(intent);
        if (intentMap == null)
            throw new IllegalArgumentException("you're querying an unknown intent: " + intent);
        // get the sentence candidates that match the intent and strategy
        List<WeightedCandidate> candidates = filterFillable(intentMap.get(strategy), srOutput, systemPreferences);
        // if no strategy is known, try "NONE" as strategy
        if (candidates == null || candidates.isEmpty()) {
            candidates = filterFillable(intentMap.get("NONE"), srOutput, systemPreferences);
        }
        // if none does not exist either, use all available social strategies
        if (candidates == null || candidates.isEmpty()) {
            candidates = filterFillable(intentMap.get(ANY_STRATEGY), srOutput, systemPreferences);
        }
        assert candidates != null && !candidates.isEmpty() : "could not find any intent for " + intent + "+" + strategy;
        return candidates;
    }

    private static List<WeightedCandidate> filterFillable(List<Template> candidates, SROutput srOutput, UserFrame.Frame systemPreferences) {
        List<WeightedCandidate> filteredCandidates = new ArrayList<>();
        if (candidates != null) {
            for (Template template : candidates) {
                WeightedCandidate match = template.match(srOutput, false);
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

    private static String selectByWeight(List<WeightedCandidate> candidates) {
        assert candidates.size() > 0 : "filtering ended without any options.";
        // FIXME: to be implemented
        // temporarily implemented in filterFillable (when adding candidates to list)
        return candidates.get(0).sentence;
    }

    public boolean matchesPattern(String template, SROutput srOutput) {
        return replacePatterns(template, srOutput, false) != null;
    }

    public String replacePatterns(String template, SROutput srOutput) {
        return replacePatterns(template, srOutput, true);
    }

    private static Random r = new Random();
    private String getAnyOf(List<?> list) {
        if (list.isEmpty())
            return null;
        Object selected = list.get(r.nextInt(list.size()));
        if (selected instanceof Entity)
            return ((Entity) selected).getValue();
        else
            return selected.toString();
    }

    /**
     * Check whether the entity detected from the user (actor, director, genre) is in SARA's preferences as a like or dislike
     * @param srOutput
     * @param valence
     * @return
     * @throws IOException
     */
    public boolean containEntity(SROutput srOutput, float valence) throws IOException {
        String latestEntityValue, latestEntityType;
        latestEntityValue = getLatestEntityValue(srOutput); // latest entity value

        latestEntityType = getLatestEntityType(srOutput).toLowerCase();
        // Test if entity is part of SARA likes
        if (valence>0) {
            for (Entity entity : systemPreferences.getList(latestEntityType).getLike()) {
                System.out.println("---------------------- SARA" + entity.getEntity());
                System.out.println("---------------------- User" + latestEntityValue);
                if (entity.getEntity().equals(latestEntityValue)) {
                    systemPreferences.getGenres().getLike().toString();
                    System.out.println("---------------------- SARA likes " + latestEntityValue);
                    return true;
                }
            }
        }
        // Test if entity is part of SARA dislikes
        if (valence<0) {
            for (Entity entity : systemPreferences.getList(latestEntityType).getDislike()) {
                System.out.println("---------------------- SARA" + entity.getEntity());
                System.out.println("---------------------- User" + latestEntityValue);
                if (entity.getEntity().equals(latestEntityValue)) {
                    systemPreferences.getGenres().getLike().toString();
                    System.out.println("---------------------- SARA dislikes " + latestEntityValue);
                    return true;
                }
            }
        }
        System.out.println("---------------------- SARA does not care about " + latestEntityValue);
        return false;
    }


    /**
     * replaces all #patterns in template
     * @return template with patterns filled in OR null if a pattern could not be filled
     */
    private String replacePatterns(String template, SROutput srOutput, boolean replace) {
        Matcher m = slotMarker.matcher(template);
        while (m.matches()) {
            String slotName = m.group(1);
            String value = null, latestEntityValue, latestUtterance;
            float latestEntityValence = 0;
            UserFrame.Frame frame = srOutput.getDMOutput().getUserFrame().getFrame();
            DMOutput dmOutput = srOutput.getDMOutput();
            try {
                switch (slotName) {
                    case "#title":
                        if (replace) {
                            value = dmOutput.getRecommendation().getTitle();
                        } else
                            value = dmOutput.isRecommendation() ? "" : null;
                        break;
                    case "#reason":
                        if (replace) {
                            value = dmOutput.getRecommendation().getRexplanations().get(0).getExplanations().get(0);
                        } else {
                            value = dmOutput.isRecommendation() ? "" : null;
                        }
                        break;
                    case "#previousMovie":
                        // Gets the latest movie that the user liked. Supposedly used to change greeting phase.
                        int size = frame.getMovies().getLike().size();
                        value = size > 0 ? frame.getMovies().getLike().get(size-1) : null;
                        break;
                    case "#likedActor":
                        value = getAnyOf(frame.getActors().getLike());
                        break;
                    case "#dislikedActor":
                        value = getAnyOf(frame.getActors().getDislike());
                        break;
                    case "#likedGenre":
                        value = getAnyOf(frame.getGenres().getLike());
                        break;
                    case "#dislikedGenre":
                        value = getAnyOf(frame.getGenres().getDislike());
                        break;
                    case "#likedDirector":
                        value = getAnyOf(frame.getDirectors().getLike());
                        break;
                    case "#dislikedDirector":
                        value = getAnyOf(frame.getDirectors().getDislike());
                        break;
                    case "#latest":
                        // Get the latest entity detected from the user
                        latestUtterance = dmOutput.getUtterance();
                        latestEntityValue = getLatestEntityValue(srOutput); // latest entity
                        // if entity is from user's last utterance
                        if (latestUtterance.contains(latestEntityValue)) {
                            value = latestEntityValue;
                        }
                        break;
                    case "#agree":
                        // if SARA shares the same positive preference (Both user and SARA like it)
                        latestEntityValue = getLatestEntityValue(srOutput);
                        latestEntityValence = getLatestEntityValence(srOutput);
                        latestUtterance = dmOutput.getUtterance().toLowerCase();
			System.out.println("--------------Agree Testing on " + latestEntityValue + "--------------- in " + latestUtterance);
                        if (latestUtterance.contains(latestEntityValue)){
				System.out.println("-----------Contained --------------");
                            if (containEntity(srOutput, 1) && latestEntityValence >0){
                                value = latestEntityValue;
                            }
                        }
                        break;
                    case "#agreeOnDislike":
                        // if SARA shares the same negative preference (Both user and SARA dislike it)
                        latestEntityValue = getLatestEntityValue(srOutput);
                        latestEntityValence = getLatestEntityValence(srOutput);
                        latestUtterance = dmOutput.getUtterance().toLowerCase();
                        if (latestUtterance.contains(latestEntityValue)){
                            if (containEntity(srOutput, -1) && latestEntityValence <0){
                                value = latestEntityValue;
                            }
                        }
                        break;
                    case "#disagree":
                        // if SARA has opposite preference (User likes but SARA dislikes)
                        latestEntityValue = getLatestEntityValue(srOutput);
			latestEntityValence = getLatestEntityValence(srOutput);
                        latestUtterance = dmOutput.getUtterance().toLowerCase();
                        if (latestUtterance.contains(latestEntityValue)){
                            if (containEntity(srOutput, -1) && latestEntityValence >0){
                                value = latestEntityValue;
                            }
                        }
                        break;
                    case "#disagreeOnDislike":
                        // if SARA shares the same negative preference (Both user and SARA dislike it)
                        latestEntityValue = getLatestEntityValue(srOutput);
                        latestEntityValence = getLatestEntityValence(srOutput);
                        latestUtterance = dmOutput.getUtterance().toLowerCase();
                        if (latestUtterance.contains(latestEntityValue)){
                            if (containEntity(srOutput, 1) && latestEntityValence <0){
                                value = latestEntityValue;
                            }
                        }
                        break;
                    case "#different":
                        // if SARA has different preferences (User likes, but SARA neither likes or dislikes)
                        latestEntityValue = getLatestEntityValue(srOutput);
                        latestUtterance = dmOutput.getUtterance().toLowerCase();
                        if (latestUtterance.contains(latestEntityValue)){
                            if (!containEntity(srOutput, -1) && !containEntity(srOutput, 1)){
                                value = latestEntityValue;
                            }
                        }
                        break;
                    default:
                        throw new RuntimeException("SentenceGeneratorTemplate#replacePatterns() found a marker that I do not understand: " + slotName);
                }
            } catch (NullPointerException | IndexOutOfBoundsException npe) {
                // null pointer exceptions occur when we're unable to access the field
                // it's much simpler to catch them than to handle them individually for all cases
                npe.printStackTrace();
                value = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (value == null) {
                template = null;
                break;
            }
            if (replace) {
                template = template.replaceAll(slotName, value); // fill in template
            } else {
                template = template.replaceAll(slotName, ""); // remove template so we do not match on it again
            }
            m = slotMarker.matcher(template);
        }
        return template;
    }

    private static class WeightedCandidate {
        String sentence;
        double weight;
        WeightedCandidate(String sentence, double weight) {
            this.sentence = sentence;
            this.weight = weight;
        }
    }

    private final static Pattern slotMarker = Pattern.compile(".*?(#[^ \\.,\\?!]*).*");

    /** a template that can return a string with values filled in from a frame/explanation/... */
    private class Template {

        final String template;
        final double weight;
        Template(String template, double weight) {
            this.template = template;
            this.weight = weight;
        }

        WeightedCandidate match(SROutput srOutput, boolean replace) {
            return matchesPattern(template, srOutput) ? new WeightedCandidate(template, weight) : null;
        }

        @Override public String toString() {
            return template + " (" + Double.toString(weight) + ")";
        }
    }

    /**
     * Uses latest intent to get latest entity value.
     */
    private static String getLatestEntityValue(SROutput srOutput) {
        int latestEntityIndex = 0;
        try {
            switch(srOutput.getDMOutput().getAction()) {
                case "ask_directors": // refer to genre
                    latestEntityIndex = srOutput.getDMOutput().getUserFrame().getFrame().getGenres().getLike().size() - 1;
                    return srOutput.getDMOutput().getUserFrame().getFrame().getGenres().getLike().get(latestEntityIndex).getValue();
                case "ask_actors": // refer to director
                    latestEntityIndex = srOutput.getDMOutput().getUserFrame().getFrame().getDirectors().getLike().size() - 1;
                    return srOutput.getDMOutput().getUserFrame().getFrame().getDirectors().getLike().get(latestEntityIndex).getValue();
                case "recommend": // refer to actor
                    latestEntityIndex = srOutput.getDMOutput().getUserFrame().getFrame().getActors().getLike().size() - 1;
                    return srOutput.getDMOutput().getUserFrame().getFrame().getActors().getLike().get(latestEntityIndex).getValue();
                default:
                    return null;
            }
        } catch (NullPointerException npe) {
            npe.printStackTrace();
            throw new NullPointerException();
        }
    }

    private static float getLatestEntityValence(SROutput srOutput) {
        int latestEntityIndex = 0;
        try {
            switch(srOutput.getDMOutput().getAction()) {
                case "ask_directors": // refer to genre
                    latestEntityIndex = srOutput.getDMOutput().getUserFrame().getFrame().getGenres().getLike().size() - 1;
                    return srOutput.getDMOutput().getUserFrame().getFrame().getGenres().getLike().get(latestEntityIndex).getPolarity();
                case "ask_actors": // refer to director
                    latestEntityIndex = srOutput.getDMOutput().getUserFrame().getFrame().getDirectors().getLike().size() - 1;
                    return srOutput.getDMOutput().getUserFrame().getFrame().getDirectors().getLike().get(latestEntityIndex).getPolarity();
                case "recommend": // refer to actor
                    latestEntityIndex = srOutput.getDMOutput().getUserFrame().getFrame().getActors().getLike().size() - 1;
                    return srOutput.getDMOutput().getUserFrame().getFrame().getActors().getLike().get(latestEntityIndex).getPolarity();
                default:
                    return 0;
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
            switch(srOutput.getDMOutput().getAction()) {
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
    private class Sentence {
        String intent;
        String strategy;
        String sentence;
        double weight;

        Sentence(String intent, String strategy, double weight, String sentence) {
            this.intent = intent;
            this.strategy = strategy;
            this.weight = weight;
            this.sentence = sentence.trim();
        }

        String getIntent() { return intent; }
        String getStrategy() { return strategy; }
        double getWeight() { return weight; }
        Template getTemplate() {return new Template(sentence, weight);}
    }

    /**
     * turn a line formatted in the following way into a sentence object:
     * - empty lines are OK, lines starting with a # character are OK (both are disregarded)
     * - tab-separated fields: phase \t intent \t conversational strategy \t weight (as a double) \t sentence (possibly with attribute markers)
     * @return the Sentence object or NULL if comment
     * @throws IllegalArgumentException for lines that are neither comments nor properly formatted sentenceDB entries
     */
    public Sentence fromLine(String line) {
        Sentence sentence = null;
        if(!line.startsWith("#") && !line.trim().isEmpty()){
            String[] tokens = line.split("\\s+", 5);
            if(tokens.length == 5){
                // assumes that weight (token[3]) is a double
                sentence = new Sentence(tokens[1], tokens[2], Double.parseDouble(tokens[3]), tokens[4]);
            } else {
                throw new IllegalArgumentException("line was not a comment and did not have the right format: " + line + Arrays.toString(tokens));
            }
        }
        return sentence;
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
                Sentence sentence = fromLine(line);
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
     * Load SARA's preferences
     */
    static UserFrame.Frame loadSARAPreferences(InputStream saraDB) {
        UserFrame.Frame systemPreferences = new UserFrame.Frame();
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(saraDB));
            String line = null;
            while((line = in.readLine()) != null && !line.startsWith("#") && !line.trim().isEmpty()) {
                String[] tokens = line.split("\\s+", 3);
                if (tokens.length == 3) {
                    String entity = tokens[0], valence = tokens[1], preference = tokens[2];
                    Utils.checkContents(entity, "genre", "director", "actor");
                    Utils.checkContents(valence, "liked", "disliked");

                    UserFrame.PreferenceList prefList = systemPreferences.getList(entity);
                    // polarity is positive for liked, negative otherwise (must be disliked)
                    int polarity = "liked".equals(valence) ? 1 : -1;
                    List<Entity> valenceList = "liked".equals(valence) ? prefList.getLike() : prefList.getDislike();
                    // Create UserFrame-like object to store system's preferences
                    valenceList.add(new Entity(preference, polarity));
                } else
                    throw new IOException(line);
            }
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } // end try
        return systemPreferences;
    }
}
