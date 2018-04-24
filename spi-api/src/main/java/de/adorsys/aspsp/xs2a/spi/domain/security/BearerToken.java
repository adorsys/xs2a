package de.adorsys.aspsp.xs2a.spi.domain.security;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BearerToken {
    private final String token;
}
