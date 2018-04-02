package de.adorsys.aspsp.xs2a.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;

@Slf4j
public final class FileReaderUtil {
    private static final String ROOT_PATH = "classpath:json/";

    private FileReaderUtil(){}

    public static String readContent(String pathToFile){
        try {
            File file = ResourceUtils.getFile(ROOT_PATH + pathToFile);
            return new String(Files.readAllBytes(file.toPath()));
        } catch (FileNotFoundException e) {
            log.error("Can't read from: {}, reason: {}", pathToFile, e);
        } catch (IOException e) {
            log.error("Can't read from: {}, reason: {}", pathToFile, e);
        }
        return "";
    }
}
