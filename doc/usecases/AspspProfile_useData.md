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
 - availableBookingStatuses
 - supportedAccountReferenceFields
 - consent lifetime
 - transaction lifetime
 - all psd2 support
 - bank offered consent support
 - transactions without balances supported;
 - signing basket supported;
 - payment cancellation authorization mandated;
 - piis consent supported;
 - delta report supported;
 - redirect url expiration time;
 
 To test getting data of ASPSP Profile need to make next steps:
 
 * Run ASPSP-profile service.
 
 * To get 'aspsp settings', which includes all the settings described above except 'sca approach', create Get request to URI 'http://your_host:port/aspsp-profile'  
 
 * To get 'sca approach' create Get request to URI 'http://your_host:port/aspsp-profile/sca-approach'
 
