/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.adorsys.aspsp.xs2a.integtest.stepdefinitions.pis;

import de.adorsys.aspsp.xs2a.integtest.util.Context;
import de.adorsys.psd2.model.TppMessages;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;

public class AbstractErrorfulSteps {

    @Autowired
    private Context<HashMap, TppMessages> context;

    public void setErrorfulIds (String filename) {
        final String NOT_EXISTING_PAYMENT_ID_FILE_NAME = "not-existing-paymentId";
        final String NOT_EXISTING_AUTHORISATION_ID_FILE_NAME = "wrong-authorisation-id";
        if (filename.toLowerCase().contains(NOT_EXISTING_AUTHORISATION_ID_FILE_NAME.toLowerCase())){
            final String WRONG_AUTHORISATION_ID = "11111111-aaaa-xxxx-1111-1x1x1x1x1x1x";
            context.setAuthorisationId(WRONG_AUTHORISATION_ID);
        } else if (filename.toLowerCase().contains(NOT_EXISTING_PAYMENT_ID_FILE_NAME.toLowerCase())) {
            final String WRONG_PAYMENT_ID = "11111111-aaaa-xxxx-1111-1x1x1x1x1x1x";
            context.setPaymentId(WRONG_PAYMENT_ID);
        }
    }
}
