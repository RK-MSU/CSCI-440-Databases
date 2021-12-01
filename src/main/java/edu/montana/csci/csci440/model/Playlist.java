package edu.montana.csci.csci440.model;

import edu.montana.csci.csci440.util.DB;
import edu.montana.csci.csci440.util.Web;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Playlist extends Model {

    Long playlistId;
    String name;

    public Playlist() {
    }

    protected Playlist(ResultSet results) throws SQLException {
        playlistId = results.getLong("PlaylistId");
        name = results.getString("Name");
    }


    public List<Track> getTracks(){
        // TODO implement, order by track name

        String query = "SELECT t.TrackId, t.Name, t.AlbumId, t.MediaTypeId, t.GenreId, t.Composer, t.Milliseconds, t.Bytes, t.UnitPrice " +
                "FROM tracks t " +
                "JOIN playlist_track pt on t.TrackId = pt.TrackId " +
                "JOIN playlists p on pt.PlaylistId = p.PlaylistId " +
                "WHERE p.PlaylistId=? " +
                "ORDER BY t.Name";

        try (Connection conn = DB.connect(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setLong(1, this.getPlaylistId());

            ResultSet results = stmt.executeQuery();
            List<Track> resultList = new LinkedList<>();
            while (results.next()) {
                resultList.add(new Track(results));
            }

            return resultList;
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }
    }


    public List<Track> getPlaylistTracks(){
        String query = "SELECT t.TrackId, t.Name, t.AlbumId, t.MediaTypeId, t.GenreId, t.Composer, t.Milliseconds, t.Bytes, t.UnitPrice " +
                "FROM tracks t " +
                "JOIN playlist_track pt on t.TrackId = pt.TrackId " +
                "JOIN playlists p on pt.PlaylistId = p.PlaylistId " +
                "WHERE p.PlaylistId=? " +
                "ORDER BY t.Name " +
                "LIMIT ? OFFSET ?";

        int page = Web.getPage();
        int count = Web.PAGE_SIZE;


        try (Connection conn = DB.connect(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setLong(1, this.getPlaylistId());
            stmt.setInt(2, count);
            stmt.setInt(3, count * (page - 1));

            ResultSet results = stmt.executeQuery();
            List<Track> resultList = new LinkedList<>();
            while (results.next()) {
                resultList.add(new Track(results));
            }

            return resultList;
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }
    }


    public void removeTrack(Track t) {
        String query = "DELETE FROM playlist_track WHERE TrackId=?";
        try (Connection conn = DB.connect(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setLong(1, t.getTrackId());
            stmt.executeUpdate();
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }
    }


    public int getTrackCount() {
        String query = "SELECT COUNT(*) AS TrackCount " +
                "FROM tracks t " +
                "JOIN playlist_track pt on t.TrackId = pt.TrackId " +
                "JOIN playlists p on pt.PlaylistId = p.PlaylistId " +
                "WHERE p.PlaylistId=?";
        try (Connection conn = DB.connect(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setLong(1, this.getPlaylistId());
            ResultSet results = stmt.executeQuery();
            if (results.next()) {
                return results.getInt("TrackCount");
            }
            return 0;
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }

    }

    public Long getPlaylistId() {
        return playlistId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static List<Playlist> all() {
        return all(0, Integer.MAX_VALUE);
    }

    public static List<Playlist> all(int page, int count) {
        String query = "SELECT * FROM playlists LIMIT ? OFFSET ?";

        try (Connection conn = DB.connect(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, count);
            stmt.setInt(2, count * (page - 1));

            ResultSet results = stmt.executeQuery();
            List<Playlist> resultList = new LinkedList<>();
            while (results.next()) {
                resultList.add(new Playlist(results));
            }

            return resultList;
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }
    }

    public static Playlist find(int i) {
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM playlists WHERE PlaylistId=?")) {
            stmt.setLong(1, i);
            ResultSet results = stmt.executeQuery();
            if (results.next()) {
                return new Playlist(results);
            } else {
                return null;
            }
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }
    }

}
