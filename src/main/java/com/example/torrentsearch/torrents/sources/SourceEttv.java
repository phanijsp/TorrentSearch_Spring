package com.example.torrentsearch.torrents.sources;

import com.example.torrentsearch.torrents.TorrentDataHolder;
import com.example.torrentsearch.torrents.TorrentSource;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

@Service
public class SourceEttv implements TorrentSource {
	@Autowired
	ArrayList<Class<?>> getSources;

	@PostConstruct
	public void fun() {
		getSources.add(SourceEttv.class);
	}

	@Override
	public TorrentDataHolder[] getTorrents(String searchQuery) {

		return new TorrentDataHolder[0];
	}
}
