package com.driver;

import java.util.*;

import org.springframework.stereotype.Repository;

@Repository
public class SpotifyRepository {
    public HashMap<Artist, List<Album>> artistAlbumMap;
    public HashMap<Album, List<Song>> albumSongMap;
    public HashMap<Playlist, List<Song>> playlistSongMap;
    public HashMap<Playlist, List<User>> playlistListenerMap;
    public HashMap<User, Playlist> creatorPlaylistMap;
    public HashMap<User, List<Playlist>> userPlaylistMap;
    public HashMap<Song, List<User>> songLikeMap;

    public List<User> users;
    public List<Song> songs;
    public List<Playlist> playlists;
    public List<Album> albums;
    public List<Artist> artists;

    public SpotifyRepository(){
        //To avoid hitting apis multiple times, initialize all the hashmaps here with some dummy data
        artistAlbumMap = new HashMap<>();
        albumSongMap = new HashMap<>();
        playlistSongMap = new HashMap<>();
        playlistListenerMap = new HashMap<>();
        creatorPlaylistMap = new HashMap<>();
        userPlaylistMap = new HashMap<>();
        songLikeMap = new HashMap<>();

        users = new ArrayList<>();
        songs = new ArrayList<>();
        playlists = new ArrayList<>();
        albums = new ArrayList<>();
        artists = new ArrayList<>();
    }

    public User createUser(String name, String mobile) {
        User user = new User(name, mobile);
        users.add(user);
        return user;
    }

    public Artist createArtist(String name) {
        Artist artist = new Artist(name);
        artists.add(artist);
        return artist;
    }

    public Album createAlbum(String title, String artistName) {
        Artist artist = findArtist(artistName);
        if (artist == null) {
            artist = createArtist(artistName);
        }
        Album album = new Album(title);
        albums.add(album);
        artistAlbumMap.computeIfAbsent(artist, k -> new ArrayList<>()).add(album);
        return album;
    }

    public Song createSong(String title, String albumName, int length) throws Exception{
        Album album = findAlbum(albumName);
        if (album == null) {
            throw new Exception("Album does not exist");
        }
        Song song = new Song(title, length);
        songs.add(song);
        albumSongMap.computeIfAbsent(album, k -> new ArrayList<>()).add(song);
        return song;
    }

    public Playlist createPlaylistOnLength(String mobile, String title, int length) throws Exception {
        User user = findUser(mobile);
        if (user == null) {
            throw new Exception("User does not exist");
        }
        Playlist playlist = new Playlist(title);
        List<Song> songsByLength = findSongsByLength(length);
        playlistSongMap.put(playlist, songsByLength);
        playlistListenerMap.put(playlist, new ArrayList<>(Collections.singleton(user)));
        userPlaylistMap.computeIfAbsent(user, k -> new ArrayList<>()).add(playlist);
        creatorPlaylistMap.put(user, playlist);
        playlists.add(playlist);
        return playlist;
    }

    public Playlist createPlaylistOnName(String mobile, String title, List<String> songTitles) throws Exception {
        User user = findUser(mobile);
        if (user == null) {
            throw new Exception("User does not exist");
        }
        Playlist playlist = new Playlist(title);
        List<Song> songsByTitles = findSongsByTitles(songTitles);
        playlistSongMap.put(playlist, songsByTitles);
        playlistListenerMap.put(playlist, new ArrayList<>(Collections.singleton(user)));
        userPlaylistMap.computeIfAbsent(user, k -> new ArrayList<>()).add(playlist);
        creatorPlaylistMap.put(user, playlist);
        playlists.add(playlist);
        return playlist;
    }

    public Playlist findPlaylist(String mobile, String playlistTitle) throws Exception {
        User user = findUser(mobile);
        if (user == null) {
            throw new Exception("User does not exist");
        }
        Playlist playlist = playlists.stream()
                .filter(pl -> pl.getTitle().equals(playlistTitle))
                .findFirst()
                .orElseThrow(() -> new Exception("Playlist does not exist"));
        if (!playlistListenerMap.get(playlist).contains(user) && !creatorPlaylistMap.get(user).equals(playlist)) {
            playlistListenerMap.get(playlist).add(user);
        }
        return playlist;
    }

    public Song likeSong(String mobile, String songTitle) throws Exception {
        User user = findUser(mobile);
        if (user == null) {
            throw new Exception("User does not exist");
        }
        Song song = findSong(songTitle);
        if (song == null) {
            throw new Exception("Song does not exist");
        }
        if (!songLikeMap.containsKey(song)) {
            songLikeMap.put(song, new ArrayList<>());
        }
        if (!songLikeMap.get(song).contains(user)) {
            songLikeMap.get(song).add(user);
            song.setLikes(song.getLikes() + 1);
            Artist artist = findArtistBySong(song);
            if (artist != null) {
                artist.setLikes(artist.getLikes() + 1);
            }
        }
        return song;
    }

    public String mostPopularArtist() {
        return artists.stream()
                .max(Comparator.comparingInt(Artist::getLikes))
                .map(Artist::getName)
                .orElse(null);
    }

    public String mostPopularSong() {
        return songs.stream()
                .max(Comparator.comparingInt(Song::getLikes))
                .map(Song::getTitle)
                .orElse(null);
    }

    private User findUser(String mobile) {
        return users.stream()
                .filter(user -> user.getMobile().equals(mobile))
                .findFirst()
                .orElse(null);
    }

    private Artist findArtist(String name) {
        return artists.stream()
                .filter(artist -> artist.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    private Album findAlbum(String title) {
        return albums.stream()
                .filter(album -> album.getTitle().equals(title))
                .findFirst()
                .orElse(null);
    }

    private Song findSong(String title) {
        return songs.stream()
                .filter(song -> song.getTitle().equals(title))
                .findFirst()
                .orElse(null);
    }

    private List<Song> findSongsByLength(int length) {
        return songs.stream()
                .filter(song -> song.getLength() == length)
                .toList();
    }

    private List<Song> findSongsByTitles(List<String> titles) {
        return songs.stream()
                .filter(song -> titles.contains(song.getTitle()))
                .toList();
    }

    private Artist findArtistBySong(Song song) {
        for (Map.Entry<Album, List<Song>> entry : albumSongMap.entrySet()) {
            if (entry.getValue().contains(song)) {
                return findArtist(entry.getKey().getTitle()); // Assuming album title is artist name
            }
        }
        return null;
    }
}
