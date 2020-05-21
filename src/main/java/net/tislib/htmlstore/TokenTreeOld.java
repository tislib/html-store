package net.tislib.htmlstore;

import lombok.Data;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Node;

import java.net.URL;
import java.util.*;

public class TokenTreeOld {

    Map<String, PathTreeItem> tokenMap = new HashMap<>();

    public void add(Token token) {
        tokenMap.putIfAbsent(token.text, new PathTreeItem());
        add(tokenMap.get(token.text), token);
    }

    private void add(PathTreeItem pathTreeItem, Token token) {
        if (pathTreeItem.pathItem == null) {
            pathTreeItem.pathItem = token.paths.get(0);
        }
        addByWalking(pathTreeItem, token.paths.subList(1, token.paths.size()), token.url);
    }

    private void addByWalking(PathTreeItem parent, List<PathItem> path, URL url) {
        if (path.size() == 0) {
            parent.pageUrls.add(url.hashCode());
            return;
        }
        PathItem currentPathItem = path.get(0);
        List<PathItem> rightPath = path.subList(1, path.size());
        boolean found = false;
        for (PathTreeItem item : parent.children) {
            if (item.pathItem.equals(currentPathItem)) {
                found = true;
                addByWalking(item, rightPath, url);
            }
        }

        if (!found) {
            PathTreeItem pathTreeItem = new PathTreeItem();
            pathTreeItem.pathItem = currentPathItem;
            parent.children.add(pathTreeItem);
            addByWalking(pathTreeItem, rightPath, url);
        }
    }

    public void analyze() {
    }

    public int size() {
        int size = 0;
        for (PathTreeItem pathTreeItem : tokenMap.values()) {
            size += size(pathTreeItem);
        }
        return size;
    }

    private int size(PathTreeItem parent) {
        int size = 1;
        for (PathTreeItem pathTreeItem : parent.children) {
            size += size(pathTreeItem);
        }
        return size;
    }

    public Object getTokenMap() {
        return tokenMap;
    }

    public void add(Document document, URL url) {
        try {
            Document doc = process(document, url);
            System.out.println(doc);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Document process(Document document, URL url) {
        Document newDocument = new Document(url.toString());
        for (Node node : document.childNodes()) {
            newDocument.appendChild(node.clone());
        }
        return newDocument;
    }

    @Data
    public static class PathTreeItem {
        PathItem pathItem;
        Set<Integer> pageUrls = new HashSet<>();

        Set<PathTreeItem> children = new HashSet<>();
    }
}
