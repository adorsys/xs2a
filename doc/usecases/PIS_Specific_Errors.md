#AIS Specific Error Codes Use Cases

  ERRORS not implemented so far  
 `REQUIRED_KID_MISSING`  - awaiting further information  
 `EXECUTION_DATE_INVALID` - shall be done in separate task using AoP or Interceptor after clarification, currently is not a required field according PSD2 v1.0
 
 Payment Product related errors and their causes are same for all PIS endpoints:
  -------------
 **Failure: Payment Product not supported by ASPSP**
   
   The endpoint should be queried with:
   * _**payment-product** (nonexistent/unsupported by ASPSP payment product)_
  
   **Result:** You get an error message.   
   **Http Status:** `404 PRODUCT_UNKNOWN`
      
  -------------
  **Failure: Payment Product not supported for current PSU**
   
   The endpoint should be queried with:
   * _**payment-product** (existing and supported by ASPSP, but not available for current PSU payment product)_
   
   **Result:** You get an error message.   
   **Http Status:** `403 PRODUCT_INVALID`
     
  -------------
  **Failure: ASPSP Rejected the requested payment request**
   
   The endpoint should be queried with:
   * payment-product (existing and supported by ASPSP, available for current PSU payment product)_
   * _**single/bulk/periodic payment**_ (Requested payment Amount exceeds available funds for payers account)
   
   **Result:** You get an error message.   
   **Http Status:** `400 PAYMENT_FAILED`
   
  -------------
  **Failure: Malformed or missing payment**  
     
   The endpoint should be queried with:
   * payment-product (existing and supported by ASPSP, available for current PSU payment product)_
   * _**single/bulk/periodic payment**_ (Requested payment is empty or paydays are set to past dates)
     
   **Result:** You get an error message.   
   **Http Status:** `400 FORMAT_ERROR`
 
 -----------------------
