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

package de.adorsys.psd2.aspsp.profile.web.exception;

import de.adorsys.psd2.aspsp.profile.exception.AspspProfileConfigurationNotFoundException;
import de.adorsys.psd2.aspsp.profile.exception.InstanceIdIsMandatoryHeaderException;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ProfileExceptionAdvisor {

    private static final Logger logger = LoggerFactory.getLogger(ProfileExceptionAdvisor.class);

    @ExceptionHandler(AspspProfileConfigurationNotFoundException.class)
    public ResponseEntity<ErrorMessage> handleAspspProfileConfigurationNotFoundException(Exception ex) {
        var body = new ErrorMessage(MessageErrorCode.RESOURCE_UNKNOWN_404, ex.getMessage());
        logger.debug("Exception code: {}, \n message: {} \n", body.getCode(), ex.getMessage(), ex);
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InstanceIdIsMandatoryHeaderException.class)
    public ResponseEntity<ErrorMessage> handleInstanceIdIsMandatoryHeaderException(Exception ex) {
        var body = new ErrorMessage(MessageErrorCode.FORMAT_ERROR, ex.getMessage());
        logger.debug("Exception code: {}, \n message: {} \n", body.getCode(), ex.getMessage(), ex);
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }
}
