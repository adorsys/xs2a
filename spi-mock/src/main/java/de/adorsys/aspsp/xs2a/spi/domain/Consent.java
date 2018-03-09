package de.adorsys.aspsp.xs2a.spi.domain;


import de.adorsys.aspsp.xs2a.spi.domain.ais.consents.AccountInformationConsentRequestBody;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Consent {
    private String id;
    private AccountInformationConsentRequestBody aicRequest;
    private boolean withBalance;
    private boolean psuInvolved;
}
