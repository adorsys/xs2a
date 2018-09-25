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

package de.adorsys.aspsp.xs2a.domain.consent;

import de.adorsys.psd2.model.ChosenScaMethod;
import lombok.Data;

@Data
public class Xs2aChosenScaMethod {
    private String authenticationType;
    private String authenticationMethodId;

    /*
    TODO Delete inner class after fixing bug in ChosenScaMethod : https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/343
     This class was created for extending incorrect class ChosenScaMethod.
     Because ChosenScaMethod doesn't have any fields
    */
    @Data
    public class ExtendedChosenScaMethod extends ChosenScaMethod {
        private String authenticationType;
        private String authenticationMethodId;
    }
}
