# Release notes v. 1.16.1

## Bugfix: Get transactions url needs / at the end
              
`GET /v1/accounts/{account-id}/transactions/` endpoint required `/` at the end of the url. Otherwise the request did not work.
Now `GET /v1/accounts/{account-id}/transactions/` AND `GET /v1/accounts/{account-id}/transactions` (without slash at the end) 
 both works as urls for requesting the transactions.


