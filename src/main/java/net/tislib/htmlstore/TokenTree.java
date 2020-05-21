package net.tislib.htmlstore;

import lombok.Data;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class TokenTree {

    private long pageIndex = 0;
    private Map<Long, String> urls = new HashMap<>();
    private Map<String, Integer> textMap = new HashMap<>();
    private Map<Integer, String> textMapR = new HashMap<>();
    private PathTreeItem root = new PathTreeItem("root");

    public void analyze() {
    }

    public int size() {
        return getDocument().html().length();
    }

    public int count() {
        return count(root);
    }

    private int count(PathTreeItem root) {
        return 1 + root.children.stream().mapToInt(this::count).sum();
    }

    public Document getDocument() {
        Document document = new Document("http://example.com");
        document.html("<metadata><urls></urls><texts></texts></metadata>");

        Element urlsMeta = document.selectFirst("metadata > urls");
        Element textMeta = document.selectFirst("metadata > texts");

        for (Map.Entry<Long, String> entry : urls.entrySet()) {
            Element element = new Element("u");
            element.text(entry.getValue());
            element.attr("i", String.valueOf(entry.getKey()));
            urlsMeta.appendChild(element);
        }

        for (Map.Entry<String, Integer> entry : textMap.entrySet()) {
            Element element = new Element("t");
            element.text(entry.getKey());
            element.attr("i", String.valueOf(entry.getValue()));
            textMeta.appendChild(element);
        }

        document.appendChild(toElement(root));

        return document;
    }

    public Document getDocument2() {
        Document document = new Document("http://example.com");
        document.html("<metadata><urls></urls><texts></texts></metadata>");

        Element urlsMeta = document.selectFirst("metadata > urls");
        Element textMeta = document.selectFirst("metadata > texts");

        for (Map.Entry<Long, String> entry : urls.entrySet()) {
            Element element = new Element("u");
            element.text(entry.getValue());
            element.attr("i", String.valueOf(entry.getKey()));
            urlsMeta.appendChild(element);
        }

        for (Map.Entry<String, Integer> entry : textMap.entrySet()) {
            Element element = new Element("t");
            element.text(entry.getKey());
            element.attr("i", String.valueOf(entry.getValue()));
            textMeta.appendChild(element);
        }

        Map<PathTreeItem, Integer> refs = new HashMap<>();

        document.appendChild(toElement2(root, refs));

        return document;
    }

    private Element toElement2(PathTreeItem root, Map<PathTreeItem, Integer> refs) {
        if (depth(root) > 2) {
            if (refs.containsKey(root)) {
                Element ref = new Element("ref");
                ref.attr("i", String.valueOf(refs.get(root)));
                return ref;
            }
        }
        Element element = new Element(root.tag);

        root.attributes.forEach(element::attr);

        String pages = root.pageUrls.stream().map(String::valueOf).collect(Collectors.joining(","));
        if (pages.length() > 0) {
            element.attr("p", pages);
        }

        root.children.stream()
                .map(root1 -> toElement2(root1, refs))
                .forEach(element::appendChild);

        if (depth(root) > 2) {
            int index = refs.size();
            refs.put(root, index);
            element.attr("ri", String.valueOf(index));
        }

        return element;
    }

    private int depth(PathTreeItem root) {
        if (root.children.size() == 0) {
            return 1;
        }
        return 1 + root.children.stream().map(this::depth)
                .max(Comparator.comparingInt(item -> item))
                .get();
    }

    private Element toElement(PathTreeItem root) {
        Element element = new Element(root.tag);

        root.attributes.forEach(element::attr);

        String pages = root.pageUrls.stream().map(String::valueOf).collect(Collectors.joining(","));
        if (pages.length() > 0) {
            element.attr("p", pages);
        }

        root.children.stream()
                .map(this::toElement)
                .forEach(element::appendChild);

        return element;
    }

    public void add(Document newDocument, URL url) {
        try {
            urls.put(++pageIndex, url.toString());
            newDocument.select(".header-main-nav").remove();
            newDocument.select(".footer-container").remove();
            newDocument = TokenUtil.prepareDoc(newDocument);

            merge(root, newDocument, pageIndex);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void merge(PathTreeItem existingElement, Element newElement, Long pageId) {
        List<Element> childNodes = newElement.children();

        ArrayList<PathTreeItem> existingNodesCopy = new ArrayList<>(existingElement.children);


        for (Element node : new ArrayList<>(childNodes)) {
            if (node.tagName().equals("text")) {
                String text = node.attr("value");
                if (!textMap.containsKey(text)) {
                    textMap.put(text, textMap.size());
                    textMapR.put(textMap.size() - 1, text);
                }
                int index = textMap.get(text);
                node.removeAttr("value");
                node.attr("i", String.valueOf(index));
            }

            for (PathTreeItem existingNode : existingNodesCopy) {
                if (checkNodesSimilar(node, existingNode)) {
                    merge(existingNode, node, pageId);

                    childNodes.remove(node);
                }
            }
        }

        for (Element node : childNodes) {
            PathTreeItem existingNode = new PathTreeItem(node.tagName());
            existingNode.attributes.putAll(TokenUtil.toMap(node.attributes()));
            merge(existingNode, node, pageId);
            existingElement.getChildren().add(existingNode);
        }

        if (childNodes.size() == 0) {
            existingElement.pageUrls.add(pageId);
        }
    }

    private boolean checkNodesSimilar(Element node, PathTreeItem pathTreeItem) {
        return pathTreeItem != null && node.tagName().equals(pathTreeItem.tag)
                && equals(node.attributes(), pathTreeItem.attributes);
    }

    private boolean equals(Attributes attributes, Map<String, String> map) {
        for (Attribute attribute : attributes) {
            if (!attribute.getValue().equals(map.get(attribute.getKey()))) {
                return false;
            }
        }

        return true;
    }

    public Object getData() {
        Map<String, Object> data = new HashMap<>();
        data.put("merged", toRecData(root, null));
        data.put("urls", urls);

        for (Map.Entry<Long, String> entry : urls.entrySet()) {
            data.put(String.valueOf(entry.getKey()), toRecData(root, entry.getKey()));
        }

//        return toRecData(root, 1L);

        return data;
    }

    public Object getTemplate() {
        return toTemplate(root);
    }

    private Object toTemplate(PathTreeItem root) {
        if (root.tag.equals("text")) {
            return textMapR.get(Integer.valueOf(root.attributes.get("i")));
        }
        List<Object> res = root.children.stream()
                .map(this::toTemplate)
                .map(this::arrayFlatMap)
                .filter(Objects::nonNull)
                .limit(10)
                .collect(Collectors.toList());

        if (res.stream().noneMatch(item -> item instanceof String)) {
            return res;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("field", "");
        data.put("sub", res);
        if (res.size() == 0) {
            return null;
        }
        return data;
    }

    private Object toRecData(PathTreeItem root, Long key) {
        if (root.tag.equals("text")) {
            Map<String, Object> data = new HashMap<>();
            String text = textMapR.get(Integer.valueOf(root.attributes.get("i")));
            data.put("txt", text);
            if (key == null) {
                data.put("p", root.pageUrls);
            }
            if (key != null && !root.pageUrls.contains(key)) {
                return Collections.emptyList();
            }
            if (!filterItem(text)) {
                return Collections.emptyList();
            }
            return Collections.singletonList(data);
        }
        return root.children.stream()
                .map(root1 -> toRecData(root1, key))
                .map(this::arrayFlatMap)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private Object arrayFlatMap(Object val) {
        if (val instanceof List) {
            List<?> objects = (List<?>) val;
            objects = objects.stream().filter(this::filterItem).collect(Collectors.toList());
            if (objects.size() == 1 && (objects.get(0) instanceof List)) {
                return arrayFlatMap((List<Object>) objects.get(0));
            }
            if (objects.size() == 0) {
                return null;
            }
        }
        return val;
    }

    private boolean filterItem(Object object) {
        if (object instanceof String) {
            if (((String) object).matches("\\W+")) {
                return false;
            }
        }
        return true;
    }

    @Data
    public static class DataTreeItem {
        String text;
        private Set<Long> p = new HashSet<>();

        List<DataTreeItem> items;
    }

    @Data
    public static class PathTreeItem {
        public final String tag;
        public String text;

        public Map<String, String> attributes = new HashMap<>();

        private Set<Long> pageUrls = new HashSet<>();

        private List<PathTreeItem> children = new ArrayList<>();
    }
}
