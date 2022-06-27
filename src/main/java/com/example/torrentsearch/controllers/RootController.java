package com.example.torrentsearch.controllers;

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

    static class CircularQueue<E> extends LinkedList<E> {
        private int capacity = 10;

        public CircularQueue(int capacity){
            this.capacity = capacity;
        }

        @Override
        public boolean add(E e) {
            if(size() >= capacity)
                removeFirst();
            return super.add(e);
        }
    }

    private final Queue<Map.Entry<String, String>> responseQueue = new CircularQueue<>(5);


    @PostMapping("/search")
    public String index(@RequestParam(value = "type") String type, @RequestParam(value = "query") String query) {
        long start = System.currentTimeMillis();

        for(Entry<String, String> entry : responseQueue){
            if(entry.getKey().equals(query)){
                System.out.println("Found in cache... "+query+" Queue size... "+responseQueue.size());
                return entry.getValue();
            }
        }

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
                break;
        }

        Entry<String, String> entry = new AbstractMap.SimpleEntry<String, String>(query, responseJSON.toString());
        responseQueue.add(entry);
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
