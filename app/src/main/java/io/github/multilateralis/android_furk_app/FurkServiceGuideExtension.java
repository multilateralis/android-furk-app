package io.github.multilateralis.android_furk_app;
import android.content.Intent;

import com.battlelancer.seriesguide.api.Action;
import com.battlelancer.seriesguide.api.Episode;
import com.battlelancer.seriesguide.api.Movie;
import com.battlelancer.seriesguide.api.SeriesGuideExtension;

public class FurkServiceGuideExtension extends SeriesGuideExtension {

    public FurkServiceGuideExtension() {
        super("FurkExtension");
    }

    @Override
    protected void onRequest(int episodeIdentifier, Episode episode) {

        Intent intent = new Intent(this, SearchActivity.class);
        String query = buildEpisodeQuery(episode);
        intent.putExtra("query",query);
        publishAction(new Action.Builder("Furk.net search", episodeIdentifier)
                .viewIntent(intent)
                .build());
    }

    protected void onRequest(int movieIdentifier, Movie movie) {

        Intent intent = new Intent(this, SearchActivity.class);
        String query = buildMovieQuery(movie);
        intent.putExtra("query",query);
        publishAction(new Action.Builder("Furk.net search", movieIdentifier)
                .viewIntent(intent)
                .build());
    }

    private String buildMovieQuery(Movie movie) {
        return movie.getTitle() + " " + Integer.toString(movie.getReleaseDate().getYear() + 1900);
    }

    private String buildEpisodeQuery(Episode episode) {
        String query = episode.getShowTitle() +" S";

        if(episode.getSeason() > 9)
            query += episode.getSeason().toString();
        else
            query += "0"+episode.getSeason().toString();

        query += "E";

        if(episode.getNumber() > 9)
            query += episode.getNumber().toString();
        else
            query += "0"+episode.getNumber().toString();

        return query;
    }

}