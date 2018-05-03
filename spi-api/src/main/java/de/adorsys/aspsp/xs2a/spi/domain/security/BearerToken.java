package de.adorsys.aspsp.xs2a.spi.domain.security;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import static de.adorsys.aspsp.xs2a.spi.domain.constant.Constant.SPACE_SEPARATOR;

@Getter
@RequiredArgsConstructor
public class BearerToken {
    private final String token;

    public String getToken(){
        return StringUtils.substringAfter(token, SPACE_SEPARATOR);
    }
}
