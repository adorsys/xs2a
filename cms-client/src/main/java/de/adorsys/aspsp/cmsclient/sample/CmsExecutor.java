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

package de.adorsys.aspsp.cmsclient.sample;

import de.adorsys.aspsp.cmsclient.cms.CmsServiceInvoker;
import de.adorsys.aspsp.cmsclient.cms.model.ais.*;
import de.adorsys.aspsp.cmsclient.cms.model.pis.CreatePaymentConsentMethod;
import de.adorsys.aspsp.cmsclient.cms.model.pis.GetPaymentConsentByIdMethod;
import de.adorsys.aspsp.cmsclient.cms.model.pis.GetPaymentConsentStatusByIdMethod;
import de.adorsys.aspsp.cmsclient.cms.model.pis.UpdatePaymentConsentStatusMethod;
import de.adorsys.aspsp.cmsclient.core.Configuration;
import de.adorsys.aspsp.cmsclient.core.util.HttpUriParams;
import de.adorsys.aspsp.xs2a.consent.api.*;
import de.adorsys.aspsp.xs2a.consent.api.ais.AisAccountAccessInfo;
import de.adorsys.aspsp.xs2a.consent.api.ais.AisAccountConsent;
import de.adorsys.aspsp.xs2a.consent.api.ais.CreateAisConsentRequest;
import de.adorsys.aspsp.xs2a.consent.api.ais.CreateAisConsentResponse;
import de.adorsys.aspsp.xs2a.consent.api.pis.PisPayment;
import de.adorsys.aspsp.xs2a.consent.api.pis.PisPaymentProduct;
import de.adorsys.aspsp.xs2a.consent.api.pis.PisPaymentType;
import de.adorsys.aspsp.xs2a.consent.api.pis.proto.CreatePisConsentResponse;
import de.adorsys.aspsp.xs2a.consent.api.pis.proto.PisConsentRequest;
import de.adorsys.aspsp.xs2a.consent.api.pis.proto.PisConsentResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Currency;
import java.util.Optional;

import static java.util.Collections.singletonList;

public class CmsExecutor {
    private static final Log logger = LogFactory.getLog(CmsExecutor.class);

    private static final String CMS_BASE_URL = "http://localhost:38080";
    private static final int CONNECTION_TIMEOUT = 5000;
    private static final int CONNECTION_REQUEST_TIMEOUT = 5000;
    private static String consentId = "Test consent id";

    public static void main(String[] args) throws IOException, URISyntaxException {
        Configuration configuration = new Configuration(CMS_BASE_URL, CONNECTION_TIMEOUT, CONNECTION_REQUEST_TIMEOUT);

        CmsServiceInvoker cmsServiceInvoker = configuration.getRestServiceInvoker();

        createAisConsent(cmsServiceInvoker);
        getAisConsentById(cmsServiceInvoker);
        getConsentStatusById(cmsServiceInvoker);
        saveConsentActionLog(cmsServiceInvoker);
        updateConsentStatus(cmsServiceInvoker);

        createPaymentConsent(cmsServiceInvoker);
        getPaymentConsentById(cmsServiceInvoker);
        getPaymentConsentStatusById(cmsServiceInvoker);
        updatePaymentConsentStatus(cmsServiceInvoker);
    }

    private static void createAisConsent(CmsServiceInvoker cmsServiceInvoker) throws IOException, URISyntaxException {
        Optional<CreateAisConsentResponse> createAisResponse = Optional.ofNullable(cmsServiceInvoker.invoke(new CreateAisConsentMethod(buildAisConsentRequest())));
        createAisResponse.ifPresent(resp -> consentId = resp.getConsentId());
        logger.info("Consent ID: " + consentId);
    }

    private static void getAisConsentById(CmsServiceInvoker cmsServiceInvoker) throws IOException, URISyntaxException {
        HttpUriParams uriParams = HttpUriParams.builder()
                                      .addPathVariable("consent-id", consentId)
                                      .build();
        Optional<AisAccountConsent> aisAccountConsent = Optional.ofNullable(cmsServiceInvoker.invoke(new GetAisConsentMethod(uriParams)));
        aisAccountConsent.ifPresent(consent -> logger.info("Ais account consent status: " + consent.getConsentStatus()));
    }

    private static void saveConsentActionLog(CmsServiceInvoker cmsServiceInvoker) throws IOException, URISyntaxException {
        cmsServiceInvoker.invoke(new SaveConsentActionLogMethod(new ConsentActionRequest("tpp-id", consentId, ActionStatus.SUCCESS)));
    }

    private static void getConsentStatusById(CmsServiceInvoker cmsServiceInvoker) throws IOException, URISyntaxException {
        HttpUriParams uriParams = HttpUriParams.builder()
                                      .addPathVariable("consent-id", consentId)
                                      .build();
        Optional<ConsentStatusResponse> consentStatusResponse = Optional.ofNullable(cmsServiceInvoker.invoke(new GetConsentStatusByIdMethod(uriParams)));
        consentStatusResponse.ifPresent(status -> logger.info("Status of the consent: " + status.getConsentStatus().name()));
    }

    private static void updateConsentStatus(CmsServiceInvoker cmsServiceInvoker) throws IOException, URISyntaxException {
        HttpUriParams uriParams = HttpUriParams.builder()
                                      .addPathVariable("consent-id", consentId)
                                      .addPathVariable("status", ConsentStatus.REVOKED_BY_PSU.name())
                                      .build();
        cmsServiceInvoker.invoke(new UpdateConsentStatusMethod(uriParams));
    }

    private static CreateAisConsentRequest buildAisConsentRequest() {
        CreateAisConsentRequest request = new CreateAisConsentRequest();
        request.setAccess(buildAccess());
        request.setCombinedServiceIndicator(true);
        request.setFrequencyPerDay(10);
        request.setPsuId("psu-id-1");
        request.setRecurringIndicator(true);
        request.setTppId("tpp-id-1");
        request.setValidUntil(LocalDate.of(2020, 12, 31));
        request.setTppRedirectPreferred(true);
        return request;
    }

    private static AisAccountAccessInfo buildAccess() {
        AisAccountAccessInfo info = new AisAccountAccessInfo();
        info.setAccounts(singletonList(new AccountInfo("iban-1", "EUR")));
        return info;
    }

    private static void createPaymentConsent(CmsServiceInvoker cmsServiceInvoker) throws IOException, URISyntaxException {
        Optional<CreatePisConsentResponse> createPisResponse = Optional.ofNullable(cmsServiceInvoker.invoke(new CreatePaymentConsentMethod(buildPisConsentRequest())));
        createPisResponse.ifPresent(resp -> consentId = resp.getConsentId());
        logger.info("Consent ID: " + consentId);
    }

    private static void getPaymentConsentById(CmsServiceInvoker cmsServiceInvoker) throws IOException, URISyntaxException {
        HttpUriParams uriParams = HttpUriParams.builder()
                                      .addPathVariable("consent-id", consentId)
                                      .build();
        Optional<PisConsentResponse> pisAccountConsent = Optional.ofNullable(cmsServiceInvoker.invoke(new GetPaymentConsentByIdMethod(uriParams)));
        pisAccountConsent.ifPresent(consent -> logger.info("Pis account consent status: " + consent.getConsentStatus()));
    }

    private static void getPaymentConsentStatusById(CmsServiceInvoker cmsServiceInvoker) throws IOException, URISyntaxException {
        HttpUriParams uriParams = HttpUriParams.builder()
                                      .addPathVariable("consent-id", consentId)
                                      .build();
        Optional<ConsentStatusResponse> consentStatusResponse = Optional.ofNullable(cmsServiceInvoker.invoke(new GetPaymentConsentStatusByIdMethod(uriParams)));
        consentStatusResponse.ifPresent(status -> logger.info("Status of the consent: " + status.getConsentStatus().name()));
    }

    private static void updatePaymentConsentStatus(CmsServiceInvoker cmsServiceInvoker) throws IOException, URISyntaxException {
        HttpUriParams uriParams = HttpUriParams.builder()
                                      .addPathVariable("consent-id", consentId)
                                      .addPathVariable("status", ConsentStatus.REVOKED_BY_PSU.name())
                                      .build();
        cmsServiceInvoker.invoke(new UpdatePaymentConsentStatusMethod(uriParams));
    }

    private static PisConsentRequest buildPisConsentRequest() {
        PisConsentRequest request = new PisConsentRequest();
        request.setPayments(Collections.singletonList(buildPisPayment()));
        request.setPaymentProduct(PisPaymentProduct.SCT);
        request.setPaymentType(PisPaymentType.SINGLE);
        request.setTppInfo(buildCmsTppInfo("1234_registrationNumber", "Tpp company", "Tpp role",
            "National competent authority", "Redirect URI", "Nok redirect URI"));
        request.setAspspConsentData("zzzzzzzz".getBytes());
        return request;
    }

    private static PisPayment buildPisPayment() {
        PisPayment payment = new PisPayment();
        payment.setPaymentId("32454656712432");
        payment.setEndToEndIdentification("RI-123456789");
        payment.setDebtorAccount(new CmsAccountReference("DE89370400440532013000", "89370400440532010000",
            "2356 5746 3217 1234", "2356xxxxxx1234", "+49(0)911 360698-0", Currency.getInstance("EUR")));
        payment.setUltimateDebtor("Mueller");
        payment.setCurrency(Currency.getInstance("EUR"));
        payment.setAmount(BigDecimal.valueOf(1000));
        payment.setCreditorAccount(new CmsAccountReference("DE89370400440532013000", "89370400440532010000", "2356 5746 3217 1234",
            "2356xxxxxx1234", "+49(0)911 360698-0", Currency.getInstance("EUR")));
        payment.setCreditorAgent("Telekom");
        payment.setCreditorName("Telekom");
        payment.setCreditorAddress(buildCmsAddress("Street", "123-34", "Berlin", "90431", "Germany"));
        payment.setRemittanceInformationUnstructured("Ref. Number TELEKOM-1222");
        payment.setRemittanceInformationStructured(buildCmsRemittance("Ref Number Merchant", "reference type", "reference issuer"));
        payment.setRequestedExecutionDate(LocalDate.of(2020, 1, 1));
        payment.setRequestedExecutionTime(LocalDateTime.parse("2020-01-01T15:30:35.035Z",
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

    private static CmsRemittance buildCmsRemittance(String reference, String referenceType, String referenceIssuer) {
        CmsRemittance remittance = new CmsRemittance();
        remittance.setReference(reference);
        remittance.setReferenceType(referenceType);
        remittance.setReferenceIssuer(referenceIssuer);
        return remittance;
    }

    private static CmsTppInfo buildCmsTppInfo(String registrationNumber, String tppName, String tppRole,
                                              String nationalCompetentAuthority, String redirectUri, String nokRedirectUri) {
        CmsTppInfo tppInfo = new CmsTppInfo();
        tppInfo.setRegistrationNumber(registrationNumber);
        tppInfo.setTppName(tppName);
        tppInfo.setTppRole(tppRole);
        tppInfo.setNationalCompetentAuthority(nationalCompetentAuthority);
        tppInfo.setRedirectUri(redirectUri);
        tppInfo.setNokRedirectUri(nokRedirectUri);
        return tppInfo;
    }
}
