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

package de.adorsys.psd2.xs2a.web.controller;

import de.adorsys.psd2.api.v2.ConfirmationOfFundsApi;
import de.adorsys.psd2.core.data.piis.v1.PiisConsent;
import de.adorsys.psd2.model.ConsentsConfirmationOfFunds;
import de.adorsys.psd2.model.StartScaprocessResponse;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.profile.PiisConsentSupported;
import de.adorsys.psd2.xs2a.core.psu.AdditionalPsuIdData;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.domain.HrefType;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.authorisation.AuthorisationResponse;
import de.adorsys.psd2.xs2a.domain.consent.*;
import de.adorsys.psd2.xs2a.domain.fund.CreatePiisConsentRequest;
import de.adorsys.psd2.xs2a.service.PiisConsentService;
import de.adorsys.psd2.xs2a.service.mapper.ResponseMapper;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ResponseErrorMapper;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.web.header.ConsentHeadersBuilder;
import de.adorsys.psd2.xs2a.web.header.ResponseHeaders;
import de.adorsys.psd2.xs2a.web.mapper.AuthorisationMapper;
import de.adorsys.psd2.xs2a.web.mapper.ConsentModelMapper;
import de.adorsys.psd2.xs2a.web.mapper.PiisConsentModelMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static de.adorsys.psd2.xs2a.core.domain.TppMessageInformation.of;

@Slf4j
@RestController
@AllArgsConstructor
public class ConfirmationOfFundsController implements ConfirmationOfFundsApi {
    private final PiisConsentService piisConsentService;
    private final ResponseErrorMapper responseErrorMapper;
    private final PiisConsentModelMapper piisConsentModelMapper;
    private final ResponseMapper responseMapper;
    private final AspspProfileServiceWrapper profileService;
    private final ConsentHeadersBuilder consentHeadersBuilder;
    private final AuthorisationMapper authorisationMapper;
    private final ConsentModelMapper consentModelMapper;

    @Override
    public ResponseEntity createConsentConfirmationOfFunds(UUID xRequestID, ConsentsConfirmationOfFunds body, String digest, String signature, byte[] tpPSignatureCertificate,
                                                           String psuId, String psUIDType, String psUCorporateID, String psUCorporateIDType,
                                                           String tpPRedirectPreferred, String tpPRedirectURI, String tpPNokRedirectURI, String tpPExplicitAuthorisationPreferred,
                                                           String psUIPAddress, String psUIPPort, String psUAccept, String psUAcceptCharset, String psUAcceptEncoding, String psUAcceptLanguage,
                                                           String psUUserAgent, String psUHttpMethod, UUID psUDeviceID, String psUGeoLocation) {

        if (profileService.getPiisConsentSupported() != PiisConsentSupported.TPP_CONSENT_SUPPORTED) {
            MessageError messageError = new MessageError(ErrorType.PIIS_405, of(MessageErrorCode.SERVICE_INVALID_405));
            return responseErrorMapper.generateErrorResponse(messageError);
        }

        PsuIdData psuData = new PsuIdData(psuId, psUIDType, psUCorporateID, psUCorporateIDType, psUIPAddress,
                                          new AdditionalPsuIdData(psUIPPort, psUUserAgent, psUGeoLocation, psUAccept, psUAcceptCharset, psUAcceptEncoding, psUAcceptLanguage, psUHttpMethod, psUDeviceID));

        CreatePiisConsentRequest createPiisConsentRequest = piisConsentModelMapper.toCreatePiisConsentRequest(body);
        ResponseObject<Xs2aConfirmationOfFundsResponse> xs2aConfirmationOfFundsResponseResponseObject = piisConsentService.createPiisConsentWithResponse(createPiisConsentRequest, psuData, BooleanUtils.toBoolean(tpPExplicitAuthorisationPreferred));
        if (xs2aConfirmationOfFundsResponseResponseObject.hasError()) {
            return responseErrorMapper.generateErrorResponse(xs2aConfirmationOfFundsResponseResponseObject.getError());
        }

        Xs2aConfirmationOfFundsResponse xs2aConfirmationOfFundsResponse = xs2aConfirmationOfFundsResponseResponseObject.getBody();

        ResponseHeaders headers = consentHeadersBuilder.buildCreateConsentHeaders(xs2aConfirmationOfFundsResponse.getAuthorizationId(),
                                                                                  Optional.ofNullable(xs2aConfirmationOfFundsResponse.getLinks().getSelf())
                                                                                      .map(HrefType::getHref)
                                                                                      .orElseThrow(() -> new IllegalArgumentException("Wrong href type in self link")));

        return responseMapper.created(xs2aConfirmationOfFundsResponseResponseObject, piisConsentModelMapper::mapToConsentsConfirmationOfFundsResponse, headers);
    }

    @Override
    public ResponseEntity deleteConsentConfirmationOfFunds(String consentId, UUID xRequestID, String digest, String signature, byte[] tpPSignatureCertificate, String psUIPAddress, String psUIPPort, String psUAccept, String psUAcceptCharset, String psUAcceptEncoding, String psUAcceptLanguage, String psUUserAgent, String psUHttpMethod, UUID psUDeviceID, String psUGeoLocation) {
        ResponseObject<Void> response = piisConsentService.deleteAccountConsentsById(consentId);
        return response.hasError()
                   ? responseErrorMapper.generateErrorResponse(response.getError())
                   : responseMapper.delete(response);
    }

    @Override
    public ResponseEntity getConsentConfirmationOfFunds(String consentId, UUID xRequestID, String digest, String signature, byte[] tpPSignatureCertificate, String psUIPAddress, String psUIPPort, String psUAccept, String psUAcceptCharset, String psUAcceptEncoding, String psUAcceptLanguage, String psUUserAgent, String psUHttpMethod, UUID psUDeviceID, String psUGeoLocation) {
        ResponseObject<PiisConsent> piisConsentResponseObject = piisConsentService.getPiisConsentById(consentId);
        return piisConsentResponseObject.hasError()
                   ? responseErrorMapper.generateErrorResponse(piisConsentResponseObject.getError())
                   : responseMapper.ok(piisConsentResponseObject, piisConsentModelMapper::mapToConsentConfirmationOfFundsContentResponse);
    }

    @Override
    public ResponseEntity getConsentConfirmationOfFundsStatus(String consentId, UUID xRequestID, String digest, String signature, byte[] tpPSignatureCertificate, String psUIPAddress, String psUIPPort, String psUAccept, String psUAcceptCharset, String psUAcceptEncoding, String psUAcceptLanguage, String psUUserAgent, String psUHttpMethod, UUID psUDeviceID, String psUGeoLocation) {
        ResponseObject<ConsentStatusResponse> accountConsentsStatusByIdResponse = piisConsentService.getPiisConsentStatusById(consentId);
        return accountConsentsStatusByIdResponse.hasError()
                   ? responseErrorMapper.generateErrorResponse(accountConsentsStatusByIdResponse.getError())
                   : responseMapper.ok(accountConsentsStatusByIdResponse, piisConsentModelMapper::mapToConsentConfirmationOfFundsStatusResponse);
    }

    @Override
    public ResponseEntity getConsentAuthorisation(String consentId, UUID xRequestID, String digest, String signature, byte[] tpPSignatureCertificate, String psUIPAddress, String psUIPPort, String psUAccept, String psUAcceptCharset, String psUAcceptEncoding, String psUAcceptLanguage, String psUUserAgent, String psUHttpMethod, UUID psUDeviceID, String psUGeoLocation) {
        ResponseObject<Xs2aAuthorisationSubResources> consentInitiationAuthorisationsResponse = piisConsentService.getConsentInitiationAuthorisations(consentId);
        return consentInitiationAuthorisationsResponse.hasError()
                   ? responseErrorMapper.generateErrorResponse(consentInitiationAuthorisationsResponse.getError())
                   : responseMapper.ok(consentInitiationAuthorisationsResponse, authorisationMapper::mapToAuthorisations);
    }

    @Override
    public ResponseEntity getConsentScaStatus(String consentId, String authorisationId, UUID xRequestID, String digest, String signature, byte[] tpPSignatureCertificate, String psUIPAddress, String psUIPPort, String psUAccept, String psUAcceptCharset, String psUAcceptEncoding, String psUAcceptLanguage, String psUUserAgent, String psUHttpMethod, UUID psUDeviceID, String psUGeoLocation) {
        ResponseObject<Xs2aScaStatusResponse> consentAuthorisationScaStatusResponse = piisConsentService.getConsentAuthorisationScaStatus(consentId, authorisationId);
        return consentAuthorisationScaStatusResponse.hasError()
                   ? responseErrorMapper.generateErrorResponse(consentAuthorisationScaStatusResponse.getError())
                   : responseMapper.ok(consentAuthorisationScaStatusResponse, authorisationMapper::mapToScaStatusResponse);
    }

    @Override
    public ResponseEntity<StartScaprocessResponse> startConsentAuthorisation(UUID xRequestID, String consentId, Object body, String digest, String signature, byte[] tpPSignatureCertificate, String psuId, String psUIDType, String psUCorporateID, String psUCorporateIDType, String tpPRedirectPreferred, String tpPRedirectURI, String tpPNokRedirectURI, String tpPNotificationURI, String tpPNotificationContentPreferred, String psUIPAddress, String psUIPPort, String psUAccept, String psUAcceptCharset, String psUAcceptEncoding, String psUAcceptLanguage, String psUUserAgent, String psUHttpMethod, UUID psUDeviceID, String psUGeoLocation) {

        AdditionalPsuIdData additionalPsuIdData = new AdditionalPsuIdData(psUIPPort, psUUserAgent, psUGeoLocation, psUAccept, psUAcceptCharset, psUAcceptEncoding, psUAcceptLanguage, psUHttpMethod, psUDeviceID);
        PsuIdData psuData = new PsuIdData(psuId, psUIDType, psUCorporateID, psUCorporateIDType, psUIPAddress, additionalPsuIdData);

        String password = authorisationMapper.mapToPasswordFromBody((Map) body);

        ResponseObject<AuthorisationResponse> createResponse = piisConsentService.createPiisAuthorisation(psuData, consentId, password);

        if (createResponse.hasError()) {
            return responseErrorMapper.generateErrorResponse(createResponse.getError());
        }

        AuthorisationResponse authorisationResponse = createResponse.getBody();
        ResponseHeaders responseHeaders = consentHeadersBuilder.buildStartAuthorisationHeaders(authorisationResponse.getAuthorisationId());

        return responseMapper.created(ResponseObject.builder()
                                          .body(authorisationMapper.mapToConsentCreateOrUpdateAuthorisationResponse(createResponse))
                                          .build(),
                                      responseHeaders);
    }

    @Override
    public ResponseEntity<Object> updateConsentsPsuData(UUID xRequestID, String consentId, String authorisationId, Object body, String digest, String signature, byte[] tpPSignatureCertificate, String psuId, String psUIDType, String psUCorporateID, String psUCorporateIDType, String psUIPAddress, String psUIPPort, String psUAccept, String psUAcceptCharset, String psUAcceptEncoding, String psUAcceptLanguage, String psUUserAgent, String psUHttpMethod, UUID psUDeviceID, String psUGeoLocation) {
        PsuIdData psuData = new PsuIdData(psuId, psUIDType, psUCorporateID, psUCorporateIDType, psUIPAddress);

        return updatePiisAuthorisation(psuData, authorisationId, consentId, body);
    }

    private ResponseEntity<Object> updatePiisAuthorisation(PsuIdData psuData, String authorisationId, String consentId, Object body) {
        ConsentAuthorisationsParameters updatePsuDataRequest = consentModelMapper.mapToUpdatePsuData(psuData, consentId, authorisationId, (Map) body);
        ResponseObject<UpdateConsentPsuDataResponse> updateConsentPsuDataResponse = piisConsentService.updateConsentPsuData(updatePsuDataRequest);

        if (updateConsentPsuDataResponse.hasError()) {
            return responseErrorMapper.generateErrorResponse(updateConsentPsuDataResponse.getError());
        }

        ResponseHeaders responseHeaders = consentHeadersBuilder.buildUpdatePsuDataHeaders(authorisationId);

        return responseMapper.ok(updateConsentPsuDataResponse, authorisationMapper::mapToConsentUpdatePsuAuthenticationResponse, responseHeaders);
    }
}
