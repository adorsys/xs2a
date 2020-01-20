package de.adorsys.psd2.consent.api.ais;

import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationTemplate;
import de.adorsys.psd2.xs2a.core.consent.AisConsentRequestType;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CmsAisAccountConsent {
    private String id;
    private AisAccountAccess access;
    private boolean recurringIndicator;
    private LocalDate validUntil;
    private LocalDate expireDate;
    private int frequencyPerDay;
    private LocalDate lastActionDate;
    private ConsentStatus consentStatus;
    private boolean withBalance;
    private boolean tppRedirectPreferred;
    private AisConsentRequestType aisConsentRequestType;
    private List<PsuIdData> psuIdDataList;
    private TppInfo tppInfo;
    private AuthorisationTemplate authorisationTemplate;
    private boolean multilevelScaRequired;
    private List<AisAccountConsentAuthorisation> accountConsentAuthorizations;
    private Map<String, Integer> usageCounterMap;
    private OffsetDateTime creationTimestamp;
    private OffsetDateTime statusChangeTimestamp;
}
