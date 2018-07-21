package de.adorsys.aspsp.xs2a.integtest.model;

import lombok.Getter;

import java.util.Map;

@Getter
public class Response<T> {
    private String code;
    private Map<String, String> header;
    private T body;
}
