#Payment confirmation demo for redirect approach 

"Payment confirmation demo" are test pages and services for showing how XS2A interface will work with TPP's payment initiation request through redirect SCA approach.

To test the flow of payment confirmation locally, follow next steps:

## Happy path
* You can find TPP demo start page inside the project by path '/tpp-demo/index.html'. You can run it in your browser locally. When page is open you should see "TPP demo" page with four different products to buy.  
  This demo contains hardcoded account number with IBAN (for debtor IBAN is `DE89370400440532013002` and for creditor IBAN is `DE89370400440532013000`).  
  Please create a PSU Objects with accounts including hardcoded IBANs and an e-mail address you can access (to get incoming TANs) on a mock server.
                                                                                                  
* Choose one of the product and press "Buy now".

* You will be redirected to the login page. Enter the credentials for your keycloak user. By default it is `aspsp` for login and `zzz` for password.

* After successful login you will be redirected to TAN confirmation page. You should receive TAN number on the email you added while creating PSU. Copy TAN and enter in the the field, then press "Validate".

* If the TAN is correct, you will be redirected to the next page, consent confirmation. There you should choose "Yes".

* After giving the consent you will see page with payment details (payment id, purchase price and currency). It means that payment successfully executed.

## Unhappy paths

   **Wrong credentials**

   * You can find TPP demo start page inside the project by path '/tpp-demo/index.html'. You can run it in your browser locally. When page is open you should see "TPP demo" page with four different products to buy.  
     This demo contains hardcoded account number with IBAN (for debtor IBAN is `DE89370400440532013002` and for creditor IBAN is `DE89370400440532013000`).  
     Please create a PSU Objects with accounts including hardcoded IBANs and an e-mail address you can access (to get incoming TANs) on a mock server.
  
   * Choose one of the product and press "Buy now".
   
   * You will be redirected to the login page. Enter wrong credentials for your keycloak user, for example `wrong password` and `wrong login`.
   
   * You should see a message from keycloak about wrong login or password.
   
   **Wrong TAN number**
   
   * You can find TPP demo start page inside the project by path '/tpp-demo/index.html'. You can run it in your browser locally. When page is open you should see "TPP demo" page with four different products to buy.  
     This demo contains hardcoded account number with IBAN (for debtor IBAN is `DE89370400440532013002` and for creditor IBAN is `DE89370400440532013000`).  
     Please create a PSU Objects with accounts including hardcoded IBANs and an e-mail address you can access (to get incoming TANs) on a mock server.
        
   * Choose one of the product and press "Buy now".
   
   * You will be redirected to the login page. Enter the credentials for your keycloak user. By default it is `aspsp` for login and `zzz` for password.
   
   * After successful login you will be redirected to TAN confirmation page. Enter  wrong TAN number in the the field, for example, `1`, than press "Validate".

   * You will see page with the message  `TAN confirmation failed! Your payment is unsuccessful`.
   
   **Consent revoked by PSU**
   
   * You can find TPP demo start page inside the project by path '/tpp-demo/index.html'. You can run it in your browser locally. When page is open you should see "TPP demo" page with four different products to buy.  
     This demo contains hardcoded account number with IBAN (for debtor IBAN is `DE89370400440532013002` and for creditor IBAN is `DE89370400440532013000`).  
     Please create a PSU Objects with accounts including hardcoded IBANs and an e-mail address you can access (to get incoming TANs) on a mock server.
        
   * Choose one of the product and press "Buy now".

   * You will be redirected to the login page. Enter the credentials for your keycloak user. By default it is `aspsp` for login and `zzz` for password.

   * After successful login you will be redirected to TAN confirmation page. You should receive TAN number on the email you added while creating PSU. Copy TAN and enter in the the field, than press "Validate".

   * If the TAN is correct, you will be redirected to the next page, consent confirmation. There you should choose "No".
   
   * You will see page with the message  `You decline to make a payment. Payment has failed`.
