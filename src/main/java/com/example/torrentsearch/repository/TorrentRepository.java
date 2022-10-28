package com.example.torrentsearch.repository;

import com.example.torrentsearch.torrents.TorrentDataHolder;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface TorrentRepository extends MongoRepository<TorrentDataHolder, String> {
    public List<TorrentDataHolder> findByTitle(String title);

    @Query("{$text: {$search:\"?0\"}}")
    public List<TorrentDataHolder> findByTitleLike(String title);

}
