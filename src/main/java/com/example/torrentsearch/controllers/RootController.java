package com.example.torrentsearch.controllers;

import com.example.torrentsearch.torrents.TorrentDataHolder;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

@RestController
public class RootController {

	@Autowired
	ArrayList<Class<?>> getSources;

	@GetMapping("/")
	public String index() {
		long start = System.currentTimeMillis();

		JSONObject responseJSON = new JSONObject();
		JSONArray torrentsArrayJSON = new JSONArray();
		ArrayList<Thread> sourceRunners = new ArrayList<>();
		for (Class<?> getSource : getSources) {
			Thread t = new Thread(() -> {
				try {
					TorrentDataHolder[] dataHolders = (TorrentDataHolder[]) getSource
							.getMethod("getTorrents", String.class)
							.invoke(getSource.getConstructor().newInstance(), "Avengers");
					for (TorrentDataHolder dataHolder : dataHolders) {
						torrentsArrayJSON.put(dataHolder.getDataInJSON());
					}
				} catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
					e.printStackTrace();
				}
			});
			t.start();
			sourceRunners.add(t);
		}
		for(Thread sourceRunner : sourceRunners){
			try {
				sourceRunner.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		long end = System.currentTimeMillis();
		float sec = (end - start) / 1000F;
		System.out.println(sec + " seconds");
		responseJSON
				.put("time", sec)
				.put("torrents", torrentsArrayJSON);
		return responseJSON.toString();

	}


}
