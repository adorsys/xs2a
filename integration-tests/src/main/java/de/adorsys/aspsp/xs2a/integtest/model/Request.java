package de.adorsys.aspsp.xs2a.integtest.model;

import lombok.Getter;

import java.util.Map;

@Getter
public class Request<T> {
    private Map<String, String> header;
    private T body;
}
