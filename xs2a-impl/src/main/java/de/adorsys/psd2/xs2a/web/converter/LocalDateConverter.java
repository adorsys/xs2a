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

package de.adorsys.psd2.xs2a.web.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Converter, responsible for properly mapping incoming String in request parameters into LocalDate instance.
 * Applied globally for all controllers.
 * Used primarily in {@link de.adorsys.psd2.api.AccountApi#getTransactionList} and
 * {@link de.adorsys.psd2.api.CardAccountsApi#getCardAccountTransactionList}
 */
@Component
public class LocalDateConverter implements Converter<String, LocalDate> {
    @Override
    public LocalDate convert(String source) {
        return convert(source, DateTimeFormatter.ISO_DATE);
    }

    public LocalDate convert(String source, DateTimeFormatter formatter) {
        return LocalDate.parse(source, formatter);
    }
}
