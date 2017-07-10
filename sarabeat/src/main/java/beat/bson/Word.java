package beat.bson;

import java.util.List;

/**
 * Created by yoichimatsuyama on 4/14/17.
 */
public class Word {
    String word;
    int start_id;
    int end_id;
    float start_time;
    float end_time;
    List<Viseme> visemes;

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public int getStart_id() {
        return start_id;
    }

    public void setStart_id(int start_id) {
        this.start_id = start_id;
    }

    public int getEnd_id() {
        return end_id;
    }

    public void setEnd_id(int end_id) {
        this.end_id = end_id;
    }

    public float getStart_time() {
        return start_time;
    }

    public void setStart_time(float start_time) {
        this.start_time = start_time;
    }

    public float getEnd_time() {
        return end_time;
    }

    public void setEnd_time(float end_time) {
        this.end_time = end_time;
    }

    public List<Viseme> getVisemes() {
        return visemes;
    }

    public void setVisemes(List<Viseme> visemes) {
        this.visemes = visemes;
    }
}
