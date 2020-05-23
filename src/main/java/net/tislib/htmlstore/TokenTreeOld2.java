package net.tislib.htmlstore;

import lombok.Data;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TokenTreeOld2 {

    private final Document document = new Document("http://example.com");
    private long pageIndex = 1;

    public TokenTreeOld2() {
        document.html("<metadata><urls></urls></metadata>");
    }

    public void analyze() {
    }

    public int size() {
        return document.html().length();
    }

    public Document getDocument() {
        return document;
    }

    public void add(Document newDocument, URL url) {
        Integer id = getUrlId(url);

        merge(document, DocumentUtil.prepareDoc(newDocument), id);
    }

    private void merge(Element existingElement, Element newElement, Integer pageId) {
        List<Element> childNodes = newElement.children();
        List<Element> mergedElements = new ArrayList<>();

        for (int i = 0; i < childNodes.size(); i++) {
            Element node = childNodes.get(i);
            Element existingNode = existingElement.childNodeSize() >= i ? null : (Element) existingElement.childNode(i);

            if (!checkNodesSimilar(node, existingNode)) {
                existingNode = new Element(node.tagName());
                existingNode.attributes().addAll(node.attributes());
            }
            merge(existingNode, node, pageId);
        }

        if (childNodes.size() == 0) {
            existingElement.appendChild(pageElement(pageId));
        }

        existingElement.html("");
        mergedElements.forEach(existingElement::appendChild);
    }

    private Node pageElement(Integer pageId) {
        Element pageElement = new Element("page");
        pageElement.text(String.valueOf(pageId));
        return pageElement;
    }

    private boolean checkNodesSimilar(Element node, Element existingElement) {
        return existingElement != null && node.tagName().equals(existingElement.tagName())
                && node.attributes().equals(existingElement.attributes());
    }

    private Integer getUrlId(URL url) {

        for (Element element : document.select("metadata > urls > url")) {
            if (element.text().equals(url.toString())) {
                return Integer.valueOf(element.attr("index"));
            }
        }

        Element urlsHolder = document.selectFirst("metadata > urls");

        Element element = new Element("url");
        element.attr("index", String.valueOf(pageIndex++));
        element.text(url.toString());
        urlsHolder.appendChild(element);


        return Integer.valueOf(element.attr("index"));
    }

    @Data
    public static class PathTreeItem {
        PathItem pathItem;
        Set<Integer> pageUrls = new HashSet<>();

        Set<PathTreeItem> children = new HashSet<>();
    }
}
