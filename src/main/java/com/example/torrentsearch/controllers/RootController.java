package com.example.torrentsearch.controllers;

import com.example.torrentsearch.torrents.TorrentDataHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

@RestController
public class RootController {

    @Autowired
    ArrayList<Class<?>> getSources;

    @GetMapping("/")
    public String index()  {
        StringBuilder response = new StringBuilder();
        for (Class<?> getSource : getSources) {
            try {
                TorrentDataHolder[] dataHolders = (TorrentDataHolder[]) getSource
                        .getMethod("getTorrents", String.class)
                        .invoke(getSource.getConstructor().newInstance(), "Aditya");
                response.append(dataHolders.length);
            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return response.toString();

    }


}
