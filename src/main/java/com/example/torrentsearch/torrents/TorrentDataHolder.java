package com.example.torrentsearch.torrents;

public class TorrentDataHolder {
    String title;
    String seeds;
    String leeches;
    String size;
    String added;
    String source;
    String sourceUrl;
    String magnet;

    public TorrentDataHolder(String title, String seeds, String leeches, String size, String added, String source, String sourceUrl, String magnet) {
        this.title = title;
        this.seeds = seeds;
        this.leeches = leeches;
        this.size = size;
        this.added = added;
        this.source = source;
        this.sourceUrl = sourceUrl;
        this.magnet = magnet;
    }
}
