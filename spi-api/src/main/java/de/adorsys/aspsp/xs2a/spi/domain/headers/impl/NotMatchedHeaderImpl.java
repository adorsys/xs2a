package de.adorsys.aspsp.xs2a.spi.domain.headers.impl;

import de.adorsys.aspsp.xs2a.spi.domain.headers.RequestHeaders;

public class NotMatchedHeaderImpl implements RequestHeaders {
    @Override
    public boolean isValid() {
        return false;
    }
}
