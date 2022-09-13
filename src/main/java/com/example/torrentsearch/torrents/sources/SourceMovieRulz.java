package com.example.torrentsearch.torrents.sources;

import com.example.torrentsearch.configurations.SourceCategories;
import com.example.torrentsearch.configurations.SourceConfiguration;
import com.example.torrentsearch.torrents.TorrentDataHolder;
import com.example.torrentsearch.torrents.TorrentSource;
import org.apache.juli.logging.Log;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

@Service
public class SourceMovieRulz implements TorrentSource {
    Logger logger = Logger.getLogger(SourceMovieRulz.class);

    @Autowired
    ArrayList<Class<?>> getSources;

    @PostConstruct
    public void fun(){getSources.add(SourceMovieRulz.class);}

    final String baseUrl = "https://7movierulz.ag/";

    @Override
    public TorrentDataHolder[] getTorrents(String searchQuery) {
        ArrayList<TorrentDataHolder> torrentDataHolderArrayList = new ArrayList<>();
        try{
            String url = baseUrl+"?s="+ URLEncoder.encode(searchQuery, StandardCharsets.UTF_8);

            Document document = Jsoup.connect(url)
                    .ignoreContentType(true)
                    .ignoreHttpErrors(true)
                    .userAgent(SourceConfiguration.userAgent)
                    .referrer(SourceConfiguration.referrer)
                    .followRedirects(true)
                    .timeout(SourceConfiguration.sourceConnectionTimeout)
                    .get();

            Elements movieNodes = document.select(".featured .cont_display a[href]");
            for(Element element: movieNodes){
                String endUrl;
                if(element!=null && (endUrl=element.attr("href"))!=null){
                    torrentDataHolderArrayList.addAll(
                            getEndUrlTorrents(endUrl)
                    );
                }
            }
            logger.debug(movieNodes.size());

        }catch (Exception e){
            logger.error(e);
        }
        return torrentDataHolderArrayList.toArray(TorrentDataHolder[]::new);
    }

    public ArrayList<TorrentDataHolder> getEndUrlTorrents(String url){
        ArrayList<TorrentDataHolder> torrentDataHolderArrayList = new ArrayList<>();
        try{
            Document document = Jsoup.connect(url)
                    .ignoreContentType(true)
                    .ignoreHttpErrors(true)
                    .userAgent(SourceConfiguration.userAgent)
                    .referrer(SourceConfiguration.referrer)
                    .followRedirects(true)
                    .timeout(SourceConfiguration.sourceConnectionTimeout)
                    .get();
            Elements elements = document.select("[href^=magnet:]");
            Elements sizeNodes = elements.select("small:eq(1)");
            if(elements.size()==sizeNodes.size()){
                for(int i = 0 ; i < elements.size() ; i++){
                    String title = getMagnetTitle(elements.get(i).attr("href"));
                    if(title!=null)
                    torrentDataHolderArrayList.add(new TorrentDataHolder(
                            SourceCategories.Movie,
                            title,
                            "0",
                            "0",
                            sizeNodes.get(i).text(),
                            "NA",
                            SourceMovieRulz.class.getSimpleName(),
                            url,
                            SourceConfiguration.addTrackers(elements.get(i).attr("href"))
                    ));
                }
            }
        }catch (Exception e){
            logger.error(e);
        }
        return torrentDataHolderArrayList;
    }

    private String getMagnetTitle(String href) {
        String[] arr = href.split("&");
        for(String s: arr){
            if(s.startsWith("dn=")){
                try {
                    return URLDecoder.decode(s.replace("dn=",""),StandardCharsets.UTF_8);
                }catch (Exception e){
                    logger.error(e);
                }
            }
        }
        return null;
    }
}
