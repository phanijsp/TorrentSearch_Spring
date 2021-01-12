package com.example.torrentsearch.torrents.sources;

import com.example.torrentsearch.torrents.TorrentDataHolder;
import com.example.torrentsearch.torrents.TorrentSource;
import com.example.torrentsearch.torrents.TorrentValidator;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

@Service
public class SourceLime implements TorrentSource {
	@Autowired
	ArrayList<Class<?>> getSources;

	@PostConstruct
	public void fun() {
		getSources.add(SourceLime.class);
	}

	@Override
	public TorrentDataHolder[] getTorrents(String searchQuery) {
		final String baseUrl = "https://wwv.xn--lmetorrents-ocb.cc";
		ArrayList<TorrentDataHolder> torrentDataHolderArrayList = new ArrayList<>();
		try {
			String url = "https://wwv.xn--lmetorrents-ocb.cc/search/all/" + URLEncoder.encode(searchQuery, StandardCharsets.UTF_8) + "/";
			System.out.println(url);
			Document document = Jsoup.connect(url)
					.ignoreContentType(true)
					.ignoreHttpErrors(true)
					.userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:25.0) Gecko/20100101 Firefox/25.0")
					.referrer("http://www.google.com")
					.followRedirects(true)
					.timeout(2000)
					.get();
			Elements titleNodes = document.select(".table2 .tt-name a:eq(1)");
			Elements seedsNodes = document.select(".table2 .tdseed");
			Elements leechesNodes = document.select(".table2 .tdleech");
			Elements sizeNodes = document.select(".table2 .tdnormal:eq(2)");
			Elements addedNodes = document.select(".table2 .tdnormal:eq(1)");
			Elements endUrlNodes = document.select(".table2 .tt-name a:eq(1)");
			System.out.println(
					"titleNodesSize: " + titleNodes.size() +
							"seedNodesSize: " + seedsNodes.size() +
							"leechesNodesSize: " + leechesNodes.size() +
							"sizeNodesSize: " + sizeNodes.size() +
							"addedNodes: " + addedNodes.size() +
							"endUrlNodesSize: " + endUrlNodes.size()
			);
			ArrayList<Thread> magnetFetchers = new ArrayList<>();
			if (TorrentValidator.validate(new Elements[]{titleNodes, seedsNodes, leechesNodes, sizeNodes, addedNodes, endUrlNodes})) {
				for (int i = 0; i < titleNodes.size(); i++) {
					int finalI = i;
					Thread MagnetFetcher = new Thread(() -> {
						String magnet = getMagnet(baseUrl, endUrlNodes.get(finalI).attr("href"));
						if (magnet.startsWith("magnet")) {
							torrentDataHolderArrayList.add(new TorrentDataHolder(
									titleNodes.get(finalI).text(),
									seedsNodes.get(finalI).text(),
									leechesNodes.get(finalI).text(),
									sizeNodes.get(finalI).text(),
									addedNodes.get(finalI).text(),
									SourceLime.class.getSimpleName(),
									appendBaseEndUrls(baseUrl, endUrlNodes.get(finalI).attr("href")),
									magnet
							));
						}
					});
					MagnetFetcher.start();
					magnetFetchers.add(MagnetFetcher);
				}
				for (Thread t : magnetFetchers) {
					t.join();
				}
			}
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
		return torrentDataHolderArrayList.toArray(TorrentDataHolder[]::new);
	}

	private String getMagnet(String baseUrl, String endUrl) {
		String magnetLink = "";
		String url = appendBaseEndUrls(baseUrl, endUrl);
		try {
			Document document = Jsoup.connect(url).ignoreContentType(true)
					.userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:25.0) Gecko/20100101 Firefox/25.0")
					.referrer("http://www.google.com")
					.followRedirects(true)
					.timeout(5000)
					.get();
			System.out.println(url);
			Elements magnets = document.select(".dltorrent p a:contains(Magnet Download)");
			if (magnets.size() > 0) {
				return magnets.get(0).attr("href");
			}
		} catch (IOException e) {
			System.out.println("IOException at TorrentListGrabber.java while trying to parse magnet link from url " + url);
		}
		return magnetLink;
	}

	public  String appendBaseEndUrls(String baseUrl, String endUrl){
		if(endUrl.startsWith(baseUrl)){
			return endUrl;
		}else{
			return baseUrl + endUrl;
		}
	}
}
