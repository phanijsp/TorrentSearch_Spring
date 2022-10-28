package com.example.torrentsearch.services;

import com.example.torrentsearch.torrents.TorrentDataHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.data.mongodb.core.query.TextQuery;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TorrentService {

    @Autowired
    MongoTemplate mongoTemplate;

    public List<TorrentDataHolder> getTorrents(String searchQuery){
        TextQuery textQuery = TextQuery.queryText(new TextCriteria().matchingAny(searchQuery)).sortByScore();
        textQuery.limit(30);
        return mongoTemplate.find(textQuery,TorrentDataHolder.class,"torrentDataHolder");

    }

    public void save(TorrentDataHolder torrentDataHolder){
        mongoTemplate.save(torrentDataHolder);
    }

}
