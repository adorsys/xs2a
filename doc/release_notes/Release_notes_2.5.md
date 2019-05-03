# Release notes v.2.5

## Deleted deprecated method createConsent in CmsAspspPiisService
Method `de.adorsys.psd2.consent.aspsp.api.piis.CmsAspspPiisService#createConsent(PsuIdData, TppInfo, List, LocalDate, int)` was removed,
use `CmsAspspPiisService#createConsent(PsuIdData, CreatePiisConsentRequest)` instead.
