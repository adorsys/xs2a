#Get information from ASPSP Profile service

As an ASPSP I want to store my settings in separate file and give an access to them via REST API.

Provide access to next values:
 - frequency per day
 - combined service indicator
 - available payment products
 - available payment types
 - sca approach
 - tppSignatureRequired
 - pisRedirectUrlToAspsp
 - aisRedirectUrlToAspsp
 - multicurrencyAccountLevel
 
 To test getting data of ASPSP Profile need to make next steps:
 
 * Run ASPSP-profile service.
 
 * To get 'frequency per day' create Get request to URI 'http://your_host:port/aspsp-profile/frequency-per-day'  
 
 * To get 'combined service indicator' create Get request to URI 'http://your_host:port/aspsp-profile/combined-service-indicator"'
 
 * To get 'available payment products' create Get request to URI 'http://your_host:port/aspsp-profile/available-payment-products'
 
 * To get 'available payment types' create Get request to URI 'http://your_host:port/aspsp-profile/available-payment-types'
  
 * To get 'sca approach' create Get request to URI 'http://your_host:port/aspsp-profile/sca-approach'
 
 * To get 'tppSignatureRequired' create Get request to URI 'http://your_host:port/aspsp-profile/tpp-signature-required'

 * To get 'pisRedirectUrlToAspsp' create Get request to URI 'http://your_host:port/aspsp-profile/redirect-url-to-aspsp-pis'

 * To get 'aisRedirectUrlToAspsp' create Get request to URI 'http://your_host:port/aspsp-profile/redirect-url-to-aspsp-ais'
 
 * To get 'multicurrencyAccountLevel' create Get request to URI 'http://your_host:port/aspsp-profile/multicurrency-account-level'

