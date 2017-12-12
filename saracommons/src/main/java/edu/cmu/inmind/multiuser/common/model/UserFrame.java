package edu.cmu.inmind.multiuser.common.model;

import java.util.ArrayList;
import java.util.List;

/**
 * User Frame
 * Created by yoichimatsuyama on 4/13/17.
 */
public class UserFrame {

    private PreferenceList actors = new PreferenceList();
    private PreferenceList genres = new PreferenceList();
    private PreferenceList directors = new PreferenceList();
    private PreferenceList movies = new PreferenceList();

    public PreferenceList getList(String listName) {
        Utils.checkContents(listName.toLowerCase(), "genre", "director", "actor", "movie", "genres", "directors", "actors", "movies");
        switch (listName.toLowerCase()) {
            case "genre":
            case "genres":
                return getGenres();
            case "director":
            case "directors":
                return getDirectors();
            case "actor":
            case "actors":
                return getActors();
            case "movie":
            case "movies":
                return getMovies();
            default:
                throw new IllegalArgumentException("you're kidding me, searching for " + listName);
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

    public PreferenceList getMovies() {
        return movies;
    }
    public void setMovies(PreferenceList movies) {
        this.movies = movies;
    }

    /**
     * Actors, Genres, Directors
     */
    public static class PreferenceList {
        private List<String> like = new ArrayList<>();
        private List<String> dislike = new ArrayList<>();

        public PreferenceList() {}
        public PreferenceList(List<String> liked, List<String> disliked) {
            setLike(liked);
            setDislike(disliked);
        }

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
    }

}