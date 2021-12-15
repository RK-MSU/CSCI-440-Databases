package edu.montana.csci.csci440.controller;

import edu.montana.csci.csci440.model.Artist;
import edu.montana.csci.csci440.model.Track;
import edu.montana.csci.csci440.util.Web;

import java.util.ArrayList;
import java.util.List;

import static spark.Spark.get;
import static spark.Spark.post;

public class TracksController {

    static List<String> sortValues = new ArrayList<String>(List.of("ASC", "DESC"));
    static List<String> orderByValues = new ArrayList<String>();

    private static void setupSortItems() {
        orderByValues.add("TrackId");
        orderByValues.add("Milliseconds");
        orderByValues.add("Bytes");
        orderByValues.add("Name");
    }

    public static void init() {
        /* CREATE */
        get("/tracks/new", (req, resp) -> {
            Track track = new Track();
            return Web.renderTemplate("templates/tracks/new.vm", "album", track);
        });

        post("/tracks/new", (req, resp) -> {
            Track track = new Track();
            Web.putValuesInto(track, "Name", "Milliseconds", "Bytes", "UnitPrice", "AlbumId");
            if (track.create()) {
                Web.message("Created A Track!");
                return Web.redirect("/tracks/" + track.getTrackId());
            } else {
                Web.error("Could Not Create A Track!");
                return Web.renderTemplate("templates/tracks/new.vm",
                        "track", track);
            }
        });

        /* READ */
        get("/tracks", (req, resp) -> {

            setupSortItems();

            String search = req.queryParams("q");
            String orderBy = req.queryParams("o");
            String sort = req.queryParams("s");

            if(!orderByValues.contains(orderBy)) {
                orderBy = orderByValues.get(0);
            }
            if(!sortValues.contains(sort)) {
                sort = sortValues.get(0);
            }

            List<Track> tracks;
            if (search != null) {
                tracks = Track.search(Web.getPage(), Web.PAGE_SIZE, orderBy, search);
            } else {
//                tracks = Track.all(Web.getPage(), Web.PAGE_SIZE, orderBy);
                tracks = Track.all(Web.getPage(), Web.PAGE_SIZE, orderBy, sort);
            }

            String query_tmpl = "o=%s&s=%s";
            String id_query_str = "";
            String milliseconds_query_str = "";
            String bytes_query_str = "";
            String name_query_str = "";

            // TrackId col
            if(orderBy.equals(orderByValues.get(0)) && sort.equals(sortValues.get(0))) {
                id_query_str = String.format(query_tmpl, orderByValues.get(0), sortValues.get(1));
            } else {
                id_query_str = String.format(query_tmpl, orderByValues.get(0), sortValues.get(0));
            }

            // Milliseconds col
            if(orderBy.equals(orderByValues.get(1)) && sort.equals(sortValues.get(0))) {
                milliseconds_query_str = String.format(query_tmpl, orderByValues.get(1), sortValues.get(1));
            } else {
                milliseconds_query_str = String.format(query_tmpl, orderByValues.get(1), sortValues.get(0));
            }

            // Bytes col
            if(orderBy.equals(orderByValues.get(2)) && sort.equals(sortValues.get(0))) {
                bytes_query_str = String.format(query_tmpl, orderByValues.get(2), sortValues.get(1));
            } else {
                bytes_query_str = String.format(query_tmpl, orderByValues.get(2), sortValues.get(0));
            }

            // Name col
            if(orderBy.equals(orderByValues.get(3)) && sort.equals(sortValues.get(0))) {
                name_query_str = String.format(query_tmpl, orderByValues.get(3), sortValues.get(1));
            } else {
                name_query_str = String.format(query_tmpl, orderByValues.get(3), sortValues.get(0));
            }


            long totalTracks = Track.count();

            return Web.renderTemplate("templates/tracks/index.vm",
                    "tracks", tracks,
                    "totalTracks", totalTracks,
                    "id_q_str", id_query_str,
                    "milliseconds_q_str", milliseconds_query_str,
                    "bytes_q_str", bytes_query_str,
                    "name_q_str", name_query_str);
        });

        get("/tracks/search", (req, resp) -> {
            String search = req.queryParams("q");
            List<Track> tracks;

            tracks = Track.advancedSearch(Web.getPage(), Web.PAGE_SIZE,
                    search,
                    Web.integerOrNull("ArtistId"),
                    Web.integerOrNull("AlbumId"),
                    Web.integerOrNull("max"),
                    Web.integerOrNull("min"));

            return Web.renderTemplate("templates/tracks/search.vm",
                    "tracks", tracks);
        });

        /* VIEW */
        get("/tracks/:id", (req, resp) -> {
            Track track = Track.find(Integer.parseInt(req.params(":id")));
            return Web.renderTemplate("templates/tracks/show.vm",
                    "track", track);
        });

        /* UPDATE */
        get("/tracks/:id/edit", (req, resp) -> {
            Track track = Track.find(Integer.parseInt(req.params(":id")));
            return Web.renderTemplate("templates/tracks/edit.vm",
                    "track", track);
        });

        post("/tracks/:id", (req, resp) -> {
            Track track = Track.find(Integer.parseInt(req.params(":id")));
            Web.putValuesInto(track, "Name", "Milliseconds", "Bytes", "UnitPrice");
            if (track.update()) {
                Web.message("Updated Track!");
                return Web.redirect("/tracks/" + track.getTrackId());
            } else {
                Web.error("Could Not Update Track!");
                return Web.renderTemplate("templates/tracks/edit.vm",
                        "track", track);
            }
        });

        /* DELETE */
        get("/tracks/:id/delete", (req, resp) -> {
            Track track = Track.find(Integer.parseInt(req.params(":id")));
            track.delete();
            Web.message("Deleted Track " + track.getName());
            return Web.redirect("/tracks");
        });
    }
}
