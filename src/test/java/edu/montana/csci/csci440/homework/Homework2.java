package edu.montana.csci.csci440.homework;

import edu.montana.csci.csci440.DBTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Homework2 extends DBTest {

    @Test
    /*
     * Create a view tracksPlus to display the artist, song title, album, and genre for all tracks.
     */
    public void createTracksPlusView(){
        //TODO fill this in
        String query = "CREATE VIEW tracksPlus AS " +
                "SELECT t.TrackId, t.Name as SongTitle, g.Name as GenreName, alb.Title as AlbumTitle, art.Name as ArtistName " +
                "FROM tracks t " +
                "JOIN genres g on t.GenreId = g.GenreId " +
                "JOIN albums alb on t.AlbumId = alb.AlbumId " +
                "JOIN artists art on art.ArtistId = alb.ArtistId";
        executeDDL(query);

        List<Map<String, Object>> results = executeSQL("SELECT * FROM tracksPlus ORDER BY TrackId");
        assertEquals(3503, results.size());
        assertEquals("Rock", results.get(0).get("GenreName"));
        assertEquals("AC/DC", results.get(0).get("ArtistName"));
        assertEquals("For Those About To Rock We Salute You", results.get(0).get("AlbumTitle"));
    }

    @Test
    /*
     * Create a table grammy_infos to track grammy information for an artist.  The table should include
     * a reference to the artist, the album (if the grammy was for an album) and the song (if the grammy was
     * for a song).  There should be a string column indicating if the artist was nominated or won.  Finally,
     * there should be a reference to the grammy_category table
     *
     * Create a table grammy_category
     */
    public void createGrammyInfoTable(){
        //TODO fill these in
        executeDDL("CREATE TABLE grammy_categories (" +
                "GrammyCategoryId INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "Name NVARCHAR(160) UNIQUE " +
                ")");
        executeDDL("CREATE TABLE grammy_infos ( " +
                "GrammyInfoId INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "ArtistId INTEGER NOT NULL, " +
                "AlbumId INTEGER NOT NULL, " +
                "TrackId INTEGER NOT NULL, " +
                "GrammyCategoryId INTEGER NOT NULL, " +
                "Status NVARCHAR(50) NOT NULL CHECK ( Status='Won' OR Status='Nominated'), " +
                "FOREIGN KEY (ArtistId) REFERENCES artists(ArtistId), " +
                "FOREIGN KEY (AlbumId) REFERENCES albums(AlbumId), " +
                "FOREIGN KEY (TrackId) REFERENCES tracks(TrackId), " +
                "FOREIGN KEY (GrammyCategoryId) REFERENCES grammy_categories(GrammyCategoryId)" +
                ")");

        // TEST CODE
        executeUpdate("INSERT INTO grammy_categories(Name) VALUES ('Greatest Ever');");
        Object categoryId = executeSQL("SELECT GrammyCategoryId FROM grammy_categories").get(0).get("GrammyCategoryId");

        executeUpdate("INSERT INTO grammy_infos(ArtistId, AlbumId, TrackId, GrammyCategoryId, Status) VALUES (1, 1, 1, " + categoryId + ",'Won');");

        List<Map<String, Object>> results = executeSQL("SELECT * FROM grammy_infos");
        assertEquals(1, results.size());
        assertEquals(1, results.get(0).get("ArtistId"));
        assertEquals(1, results.get(0).get("AlbumId"));
        assertEquals(1, results.get(0).get("TrackId"));
        assertEquals(1, results.get(0).get("GrammyCategoryId"));
    }

    @Test
    /*
     * Bulk insert five categories of your choosing in the genres table
     */
    public void bulkInsertGenres(){
        Integer before = (Integer) executeSQL("SELECT COUNT(*) as COUNT FROM genres").get(0).get("COUNT");

        //TODO fill this in
        executeUpdate("INSERT INTO genres(Name) VALUES ('Awesome1'), ('Awesome2'), ('Awesome3'), ('Awesome4'), ('Awesome5')");

        Integer after = (Integer) executeSQL("SELECT COUNT(*) as COUNT FROM genres").get(0).get("COUNT");
        assertEquals(before + 5, after);
    }

}
