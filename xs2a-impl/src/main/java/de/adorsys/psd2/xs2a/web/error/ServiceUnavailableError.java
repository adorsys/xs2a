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

package de.adorsys.psd2.xs2a.web.error;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.adorsys.psd2.model.TppMessageCategory;
import lombok.Value;
import org.springframework.http.HttpStatus;

import java.util.Collections;
import java.util.List;

@Value
public class ServiceUnavailableError {
    @JsonProperty("tppMessages")
    private List<ServiceUnavailableTppMessage> tppMessages = Collections.singletonList(new ServiceUnavailableTppMessage());

    @Value
    private static class ServiceUnavailableTppMessage {
        private TppMessageCategory category = TppMessageCategory.WARNING;
        private String code = HttpStatus.SERVICE_UNAVAILABLE.name();
        private String text = HttpStatus.SERVICE_UNAVAILABLE.getReasonPhrase();
    }
}
