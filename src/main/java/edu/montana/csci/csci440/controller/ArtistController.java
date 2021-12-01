package edu.montana.csci.csci440.controller;

import edu.montana.csci.csci440.model.Artist;
import edu.montana.csci.csci440.model.Track;
import edu.montana.csci.csci440.util.Web;

import java.util.ArrayList;
import java.util.List;

import static spark.Spark.get;
import static spark.Spark.post;

public class ArtistController {

    static List<String> sortValues = new ArrayList<String>(List.of("ASC", "DESC"));
    static List<String> orderByValues = new ArrayList<String>();

    public static void init() {
        /* CREATE */
        get("/artists/new", (req, resp) -> {
            Artist artist = new Artist();
            return Web.renderTemplate("templates/artists/new.vm", "artist", artist);
        });

        post("/artists/new", (req, resp) -> {
            Artist artist = new Artist();
            Web.putValuesInto(artist, "Name");
            if (artist.create()) {
                Web.message("Created An Artist!");
                return Web.redirect("/artists/" + artist.getArtistId());
            } else {
                Web.error("Could Not Create An Artist!");
                return Web.renderTemplate("templates/artists/new.vm",
                        "artist", artist);
            }
        });

        /* READ */
        get("/artists", (req, resp) -> {

            orderByValues.add("ArtistId");
            orderByValues.add("Name");

            String orderBy = req.queryParams("orderBy");
            String sort = req.queryParams("sort");

            if(!orderByValues.contains(orderBy)) {
                orderBy = orderByValues.get(0);
            }
            if(!sortValues.contains(sort)) {
                sort = sortValues.get(0);
            }


            List<Artist> artists = Artist.all(Web.getPage(), Web.PAGE_SIZE, orderBy, sort);

            String query_tmpl = "orderBy=%s&sort=%s";
            String id_query_str = "";
            String name_query_str = "";

            // ArtistId col
            if(orderBy.equals(orderByValues.get(0)) && sort.equals(sortValues.get(0))) {
                id_query_str = String.format(query_tmpl, orderByValues.get(0), sortValues.get(1));
            } else {
                id_query_str = String.format(query_tmpl, orderByValues.get(0), sortValues.get(0));
            }

            // Name col
            if(orderBy.equals(orderByValues.get(1)) && sort.equals(sortValues.get(0))) {
                name_query_str = String.format(query_tmpl, orderByValues.get(1), sortValues.get(1));
            } else {
                name_query_str = String.format(query_tmpl, orderByValues.get(1), sortValues.get(0));
            }

            return Web.renderTemplate("templates/artists/index.vm",
                    "artists", artists,
                    "id_q_str", id_query_str,
                    "name_q_str", name_query_str);
        });

        get("/artists/:id", (req, resp) -> {
            Artist artist = Artist.find(Integer.parseInt(req.params(":id")));
            return Web.renderTemplate("templates/artists/show.vm",
                    "artist", artist);
        });

        /* UPDATE */
        get("/artists/:id/edit", (req, resp) -> {
            Artist artist = Artist.find(Integer.parseInt(req.params(":id")));
            return Web.renderTemplate("templates/artists/edit.vm",
                    "artist", artist);
        });

        post("/artists/:id", (req, resp) -> {
            Artist artist = Artist.find(Integer.parseInt(req.params(":id")));
            Web.putValuesInto(artist, "Name");
            if (artist.update()) {
                Web.message("Updated Artist!");
                return Web.redirect("/artists/" + artist.getArtistId());
            } else {
                Web.error("Could Not Update Artist!");
                return Web.renderTemplate("templates/artists/edit.vm",
                        "artist", artist);
            }
        });

        /* DELETE */
        get("/artists/:id/delete", (req, resp) -> {
            Artist artist = Artist.find(Integer.parseInt(req.params(":id")));
            artist.delete();
            Web.message("Deleted Artist " + artist.getName());
            return Web.redirect("/artists");
        });
    }
}
