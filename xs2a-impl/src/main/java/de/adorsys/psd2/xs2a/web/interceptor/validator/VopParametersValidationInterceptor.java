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

package de.adorsys.psd2.xs2a.web.interceptor.validator;

import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.profile.BulkVop;
import de.adorsys.psd2.xs2a.core.profile.SingleVop;
import de.adorsys.psd2.xs2a.web.PathParameterExtractor;
import de.adorsys.psd2.xs2a.web.Psd2VopMethodNameConstant;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.RESOURCE_UNKNOWN_404;

/**
 * This interceptor is used for incoming VOP requests validation to ensure that party-verification-endpoint
 * and bulk-party-verification-endpoint path variables contain valid values. This interceptor should be
 * registered (and launched) before the main request validation interceptor.
 * <p>
 * Valid values for party-verification-endpoint: payee-verifications, payer-verifications, party-verifications
 * Valid values for bulk-party-verification-endpoint: bulk-payee-verifications, bulk-payer-verifications, bulk-party-verifications
 * <p>
 * If an invalid endpoint value is provided, the request is rejected with a 404 (Party verification not supported) status.
 *
 * <p>Header Validation: Only X-Request-ID header is required. All other headers (Digest, X-JWS-Signature,
 * Body-Sig-Profile, Accept, API-Contract-ID) are optional and can be null or empty.</p>
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class VopParametersValidationInterceptor implements HandlerInterceptor {

    private static final String PARTY_VERIFICATION_ENDPOINT = "party-verification-endpoint";
    private static final String BULK_PARTY_VERIFICATION_ENDPOINT = "bulk-party-verification-endpoint";

    private final ErrorBuildingService errorBuildingService;
    private final PathParameterExtractor pathParameterExtractor;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        return isRequestValid(request, response, handler);
    }

    private boolean isRequestValid(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {

        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            String methodName = handlerMethod.getMethod().getName();

            // Filter requests that are addressed to VOP API
            boolean vopRequest = Arrays.stream(Psd2VopMethodNameConstant.values())
                                     .anyMatch(mn -> mn.getValue().equals(methodName));

            if (vopRequest) {
                return isVopRequestValid(request, response, methodName);
            }
        }

        return true;
    }

    private boolean isVopRequestValid(HttpServletRequest request, HttpServletResponse response, String methodName) throws IOException {
        // Creditor confirmation uses payment path variables, not VOP endpoint variables
        if (isVopConfirmationEndpoint(methodName)) {
            return true; // Skip validation for creditor confirmation
        }

        Map<String, String> pathParameters = pathParameterExtractor.extractParameters(request);

        // Validate party-verification-endpoint for single VOP requests
        if (isVopSingleEndpoint(response, methodName, pathParameters)) {
            return false;
        }

        // Validate bulk-party-verification-endpoint for bulk VOP requests
        return !isVopBulkEndpoint(response, methodName, pathParameters);
    }

    private boolean isVopBulkEndpoint(HttpServletResponse response, String methodName, Map<String, String> pathParameters) throws IOException {
        if (Psd2VopMethodNameConstant.BULK_VERIFICATION_OF_PARTY.getValue().equals(methodName) ||
            Psd2VopMethodNameConstant.GET_BULK_VERIFICATION_OF_PARTY.getValue().equals(methodName) ||
            Psd2VopMethodNameConstant.GET_BULK_VERIFICATION_OF_PARTY_STATUS.getValue().equals(methodName)) {

            String bulkPartyVerificationEndpoint = pathParameters.get(BULK_PARTY_VERIFICATION_ENDPOINT);

            if (bulkPartyVerificationEndpoint == null || BulkVop.getByValue(bulkPartyVerificationEndpoint).isEmpty()) {
                log.warn("Invalid bulk-party-verification-endpoint: {}", bulkPartyVerificationEndpoint);
                MessageError messageError = new MessageError(ErrorType.VOP_404,
                    TppMessageInformation.of(RESOURCE_UNKNOWN_404, "Party verification not supported"));
                errorBuildingService.buildVopErrorResponse(response, messageError);
                return true;
            }
        }
        return false;
    }

    private boolean isVopSingleEndpoint(HttpServletResponse response, String methodName, Map<String, String> pathParameters) throws IOException {
        if (Psd2VopMethodNameConstant.VERIFICATION_OF_PARTY.getValue().equals(methodName)) {
            String partyVerificationEndpoint = pathParameters.get(PARTY_VERIFICATION_ENDPOINT);

            if (partyVerificationEndpoint == null || SingleVop.getByValue(partyVerificationEndpoint).isEmpty()) {
                log.warn("Invalid party-verification-endpoint: {}", partyVerificationEndpoint);
                MessageError messageError = new MessageError(ErrorType.VOP_404,
                    TppMessageInformation.of(RESOURCE_UNKNOWN_404, "Party verification not supported"));
                errorBuildingService.buildVopErrorResponse(response, messageError);
                return true;
            }
        }
        return false;
    }

    private static boolean isVopConfirmationEndpoint(String methodName) {
        return Psd2VopMethodNameConstant.CREDITOR_CONFIRMATION.getValue().equals(methodName);
    }
}
