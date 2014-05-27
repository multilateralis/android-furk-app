package com.simple.furk;
import android.content.Intent;
import android.net.Uri;
import android.text.Html;

import com.battlelancer.seriesguide.api.Action;
import com.battlelancer.seriesguide.api.Episode;
import com.battlelancer.seriesguide.api.SeriesGuideExtension;

import java.net.URLEncoder;

public class FurkServiceGuideExtension extends SeriesGuideExtension {

    public FurkServiceGuideExtension() {
        super("FurkExtension");
    }

    @Override
    protected void onRequest(int episodeIdentifier, Episode episode) {

        Intent intent = new Intent(this, MainActivity.class);
        intent.setAction("com.simple.furk.TORRENT_SEARCH");
        String query = buildEpisodeQuery(episode);
        String episodeURI =  "http://kickass.to/usearch/" + URLEncoder.encode(query) + "/?field=seeders&sorder=desc&rss=1";
        intent.setData(Uri.parse(episodeURI));
        intent.putExtra("query",query);
        publishAction(new Action.Builder("Furk.net search", episodeIdentifier)
                .viewIntent(intent)
                .build());
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