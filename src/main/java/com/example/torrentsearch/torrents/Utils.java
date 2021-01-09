package com.example.torrentsearch.torrents;

public class Utils {
	public static String appendBaseEndUrls(String baseUrl, String endUrl){
		if(endUrl.startsWith(baseUrl)){
			return endUrl;
		}else{
			return baseUrl + endUrl;
		}
	}
}