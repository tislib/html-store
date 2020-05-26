package net.tislib.htmlstore;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class TokenTree implements HtmlStore {

    private final TokenTreeConfig config;

    private long pageIndex = 0;
    private Map<Long, String> pageKeys = new HashMap<>();
    private Map<String, Long> pageKeysR = new HashMap<>();
    private Map<String, Integer> textMap = new HashMap<>();
    private Map<Integer, String> textMapR = new HashMap<>();
    private PathTreeItem root = new PathTreeItem("root");

    @Override
    public void put(String key, String htmlContent) {
        Document document = Jsoup.parse(htmlContent);
        this.add(document, key);
    }

    @Override
    public String get(String key) {
        return this.getContent(key);
    }

    @Override
    public String export() {
        return this.getDocument().html();
    }

    public Object getDataTemplate() {
        return this.getTemplate();
    }

    public void analyze() {
    }

    public int size() {
        return getDocument().html().length();
    }

    public int count() {
        return count(root);
    }

    private int count(PathTreeItem root) {
        return 1 + root.getChildren().stream().mapToInt(this::count).sum();
    }

    public Document getDocument() {
        Document document = new Document("http://example.com");
        document.html("<metadata><urls></urls><texts></texts></metadata>");

        Element urlsMeta = document.selectFirst("metadata > urls");
        Element textMeta = document.selectFirst("metadata > texts");

        for (Map.Entry<Long, String> entry : pageKeys.entrySet()) {
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

    private Element toElement(PathTreeItem root) {
        Element element = new Element(root.getTag());

        root.getAttributes().forEach(element::attr);

        String pages = root.getPageUrls().stream().map(String::valueOf).collect(Collectors.joining(","));
        if (pages.length() > 0) {
            element.attr("p", pages);
        }

        root.getChildren().stream()
                .map(this::toElement)
                .forEach(element::appendChild);

        return element;
    }

    public void add(Document newDocument, String key) {
        try {
            pageKeys.put(++pageIndex, key);
            pageKeysR.put(key, pageIndex);
            newDocument = DocumentUtil.prepareDoc(newDocument, config);

            merge(root, newDocument, pageIndex);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void merge(PathTreeItem parentNode, Element newElement, Long pageId) {
        List<Element> childNodes = newElement.children();

        ArrayList<PathTreeItem> existingNodesCopy = new ArrayList<>(parentNode.getChildren());


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
            existingNode.setAttributes(DocumentUtil.toMap(node.attributes()));
            existingNode.setParent(parentNode);
            merge(existingNode, node, pageId);
            parentNode.getChildren().add(existingNode);
        }

        parentNode.getPageUrls().add(pageId);
    }

    private boolean checkNodesSimilar(Element node, PathTreeItem pathTreeItem) {
        return pathTreeItem != null && node.tagName().equals(pathTreeItem.getTag())
                && equals(node.attributes(), pathTreeItem.getAttributes());
    }

    private boolean equals(Attributes attributes, Map<String, String> map) {
        for (Attribute attribute : attributes) {
            if (!attribute.getValue().equals(map.get(attribute.getKey()))) {
                return false;
            }
        }

        return true;
    }

    public Object getData(String item) {
        return toRecData(root, pageKeysR.get(item));
    }

    public Object getTemplate() {
        return toTemplate(root);
    }

    private Object toTemplate(PathTreeItem root) {
        if (root.getTag().equals("text")) {
            return textMapR.get(Integer.valueOf(root.getAttributes().get("i")));
        }
        List<Object> res = root.getChildren().stream()
                .map(this::toTemplate)
                .map(this::arrayFlatMap)
                .filter(Objects::nonNull)
                .limit(10)
                .collect(Collectors.toList());

        if (res.stream().noneMatch(item -> item instanceof String)) {
            return res;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("field", SelectionHelper.predictFieldName(root));
        data.put("selector", SelectionHelper.getUniqueCssSelector(root));
        data.put("sub", res);
        if (res.size() == 0) {
            return null;
        }
        return data;
    }

    private Object toRecData(PathTreeItem root, Long key) {
        if (root.getTag().equals("text")) {
            Map<String, Object> data = new HashMap<>();
            String text = textMapR.get(Integer.valueOf(root.getAttributes().get("i")));
            data.put("txt", text);
            if (key == null) {
                data.put("p", root.getPageUrls());
            }
            if (key != null && !root.getPageUrls().contains(key)) {
                return Collections.emptyList();
            }
            if (!filterItem(text)) {
                return Collections.emptyList();
            }
            return Collections.singletonList(data);
        }
        return root.getChildren().stream()
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

    public String getContent(String key) {
        Long index = pageKeysR.get(key);

        return ((Element) toElement(root, index)).html();
    }

    private Node toElement(PathTreeItem root, Long index) {
        Element element = new Element(root.getTag());

        root.getAttributes().forEach(element::attr);

        if (root.getPageUrls() != null && root.getPageUrls().size() > 0 && !root.getPageUrls().contains(index)) {
            return null;
        }

        Function<PathTreeItem, Integer> attrIndex = item -> {
            if (item.getAttributes().get("index") == null) {
                return 0;
            }
            return Integer.valueOf(item.getAttributes().get("index"));
        };

        if (element.tagName().equals("text")) {
            return new TextNode(textMapR.get(Integer.parseInt(element.attr("i"))));
        }

        if (element.tagName().equals("doctype")) {
            return null;
        }

        element.removeAttr("index");

        root.getChildren().stream()
                .sorted(Comparator.comparing(attrIndex))
                .map(item -> toElement(item, index))
                .filter(Objects::nonNull)
                .forEach(element::appendChild);

        return element;
    }

    @Data
    public static class DataTreeItem {
        String text;
        private Set<Long> p = new HashSet<>();

        List<DataTreeItem> items;
    }

}
