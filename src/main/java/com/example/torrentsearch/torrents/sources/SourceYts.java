package com.example.torrentsearch.torrents.sources;

import com.example.torrentsearch.configurations.SourceCategories;
import com.example.torrentsearch.configurations.SourceConfiguration;
import com.example.torrentsearch.torrents.TorrentDataHolder;
import com.example.torrentsearch.torrents.TorrentSource;
import com.example.torrentsearch.torrents.TorrentValidator;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

@Service
public class SourceYts implements TorrentSource {
	Logger logger = Logger.getLogger(SourceYts.class);
	@Autowired
	ArrayList<Class<?>> getSources;

	@PostConstruct
	public void fun() {
		getSources.add(SourceYts.class);
	}

	final String baseUrl = "https://yts.mx/api/v2/list_movies.json";

	@Override
	public TorrentDataHolder[] getTorrents(String searchQuery) {
		ArrayList<TorrentDataHolder> torrentDataHolderArrayList = new ArrayList<>();
		try {
			HttpsURLConnection httpsURLConnection = (HttpsURLConnection) new URL(baseUrl+"?query_term"+URLEncoder.encode(searchQuery, StandardCharsets.UTF_8)).openConnection();
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpsURLConnection.getInputStream()));
			String line;
			StringBuilder data= new StringBuilder();
			while((line = bufferedReader.readLine()) != null){
				data.append(line);
			}
			JSONObject jsonObject = new JSONObject(data.toString());
			if(jsonObject.getString("status").equalsIgnoreCase("ok")){
				JSONObject dataObject = jsonObject.getJSONObject("data");
				JSONArray moviesArr = dataObject.getJSONArray("movies");
				System.out.println(moviesArr.length());
				for(int i = 0 ; i < moviesArr.length() ; i ++){
					JSONObject movieObj = moviesArr.getJSONObject(i);
					JSONArray torrentsArr = movieObj.getJSONArray("torrents");
					if(torrentsArr.length()>0){
						for(int j = 0 ; j < torrentsArr.length() ; j ++){
							JSONObject torrentObj = torrentsArr.getJSONObject(j);
							TorrentDataHolder torrentDataHolder = new TorrentDataHolder(SourceCategories.Movie,
									movieObj.getString("title"),
									torrentObj.getString("seeds"),
									torrentObj.getString("peers"),
									torrentObj.getString("size"),
									torrentObj.getString("date_uploaded"),
									SourcePiratesBay.class.getSimpleName(),
									movieObj.getString("url"),
									torrentObj.getString("hash")
									);
						}
					}

				}
			}else{
				logger.error("data response is in wrong format");
			}

		} catch (Exception e) {
			logger.error(e);
		}
		return torrentDataHolderArrayList.toArray(TorrentDataHolder[]::new);
	}

	private String getMagnet(String endUrl) {
		String magnetLink = "";
		String url = appendBaseEndUrls(baseUrl, endUrl);
		try {
			Document document = Jsoup.connect(url).ignoreContentType(true)
					.userAgent(SourceConfiguration.userAgent)
					.referrer(SourceConfiguration.referrer)
					.followRedirects(true)
					.timeout(SourceConfiguration.magnetConnectionTimeout)
					.get();
			Elements magnets = document.select("h4 a");
			if (magnets.size() > 0) {
				return magnets.get(0).attr("href");
			}
		} catch (IOException e) {
			logger.error(e);
		}
		return magnetLink;
	}

	public String appendBaseEndUrls(String baseUrl, String endUrl) {
		if (endUrl.startsWith(baseUrl)) {
			return endUrl;
		} else {
			return baseUrl + endUrl;
		}
	}

	public String getCategory(Element element) {
		String cat = element.attr("class").toLowerCase();
		return switch (cat) {
			case "tv1" -> SourceCategories.Movie;
			case "tv2" -> SourceCategories.TV;
			case "tv4" -> SourceCategories.Music;
			case "tv5" -> SourceCategories.Applications;
			default -> SourceCategories.Other;
		};
	}
}
