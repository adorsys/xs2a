#AIS Specific Error Codes Use Cases

  ERRORS not implemented so far  
 `SESSIONS_NOT_SUPPORTED`  
 `REQUESTED_FORMATS_INVALID` 
 
AIS Consent related errors and their causes are same for all AIS endpoints:
 -------------
**Failure: Wrong or Nonexistent AIS consent**
  
  The endpoint should be queried with:
  * _**consent-id** (wrong/nonexistent AIS consent id)_
 
  **Result:** You get an error message.   
  **Http Status:** `400 CONSENT_UNKNOWN_400`
     
 -------------
 **Failure: AIS Consent expired**
  
  The endpoint should be queried with:
  * _**consent-id** (id of present AIS consent which is outdated/expired)_  
  
  **Result:** You get an error message.   
  **Http Status:** `401 CONSENT_EXPIRED`
    
 -------------
 **Failure: Daily AIS consent access limit exceeded**
  
  The endpoint should be queried with:
  * _**consent-id** (id of present and valid AIS consent with frequencyPerDay value at 0)_
  
  **Result:** You get an error message.   
  **Http Status:** `429 ACCESS_EXCEEDED`
    
 -------------
 **Failure: Mismatch in request and AIS consent permission**
  
  The endpoint should be queried with:
  * _**with-balance** (value mismatching AIS consent)_
  
  Either with-balance is set to TRUE where AIS consent does not have such permission
  Or ASPSP could not return any requested data
  
  **Result:** You get an error message.   
  **Http Status:** `401 CONSENT_INVALID`
  
 ----------------------

**_"/api/v1/accounts"_**
 
 Read All Accounts:
 -----------------------
 **Success:**
 
 The endpoint should be queried with:
 * with-balance (according to AIS consent)
 * tpp-transaction-id (correct UUID)
 * tpp-request-id (correct UUID)
 * consent-id (id of present ASPSP AIS consent)
 
 **Result:** You retrieve a list of account details relevant to the previously received AIS consent.   
 **Http Status:** `200 OK`
 
 -------------
 
**_"/api/v1/accounts/{account-id}"_**
 
 Read Account Details:
 -----------------------
 **Success:**
 
 The endpoint should be queried with:
 * account-id (id of an existing PSU account)
 * with-balance (according to AIS consent)
 * tpp-transaction-id (correct UUID)
 * tpp-request-id (correct UUID)
 * consent-id (id of present ASPSP AIS consent)
 
 **Result:** You retrieve account details relevant to the previously received AIS consent.   
 **Http Status:** `200 OK`
 
 -------------
 **Failure: Wrong or Nonexistent account**
  
  The endpoint should be queried with:
  * _**account-id**_ (wrong or nonexistent id of a PSU account)
  * with-balance (according to AIS consent)
  * tpp-transaction-id (correct UUID)
  * tpp-request-id (correct UUID)
  * consent-id (id of present and valid AIS consent with frequencyPerDay value at 0)
  
  **Result:** You get an error message.   
  **Http Status:** `404 RESOURCE_UNKNOWN_404`
  
  ----------------------
  
 **_"/api/v1/accounts/{account-id}/balances"_**
  
  Read Account Balances:
  -----------------------
  **Success:**
  
  The endpoint should be queried with:
  * account-id (id of an existing PSU account)
  * with-balance (according to AIS consent)
  * tpp-transaction-id (correct UUID)
  * tpp-request-id (correct UUID)
  * consent-id (id of present ASPSP AIS consent)
  
  **Result:** You get a list of account balances relevant to the previously received AIS consent.   
  **Http Status:** `200 OK`
  
  -------------
  **Failure: Wrong or Nonexistent account**
   
   The endpoint should be queried with:
   * _**account-id**_ (wrong or nonexistent id of a PSU account)
   * with-balance (according to AIS consent)
   * tpp-transaction-id (correct UUID)
   * tpp-request-id (correct UUID)
   * consent-id (id of present and valid AIS consent with frequencyPerDay value at 0)
   
   **Result:** You get an error message.   
   **Http Status:** `404 RESOURCE_UNKNOWN_404`
   
  ----------------------
  
 **_"/api/v1/accounts/{account-id}/transactions"_**
  
  Read Transaction by transaction id:
  -----------------------
  **Success:**
  
  The endpoint should be queried with:
  * account-id (id of an existing PSU account)
  * transaction-id (id of an existing transaction carried out with account retrieved from ASPSP by account-id and present at transactions section of AccountAccess in AIS Consent)
  * booking status (BOOKED, PENDING or BOTH)(IGNORED IF TRANSACTION ID IS PRESENT)
  * with-balance (according to AIS consent) (CURRENTLY IGNORED)
  * tpp-transaction-id (correct UUID)
  * tpp-request-id (correct UUID)
  * consent-id (id of present ASPSP AIS consent)
  
  **Result:** You retrieve a transaction.  
  **Http Status:** `200 OK`
  
  -------------
  **Failure: Wrong or Nonexistent account**
   
   The endpoint should be queried with:
  * _**account-id**_ (wrong or nonexistent id of a PSU account)
  * transaction-id (id of an existing transaction carried out with account retrieved from ASPSP by account-id and present at transactions section of AccountAccess in AIS Consent)
  * booking status (BOOKED, PENDING or BOTH)(IGNORED IF TRANSACTION ID IS PRESENT)
  * with-balance (according to AIS consent) (CURRENTLY IGNORED)
  * tpp-transaction-id (correct UUID)
  * tpp-request-id (correct UUID)
  * consent-id (id of present ASPSP AIS consent)
   
   **Result:** You get an error message.   
   **Http Status:** `404 RESOURCE_UNKNOWN_404`
   
  ----------------------
  **Failure: Wrong, Nonexistent or not account related transaction id**
   
   The endpoint should be queried with:
  * account-id (wrong or nonexistent id of a PSU account)
  * _**transaction-id**_ (wrong, nonexistent or not related to requested account transaction id)
  * booking status (BOOKED, PENDING or BOTH)(IGNORED IF TRANSACTION ID IS PRESENT)
  * with-balance (according to AIS consent) (CURRENTLY IGNORED)
  * tpp-transaction-id (correct UUID)
  * tpp-request-id (correct UUID)
  * consent-id (id of present ASPSP AIS consent)
   
   **Result:** You get an error message.   
   **Http Status:** `401 CONSENT_INVALID`
   
  ----------------------
 Read Transactions indicating period and booking status:
  -----------------------
  **Success:**
  
  The endpoint should be queried with:
  * account-id (id of an existing PSU account)
  * date from (a date in the past filled in format "yyyy-mm-dd")
  * date to (a date in the past filled in format "yyyy-mm-dd" is automatically set no Current date if left empty)
  * booking status (BOOKED, PENDING or BOTH)
  * with-balance (according to AIS consent) (CURRENTLY IGNORED)
  * tpp-transaction-id (correct UUID)
  * tpp-request-id (correct UUID)
  * consent-id (id of present ASPSP AIS consent)
  
  **Result:** You retrieve 2 lists of transaction BOOKED and PENDING according to the request.  
  **Http Status:** `200 OK`
  
  -------------
  **Failure: Wrong or Nonexistent account**
   
   The endpoint should be queried with:
  * _**account-id**_ (wrong or nonexistent id of a PSU account)
  * date from (a date in the past filled in format "yyyy-mm-dd")
  * date to (a date in the past filled in format "yyyy-mm-dd" is automatically set no Current date if left empty)
  * booking status (BOOKED, PENDING or BOTH)
  * with-balance (according to AIS consent) (CURRENTLY IGNORED)
  * tpp-transaction-id (correct UUID)
  * tpp-request-id (correct UUID)
  * consent-id (id of present ASPSP AIS consent)
   
   **Result:** You get an error message. 
   **Http Status:** `404 RESOURCE_UNKNOWN_404`
   
  ----------------------
