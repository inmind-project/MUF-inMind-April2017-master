package edu.cmu.inmind.multiuser.common.model;

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
        private Actors actors = new Actors();
        private Genres genres = new Genres();
        private Directors directors = new Directors();
        private Movies movies = new Movies();

        public Actors getActors() {
            return actors;
        }

        public void setActors(Actors actors) {
            this.actors = actors;
        }

        public Genres getGenres() {
            return genres;
        }

        public void setGenres(Genres genres) {
            this.genres = genres;
        }

        public Directors getDirectors() {
            return directors;
        }

        public void setDirectors(Directors directors) {
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
     * Movies
     */
    public static class Actors {
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

    /**
     * Genres
     */
    public static class Genres {
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

    /**
     * Directors
     */
    public static class Directors {
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
