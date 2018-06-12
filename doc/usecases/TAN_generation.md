#TAN generation and validation

As a PSU I want to receive e-mail tan (transaction authentication number).

The E-mail tan is necessary to demonstrate payment confirmation with redirect approach.
On the stage of SCA (after the login page) the system sends the email to the user with transaction authentication number.
On the next step, the user should confirm the payment and fill the correct TAN.

To test validation and generation of TAN in swagger, follow next steps:

* Run ASPSP-mock server.

* Open psu-controller, go to **POST** endpoint "**createPsu**".

* Create psu (fill all the fields in json) with your email, copy psuId. The response code should be 201.

* Go to the psu-authentication-controller, open **POST** endpoint "**generateAndSendTan**".

* Fill the field "**psuId**" with the previously copied psuId and press "Execute". The response code should be 201. You will receive an email letter with TAN number.

* Open GET endpoint in the same controller, enter psu id in the field "**psuId**" and tan number from the letter in the field "**tanNumber**". The response code should be 200 - it means TAN number is verified.


To test unhappy paths you can enter wrong psu id or wrong TAN number in steps 5 or 6. 

