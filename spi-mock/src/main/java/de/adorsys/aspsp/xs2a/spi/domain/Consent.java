package de.adorsys.aspsp.xs2a.spi.domain;


import de.adorsys.aspsp.xs2a.spi.domain.ais.consents.CreateConsentReq;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Data
@AllArgsConstructor
public class Consent {
    private String id;
    private CreateConsentReq aicRequest;
    private boolean withBalance;
    private boolean psuInvolved;
}
