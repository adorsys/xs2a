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

package de.adorsys.psd2.xs2a.web.validator.body.payment;

import de.adorsys.psd2.core.payment.model.PurposeCode;
import de.adorsys.psd2.mapper.Xs2aObjectMapper;
import de.adorsys.psd2.model.ChargeBearer;
import de.adorsys.psd2.model.FrequencyCode;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.service.profile.StandardPaymentProductsResolver;
import de.adorsys.psd2.xs2a.validator.payment.CountryPaymentValidatorResolver;
import de.adorsys.psd2.xs2a.web.PathParameterExtractor;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import de.adorsys.psd2.xs2a.web.validator.body.*;
import de.adorsys.psd2.xs2a.web.validator.body.raw.FieldExtractor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.*;
import static de.adorsys.psd2.xs2a.web.validator.constants.Xs2aRequestBodyDateFields.PAYMENT_DATE_FIELDS;

@Slf4j
@Component
public class PaymentBodyValidatorImpl extends AbstractBodyValidatorImpl implements PaymentBodyValidator {
    private static final String PAYMENT_SERVICE_PATH_VAR = "payment-service";
    private static final String PAYMENT_PRODUCT_PATH_VAR = "payment-product";
    static final String PERIODIC_PAYMENT_PATH_VAR = "periodic-payments";
    static final String BULK_PAYMENT_PATH_VAR = "bulk-payments";
    static final String PURPOSE_CODE_FIELD_NAME = "purposeCode";
    static final String FREQUENCY_FIELD_NAME = "frequency";
    static final String CHARGE_BEARER_FIELD_NAME = "chargeBearer";
    static final String BATCH_BOOKING_PREFERRED_FIELD_NAME = "batchBookingPreferred";
    static final String CURRENCY_STRING = "currency";

    private DateFieldValidator dateFieldValidator;
    private CurrencyValidator currencyValidator;

    private TppRedirectUriBodyValidatorImpl tppRedirectUriBodyValidator;

    private final StandardPaymentProductsResolver standardPaymentProductsResolver;
    private final FieldExtractor fieldExtractor;
    private final PathParameterExtractor pathParameterExtractor;
    private CountryPaymentValidatorResolver countryPaymentValidatorResolver;

    @Autowired
    public PaymentBodyValidatorImpl(ErrorBuildingService errorBuildingService, Xs2aObjectMapper xs2aObjectMapper,
                                    StandardPaymentProductsResolver standardPaymentProductsResolver,
                                    TppRedirectUriBodyValidatorImpl tppRedirectUriBodyValidator,
                                    DateFieldValidator dateFieldValidator, FieldExtractor fieldExtractor,
                                    CurrencyValidator currencyValidator,
                                    PathParameterExtractor pathParameterExtractor, CountryPaymentValidatorResolver countryPaymentValidatorResolver,
                                    FieldLengthValidator fieldLengthValidator) {
        super(errorBuildingService, xs2aObjectMapper, fieldLengthValidator);
        this.standardPaymentProductsResolver = standardPaymentProductsResolver;
        this.dateFieldValidator = dateFieldValidator;
        this.tppRedirectUriBodyValidator = tppRedirectUriBodyValidator;
        this.fieldExtractor = fieldExtractor;
        this.currencyValidator = currencyValidator;
        this.pathParameterExtractor = pathParameterExtractor;
        this.countryPaymentValidatorResolver = countryPaymentValidatorResolver;
    }

    @Override
    public MessageError validate(HttpServletRequest request, MessageError messageError) {
        if (isRawPaymentProduct(getPathParameters(request))) {
            log.info("Raw payment product is detected.");
            return messageError;
        }

        return super.validate(request, messageError);
    }

    @Override
    public MessageError validateBodyFields(HttpServletRequest request, MessageError messageError) {
        tppRedirectUriBodyValidator.validate(request, messageError);

        String paymentService = getPathParameters(request).get(PAYMENT_SERVICE_PATH_VAR);
        return countryPaymentValidatorResolver.getPaymentBodyFieldValidator()
                   .validate(request, paymentService, messageError);
    }

    @Override
    public MessageError validateRawData(HttpServletRequest request, MessageError messageError) {
        dateFieldValidator.validateDayOfExecution(request, messageError);
        dateFieldValidator.validateDateFormat(request, PAYMENT_DATE_FIELDS.getDateFields(), messageError);

        validateCurrency(request, messageError);
        validateBulkPaymentFields(request, messageError);
        validateFrequencyForPeriodicPayment(request, messageError);
        validatePurposeCode(request, messageError);
        validateChargeBearerList(request, messageError);
        return messageError;
    }

    private void validateCurrency(HttpServletRequest request, MessageError messageError) {
        List<String> currencyList = getCurrencyOptionalString(request);
        if (!currencyList.isEmpty()) {
            currencyList.forEach(c -> currencyValidator.validateCurrency(c, messageError));
        }
    }

    private List<String> getCurrencyOptionalString(HttpServletRequest request) {
        return fieldExtractor.extractOptionalList(request, CURRENCY_STRING);
    }

    private void validateBulkPaymentFields(HttpServletRequest request, MessageError messageError) {
        boolean isBulkPayment = getPathParameters(request).get(PAYMENT_SERVICE_PATH_VAR).equals(BULK_PAYMENT_PATH_VAR);
        if (isBulkPayment) {
            validateBatchBookingPreferredField(request, messageError);
        }
    }

    private void validateBatchBookingPreferredField(HttpServletRequest request, MessageError messageError) {
        Optional<String> fieldValue = fieldExtractor.extractOptionalField(request, BATCH_BOOKING_PREFERRED_FIELD_NAME);
        if (fieldValue.isPresent()) {
            try {
                BooleanUtils.toBoolean(fieldValue.get(), "true", "false");
            } catch (IllegalArgumentException e) {
                errorBuildingService.enrichMessageError(messageError, TppMessageInformation.of(FORMAT_ERROR_BOOLEAN_VALUE, BATCH_BOOKING_PREFERRED_FIELD_NAME));
            }
        }
    }

    private void validateFrequencyForPeriodicPayment(HttpServletRequest request, MessageError messageError) {
        boolean isPeriodicPayment = getPathParameters(request).get(PAYMENT_SERVICE_PATH_VAR).equals(PERIODIC_PAYMENT_PATH_VAR);
        if (isPeriodicPayment) {
            Optional<String> frequencyOptional = fieldExtractor.extractField(request, FREQUENCY_FIELD_NAME, messageError);
            if (frequencyOptional.isEmpty()) {
                errorBuildingService.enrichMessageError(messageError, TppMessageInformation.of(FORMAT_ERROR_NULL_VALUE, FREQUENCY_FIELD_NAME));
            } else if (FrequencyCode.fromValue(frequencyOptional.get()) == null) {
                errorBuildingService.enrichMessageError(messageError, TppMessageInformation.of(FORMAT_ERROR_WRONG_FORMAT_VALUE, FREQUENCY_FIELD_NAME));
            } else if (FrequencyCode.MONTHLYVARIABLE.equals(FrequencyCode.fromValue(frequencyOptional.get()))) {
                dateFieldValidator.validateMonthsOfExecution(request, messageError);
            }
        }
    }

    private void validateChargeBearerList(HttpServletRequest request, MessageError messageError) {
        List<String> chargeBearerList = getChargeBearerOptionalString(request);
        if (!chargeBearerList.isEmpty()) {
            chargeBearerList.forEach(c -> validateChargeBearer(messageError, c));
        }
    }

    private List<String> getChargeBearerOptionalString(HttpServletRequest request) {
        return fieldExtractor.extractOptionalList(request, CHARGE_BEARER_FIELD_NAME);
    }

    private void validateChargeBearer(MessageError messageError, String chargeBearer) {
        if (chargeBearer != null
                && ChargeBearer.fromValue(chargeBearer) == null) {
            errorBuildingService.enrichMessageError(messageError, TppMessageInformation.of(FORMAT_ERROR_WRONG_FORMAT_VALUE, CHARGE_BEARER_FIELD_NAME));
        }
    }

    private boolean isRawPaymentProduct(Map<String, String> pathParametersMap) {
        String paymentProduct = pathParametersMap.get(PAYMENT_PRODUCT_PATH_VAR);
        return standardPaymentProductsResolver.isRawPaymentProduct(paymentProduct);
    }

    private void validatePurposeCode(HttpServletRequest request, MessageError messageError) {
        String purposeCode = fieldExtractor.extractField(request, PURPOSE_CODE_FIELD_NAME, messageError).orElse(null);

        if (purposeCode != null
                && PurposeCode.fromValue(purposeCode) == null) {
            errorBuildingService.enrichMessageError(messageError, TppMessageInformation.of(FORMAT_ERROR_WRONG_FORMAT_VALUE, PURPOSE_CODE_FIELD_NAME));
        }
    }

    private Map<String, String> getPathParameters(HttpServletRequest request) {
        return pathParameterExtractor.extractParameters(request);
    }
}
