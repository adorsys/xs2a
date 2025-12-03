/*
 * Copyright 2018-2025 adorsys GmbH & Co KG
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
 * contact us at sales@adorsys.com.
 */

package de.adorsys.psd2.xs2a.web.validator.path;

import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.regex.Pattern;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.FORMAT_ERROR;

@Component
public class VopBulkIdPathParameterValidator implements PathParameterValidator {

    private static final String VOP_BULK_ID_PARAM = "vopBulkId";
    private static final String UUID_REGEX = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";
    private static final Pattern UUID_PATTERN = Pattern.compile(UUID_REGEX);
    private static final String INVALID_VOP_BULK_ID_ERROR_MESSAGE = "vopBulkId parameter is invalid. Should be UUID format.";

    private final ErrorBuildingService errorBuildingService;

    @Autowired
    public VopBulkIdPathParameterValidator(ErrorBuildingService errorBuildingService) {
        this.errorBuildingService = errorBuildingService;
    }

    @Override
    public MessageError validate(Map<String, String> pathParameterMap, MessageError messageError) {
        String vopBulkId = pathParameterMap.get(VOP_BULK_ID_PARAM);
        
        if (vopBulkId == null || vopBulkId.trim().isEmpty()) {
            // vopBulkId is not present in this request, which is fine for non-bulk endpoints
            return messageError;
        }
        
        if (!isValidUuid(vopBulkId)) {
            errorBuildingService.enrichMessageError(messageError, 
                TppMessageInformation.of(FORMAT_ERROR, INVALID_VOP_BULK_ID_ERROR_MESSAGE));
        }
        
        return messageError;
    }

    private boolean isValidUuid(String uuid) {
        return UUID_PATTERN.matcher(uuid).matches();
    }
}