package com.example.torrentsearch.controllers;

import com.example.torrentsearch.repository.TorrentRepository;
import com.example.torrentsearch.torrents.TorrentDataHolder;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.URLDecoder;
import java.util.*;
import java.util.Map.Entry;

@RestController
public class RootController implements ErrorController {
    Logger logger = Logger.getLogger(RootController.class);
    @Autowired
    ArrayList<Class<?>> getSources;

    @Autowired
    private TorrentRepository torrentRepository;

    @PostMapping("/express")
    public String expressIndex(@RequestParam(value = "query") String query){
        long start = System.currentTimeMillis();
        JSONObject responseJSON = new JSONObject();
        List<TorrentDataHolder> torrentDataHolders = torrentRepository.findByTitleLike(query);
        JSONArray torrentsArrayJSON = new JSONArray();
        for(TorrentDataHolder dataHolder: torrentDataHolders){
            torrentsArrayJSON.put(dataHolder.getDataInJSON());
        }
        long end = System.currentTimeMillis();
        float sec = (end - start) / 1000F;
        responseJSON
                .put("time", sec)
                .put("torrents", torrentsArrayJSON)
                .put("sources", "");
        return responseJSON.toString();
    }

    @PostMapping("/search")
    public String index(@RequestParam(value = "type") String type, @RequestParam(value = "query") String queryX) {
        long start = System.currentTimeMillis();
        String query = queryX.trim();

        JSONObject responseJSON = new JSONObject();

        switch (type) {
            case "search":
                try {
                    logger.debug("Receieved a search request for: "+URLDecoder.decode(query, "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    logger.error(e);
                }
                JSONArray torrentsArrayJSON = new JSONArray();
                JSONObject sourcesJSON = new JSONObject();
                ArrayList<Thread> sourceRunners = new ArrayList<>();
                for (Class<?> getSource : getSources) {
                    Thread t = new Thread(() -> {
                        try {
                            TorrentDataHolder[] dataHolders = (TorrentDataHolder[]) getSource
                                    .getMethod("getTorrents", String.class)
                                    .invoke(getSource.getConstructor().newInstance(), URLDecoder.decode(query, "UTF-8"));
                            sourcesJSON.put(getSource.getSimpleName(), String.valueOf(dataHolders.length));
                            for (TorrentDataHolder dataHolder : dataHolders) {
                                torrentsArrayJSON.put(dataHolder.getDataInJSON());
                                try {
                                    torrentRepository.save(dataHolder);
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                            }
                        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException | UnsupportedEncodingException e) {
                            logger.error(e);
                        }
                    });
                    t.start();
                    sourceRunners.add(t);
                }

                for (Thread sourceRunner : sourceRunners) {
                    try {
                        sourceRunner.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                long end = System.currentTimeMillis();
                float sec = (end - start) / 1000F;
                responseJSON
                        .put("time", sec)
                        .put("torrents", torrentsArrayJSON)
                        .put("sources", sourcesJSON);
                System.out.println("For query :"+query);
                System.out.println(sourcesJSON);
                break;
        }

        return responseJSON.toString();

    }

    private static final String PATH = "/error";

    @RequestMapping(value = PATH)
    public String error() {
        return "<html><body><p style=\"font-size: 696px\">69</p></body></html>";
    }

    @Override
    public String getErrorPath() {
        return PATH;
    }

}
