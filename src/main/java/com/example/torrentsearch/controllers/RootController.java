package com.example.torrentsearch.controllers;

import com.example.torrentsearch.torrents.TorrentDataHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

@RestController
public class RootController {

    @Autowired
    ArrayList<Class<?>> getSources;

    @GetMapping("/")
    public String index()  {
        StringBuilder response = new StringBuilder();
        for (Class<?> getSource : getSources) {
            try {
                for(Method method : getSource.getMethods()){
                    response.append(method.getName());
                    if(method.getName().equals("boom")){
                        response.append(method.invoke(getSource.getConstructor().newInstance()));
                    }
                }
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        return response.toString();

    }


}
