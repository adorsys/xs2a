/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
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
