# Release notes

## version 1.7 (release date: 14.09.2018)

- Implementation of specification 1.2 according to the yml file from Berlin Group.
- Support of Embedded SCA approach for single payment:
    -  Embedded SCA Approach without SCA method
    -  Embedded SCA Approach with only one SCA method available 
    -  Embedded SCA Approach with Selection of an SCA method 
- Implementation of Start Authorization Process for Embedded SCA approach
- Add new endpoint to CMS to enable ASPSP to modify Account Access at AIS Consent providing consent Id.
- Consent request Type is added to the Consent
- SCA method for PSU is added


## version 1.8 (Planned date: 28.09.2018)
- Update bulk payments and consent requests according to specification 1.2
- Support of Embedded SCA approach for bulk payment.
- Support of Embedded SCA approach for periodic payment.
- Support of Embedded SCA approach for consent request.
- Validate Qwac certificate.
- Support get transaction information for a given account. Embedded approach.
- Migration to package and Maven GroupId "de.adorsys.psd2": aspsp-profile 
- Fix the account consent information request to return link information.
- Add endpoint updateAspspConsentData for PIS consent to Consent Management Service.
- Remove empty links in the responses.
