package net.tislib.htmlstore;

public interface HtmlStore {

    void put(String key, String htmlContent);

    String get(String key);

    String export();
}
