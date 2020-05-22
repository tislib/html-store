import lombok.SneakyThrows;
import net.tislib.htmlstore.HtmlStore;
import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;

public class HtmlCompression {

    private HtmlStore htmlStore = new HtmlStore();

    @Test
    public void compressDecompress() {
        String testData = this.getClass().getClassLoader().getResource("test-data").getFile();

        for (String item : new File(testData).list()) {
            compressDecompress(item);
        }
    }

    private void compressDecompress(String item) {
        String htmlContent = readFile(item);

        htmlStore.put(item, htmlContent);

        String decompressedHtmlContent = htmlStore.get(item);

        assertDomEquals(htmlContent, decompressedHtmlContent);
    }

    private void assertDomEquals(String htmlContent, String decompressedHtmlContent) {
        Function<String, String> normalizer = (original) ->
                original
                        .replaceAll("[\\s+]?\n+[\\s+]?", "") // remove newline chars
                        .replaceAll("(>)(\\s+)(<)", "$1$3") // remove white space between tags
                        .replaceAll("<!.*?-->", "")
                        .replaceAll("<script.*?-->", "")
                        .replaceAll("<head></head>", "")
                        .replaceAll("<body></body>", "")
                        .toLowerCase();
        String html1 = normalizer.apply(htmlContent);
        String html2 = normalizer.apply(decompressedHtmlContent);


        Document document1 = Jsoup.parse(html1);
        Document document2 = Jsoup.parse(html2);

        assertDomEquals(document1.head(), document2.head());
    }

    private void assertDomEquals(Element document1, Element document2) {
        for (int i = 0; i < document1.childNodeSize(); i++) {
            Node node1 = document1.childNode(i);
            Node node2 = document2.childNode(i);
            Assert.assertEquals(node1.getClass(), node2.getClass());

            if (node1 instanceof TextNode) {
                Assert.assertEquals(((TextNode) node1).text().trim(), ((TextNode) node2).text().trim());
            } else if (node1 instanceof Element && node2 instanceof Element) {
                Assert.assertEquals(((Element) node1).tagName(), ((Element) node2).tagName());
                String tagName = ((Element) node1).tagName();
                if (tagName.equals("script") || tagName.equals("style")) {
                    continue;
                }
                assertDomEquals((Element) node1, (Element) node2);
            }
        }
    }


    @SneakyThrows
    private String readFile(String item) {
        try (FileInputStream fis = new FileInputStream(this.getClass().getClassLoader().getResource("test-data/" + item).getFile())) {
            return IOUtils.toString(fis, StandardCharsets.UTF_8);
        }
    }

}
