package de.adorsys.aspsp.xs2a.spi.domain.security;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class BearerToken {
    private final String token;
}
