package com.example.torrentsearch.configurations;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;

@Configuration
public class Config extends AbstractMongoClientConfiguration {

    @Override
    protected String getDatabaseName() {
        return "Torrents";
    }

    @Override
    protected boolean autoIndexCreation() {
        return true;
    }
    }