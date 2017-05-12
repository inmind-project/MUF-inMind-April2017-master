package edu.cmu.inmind.multiuser.common.model;

import java.util.List;

/**
 * User Frame
 * Created by yoichimatsuyama on 4/13/17.
 */
public class UserFrame {
    private Frame frame;
    private List<String> ask_stack;
    private List<String> universals;

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


    /**
     * Frame
     */
    public class Frame {
        private Actors actors;
        private Genres genres;
        private Directors directors;
        private Movies movies;

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
    public class Actors {
        private List<Entity> like;
        private List<Entity> dislike;

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
    public class Genres {
        private List<Entity> like;
        private List<Entity> dislike;

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
    public class Directors {
        private List<Entity> like;
        private List<Entity> dislike;

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

    public class Movies {
        private List<String> like;
        private List<String> dislike;
        private List<String> history;

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
