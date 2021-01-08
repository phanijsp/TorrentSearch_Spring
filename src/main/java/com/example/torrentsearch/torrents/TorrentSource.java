package com.example.torrentsearch.torrents;

public interface TorrentSource {
    TorrentDataHolder[] getTorrents(String searchQuery);
}
