package com.frostphyr.notiphy.io;

import okhttp3.OkHttpClient;

public class Http {

    private static final Http instance = new Http();

    public static Http getInstance() {
        return instance;
    }

    private final OkHttpClient client = new OkHttpClient();

    public OkHttpClient getClient() {
        return client;
    }

}
