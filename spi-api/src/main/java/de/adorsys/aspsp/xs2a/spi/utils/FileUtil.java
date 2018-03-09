package de.adorsys.aspsp.xs2a.spi.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileUtil {

    public static String getJsonStringFromFile(String fullPathToFile) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = Files.newBufferedReader(Paths.get(fullPathToFile), Charset.defaultCharset());
        br.lines().forEach(sb::append);
        return sb.toString();
    }
}
