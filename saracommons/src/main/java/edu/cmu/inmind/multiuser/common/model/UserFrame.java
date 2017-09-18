package edu.cmu.inmind.multiuser.common.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * User Frame
 * Created by yoichimatsuyama on 4/13/17.
 */
public class UserFrame {
    private Frame frame = new Frame();
    private List<String> ask_stack = new ArrayList<>();
    private List<String> universals = new ArrayList<>();
    private String latestUtterance;

    public Frame getFrame() {
        return frame;
    }
    public void setFrame(Frame frame) {
        this.frame = frame;
    }

    public List<String> getAsk_stack() {
        return ask_stack;
    }
    public void setAsk_stack(List<String> ask_stack) {
        this.ask_stack = ask_stack;
    }

    public List<String> getUniversals() {
        return universals;
    }
    public void setUniversals(List<String> universals) {
        this.universals = universals;
    }

    public String getLatestUtterance() {
        return latestUtterance;
    }
    public void setLatestUtterance(String string) {
        this.latestUtterance = string;
    }


    /**
     * Frame
     */
    public static class Frame {
        private PreferenceList actors = new PreferenceList();
        private PreferenceList genres = new PreferenceList();
        private PreferenceList directors = new PreferenceList();
        private Movies movies = new Movies();

        public PreferenceList getList(String listName) throws IOException {
            Utils.checkContents(listName, "genre", "director", "actor");
            switch (listName) {
                case "genre": return getGenres();
                case "director": return getDirectors();
                case "actor": return getActors();
                default:
                    throw new RuntimeException("you're kidding me.");
            }
        }

        public PreferenceList getActors() {
            return actors;
        }
        public void setActors(PreferenceList actors) {
            this.actors = actors;
        }

        public PreferenceList getGenres() { return genres; }
        public void setGenres(PreferenceList genres) {
            this.genres = genres;
        }

        public PreferenceList getDirectors() {
            return directors;
        }
        public void setDirectors(PreferenceList directors) {
            this.directors = directors;
        }

        public Movies getMovies() {
            return movies;
        }
        public void setMovies(Movies movies) {
            this.movies = movies;
        }
    }


    /**
     * Actors, Genres, Directors
     */
    public static class PreferenceList {
        private List<Entity> like = new ArrayList<>();
        private List<Entity> dislike = new ArrayList<>();

        public List<Entity> getLike() {
            return like;
        }
        public void setLike(List<Entity> like) {
            this.like = like;
        }

        public List<Entity> getDislike() {
            return dislike;
        }
        public void setDislike(List<Entity> dislike) {
            this.dislike = dislike;
        }
    }

    public static class Movies {
        private List<String> like = new ArrayList<>();
        private List<String> dislike = new ArrayList<>();
        private List<String> history = new ArrayList<>();

        public List<String> getLike() {
            return like;
        }
        public void setLike(List<String> like) {
            this.like = like;
        }

        public List<String> getDislike() {
            return dislike;
        }
        public void setDislike(List<String> dislike) {
            this.dislike = dislike;
        }

        public List<String> getHistory() {
            return history;
        }
        public void setHistory(List<String> history) {
            this.history = history;
        }

    }
}