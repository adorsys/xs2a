#Payment confirmation demo for redirect approach 

"Payment confirmation demo" are test pages and services for showing how XS2A interface will work with TPP`s payment initiation request through redirect SCA approach.

To test the flow of payment confirmation locally, follow next steps:

##Happy path

* Open ASPSP-mock-server and go to the TPP demo page (by default the URL is `http://localhost:28080/tpp/product/index.html`). You should see "TPP demo" page with four different products to buy. 

* Choose one of the product and press "Buy now".

* You will be redirected to the login page. Enter the credentials for your keycloak user. By default it is `aspsp` for login and `zzz` for password.

* After successful login you will be redirected to TAN confirmation page. You should receive TAN number on the email you added while creating PSU. Copy TAN and enter in the the field, then press "Validate".

* If the TAN is correct, you will be redirected to the next page, consent confirmation. There you should choose "Yes".

* After giving the consent you will see page with payment details (payment id, purchase price and currency). It means that payment successfully executed.

##Unhappy paths

   **Wrong credentials**

   * Open url http://localhost:28080/tpp/product/index.html. You should see "TPP demo" page with four different products to buy. 
   
   * Choose one of the product and press "Buy now".
   
   * You will be redirected to the login page. Enter wrong credentials for your keycloak user, for example `wrong password` and `wrong login`.
   
   * You should see a message from keycloak about wrong login or password.
   
   **Wrong TAN number**
   
   * Open url http://localhost:28080/tpp/product/index.html. You should see "TPP demo" page with four different products to buy. 
   
   * Choose one of the product and press "Buy now".
   
   * You will be redirected to the login page. Enter the credentials for your keycloak user. By default it is `aspsp` for login and `zzz` for password.
   
   * After successful login you will be redirected to TAN confirmation page. Enter  wrong TAN number in the the field, for example, `1`, than press "Validate".

   * You will see page with the message  `TAN confirmation failed! Your payment is unsuccessful`.
   
   **Consent revoked by PSU**
   
   * Open url http://localhost:28080/tpp/product/index.html. You should see "TPP demo" page with four different products to buy. 

   * Choose one of the product and press "Buy now".

   * You will be redirected to the login page. Enter the credentials for your keycloak user. By default it is `aspsp` for login and `zzz` for password.

   * After successful login you will be redirected to TAN confirmation page. You should receive TAN number on the email you added while creating PSU. Copy TAN and enter in the the field, than press "Validate".

   * If the TAN is correct, you will be redirected to the next page, consent confirmation. There you should choose "No".
   
   * You will see page with the message  `You decline to make a payment. Payment has failed`.
