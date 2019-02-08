# How to run the demo of multiple sca approaches
ASPSP can support several SCA approaches, so now XS2A Interface supports multiple SCA approaches also.

## For PIS 

1. Initiate 'Create payment' request by triggering the endpoint POST `/v1/{payment-service}/{payment-product}`. You can provide header
`tpp-redirect-preferred` with values true or false.
2. Check which SCA approach you have in the response header `Aspsp-Sca-Approach`: if only one approach is stored in ASPSP-profile, then this approach should be used.
If you have more than 1 SCA approach in ASPSP-profile, chosen approach will depend on value of `tpp-redirect-preferred`: if the header is true and ASPSP
supports REDIRECT approach, REDIRECT approach will be used. Otherwise first approach in ASPSP-profile option `scaApproaches` should be used.

## For Payment Cancellation

1. Initiate 'Create payment' request by triggering the endpoint POST `/v1/{payment-service}/{payment-product}`.
2. Start payment cancellation request by triggering the endpoint DELETE `/v1/{payment-service}/{payment-product}/{paymentId}`. The header `tpp-redirect-preferred` cannot be used here.
3. If an ASPSP requires payment cancellation authorisation, start payment cancellation authorisation process by triggering the endpoint POST `/v1/{payment-service}/{payment-product}/{paymentId}/cancellation-authorisations`.
2. Check which SCA approach you have in the response header `Aspsp-Sca-Approach`: if only one approach is stored in ASPSP-profile, then this approach should be used.
If you have more than 1 SCA approach in ASPSP-profile, first approach in ASPSP-profile option `scaApproaches` should be used.

## For AIS

1. Initiate 'Create consent' request by triggering the endpoint POST `/v1/consents`. You can provide header
`tpp-redirect-preferred` with values true or false.
2. Check which SCA approach you have in the response header `Aspsp-Sca-Approach`: if only one approach is stored in ASPSP-profile, then this approach should be used.
If you have more than 1 SCA approach in ASPSP-profile, chosen approach will depend on value of `tpp-redirect-preferred`: if the header is true and ASPSP
supports REDIRECT approach, REDIRECT approach will be used. Otherwise first approach in ASPSP-profile option `scaApproaches` should be used.
