package com.example.torrentsearch.torrents.sources;

import com.example.torrentsearch.torrents.TorrentDataHolder;
import com.example.torrentsearch.torrents.TorrentSource;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;

@Component
public class SourceEttv{
    @Autowired
    ArrayList<Class<?>> getSources;

    @PostConstruct
    public void fun(){
        System.out.println("In Fun");
        getSources.add(SourceEttv.class);
    }

    public String boom(){
        return "Kaboom!";
    }

}
