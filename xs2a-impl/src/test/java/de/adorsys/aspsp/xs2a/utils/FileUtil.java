package de.adorsys.aspsp.xs2a.utils;

import org.apache.commons.io.FileUtils;

import java.io.*;

public class FileUtil {
    
    public static String getStringFromFile(String fullPathToFile) throws IOException {
        String fullPath = FileUtil.class.getClassLoader().getResource(fullPathToFile).getFile();
        File file = new File(fullPath);
        
        return FileUtils.readFileToString(file, "UTF-8");
    }
    
    public static String getStringFromInputStream(InputStream inputStream) throws IOException {
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
