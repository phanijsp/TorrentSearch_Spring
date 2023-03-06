package com.example.torrentsearch.torrents.sources;

import com.example.torrentsearch.configurations.SourceCategories;
import com.example.torrentsearch.configurations.SourceConfiguration;
import com.example.torrentsearch.torrents.TorrentDataHolder;
import com.example.torrentsearch.torrents.TorrentSource;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;


@Service
public class SourceRarbg implements TorrentSource {
    Logger logger = Logger.getLogger(SourceRarbg.class);

    @Autowired
    ArrayList<Class<?>> getSources;

    @PostConstruct
    public void fun(){
        getSources.add(SourceRarbg.class);
    }

    final String baseUrl = "https://www.rarbgproxy.to";

    @Override
    public TorrentDataHolder[] getTorrents(String searchQuery) {
        ArrayList<TorrentDataHolder> torrentDataHolderArrayList = new ArrayList<>();
        try{
            String url = baseUrl+"/search/?search="+ URLEncoder.encode(searchQuery, StandardCharsets.UTF_8);

            Document document = Jsoup.connect(url)
                    .ignoreContentType(true)
                    .ignoreHttpErrors(true)
                    .userAgent(SourceConfiguration.userAgent)
                    .referrer(SourceConfiguration.referrer)
                    .followRedirects(true)
                    .timeout(SourceConfiguration.sourceConnectionTimeout)
                    .get();

            Elements titleNodes = document.select(".table2ta_rarbgproxy td:eq(1)");
            Elements categoryNodes = document.select(".table2ta_rarbgproxy td:eq(2)");
            Elements seedsNodes = document.select(".table2ta_rarbgproxy td:eq(5)");
            Elements leechesNodes = document.select(".table2ta_rarbgproxy td:eq(6)");
            Elements sizeNodes = document.select(".table2ta_rarbgproxy td:eq(4)");
            Elements addedNodes = document.select(".table2ta_rarbgproxy td:eq(3)");
            Elements endUrlNodes = document.select(".table2ta_rarbgproxy td:eq(1) a");

            ArrayList<Thread> magnetFetchers = new ArrayList<>();

            int maxPerSite = SourceConfiguration.maxPerSite;
            if(titleNodes.size()<maxPerSite){
                maxPerSite = titleNodes.size();
            }

            for (int i = 0; i < maxPerSite; i++) {
                int finalI = i;
                Thread MagnetFetcher = new Thread(() -> {
                    String magnet = getMagnet(endUrlNodes.get(finalI).attr("href"));
                    if (magnet.startsWith("magnet")) {
                        SourceConfiguration.addTrackers(magnet);
                        torrentDataHolderArrayList.add(new TorrentDataHolder(
                                getCategory(categoryNodes.get(finalI).text()),
                                titleNodes.get(finalI).text(),
                                seedsNodes.get(finalI).text(),
                                leechesNodes.get(finalI).text(),
                                sizeNodes.get(finalI).text(),
                                addedNodes.get(finalI).text(),
                                SourceRarbg.class.getSimpleName(),
                                appendBaseEndUrls(baseUrl, endUrlNodes.get(finalI).attr("href")),
                                magnet
                        ));
                    }
                });
                MagnetFetcher.start();
                magnetFetchers.add(MagnetFetcher);
            }
            for (Thread t : magnetFetchers) {
                t.join();
            }
        }
        catch (Exception e){
            logger.error(e);
        }
        return torrentDataHolderArrayList.toArray(TorrentDataHolder[]::new);
    }

    public String appendBaseEndUrls(String baseUrl, String endUrl) {
        if (endUrl.startsWith(baseUrl)) {
            return endUrl;
        } else {
            return baseUrl + endUrl;
        }
    }
    private String getMagnet(String endUrl) {
        String magnetLink = "";
        String url = appendBaseEndUrls(baseUrl, endUrl);
        try {
            Document document = Jsoup.connect(url).ignoreContentType(true)
                    .userAgent(SourceConfiguration.userAgent)
                    .referrer(SourceConfiguration.referrer)
                    .followRedirects(true)
                    .timeout(SourceConfiguration.magnetConnectionTimeout)
                    .get();
            Elements magnets = document.select("[href^=magnet]");
            if (magnets.size() > 0) {
                return magnets.get(0).attr("href");
            }
        } catch (IOException e) {
            logger.error(e);
        }
        return magnetLink;
    }

    public String getCategory(String cat) {
        if (cat.startsWith("Music")) {
            return SourceCategories.Music;
        } else if (cat.startsWith("Games")) {
            return SourceCategories.Applications;
        }
        else if(cat.startsWith("Movies")){
            return SourceCategories.Movie;
        }else if(cat.startsWith("TV")){
            return SourceCategories.TV;
        } else if (cat.startsWith("Apps")) {
            return SourceCategories.Applications;
        } else {
            return SourceCategories.Other;
        }
    }

}
