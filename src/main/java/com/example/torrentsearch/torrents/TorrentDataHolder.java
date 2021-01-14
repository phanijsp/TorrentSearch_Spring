package com.example.torrentsearch.torrents;

import org.json.JSONObject;

public class TorrentDataHolder {
	String category;
	String title;
	String seeds;
	String leeches;
	String size;
	String added;
	String source;
	String sourceUrl;
	String magnet;

	public TorrentDataHolder(String category, String title, String seeds, String leeches, String size, String added, String source, String sourceUrl, String magnet) {
		this.category = category;
		this.title = title;
		this.seeds = seeds;
		this.leeches = leeches;
		this.size = size;
		this.added = added;
		this.source = source;
		this.sourceUrl = sourceUrl;
		this.magnet = magnet;
	}

	public JSONObject getDataInJSON() {
		return new JSONObject()
				.put("category", category)
                .put("title", title)
                .put("seeds", seeds)
                .put("leeches", leeches)
                .put("size", size)
                .put("added", added)
                .put("source", source)
                .put("sourceUrl", sourceUrl)
                .put("magnet", magnet);
	}
}
