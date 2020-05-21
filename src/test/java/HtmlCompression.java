import lombok.SneakyThrows;
import net.tislib.htmlstore.HtmlStore;
import org.apache.commons.io.IOUtils;
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
                        .replaceAll("<!--.*?-->", "")
                        .toLowerCase();
        String html1 = normalizer.apply(htmlContent);
        String html2 = normalizer.apply(decompressedHtmlContent);
        Assert.assertEquals("Both documents are identical", html1, html2);
    }


    @SneakyThrows
    private String readFile(String item) {
        try (FileInputStream fis = new FileInputStream(this.getClass().getClassLoader().getResource("test-data/" + item).getFile())) {
            return IOUtils.toString(fis, StandardCharsets.UTF_8);
        }
    }

}
