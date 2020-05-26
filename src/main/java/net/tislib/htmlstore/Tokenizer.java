package net.tislib.htmlstore;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.SneakyThrows;
import org.jsoup.Jsoup;
import org.jsoup.nodes.*;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Tokenizer {

    static long size = 0;

    public static void main(String... args) throws IOException {
        String[] urls = Urls.urls;

        List<Token> allTokens = new ArrayList<>();
        Set<Token> distinctTokens = new HashSet<>();

        TokenTree tokenTree = new TokenTree(TokenTreeConfig.builder().build());

        long pathCount = 0;

        for (String url : urls) {
            Set<Token> tokens = tokenize(new URL(url));
            System.out.println(tokens);
            allTokens.addAll(tokens);
            distinctTokens.addAll(tokens);

//            tokens.forEach(tokenTree::add);
            pathCount += tokens.stream().mapToLong(item -> item.paths.size()).sum();
        }


        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

        System.out.println(size);
        System.out.println(tokenTree.size());
        System.out.println(pathCount);
        System.out.println(objectMapper.writeValueAsBytes(tokenTree).length);
        System.out.println(objectMapper.writeValueAsBytes(distinctTokens).length);

    }

    public static void tokenize(TokenTree tokenTree, String html, URL url) {

    }

    public static Set<Token> tokenize(String html, URL url) {
        Document document = Jsoup.parse(html);
        List<PathItem> initial = new ArrayList<>();
        PathItem pathItem = toPathItem(document, 0);
        initial.add(pathItem);

        return tokenize(initial, document, url);
    }

    @SneakyThrows
    public static Set<Token> tokenize(URL url) {
        Document document = Jsoup.parse(url, 10000);
        return tokenize(url, document);
    }

    public static Set<Token> tokenize(URL url, Document document) {
        size += document.html().length();
        List<PathItem> initial = new ArrayList<>();
        PathItem pathItem = toPathItem(document, 0);
        initial.add(pathItem);

        return tokenize(initial, document, url);
    }

    private static Set<Token> tokenize(final List<PathItem> path, Element element, URL url) {
        Set<Token> tokens = new HashSet<>();

        int index = 0;
        for (Node node : element.childNodes()) {
            if (node instanceof Element) {
                Element childElement = (Element) node;
                List<PathItem> leftPath = new ArrayList<>(path);
                leftPath.add(toPathItem(childElement, index++));
                tokens.addAll(tokenize(leftPath, childElement, url));
            } else if (node instanceof TextNode) {
                Token token = new Token();
                token.text = ((TextNode) node).text().trim();
                token.paths = new ArrayList<>(path);
                token.url = url;
                if (!token.text.isEmpty()) {
                    tokens.add(token);
                }
            }
        }

        return tokens;
    }

    private static PathItem toPathItem(Element document, int index) {
        PathItem pathItem = new PathItem();
        pathItem.attributes = document.attributes().asList().stream().collect(Collectors.toMap(Attribute::getKey, Attribute::getValue));
        pathItem.index = index;
        pathItem.tag = document.tagName();
        return pathItem;
    }

}
