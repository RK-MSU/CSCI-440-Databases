package edu.montana.csci.csci440.model;

import edu.montana.csci.csci440.util.DB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Artist extends Model {

    Long artistId;
    String name;
    String originalName;

    public Artist() {
        // new artist for insert
    }

    private Artist(ResultSet results) throws SQLException {
        name = results.getString("Name");
        artistId = results.getLong("ArtistId");

        originalName = name;
    }

    @Override
    public boolean create() {

        // validate data
        if (!verify()) {
            // not valid
            return false;
        }

        String query = "INSERT INTO artists (Name) VALUES (?)";

        try(Connection conn = DB.connect(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, getName());

            int result = stmt.executeUpdate();
            this.artistId = DB.getLastID(conn);

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

        String query = "UPDATE artists SET Name=? WHERE ArtistId=? AND NAME=?";

        try(Connection conn = DB.connect(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, getName());
            stmt.setLong(2, getArtistId());
            stmt.setString(3, originalName);

            int result = stmt.executeUpdate();

            return result == 1;
        } catch (SQLException sqlException){
            throw new RuntimeException(sqlException);
        }
    }

    @Override
    public void delete() {

        // albums
        List<Album> albums = this.getAlbums();
        for (Album a : albums) {
            a.delete();
        }

        String query = "DELETE FROM artists WHERE ArtistId=?";
        try(Connection conn = DB.connect(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setLong(1, getArtistId());
            int result = stmt.executeUpdate();
        } catch (SQLException sqlException){
            throw new RuntimeException(sqlException);
        }
    }

    @Override
    public boolean verify() {
        _errors.clear(); // clear any existing errors
        // name
        if( name == null || name.trim().equals("")) {
            addError("Name cannot be empty");
        }
        return !hasErrors();
    }

    public List<Album> getAlbums(){
        return Album.getForArtist(artistId);
    }

    public Long getArtistId() {
        return artistId;
    }

    public void setArtist(Artist artist) {
        this.artistId = artist.getArtistId();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static List<Artist> all() {
        return all(0, Integer.MAX_VALUE);
    }

    public static List<Artist> all(int page, int count) {

        String query = "SELECT * FROM artists LIMIT ? OFFSET ?";

        try (Connection conn = DB.connect(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, count);
            stmt.setInt(2, count * (page - 1));

            ResultSet results = stmt.executeQuery();
            List<Artist> resultList = new LinkedList<>();
            while (results.next()) {
                resultList.add(new Artist(results));
            }

            return resultList;
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }
    }


    public static List<Artist> all(int page, int count, String orderBy, String sort) {

        String query = "SELECT * FROM artists ORDER BY "+orderBy+" "+sort+" LIMIT ? OFFSET ?";

        try (Connection conn = DB.connect(); PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, count);
            stmt.setInt(2, count * (page - 1));

            ResultSet results = stmt.executeQuery();
            List<Artist> resultList = new LinkedList<>();
            while (results.next()) {
                resultList.add(new Artist(results));
            }

            return resultList;
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }
    }

    public static Artist find(long i) {
        String query = "SELECT * FROM artists WHERE ArtistId=?";

        try (Connection conn = DB.connect(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setLong(1, i);
            ResultSet results = stmt.executeQuery();
            if (results.next()) {
                return new Artist(results);
            } else {
                return null;
            }
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }
    }

}
