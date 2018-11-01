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

package de.adorsys.psd2.consent.client.sample;

import de.adorsys.psd2.consent.api.*;
import de.adorsys.psd2.consent.api.ais.*;
import de.adorsys.psd2.consent.api.pis.CmsRemittance;
import de.adorsys.psd2.consent.api.pis.PisConsentStatusResponse;
import de.adorsys.psd2.consent.api.pis.PisPayment;
import de.adorsys.psd2.consent.api.pis.proto.CreatePisConsentResponse;
import de.adorsys.psd2.consent.api.pis.proto.PisConsentRequest;
import de.adorsys.psd2.consent.api.pis.proto.PisConsentResponse;
import de.adorsys.psd2.consent.client.cms.CmsServiceInvoker;
import de.adorsys.psd2.consent.client.cms.model.ais.*;
import de.adorsys.psd2.consent.client.cms.model.pis.*;
import de.adorsys.psd2.consent.client.core.Configuration;
import de.adorsys.psd2.consent.client.core.util.HttpUriParams;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.profile.PaymentProduct;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static java.util.Collections.singletonList;

public class CmsExecutor {
    private static final Log logger = LogFactory.getLog(CmsExecutor.class);

    private static final String CMS_BASE_URL = "http://localhost:38080";
    private static final int CONNECTION_TIMEOUT = 5000;
    private static final int CONNECTION_REQUEST_TIMEOUT = 5000;
    private static final String CONSENT_ID = "sKrhvGddmlkItKsvBZwJyXG7Bm7Qbq2FnrJ3F2vB9mVKwiMoElPiohTItFBnfJsohO5sHYhMF_ymmQwlQdgkxA==_=_bS6p6XvTWI";
    private static final String PAYMENT_ID = "sKrhvGddmlkItKsvBZwJyXG7Bm7Qbq2FnrJ3F2vB9mVKwiMoElPiohTItFBnfJsohO5sHYhMF_ymmQwlQdgkxA==_=_bS6p6XvTWI";

    /**
     * Makes calls to CMS PIS and AIS endpoints and logs the response
     *
     * @param args Array of strings, provided with main() execution
     */
    public static void main(String[] args) throws IOException, URISyntaxException {
        Configuration configuration = new Configuration(CMS_BASE_URL, CONNECTION_TIMEOUT, CONNECTION_REQUEST_TIMEOUT);

        CmsServiceInvoker cmsServiceInvoker = configuration.getRestServiceInvoker();

        createAisConsent(cmsServiceInvoker);
        getAisConsentById(cmsServiceInvoker);
        getConsentStatusById(cmsServiceInvoker);
        saveConsentActionLog(cmsServiceInvoker);
        updateConsentAccess(cmsServiceInvoker);
        getAisConsentAspspData(cmsServiceInvoker);
        updateAisConsentAspspData(cmsServiceInvoker);
        updateConsentStatus(cmsServiceInvoker);

        createPaymentConsent(cmsServiceInvoker);
        getPaymentConsentById(cmsServiceInvoker);
        getPaymentConsentStatusById(cmsServiceInvoker);
        getPisConsentAspspData(cmsServiceInvoker);
        updatePisConsentAspspData(cmsServiceInvoker);
        updatePaymentConsentStatus(cmsServiceInvoker);
        getPaymentIdByEncryptedString(cmsServiceInvoker);
    }

    /**
     * Sends request to POST api/v1/ais/consent/ endpoint
     *
     * @param cmsServiceInvoker Service, performing rest call
     */
    private static void createAisConsent(CmsServiceInvoker cmsServiceInvoker) throws IOException, URISyntaxException {
        Optional<CreateAisConsentResponse> createAisResponse = Optional.ofNullable(cmsServiceInvoker.invoke(new CreateAisConsentMethod(buildAisConsentRequest())));
        createAisResponse.ifPresent(resp -> logger.info("Consent ID: " + resp.getConsentId()));
    }

    /**
     * Sends request to GET api/v1/ais/consent/{consent-id} endpoint
     *
     * @param cmsServiceInvoker Service, performing rest call
     */
    private static void getAisConsentById(CmsServiceInvoker cmsServiceInvoker) throws IOException, URISyntaxException {
        HttpUriParams uriParams = HttpUriParams.builder()
                                      .addPathVariable("consent-id", CONSENT_ID)
                                      .build();
        Optional<AisAccountConsent> aisAccountConsent = Optional.ofNullable(cmsServiceInvoker.invoke(new GetAisConsentMethod(uriParams)));
        aisAccountConsent.ifPresent(consent -> logger.info("Ais account consent status: " + consent.getConsentStatus()));
    }

    /**
     * Sends request to POST api/v1/ais/consent/action endpoint
     *
     * @param cmsServiceInvoker Service, performing rest call
     */
    private static void saveConsentActionLog(CmsServiceInvoker cmsServiceInvoker) throws IOException, URISyntaxException {
        cmsServiceInvoker.invoke(new SaveConsentActionLogMethod(new AisConsentActionRequest("tpp-id",
                                                                                            CONSENT_ID, ActionStatus.SUCCESS)));
    }

    /**
     * Sends request to GET api/v1/ais/consent/{consent-id}/status endpoint
     *
     * @param cmsServiceInvoker Service, performing rest call
     */
    private static void getConsentStatusById(CmsServiceInvoker cmsServiceInvoker) throws IOException, URISyntaxException {
        HttpUriParams uriParams = HttpUriParams.builder()
                                      .addPathVariable("consent-id", CONSENT_ID)
                                      .build();
        Optional<AisConsentStatusResponse> consentStatusResponse = Optional.ofNullable(cmsServiceInvoker.invoke(new GetConsentStatusByIdMethod(uriParams)));
        consentStatusResponse.ifPresent(status -> logger.info("Status of the consent: " + status.getConsentStatus().name()));
    }

    /**
     * Sends request to PUT api/v1/ais/consent/{consent-id}/access endpoint
     *
     * @param cmsServiceInvoker Service, performing rest call
     */
    private static void updateConsentAccess(CmsServiceInvoker cmsServiceInvoker) throws IOException, URISyntaxException {
        HttpUriParams uriParams = HttpUriParams.builder()
                                      .addPathVariable("consent-id", CONSENT_ID)
                                      .build();
        Optional<CreateAisConsentResponse> updateAccessResponse = Optional.ofNullable(cmsServiceInvoker.invoke(new UpdateConsentAccessMethod(buildAccess(), uriParams)));
        updateAccessResponse.ifPresent(resp -> logger.info("Access was updated in: " + resp.getConsentId()));
    }

    /**
     * Sends request to GET api/v1/ais/consent/{consent-id}/aspsp-consent-data endpoint
     *
     * @param cmsServiceInvoker Service, performing rest call
     */
    private static void getAisConsentAspspData(CmsServiceInvoker cmsServiceInvoker) throws IOException, URISyntaxException {
        HttpUriParams uriParams = HttpUriParams.builder()
                                      .addPathVariable("consent-id", CONSENT_ID)
                                      .build();
        Optional<CmsAspspConsentDataBase64> getAspspDataResponse = Optional.of(cmsServiceInvoker.invoke(new GetAisConsentAspspDataMethod(uriParams)));
        getAspspDataResponse.ifPresent(resp -> logger.info("Ais consent aspsp data: " + resp.getAspspConsentDataBase64()));
    }

    /**
     * Sends request to PUT api/v1/ais/consent/{consent-id}/aspspConsentData endpoint
     *
     * @param cmsServiceInvoker Service, performing rest call
     */
    private static void updateAisConsentAspspData(CmsServiceInvoker cmsServiceInvoker) throws IOException, URISyntaxException {
        HttpUriParams uriParams = HttpUriParams.builder()
                                      .addPathVariable("consent-id", CONSENT_ID)
                                      .build();
        Optional<CmsAspspConsentDataBase64> updateAspspDataResponse = Optional.ofNullable(cmsServiceInvoker.invoke(new UpdateAisConsentAspspDataMethod(buildCmsAspspConsentDataBase64(), uriParams)));
        updateAspspDataResponse.ifPresent(resp -> logger.info("Ais consent aspsp data was updated in: " + resp.getConsentId()));
    }

    /**
     * Sends request to PUT api/v1/ais/consent/{consent-id}/status/{status} endpoint
     *
     * @param cmsServiceInvoker Service, performing rest call
     */
    private static void updateConsentStatus(CmsServiceInvoker cmsServiceInvoker) throws IOException, URISyntaxException {
        HttpUriParams uriParams = HttpUriParams.builder()
                                      .addPathVariable("consent-id", CONSENT_ID)
                                      .addPathVariable("status", ConsentStatus.REVOKED_BY_PSU.name())
                                      .build();
        cmsServiceInvoker.invoke(new UpdateConsentStatusMethod(uriParams));
    }

    /**
     * Creates a test AIS consent request
     *
     * @return CreateAisConsentRequest
     */
    private static CreateAisConsentRequest buildAisConsentRequest() {
        CreateAisConsentRequest request = new CreateAisConsentRequest();
        request.setAccess(buildAccess());
        request.setCombinedServiceIndicator(true);
        request.setFrequencyPerDay(10);
        request.setPsuData(new PsuIdData("psu-id-1", null, null, null));
        request.setRecurringIndicator(true);
        request.setTppId("tpp-id-1");
        request.setValidUntil(LocalDate.of(2020, 12, 31));
        request.setTppRedirectPreferred(true);
        return request;
    }

    /**
     * Creates a test AIS account access info
     *
     * @return AisAccountAccessInfo
     */
    private static AisAccountAccessInfo buildAccess() {
        AisAccountAccessInfo info = new AisAccountAccessInfo();
        info.setAccounts(singletonList(new AccountInfo(UUID.randomUUID().toString(), "iban-1", "EUR")));
        return info;
    }

    /**
     * Creates consent aspsp data update request
     *
     * @return CmsAspspConsentDataBase64
     */
    private static CmsAspspConsentDataBase64 buildCmsAspspConsentDataBase64() {
        return new CmsAspspConsentDataBase64("encryptedId", Base64.getEncoder().encodeToString("decrypted consent data".getBytes()));
    }

    /**
     * Sends request to POST api/v1/pis/consent/ endpoint
     *
     * @param cmsServiceInvoker Service, performing rest call
     */
    private static void createPaymentConsent(CmsServiceInvoker cmsServiceInvoker) throws IOException, URISyntaxException {
        Optional<CreatePisConsentResponse> createPisResponse = Optional.ofNullable(cmsServiceInvoker.invoke(new CreatePaymentConsentMethod(buildPisConsentRequest())));
        createPisResponse.ifPresent(resp -> logger.info("Consent ID: " + resp.getConsentId()));
    }

    /**
     * Sends request to GET api/v1/pis/consent/{consent-id} endpoint
     *
     * @param cmsServiceInvoker Service, performing rest call
     */
    private static void getPaymentConsentById(CmsServiceInvoker cmsServiceInvoker) throws IOException, URISyntaxException {
        HttpUriParams uriParams = HttpUriParams.builder()
                                      .addPathVariable("consent-id", CONSENT_ID)
                                      .build();
        Optional<PisConsentResponse> pisAccountConsent = Optional.ofNullable(cmsServiceInvoker.invoke(new GetPaymentConsentByIdMethod(uriParams)));
        pisAccountConsent.ifPresent(consent -> logger.info("Pis account consent status: " + consent.getConsentStatus()));
    }

    /**
     * Sends request to GET api/v1/pis/consent/{consent-id}/status endpoint
     *
     * @param cmsServiceInvoker Service, performing rest call
     */
    private static void getPaymentConsentStatusById(CmsServiceInvoker cmsServiceInvoker) throws IOException, URISyntaxException {
        HttpUriParams uriParams = HttpUriParams.builder()
                                      .addPathVariable("consent-id", CONSENT_ID)
                                      .build();
        Optional<PisConsentStatusResponse> consentStatusResponse = Optional.ofNullable(cmsServiceInvoker.invoke(new GetPaymentConsentStatusByIdMethod(uriParams)));
        consentStatusResponse.ifPresent(response -> logger.info("Status of the consent: " + response.getConsentStatus().name()));
    }

    /**
     * Sends request to GET api/v1/pis/consent/{consent-id}/aspsp-consent-data endpoint
     *
     * @param cmsServiceInvoker Service, performing rest call
     */
    private static void getPisConsentAspspData(CmsServiceInvoker cmsServiceInvoker) throws IOException, URISyntaxException {
        HttpUriParams uriParams = HttpUriParams.builder()
                                      .addPathVariable("payment-id", PAYMENT_ID)
                                      .build();
        Optional<CmsAspspConsentDataBase64> getAspspDataResponse = Optional.ofNullable(cmsServiceInvoker.invoke(new GetPisConsentAspspDataMethod(uriParams)));
        getAspspDataResponse.ifPresent(resp -> logger.info("Pis consent aspsp data: " + resp.getAspspConsentDataBase64()));
    }


    /**
     * Sends request to PUT api/v1/pis/consent/{consent-id}/aspspConsentData endpoint
     *
     * @param cmsServiceInvoker Service, performing rest call
     */
    private static void updatePisConsentAspspData(CmsServiceInvoker cmsServiceInvoker) throws IOException, URISyntaxException {
        HttpUriParams uriParams = HttpUriParams.builder()
                                      .addPathVariable("consent-id", CONSENT_ID)
                                      .build();
        Optional<CmsAspspConsentDataBase64> updateAspspDataResponse = Optional.ofNullable(cmsServiceInvoker.invoke(new UpdatePisConsentAspspDataMethod(buildCmsAspspConsentDataBase64(), uriParams)));
        updateAspspDataResponse.ifPresent(resp -> logger.info("Pis consent aspsp data was updated in: " + resp.getConsentId()));
    }

    /**
     * Sends request to GET api/v1/pis/payment/{payment-id} endpoint
     *
     * @param cmsServiceInvoker Service, performing rest call
     */
    private static void getPaymentIdByEncryptedString(CmsServiceInvoker cmsServiceInvoker) throws IOException, URISyntaxException {
        HttpUriParams uriParams = HttpUriParams.builder()
                                      .addPathVariable("payment-id", PAYMENT_ID)
                                      .build();

        Optional<String> paymentId = Optional.ofNullable(cmsServiceInvoker.invoke(new GetPaymentIdByEncryptedStringMethod(uriParams)));
        paymentId.ifPresent(resp -> logger.info("Payment id decrypted: " + resp));
    }

    /**
     * Sends request to PUT api/v1/pis/consent/{consent-id}/status/{status} endpoint
     *
     * @param cmsServiceInvoker Service, performing rest call
     */
    private static void updatePaymentConsentStatus(CmsServiceInvoker cmsServiceInvoker) throws IOException, URISyntaxException {
        HttpUriParams uriParams = HttpUriParams.builder()
                                      .addPathVariable("consent-id", CONSENT_ID)
                                      .addPathVariable("status", ConsentStatus.REVOKED_BY_PSU.name())
                                      .build();
        cmsServiceInvoker.invoke(new UpdatePaymentConsentStatusMethod(uriParams));
    }

    /**
     * Creates a test PIS consent request
     *
     * @return PisConsentRequest
     */
    private static PisConsentRequest buildPisConsentRequest() {
        PisConsentRequest request = new PisConsentRequest();
        request.setPayments(singletonList(buildPisPayment()));
        request.setPaymentProduct(PaymentProduct.SEPA);
        request.setPaymentType(PaymentType.SINGLE);
        request.setTppInfo(buildCmsTppInfo("1234_registrationNumber", "Tpp company",
                                           Arrays.asList(CmsTppRole.PISP, CmsTppRole.AISP, CmsTppRole.PIISP, CmsTppRole.ASPSP),
                                           "authority id", "authority name", "Germany", "Organisation",
                                           "Organisation unit", "Nuremberg", "Bayern", "Redirect URI",
                                           "Nok redirect URI"));
        return request;
    }

    /**
     * Creates a test PIS payment
     *
     * @return PisPayment
     */
    private static PisPayment buildPisPayment() {
        PisPayment payment = new PisPayment();
        payment.setPaymentId("32454656712432");
        payment.setEndToEndIdentification("RI-123456789");
        payment.setDebtorAccount(new CmsAccountReference(UUID.randomUUID().toString(), "DE89370400440532013000", "89370400440532010000",
            "2356 5746 3217 1234", "2356xxxxxx1234", "+49(0)911 360698-0", Currency.getInstance("EUR")));
        payment.setDebtorAccount(new CmsAccountReference("DE89370400440532013000", "89370400440532010000",
                                                         "2356 5746 3217 1234", "2356xxxxxx1234", "+49(0)911 360698-0", Currency.getInstance("EUR")));
        payment.setUltimateDebtor("Mueller");
        payment.setCurrency(Currency.getInstance("EUR"));
        payment.setAmount(BigDecimal.valueOf(1000));
        payment.setCreditorAccount(new CmsAccountReference(UUID.randomUUID().toString(),"DE89370400440532013000", "89370400440532010000", "2356 5746 3217 1234",
            "2356xxxxxx1234", "+49(0)911 360698-0", Currency.getInstance("EUR")));
        payment.setCreditorAccount(new CmsAccountReference("DE89370400440532013000", "89370400440532010000", "2356 5746 3217 1234",
                                                           "2356xxxxxx1234", "+49(0)911 360698-0", Currency.getInstance("EUR")));
        payment.setCreditorAgent("Telekom");
        payment.setCreditorName("Telekom");
        payment.setCreditorAddress(buildCmsAddress("Herrnstraße", "123-34", "Nürnberg", "90431", "Germany"));
        payment.setRemittanceInformationUnstructured("Ref. Number TELEKOM-1222");
        payment.setRemittanceInformationStructured(buildCmsRemittance("Ref Number Merchant", "reference type", "reference issuer"));
        payment.setRequestedExecutionDate(LocalDate.of(2020, 1, 1));
        payment.setRequestedExecutionTime(OffsetDateTime.parse("2020-01-01T15:30:35.035Z",
                                                               DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")));
        payment.setUltimateCreditor("Telekom");
        payment.setPurposeCode("BCENECEQ");
        payment.setStartDate(LocalDate.of(2020, 1, 1));
        payment.setEndDate(LocalDate.of(2020, 3, 3));
        payment.setExecutionRule("latest");
        payment.setFrequency("ANNUAL");
        payment.setDayOfExecution(14);
        return payment;
    }

    /**
     * Creates a test CMS address entity for a PIS payment
     *
     * @param street         Street name
     * @param buildingNumber Building number
     * @param city           Name of the city
     * @param postalCode     Postal code
     * @param country        Name of the country
     * @return CmsAddress
     */
    private static CmsAddress buildCmsAddress(String street, String buildingNumber, String city,
                                              String postalCode, String country) {
        CmsAddress address = new CmsAddress();
        address.setStreet(street);
        address.setBuildingNumber(buildingNumber);
        address.setCity(city);
        address.setPostalCode(postalCode);
        address.setCountry(country);
        return address;
    }

    /**
     * Creates a test CMS remmitance entity for a PIS payment
     *
     * @param reference       Actual reference
     * @param referenceType   Reference type
     * @param referenceIssuer Issuer of the reference
     * @return CmsRemittance
     */
    private static CmsRemittance buildCmsRemittance(String reference, String referenceType, String referenceIssuer) {
        CmsRemittance remittance = new CmsRemittance();
        remittance.setReference(reference);
        remittance.setReferenceType(referenceType);
        remittance.setReferenceIssuer(referenceIssuer);
        return remittance;
    }

    /**
     * Creates CMS tpp info entity for a PIS consent request
     *
     * @param authorisationNumber Authorisation number
     * @param tppName             Tpp name
     * @param tppRoles            Tpp roles
     * @param authorityId         National competent authority id
     * @param authorityName       National competent authority name
     * @param country             Country
     * @param organisation        Organisation
     * @param organisationUnit    Organisation unit
     * @param city                City
     * @param state               State
     * @param redirectUri         Redirect URI
     * @param nokRedirectUri      Nok redirect URI
     * @return CmsTppInfo
     */
    private static CmsTppInfo buildCmsTppInfo(String authorisationNumber, String tppName, List<CmsTppRole> tppRoles,
                                              String authorityId, String authorityName, String country,
                                              String organisation, String organisationUnit, String city, String state,
                                              String redirectUri, String nokRedirectUri) {
        CmsTppInfo tppInfo = new CmsTppInfo();
        tppInfo.setAuthorisationNumber(authorisationNumber);
        tppInfo.setTppName(tppName);
        tppInfo.setTppRoles(tppRoles);
        tppInfo.setAuthorityId(authorityId);
        tppInfo.setAuthorityName(authorityName);
        tppInfo.setCountry(country);
        tppInfo.setOrganisation(organisation);
        tppInfo.setOrganisationUnit(organisationUnit);
        tppInfo.setCity(city);
        tppInfo.setState(state);
        tppInfo.setRedirectUri(redirectUri);
        tppInfo.setNokRedirectUri(nokRedirectUri);
        return tppInfo;
    }
}
