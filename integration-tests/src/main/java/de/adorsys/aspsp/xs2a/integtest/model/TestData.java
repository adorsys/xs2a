package de.adorsys.aspsp.xs2a.integtest.model;

import lombok.Getter;

@Getter
public class TestData<T>{
    private Request<T> request;
    private Response response;
}
