package com.example.torrentsearch.torrents.sources;


import com.example.torrentsearch.configurations.SourceCategories;
import com.example.torrentsearch.configurations.SourceConfiguration;
import com.example.torrentsearch.torrents.TorrentDataHolder;
import com.example.torrentsearch.torrents.TorrentSource;
import com.example.torrentsearch.torrents.TorrentValidator;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

@Service
public class SourceEttv implements TorrentSource {

    Logger logger = Logger.getLogger(SourceEttv.class);
    @Autowired
    ArrayList<Class<?>> getSources;

    @PostConstruct
    public void fun() {
        getSources.add(SourceEttv.class);
    }

    final String baseUrl = "https://www.ettvcentral.com";

    @Override
    public TorrentDataHolder[] getTorrents(String searchQuery) {
        ArrayList<TorrentDataHolder> torrentDataHolderArrayList = new ArrayList<>();
        try {
            String url = "https://www.ettvcentral.com/torrents-search.php?search=" + URLEncoder.encode(searchQuery, StandardCharsets.UTF_8);
            Document document = Jsoup.connect(url)
                    .ignoreContentType(true)
                    .ignoreHttpErrors(true)
                    .userAgent(SourceConfiguration.userAgent)
                    .referrer(SourceConfiguration.referrer)
                    .followRedirects(true)
                    .timeout(SourceConfiguration.sourceConnectionTimeout)
                    .get();
            Elements categoryNodes = document.select("tbody tr td:eq(0) a img");
            Elements titleNodes = document.select("tbody tr td:eq(1) a");
            Elements seedsNodes = document.select("tbody tr td:eq(5)");
            Elements leechesNodes = document.select("tbody tr td:eq(6)");
            Elements sizeNodes = document.select("tbody tr td:eq(3)");
            Elements addedNodes = document.select("tbody tr td:eq(2)");
            Elements endUrlNodes = document.select("tbody tr td:eq(1) a");

            ArrayList<Thread> magnetFetchers = new ArrayList<>();
            if (TorrentValidator.validate(new Elements[]{categoryNodes, titleNodes, seedsNodes, leechesNodes, sizeNodes, addedNodes, endUrlNodes})) {
                int maxPerSite = SourceConfiguration.maxPerSite;
                if(titleNodes.size()<maxPerSite){
                    maxPerSite = titleNodes.size();
                }
                for (int i = 0; i < maxPerSite; i++) {
                    int finalI = i;
                    Thread MagnetFetcher = new Thread(() -> {
                        String magnet = getMagnet(endUrlNodes.get(finalI).attr("href"));
                        if (magnet.startsWith("magnet")) {
                            torrentDataHolderArrayList.add(new TorrentDataHolder(
                                    getCategory(categoryNodes.get(finalI)),
                                    titleNodes.get(finalI).text(),
                                    seedsNodes.get(finalI).text(),
                                    leechesNodes.get(finalI).text(),
                                    sizeNodes.get(finalI).text(),
                                    addedNodes.get(finalI).text(),
                                    SourceEttv.class.getSimpleName(),
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
        } catch (IOException | InterruptedException e) {
            logger.error(e);
        }
        return torrentDataHolderArrayList.toArray(TorrentDataHolder[]::new);
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
            Elements magnets = document.select("#downloadbox a");
            if (magnets.size() > 0) {
                return magnets.get(0).attr("href");
            }
        } catch (IOException e) {
            logger.error(e);
        }
        return magnetLink;
    }

    public String appendBaseEndUrls(String baseUrl, String endUrl) {
        if (endUrl.startsWith(baseUrl)) {
            return endUrl;
        } else {
            return baseUrl + endUrl;
        }
    }

    public String getCategory(Element element) {
        String cat = element.attr("src").toLowerCase();
        if (cat.contains("/tv")) {
            return SourceCategories.TV;
        } else if (cat.contains("/movie")) {
            return SourceCategories.Movie;
        } else if (cat.contains("/software")) {
            return SourceCategories.Applications;
        } else if (cat.contains("/music")) {
            return SourceCategories.Music;
        } else {
            return SourceCategories.Other;
        }
    }
}
