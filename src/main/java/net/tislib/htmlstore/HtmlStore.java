package net.tislib.htmlstore;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.HashMap;
import java.util.Map;

public class HtmlStore {

    TokenTree tokenTree = new TokenTree();

    public void put(String key, String htmlContent) {
        Document document = Jsoup.parse(htmlContent);
        tokenTree.add(document, key);
    }

    public String get(String key) {
        return tokenTree.getContent(key);
    }
}
