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

package de.adorsys.aspsp.xs2a.domain.account;

import org.apache.commons.validator.routines.CreditCardValidator;
import org.apache.commons.validator.routines.IBANValidator;

import java.util.Optional;

public enum SupportedAccountReferenceField {
    IBAN {
        @Override
        public Optional<Boolean> isValid(Xs2aAccountReference reference) {
            return Optional.ofNullable(reference.getIban())
                       .map(SupportedAccountReferenceField::isValidIban);
        }
    },
    BBAN {
        @Override
        public Optional<Boolean> isValid(Xs2aAccountReference reference) {
            return Optional.ofNullable(reference.getBban())
                       .map(SupportedAccountReferenceField::isValidBban);
        }
    },
    PAN {
        @Override
        public Optional<Boolean> isValid(Xs2aAccountReference reference) {
            return Optional.ofNullable(reference.getPan())
                       .map(SupportedAccountReferenceField::isValidPan);
        }
    },
    MASKEDPAN {
        @Override
        public Optional<Boolean> isValid(Xs2aAccountReference reference) {
            return Optional.ofNullable(reference.getMaskedPan())
                       .map(SupportedAccountReferenceField::isValidMaskedPan);
        }
    },
    MSISDN {
        @Override
        public Optional<Boolean> isValid(Xs2aAccountReference reference) {
            return Optional.ofNullable(reference.getMsisdn())
                       .map(SupportedAccountReferenceField::isValidMsisdn);
        }
    };

    public abstract Optional<Boolean> isValid(Xs2aAccountReference reference);

    private static boolean isValidIban(String iban) {
        IBANValidator validator = IBANValidator.getInstance();
        return validator.isValid(normalizeString(iban));
    }

    private static boolean isValidBban(String bban) {
        return normalizeString(bban).length() >= 11
                   && normalizeString(bban).length() <= 28; // Can be extended with aprox 50 country specific masks
    }

    private static boolean isValidPan(String pan) {
        CreditCardValidator validator = CreditCardValidator.genericCreditCardValidator(); //Can be extended with specification of credit card types (VISA, MasterCard, AMEX etc. with array in aspsp profile)
        return validator.isValid(normalizeString(pan));
    }

    private static boolean isValidMaskedPan(String maskedPan) {
        return maskedPan.replaceAll("[- ]", "").length() >= 14
                   && maskedPan.replaceAll("[- ]", "").length() <= 16;
    }

    private static boolean isValidMsisdn(String msisdn) {
        String tel = msisdn.replaceAll("[-() ]", "");
        return tel.matches("[ˆ+]?[0-9]{8,15}")
                   || tel.matches("[ˆ00]?[0-9]{8,16}");
    }

    private static String normalizeString(String string) {
        return string.replaceAll("[^a-zA-Z0-9]", "");
    }
}
