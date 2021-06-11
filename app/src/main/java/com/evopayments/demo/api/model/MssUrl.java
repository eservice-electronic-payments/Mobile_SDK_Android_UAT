package com.evopayments.demo.api.model;

public class MssUrl {

    public String url;
    public String label;

    public MssUrl(String url, String label) {
        this.url = url;
        this.label = label;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public String toString() {
        return this.label;
    }

}
