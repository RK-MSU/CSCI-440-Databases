package edu.montana.csci.csci440.model;

import edu.montana.csci.csci440.util.DB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Album extends Model {

    Long albumId;
    Long artistId;
    String title;

    public Album() {

    }

    private Album(ResultSet results) throws SQLException {
        title = results.getString("Title");
        albumId = results.getLong("AlbumId");
        artistId = results.getLong("ArtistId");
    }

    @Override
    public boolean create() {
        // validate data
        if (!verify()) {
            // not valid
            return false;
        }

        String query = "INSERT INTO albums (Title, ArtistId) VALUES (?, ?)";

        try(Connection conn = DB.connect(); PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, getTitle());
            stmt.setLong(2, getArtistId());

            int result = stmt.executeUpdate();
            this.albumId = DB.getLastID(conn);

            return result == 1;

        } catch (SQLException sqlException){
            throw new RuntimeException(sqlException);
        }
    }

    @Override
    public boolean update() {
        // validate data
        if (!verify()) {
            // not valid
            return false;
        }

        String query = "UPDATE albums SET Title=?, ArtistId=? WHERE AlbumId=?";

        try(Connection conn = DB.connect(); PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, getTitle());
            stmt.setLong(2, getArtistId());
            stmt.setLong(3, getArtistId());

            int result = stmt.executeUpdate();

            return result == 1;

        } catch (SQLException sqlException){
            throw new RuntimeException(sqlException);
        }
    }

    @Override
    public boolean verify() {
        _errors.clear();

        if( title == null || title.trim().equals("")) {
            addError("Title cannot be empty");
        }
        if( artistId == null || artistId == 0) {
            addError("Artist cannot be empty.");
        }

        return !hasErrors();
    }


    @Override
    public void delete() {

        // tracks
        List<Track> tracks = this.getTracks();
        for (Track t : tracks) {
            t.delete();
        }

        String query = "DELETE FROM albums WHERE AlbumId=?";
        try(Connection conn = DB.connect(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setLong(1, getAlbumId());
            int result = stmt.executeUpdate();
        } catch (SQLException sqlException){
            throw new RuntimeException(sqlException);
        }
    }

    public Artist getArtist() {
        return Artist.find(artistId);
    }

    public void setArtist(Artist artist) {
        artistId = artist.getArtistId();
    }

    public List<Track> getTracks() {
        return Track.forAlbum(albumId);
    }

    public Long getAlbumId() {
        return albumId;
    }

    public void setAlbum(Album album) {
        this.albumId = album.getAlbumId();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String name) {
        this.title = name;
    }

    public Long getArtistId() {
        return artistId;
    }

    public void setArtistId(Long id) {
        artistId = id;
    }

    public static List<Album> all() {
        return all(0, Integer.MAX_VALUE);
    }

    // Paging
    public static List<Album> all(int page, int count) {

        String query = "SELECT * FROM albums LIMIT ? OFFSET ?";

        try (Connection conn = DB.connect(); PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, count);
            stmt.setInt(2, count * (page - 1));

            ResultSet results = stmt.executeQuery();
            List<Album> resultList = new LinkedList<>();
            while (results.next()) {
                resultList.add(new Album(results));
            }
            return resultList;
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }
    }

    public static List<Album> all(int page, int count, String orderBy, String sort) {

        String query = "SELECT * FROM albums ORDER BY "+orderBy+" "+sort+" LIMIT ? OFFSET ?";

        try (Connection conn = DB.connect(); PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, count);
            stmt.setInt(2, count * (page - 1));

            ResultSet results = stmt.executeQuery();
            List<Album> resultList = new LinkedList<>();
            while (results.next()) {
                resultList.add(new Album(results));
            }
            return resultList;
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }
    }


    public static Album find(long i) {
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM albums WHERE AlbumId=?")) {
            stmt.setLong(1, i);
            ResultSet results = stmt.executeQuery();
            if (results.next()) {
                return new Album(results);
            } else {
                return null;
            }
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }
    }

    public static List<Album> getForArtist(Long artistId) {
        String query = "SELECT * FROM albums WHERE ArtistId=?";

        try (Connection conn = DB.connect(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setLong(1, artistId);

            ResultSet results = stmt.executeQuery();
            List<Album> resultList = new LinkedList<>();
            while (results.next()) {
                resultList.add(new Album(results));
            }
            return resultList;
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }

    }

}
