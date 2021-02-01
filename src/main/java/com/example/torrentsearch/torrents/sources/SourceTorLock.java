package com.example.torrentsearch.torrents.sources;

import com.example.torrentsearch.configurations.SourceCategories;
import com.example.torrentsearch.configurations.SourceConfiguration;
import com.example.torrentsearch.torrents.TorrentDataHolder;
import com.example.torrentsearch.torrents.TorrentSource;
import com.example.torrentsearch.torrents.TorrentValidator;
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
public class SourceTorLock implements TorrentSource {

    @Autowired
    ArrayList<Class<?>> getSources;

    @PostConstruct
    public void fun() {
        getSources.add(SourceTorLock.class);
    }

    final String baseUrl = "https://www.torlock.com";

    @Override
    public TorrentDataHolder[] getTorrents(String searchQuery) {
        ArrayList<TorrentDataHolder> torrentDataHolderArrayList = new ArrayList<>();
        try {
            String url = "https://www.torlock.com/all/torrents/" + URLEncoder.encode(searchQuery, StandardCharsets.UTF_8) + ".html?";
            System.out.println(url);
            Document document = Jsoup.connect(url)
                    .ignoreContentType(true)
                    .ignoreHttpErrors(true)
                    .userAgent(SourceConfiguration.userAgent)
                    .referrer(SourceConfiguration.referrer)
                    .followRedirects(true)
                    .timeout(SourceConfiguration.sourceConnectionTimeout)
                    .get();
            Elements categoryNodes = document.select("table:eq(0) td:eq(0) span");
            Elements titleNodes = document.select("table:eq(0) td:eq(0) b");
            Elements seedsNodes = document.select("table:eq(0) td:eq(3)");
            Elements leechesNodes = document.select("table:eq(0) td:eq(4)");
            Elements sizeNodes = document.select("table:eq(0) td:eq(2)");
            Elements addedNodes = document.select("table:eq(0) td:eq(1)");
            Elements endUrlNodes = document.select("table:eq(0) td:eq(0) a");
            System.out.println(
                    "categoryNodes: " + categoryNodes.size() +
                            "titleNodesSize: " + titleNodes.size() +
                            "seedNodesSize: " + seedsNodes.size() +
                            "leechesNodesSize: " + leechesNodes.size() +
                            "sizeNodesSize: " + sizeNodes.size() +
                            "addedNodes: " + addedNodes.size() +
                            "endUrlNodesSize: " + endUrlNodes.size()
            );
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
                                    SourceTorLock.class.getSimpleName(),
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
            e.printStackTrace();
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
            Elements magnets = document.select("h4 a");
            if (magnets.size() > 0) {
                return magnets.get(0).attr("href");
            }
        } catch (IOException e) {
            System.out.println("IOException at TorrentListGrabber.java while trying to parse magnet link from url " + url);
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
        String cat = element.attr("class").toLowerCase();
        return switch (cat) {
            case "tv1" -> SourceCategories.Movie;
            case "tv2" -> SourceCategories.TV;
            case "tv4" -> SourceCategories.Music;
            case "tv5" -> SourceCategories.Applications;
            default -> SourceCategories.Other;
        };
    }
}
