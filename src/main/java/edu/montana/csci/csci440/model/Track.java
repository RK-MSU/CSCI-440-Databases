package edu.montana.csci.csci440.model;

import edu.montana.csci.csci440.util.DB;
import redis.clients.jedis.Client;
import redis.clients.jedis.Jedis;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Track extends Model {

    private Long trackId;
    private Long albumId;
    private Long mediaTypeId;
    private Long genreId;
    private String name;
    private Long milliseconds;
    private Long bytes;
    private BigDecimal unitPrice;

    private Album album;
    private Artist artist;

    public static final String REDIS_CACHE_KEY = "cs440-tracks-count-cache";

    public Track() {
        mediaTypeId = 1l;
        genreId = 1l;
        milliseconds  = 0l;
        bytes  = 0l;
        unitPrice = new BigDecimal("0");


    }

    protected Track(ResultSet results) throws SQLException {
        name = results.getString("Name");
        milliseconds = results.getLong("Milliseconds");
        bytes = results.getLong("Bytes");
        unitPrice = results.getBigDecimal("UnitPrice");
        trackId = results.getLong("TrackId");
        albumId = results.getLong("AlbumId");
        mediaTypeId = results.getLong("MediaTypeId");
        genreId = results.getLong("GenreId");

        album = getAlbum();
        artist = album.getArtist();
    }

    @Override
    public boolean verify() {
        _errors.clear(); // clear any existing errors

        // name
        if( name == null || name.equals("")) {
            addError("Name cannot be empty");
        }

        // must have album (i.e. albumId)
        if( albumId == null || albumId == 0) {
            addError("Album cannot be empty.");
        }

        return !hasErrors();
    }

    @Override
    public boolean create() {
        // validate data
        if (!verify()) {
            // not valid
            return false;
        }

        String query = "INSERT INTO tracks (Name, AlbumId, MediaTypeId, GenreId, Milliseconds, Bytes, UnitPrice) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try(Connection conn = DB.connect(); PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, getName());
            stmt.setLong(2, getAlbumId());
            stmt.setLong(3, getMediaTypeId());
            stmt.setLong(4, getGenreId());
            stmt.setLong(5, getMilliseconds());
            stmt.setLong(6, getBytes());
            stmt.setBigDecimal(7, getUnitPrice());

            int result = stmt.executeUpdate();
            this.trackId = DB.getLastID(conn);

            Jedis redisClient = new Jedis();
            redisClient.del(REDIS_CACHE_KEY);

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

        String query = "UPDATE tracks SET AlbumId=?, MediaTypeId=?, GenreId=?, Name=?, Milliseconds=?, Bytes=?, UnitPrice=? WHERE TrackId=?";

        try(Connection conn = DB.connect(); PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setLong(1, getAlbumId());
            stmt.setLong(2, getMediaTypeId());
            stmt.setLong(3, getGenreId());
            stmt.setString(4, getName());
            stmt.setLong(5, getMilliseconds());
            stmt.setLong(6, getBytes());
            stmt.setBigDecimal(7, getUnitPrice());
            stmt.setLong(8, getTrackId());

            int result = stmt.executeUpdate();

            return result == 1;
        } catch (SQLException sqlException){
            throw new RuntimeException(sqlException);
        }
    }

    @Override
    public void delete() {
        List<Playlist> playlists = this.getPlaylists();
        for(Playlist p : playlists) {
            p.removeTrack(this);
        }

        String invoice_query = "DELETE FROM invoice_items WHERE TrackId=?";

        try(Connection conn = DB.connect(); PreparedStatement stmt = conn.prepareStatement(invoice_query)) {
            stmt.setLong(1, getTrackId());
            stmt.executeUpdate();
        } catch (SQLException sqlException){
            throw new RuntimeException(sqlException);
        }


        String query = "DELETE FROM tracks WHERE TrackId=?";

        try(Connection conn = DB.connect(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setLong(1, getTrackId());
            stmt.executeUpdate();
        } catch (SQLException sqlException){
            throw new RuntimeException(sqlException);
        }
    }

    public static Track find(long i) {
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM tracks WHERE TrackId=?")) {
            stmt.setLong(1, i);
            ResultSet results = stmt.executeQuery();
            if (results.next()) {
                return new Track(results);
            } else {
                return null;
            }
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }
    }

    public static Long count() {
        Jedis redisClient = new Jedis(); // use this class to access redis and create a cache

        if(redisClient.exists(REDIS_CACHE_KEY)) {
            return Long.parseLong(redisClient.get(REDIS_CACHE_KEY));
        }

        String query = "SELECT COUNT(*) as Count FROM tracks";

        try (Connection conn = DB.connect(); PreparedStatement stmt = conn.prepareStatement(query)) {
            ResultSet results = stmt.executeQuery();
            if (results.next()) {
                long c = results.getLong("Count");
                redisClient.set(REDIS_CACHE_KEY, String.valueOf(results.getLong("Count")));
                return c;
            } else {
                throw new IllegalStateException("Should find a count!");
            }
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }
    }

    public Album getAlbum() {
        return Album.find(albumId);
    }

    public MediaType getMediaType() {
        return null;
    }
    public Genre getGenre() {
        return null;
    }

    public List<Playlist> getPlaylists() {

        String query = "SELECT * FROM playlists p " +
                "JOIN playlist_track pt on p.PlaylistId = pt.PlaylistId " +
                "JOIN tracks t on pt.TrackId = t.TrackId " +
                "WHERE t.TrackId=?";

        try (Connection conn = DB.connect(); PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setLong(1, this.getTrackId());

            ResultSet result = stmt.executeQuery();
            List<Playlist> resultList = new LinkedList<>();

            while (result.next()) {
                resultList.add(new Playlist(result));
            }

            return resultList;

        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }

    }

    public Long getTrackId() {
        return trackId;
    }

    public void setTrackId(Long trackId) {
        this.trackId = trackId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getMilliseconds() {
        return milliseconds;
    }

    public void setMilliseconds(Long milliseconds) {
        this.milliseconds = milliseconds;
    }

    public Long getBytes() {
        return bytes;
    }

    public void setBytes(Long bytes) {
        this.bytes = bytes;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Long getAlbumId() {
        return albumId;
    }

    public void setAlbumId(Long albumId) {
        this.albumId = albumId;
    }

    public void setAlbum(Album album) {
        albumId = album.getAlbumId();
    }

    public Long getMediaTypeId() {
        return mediaTypeId;
    }

    public void setMediaTypeId(Long mediaTypeId) {
        this.mediaTypeId = mediaTypeId;
    }

    public Long getGenreId() {
        return genreId;
    }

    public void setGenreId(Long genreId) {
        this.genreId = genreId;
    }

    public String getArtistName() {
        // TODO implement more efficiently
        //  hint: cache on this model object
        return artist.getName();
    }

    public Long getArtistId() {
        return artist.getArtistId();
    }

    public String getAlbumTitle() {
        // TODO implement more efficiently
        //  hint: cache on this model object
        return album.getTitle();
    }

    public static List<Track> advancedSearch(int page, int count,
                                             String search,
                                             Integer artistId,
                                             Integer albumId,
                                             Integer genreId,
                                             Integer maxRuntime,
                                             Integer minRuntime) {

        LinkedList<Object> args = new LinkedList<>();

        String query = "SELECT tracks.*, albums.ArtistId FROM tracks " +
                "JOIN albums ON tracks.AlbumId = albums.AlbumId " +
                // "JOIN genres ON tracks.GenreId = genres.GenreId " +
                "WHERE name LIKE ?";

        args.add("%" + search + "%");

        // Conditionally include the query and argument
        if (artistId != null) {
            query += " AND ArtistId=? ";
            args.add(artistId);
        }

        if (albumId != null) {
            query += " AND AlbumId=? ";
            args.add(albumId);
        }

//        if (genreId != null) {
//            query += " AND genres.GenreId=? ";
//            args.add(genreId);
//        }

        query += " LIMIT ?";
        args.add(count);

        try (Connection conn = DB.connect();  PreparedStatement stmt = conn.prepareStatement(query)) {
            for (int i = 0; i < args.size(); i++) {
                Object arg = args.get(i);
                stmt.setObject(i + 1, arg);
            }

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

    public static List<Track> search(int page, int count, String orderBy, String search) {
        String query = "SELECT tracks.*, albums.Title as AlbumTitle, artists.Name as ArtistName " +
                "FROM tracks " +
                "JOIN albums ON albums.AlbumId=tracks.AlbumId " +
                "JOIN artists ON artists.ArtistId=albums.ArtistId " +
                "WHERE tracks.Name LIKE ? OR AlbumTitle LIKE ? OR ArtistName LIKE ? " +
                "LIMIT ? " +
                "OFFSET ?";

        search = "%" + search + "%";

        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, search);
            stmt.setString(2, search);
            stmt.setString(3, search);
            stmt.setInt(4, count);
            stmt.setInt(5, count * (page - 1));

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

    public static List<Track> forAlbum(Long albumId) {
        String query = "SELECT * FROM tracks WHERE AlbumId=?";
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setLong(1, albumId);
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

    // Sure would be nice if java supported default parameter values
    public static List<Track> all() {
        return all(0, Integer.MAX_VALUE);
    }

    public static List<Track> all(int page, int count) {
        return all(page, count, "TrackId");
    }

    public static List<Track> all(int page, int count, String orderBy) {
        String query = "SELECT * FROM tracks ORDER BY " + orderBy + " LIMIT ? OFFSET ?";
        return _all(page, count, query);
    }

    public static List<Track> all(int page, int count, String orderBy, String sort) {
        String query = "SELECT * FROM tracks ORDER BY " + orderBy + " "+sort+" LIMIT ? OFFSET ?";
        return _all(page, count, query);
    }


    private static List<Track> _all(int page, int count, String query) {

        try (Connection conn = DB.connect(); PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, count);
            stmt.setInt(2, count * (page - 1));

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

}
