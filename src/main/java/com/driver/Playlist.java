package com.driver;

import java.util.ArrayList;
import java.util.List;

public class Playlist {
    private String title;
    private User creator;
    private List<Song> songs;
    private List<User> listeners;
    public Playlist(){
        this.songs = new ArrayList<>();
        this.listeners = new ArrayList<>();

    }

    public Playlist(String title){
        this.title = title;
        this.songs = new ArrayList<>();
        this.listeners = new ArrayList<>();
    }

    public Playlist(String title, User creator, List<Song> songs) {
        this.title = title;
        this.creator = creator;
        this.songs = songs;
        this.listeners = new ArrayList<>();
        this.listeners.add(creator);
    }

    public String getTitle() {
        return title;

    }

    public void setTitle(String title) {
        this.title = title;
    }


    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public List<Song> getSongs() {
        return songs;
    }

    public void setSongs(List<Song> songs) {
        this.songs = songs;
    }

    public List<User> getListeners() {
        return listeners;
    }

    public void setListeners(List<User> listeners) {
        this.listeners = listeners;
    }
}
