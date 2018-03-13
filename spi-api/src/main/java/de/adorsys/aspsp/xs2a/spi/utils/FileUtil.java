package de.adorsys.aspsp.xs2a.spi.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileUtil {

    public static String getJsonStringFromFile(String fullPathToFile) throws IOException {
        StringBuilder resultStringBuilder = new StringBuilder();
        BufferedReader br = Files.newBufferedReader(Paths.get(fullPathToFile), Charset.defaultCharset());
        br.lines().forEach(resultStringBuilder::append);
        return resultStringBuilder.toString();
    }
    
    public static String readFromInputStream(InputStream inputStream) throws IOException {
        StringBuilder resultStringBuilder = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = br.readLine()) != null) {
                resultStringBuilder.append(line).append("\n");
            }
        }
        return resultStringBuilder.toString();
    }
}
