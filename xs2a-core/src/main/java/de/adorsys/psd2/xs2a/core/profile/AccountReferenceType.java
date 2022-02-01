/*
 * Copyright 2018-2022 adorsys GmbH & Co KG
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version. This program is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 *
 * This project is also available under a separate commercial license. You can
 * contact us at psd2@adorsys.com.
 */

package de.adorsys.psd2.xs2a.core.profile;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

public enum AccountReferenceType {
    IBAN(1, "iban", AccountReference::getIban, AccountReference::setIban),
    BBAN(2, "bban", AccountReference::getBban, AccountReference::setBban),
    PAN(3, "pan", AccountReference::getPan, AccountReference::setPan),
    MSISDN(4, "msisdn", AccountReference::getMsisdn, AccountReference::setMsisdn),
    MASKED_PAN(5, "maskedPan", AccountReference::getMaskedPan, AccountReference::setMaskedPan);

    private String value;
    private int order;
    private Function<AccountReference, String> getter;
    private BiConsumer<AccountReference, String> setter;

    @JsonCreator
    AccountReferenceType(int order, String value, Function<AccountReference, String> getter,
                         BiConsumer<AccountReference, String> setter) {
        this.order = order;
        this.value = value;
        this.getter = getter;
        this.setter = setter;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public int getOrder() {
        return order;
    }

    public static Optional<AccountReferenceType> getByValue(String name) {
        return Arrays.stream(values())
                   .filter(type -> type.getValue().equals(name))
                   .findFirst();
    }

    public String getFieldValue(AccountReference accountReference) {
        return getter.apply(accountReference);
    }

    public void setFieldValue(AccountReference accountReference, String fieldValue) {
        setter.accept(accountReference, fieldValue);
    }
}

