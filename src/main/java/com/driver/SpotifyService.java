package com.driver;

import java.util.*;

import org.springframework.stereotype.Service;

@Service
public class SpotifyService {

    SpotifyRepository spotifyRepository = new SpotifyRepository();

    public User createUser(String name, String mobile){
        User user = new User(name, mobile);
        spotifyRepository.users.add(user);
        return user;
    }

    public Artist createArtist(String name) {
        Artist artist = spotifyRepository.artists.stream()
                .filter(a -> a.getName().equals(name))
                .findFirst()
                .orElse(new Artist(name));
        spotifyRepository.artists.add(artist);
        return artist;
    }

    public Album createAlbum(String title, String artistName) {
        Artist artist = spotifyRepository.artists.stream()
                .filter(a -> a.getName().equals(artistName))
                .findFirst()
                .orElse(new Artist(artistName));
        if (!spotifyRepository.artists.contains(artist)) {
            spotifyRepository.artists.add(artist);
        }
        Album album = new Album(title);
        artist.getAlbums().add(album);
        spotifyRepository.albums.add(album);
        return album;
    }

    public Song createSong(String title, String albumName, int length) throws Exception {
        Album album = spotifyRepository.albums.stream()
                .filter(a -> a.getTitle().equals(albumName))
                .findFirst()
                .orElseThrow(() -> new Exception("Album does not exist"));
        Song song = new Song(title, length);
        spotifyRepository.songs.add(song);
        spotifyRepository.albumSongMap.computeIfAbsent(album, k -> new ArrayList<>()).add(song);
        return song;
    }

    public Playlist createPlaylistOnLength(String mobile, String title, int length) throws Exception {
        User user = findUser(mobile);
        if (user == null) {
            throw new Exception("User does not exist");
        }
        Playlist playlist = new Playlist(title);
        List<Song> songsByLength = findSongsByLength(length);
        spotifyRepository.playlistSongMap.put(playlist, songsByLength);
        spotifyRepository.playlistListenerMap.put(playlist, new ArrayList<>(Collections.singleton(user)));
        spotifyRepository.userPlaylistMap.computeIfAbsent(user, k -> new ArrayList<>()).add(playlist);
        return playlist;
    }

    public Playlist createPlaylistOnName(String mobile, String title, List<String> songTitles) throws Exception {
        User user = findUser(mobile);
        if (user == null) {
            throw new Exception("User does not exist");
        }
        Playlist playlist = new Playlist(title);
        List<Song> songsByTitles = findSongsByTitles(songTitles);
        spotifyRepository.playlistSongMap.put(playlist, songsByTitles);
        spotifyRepository.playlistListenerMap.put(playlist, new ArrayList<>(Collections.singleton(user)));
        spotifyRepository.userPlaylistMap.computeIfAbsent(user, k -> new ArrayList<>()).add(playlist);
        return playlist;
    }

    public Playlist findPlaylist(String mobile, String playlistTitle) throws Exception {
        User user = findUser(mobile);
        if (user == null) {
            throw new Exception("User does not exist");
        }
        Playlist playlist = spotifyRepository.playlists.stream()
                .filter(pl -> pl.getTitle().equals(playlistTitle))
                .findFirst()
                .orElseThrow(() -> new Exception("Playlist does not exist"));
        if (!spotifyRepository.playlistListenerMap.get(playlist).contains(user)) {
            spotifyRepository.playlistListenerMap.get(playlist).add(user);
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
        if (!spotifyRepository.songLikeMap.containsKey(song)) {
            spotifyRepository.songLikeMap.put(song, new ArrayList<>());
        }
        if (!spotifyRepository.songLikeMap.get(song).contains(user)) {
            spotifyRepository.songLikeMap.get(song).add(user);
            song.setLikes(song.getLikes() + 1);
            Artist artist = findArtistBySong(song);
            if (artist != null) {
                artist.setLikes(artist.getLikes() + 1);
            }
        }
        return song;
    }

    public String mostPopularArtist() {
        return spotifyRepository.artists.stream()
                .max(Comparator.comparingInt(Artist::getLikes))
                .map(Artist::getName)
                .orElse(null);
    }

    public String mostPopularSong() {
        return spotifyRepository.songs.stream()
                .max(Comparator.comparingInt(Song::getLikes))
                .map(Song::getTitle)
                .orElse(null);
    }

    private User findUser(String mobile) {
        return spotifyRepository.users.stream()
                .filter(user -> user.getMobile().equals(mobile))
                .findFirst()
                .orElse(null);
    }

    private Artist findArtist(String name) {
        return spotifyRepository.artists.stream()
                .filter(artist -> artist.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    private Album findAlbum(String title) {
        return spotifyRepository.albums.stream()
                .filter(album -> album.getTitle().equals(title))
                .findFirst()
                .orElse(null);
    }

    private Song findSong(String title) {
        return spotifyRepository.songs.stream()
                .filter(song -> song.getTitle().equals(title))
                .findFirst()
                .orElse(null);
    }

    private List<Song> findSongsByLength(int length) {
        return spotifyRepository.songs.stream()
                .filter(song -> song.getLength() == length)
                .toList();
    }

    private List<Song> findSongsByTitles(List<String> titles) {
        return spotifyRepository.songs.stream()
                .filter(song -> titles.contains(song.getTitle()))
                .toList();
    }

    private Artist findArtistBySong(Song song) {
        for (Map.Entry<Album, List<Song>> entry : spotifyRepository.albumSongMap.entrySet()) {
            if (entry.getValue().contains(song)) {
                return findArtist(entry.getKey().getTitle());
            }
        }
        return null;
    }
}
