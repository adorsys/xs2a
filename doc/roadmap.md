# Roadmap

## version 1.14 (Planned date 21.12.2018)
- Support Pain endpoints for initiation of single payments 
- Multitennancy database support
- Get list of PIIS consents by psu-id in Consent Management System 
- Develop TPP access lock service 
- Implement Get SCA Status Request
- Deliver TPP-Nok-Redirect-URI when scaRedirect URI is expired (for PIS)
- Payment may have several PSUs
- Internal APIs from CMS to Bank should work with decrypted consent/payment id

## version 1.15 (Planned date 04.01.2019)
- Implement interfaces to export consents/payments from CMS
- Obsolete Consents and Payments that were not confirmed
- Only one authorisation per payment\consent\payment cancellation for one PSU-ID may be active
- Deliver TPP-Nok-Redirect-URI when scaRedirect URI is expired (for AIS)
- Consent may have several PSUs
- Unique account identifier known to bank to be saved in AIS / PIIS consent 

## version 1.16 (Planned date 18.01.2019)
- Support delta access for transaction list 
- Multilevel SCA for payments and consents
- Support of multicurrency account
- PIS Support a matrix payment-product/payment-type in aspsp-profile and corresponding services. 
- Support Get Transaction Status Response with xml format
- Support Pain endpoints for initiation of bulk payments
- Support Pain endpoints for initiation of periodic payments 
