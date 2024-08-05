package com.driver;

import java.util.ArrayList;
import java.util.List;

public class Artist {
    private List<Album> albums;
    private String name;
    private int likes;

    public Artist(){
        this.albums = new ArrayList<>();
        this.likes = 0;
        
    }

    public Artist(String name){
        this.albums = new ArrayList<>();
        this.name = name;
        this.likes = 0;
    }

    public List<Album> getAlbums() {
        return albums;
    }

    public void setAlbums(List<Album> albums) {
        this.albums = albums;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }
}
