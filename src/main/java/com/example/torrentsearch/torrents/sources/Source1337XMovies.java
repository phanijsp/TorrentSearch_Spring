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

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;


@Service
public class Source1337XMovies implements TorrentSource {
    Logger logger = Logger.getLogger(Source1337XMovies.class);

    @Autowired
    ArrayList<Class<?>> getSources;

    @PostConstruct
    public void fun(){
        getSources.add(Source1337XMovies.class);
    }

    final String baseUrl = "https://1337x.to";

    @Override
    public TorrentDataHolder[] getTorrents(String searchQuery) {
        ArrayList<TorrentDataHolder> torrentDataHolderArrayList = new ArrayList<>();
        try{
            String url = baseUrl+"/search/"+ URLEncoder.encode(searchQuery, StandardCharsets.UTF_8) +"/Movies/1/";

            Document document = Jsoup.connect(url)
                    .ignoreContentType(true)
                    .ignoreHttpErrors(true)
                    .userAgent(SourceConfiguration.userAgent)
                    .referrer(SourceConfiguration.referrer)
                    .followRedirects(true)
                    .timeout(SourceConfiguration.sourceConnectionTimeout)
                    .get();

            Elements titleNodes = document.select("tr td:eq(0) a:eq(1)");
            Elements seedsNodes = document.select("tr td:eq(1)");
            Elements leechesNodes = document.select("tr td:eq(2)");
            List<TextNode> sizeNodes = document.select("tr td:eq(4)").textNodes();
            Elements addedNodes = document.select("tr td:eq(3)");
            Elements endUrlNodes = document.select("tr td:eq(0) a:eq(1)");

            int maxPerSite = SourceConfiguration.maxPerSite;
            if(titleNodes.size()<maxPerSite){
                maxPerSite = titleNodes.size();
            }
            ArrayList<Thread> magnetFetchers = new ArrayList<>();
            for (int i = 0; i < maxPerSite; i++) {
                int finalI = i;
                Thread MagnetFetcher = new Thread(() -> {
                    String magnet = getMagnet(endUrlNodes.get(finalI).attr("href"));
                    if (magnet.startsWith("magnet")) {
                        SourceConfiguration.addTrackers(magnet);
                        torrentDataHolderArrayList.add(new TorrentDataHolder(
                                SourceCategories.Movie,
                                titleNodes.get(finalI).text(),
                                seedsNodes.get(finalI).text(),
                                leechesNodes.get(finalI).text(),
                                sizeNodes.get(finalI).text(),
                                addedNodes.get(finalI).text(),
                                Source1337XMovies.class.getSimpleName(),
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

}
