import lombok.SneakyThrows;
import net.tislib.htmlstore.DocumentUtil;
import net.tislib.htmlstore.HtmlStore;
import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;

public class HtmlCompression {

    private HtmlStore htmlStore = new HtmlStore();

    @Test
    public void compressDecompress() {
        String testData = this.getClass().getClassLoader().getResource("test-data").getFile();

        long size = 0;

        for (String item : new File(testData).list()) {
            String htmlContent = readFile(item);
            htmlStore.put(item, htmlContent);
        }

        for (String item : new File(testData).list()) {
            System.out.println(item);
            String htmlContent = readFile(item);

            String decompressedHtmlContent = htmlStore.get(item);

            String html1 = DocumentUtil.cleanUp(Jsoup.parse(htmlContent)).html();
            String html2 = DocumentUtil.cleanUp(Jsoup.parse(decompressedHtmlContent)).html();

            assertDomEquals(html1, html2);

            String data = htmlStore.export();
            store(data);

            size += htmlContent.length();
            System.out.println((data.length() * 100 / size));
        }
    }

    @SneakyThrows
    private void store(String data) {
        try (FileOutputStream fos = new FileOutputStream(this.getClass().getClassLoader().getResource("export/out.html").getFile())) {
            fos.write(data.getBytes());
        }
    }

    private void assertDomEquals(String html1, String html2) {
        Document document1 = DocumentUtil.cleanUp(Jsoup.parse(html1));
        Document document2 = DocumentUtil.cleanUp(Jsoup.parse(html2));

        assertDomEquals(document1, document2);
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
                assertAttributesEquals(node1.attributes(), node2.attributes());

                assertDomEquals((Element) node1, (Element) node2);
            }
        }
    }

    private void assertAttributesEquals(Attributes attributes, Attributes attributes1) {
        attributes.forEach(item -> {
            boolean found = false;
            for (Attribute attribute : attributes1) {
                if (item.getKey().equals(attribute.getKey()) && item.getValue().equals(attribute.getValue())) {
                    found = true;
                }
            }

            Assert.assertTrue(found);
        });
    }


    @SneakyThrows
    private String readFile(String item) {
        try (FileInputStream fis = new FileInputStream(this.getClass().getClassLoader().getResource("test-data/" + item).getFile())) {
            return IOUtils.toString(fis, StandardCharsets.UTF_8);
        }
    }

}
