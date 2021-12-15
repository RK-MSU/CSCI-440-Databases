package edu.montana.csci.csci440.controller;

import edu.montana.csci.csci440.model.Album;
import edu.montana.csci.csci440.util.Web;

import java.util.ArrayList;
import java.util.List;

import static spark.Spark.get;
import static spark.Spark.post;

public class AlbumsController {

    static List<String> sortValues = new ArrayList<String>(List.of("ASC", "DESC"));
    static List<String> orderByValues = new ArrayList<String>();

    public static void init(){
        /* CREATE */
        get("/albums/new", (req, resp) -> {
            Album album = new Album();
            return Web.renderTemplate("templates/albums/new.vm", "album", album);
        });

        post("/albums/new", (req, resp) -> {
            Album album = new Album();
            Web.putValuesInto(album, "Title");
            Web.putValuesInto(album, "ArtistId");
            if (album.create()) {
                Web.message("Created A Album!");
                return Web.redirect("/albums/" + album.getAlbumId());
            } else {
                Web.error("Could Not Create A Album!");
                return Web.renderTemplate("templates/albums/new.vm",
                        "album", album);
            }
        });

        /* READ */
        get("/albums", (req, resp) -> {

            orderByValues.add("AlbumId");
            orderByValues.add("Title");

            String orderBy = req.queryParams("orderBy");
            String sort = req.queryParams("sort");

            if(!orderByValues.contains(orderBy)) {
                orderBy = orderByValues.get(0);
            }
            if(!sortValues.contains(sort)) {
                sort = sortValues.get(0);
            }

//            List<Album> albums = Album.all(Web.getPage(), Web.PAGE_SIZE);
            List<Album> albums = Album.all(Web.getPage(), Web.PAGE_SIZE, orderBy, sort);

            String query_tmpl = "orderBy=%s&sort=%s";
            String id_query_str = "";
            String title_query_str = "";

            // ArtistId col
            if(orderBy.equals(orderByValues.get(0)) && sort.equals(sortValues.get(0))) {
                id_query_str = String.format(query_tmpl, orderByValues.get(0), sortValues.get(1));
            } else {
                id_query_str = String.format(query_tmpl, orderByValues.get(0), sortValues.get(0));
            }

            // Title col
            if(orderBy.equals(orderByValues.get(1)) && sort.equals(sortValues.get(0))) {
                title_query_str = String.format(query_tmpl, orderByValues.get(1), sortValues.get(1));
            } else {
                title_query_str = String.format(query_tmpl, orderByValues.get(1), sortValues.get(0));
            }

            return Web.renderTemplate("templates/albums/index.vm",
                    "albums", albums,
                    "id_q_str", id_query_str,
                    "title_q_str", title_query_str);
        });

        get("/albums/:id", (req, resp) -> {
            Album album = Album.find(Integer.parseInt(req.params(":id")));
            return Web.renderTemplate("templates/albums/show.vm",
                    "album", album);
        });

        /* UPDATE */
        get("/albums/:id/edit", (req, resp) -> {
            Album album = Album.find(Integer.parseInt(req.params(":id")));
            return Web.renderTemplate("templates/albums/edit.vm",
                    "album", album);
        });

        post("/albums/:id", (req, resp) -> {
            Album album = Album.find(Integer.parseInt(req.params(":id")));
            Web.putValuesInto(album, "Title");
            if (album.update()) {
                Web.message("Updated Album!");
                return Web.redirect("/albums/" + album.getAlbumId());
            } else {
                Web.error("Could Not Update Album!");
                return Web.renderTemplate("templates/albums/edit.vm",
                        "album", album);
            }
        });

        /* DELETE */
        get("/albums/:id/delete", (req, resp) -> {
            Album album = Album.find(Integer.parseInt(req.params(":id")));
            album.delete();
            Web.message("Deleted Album " + album.getTitle());
            return Web.redirect("/albums");
        });
    }
}
