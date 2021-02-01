package com.example.torrentsearch.torrents.sources;

import com.example.torrentsearch.configurations.SourceCategories;
import com.example.torrentsearch.configurations.SourceConfiguration;
import com.example.torrentsearch.torrents.TorrentDataHolder;
import com.example.torrentsearch.torrents.TorrentValidator;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

@Service
public class SourcePiratesBay {
	@Autowired
	ArrayList<Class<?>> getSources;

	@PostConstruct
	public void fun() {
		getSources.add(SourcePiratesBay.class);
	}

	final String baseUrl = "https://piratebay.live/";


	public TorrentDataHolder[] getTorrents(String searchQuery) {
		ArrayList<TorrentDataHolder> torrentDataHolderArrayList = new ArrayList<>();
		try {
			String url = "https://piratebay.live/search/" + convertSearchQuery(searchQuery);
			System.out.println(url);
			Document document = Jsoup.connect(url)
					.ignoreContentType(true)
					.ignoreHttpErrors(true)
					.userAgent(SourceConfiguration.userAgent)
					.referrer(SourceConfiguration.referrer)
					.followRedirects(true)
					.timeout(SourceConfiguration.sourceConnectionTimeout)
					.get();
			Elements categoryNodes = document.select("#searchResult tbody td:eq(0) center");
			Elements titleNodes = document.select("#searchResult .detName a");
			Elements seedsNodes = document.select("#searchResult tbody tr td:eq(2)");
			Elements leechesNodes = document.select("#searchResult tbody tr td:eq(3)");
			Elements sizeNodes = document.select("#searchResult tbody tr td:eq(1) font");
			Elements addedNodes = sizeNodes;
			Elements endUrlNodes = titleNodes;

			System.out.println(
					"categoryNodesSize: " + categoryNodes.size() +
							"titleNodesSize: " + titleNodes.size() +
							"seedNodesSize: " + seedsNodes.size() +
							"leechesNodesSize: " + leechesNodes.size() +
							"sizeNodesSize: " + sizeNodes.size() +
							"addedNodes: " + addedNodes.size() +
							"endUrlNodesSize: " + endUrlNodes.size()
			);

			if (TorrentValidator.validate(new Elements[]{categoryNodes, titleNodes, seedsNodes, leechesNodes, sizeNodes, addedNodes, endUrlNodes})) {
				Elements magnets = document.select("#searchResult tbody tr td:eq(1) :eq(1)");
				for (int i = 0; i < titleNodes.size(); i++) {
					String magnet = magnets.get(i).attr("href");
					if (magnet.startsWith("magnet")) {
						torrentDataHolderArrayList.add(new TorrentDataHolder(
								getCategory(categoryNodes.get(i).text()),
								titleNodes.get(i).text(),
								seedsNodes.get(i).text(),
								leechesNodes.get(i).text(),
								getSize(sizeNodes.get(i).text()),
								getAdded(addedNodes.get(i).text()),
								SourcePiratesBay.class.getSimpleName(),
								appendBaseEndUrls(baseUrl, endUrlNodes.get(i).attr("href")),
								magnet
						));
					}

				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return torrentDataHolderArrayList.toArray(TorrentDataHolder[]::new);
	}

	public String getCategory(String cat) {
		if (cat.startsWith("Audio")) {
			return SourceCategories.Music;
		} else if (cat.startsWith("Games")) {
			return SourceCategories.Applications;
		} else if (cat.startsWith("Video")) {
			if (cat.contains("Movie")) {
				return SourceCategories.Movie;
			} else if (cat.contains("TV")) {
				return SourceCategories.TV;
			} else {
				return SourceCategories.Other;
			}
		} else if (cat.startsWith("Applications")) {
			return SourceCategories.Applications;
		} else {
			return SourceCategories.Other;
		}
	}

	public String getSize(String s) {
		String result = "0 KB";
		if (s.contains(",") && s.toLowerCase().contains("size")) {
			String[] splits = s.split(",");
			if (splits.length > 1) {
				result = splits[1]
						.trim()
						.replace("Size", "")
						.replace("i", "");
			}
		}
		return result;
	}

	public String getAdded(String s) {
		String result = "";
		if (s.contains(",") && s.toLowerCase().contains("upload")) {
			String[] splits = s.split(",");
			if (splits.length > 0) {
				result = splits[0]
						.replace("Uploaded","")
						.trim()
						.replace(" ","-");
			}
		}
		return result;
	}

	public String appendBaseEndUrls(String baseUrl, String endUrl) {
		if (endUrl.startsWith(baseUrl)) {
			return endUrl;
		} else {
			return baseUrl + endUrl;
		}
	}
	public String convertSearchQuery(String searchQuery){
		return searchQuery.replace(" ","%20");
	}
}
