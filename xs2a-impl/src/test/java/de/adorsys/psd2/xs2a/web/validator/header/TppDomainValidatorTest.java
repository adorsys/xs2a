/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.web.validator.header;

import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import de.adorsys.psd2.xs2a.service.TppService;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.FORMAT_ERROR_INVALID_DOMAIN;
import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TppDomainValidatorTest {
    private static final String URL_HEADER_CORRECT = "www.example-TPP.com/xs2a-client/v1/ASPSPidentifcation/mytransaction-id";
    private static final String URL_HEADER_CORRECT_DE = "www.example-TPP.de/xs2a-client/v1/ASPSPidentifcation/mytransaction-id";
    private static final String URL_HEADER_SUBDOMAIN_CORRECT = "redirections.example-TPP.com/xs2a-client/v1/ASPSPidentifcation/mytransaction-id";
    private static final String URL_HEADER_WRONG_DOMAIN = "www.bad-example-TPP.com/xs2a-client/v1/ASPSPidentifcation/mytransaction-id";
    private static final String URL_HEADER_WRONG_TLD = "www.example-TPP.bad/xs2a-client/v1/ASPSPidentifcation/mytransaction-id";
    private static final String URL_HEADER_WRONG = "example-TPP";

    private static final String TPP_NAME_DOMAIN = "www.example-TPP.com";
    private static final String TPP_NAME_NON_DOMAIN = "Some bank name";
    private static final String TPP_DNS_DOMAIN = "www.example-TPP.de";
    private static final String TPP_WILDCARD_DOMAIN = "*.example-TPP.de";
    private static final TppMessageInformation TPP_MESSAGE_INFORMATION = TppMessageInformation.of(FORMAT_ERROR_INVALID_DOMAIN);

    @InjectMocks
    private TppDomainValidator tppDomainValidator;
    @Mock
    private ErrorBuildingService errorBuildingService;
    @Mock
    private ScaApproachResolver scaApproachResolver;
    @Mock
    private TppService tppService;

    @Before
    public void setUp() {
        when(errorBuildingService.buildErrorType())
            .thenReturn(ErrorType.AIS_400);
    }

    @Test
    public void validate_NoHeader_Valid() {
        //Given
        //When
        ValidationResult validate = tppDomainValidator.validate(null);
        //Then
        assertEquals(ValidationResult.valid(), validate);
    }

    @Test
    public void validate_ScaEmbedded_Valid() {
        //Given
        when(scaApproachResolver.resolveScaApproach())
            .thenReturn(ScaApproach.EMBEDDED);
        //When
        ValidationResult validate = tppDomainValidator.validate(URL_HEADER_WRONG_DOMAIN);
        //Then
        assertEquals(ValidationResult.valid(), validate);
    }

    @Test
    public void validate_NoDomainsInTpp_Valid() {
        //Given
        when(scaApproachResolver.resolveScaApproach())
            .thenReturn(ScaApproach.REDIRECT);
        when(tppService.getTppInfo())
            .thenReturn(buildTppInfo(null, null));
        //When
        ValidationResult validate = tppDomainValidator.validate(URL_HEADER_CORRECT);
        //Then
        assertEquals(ValidationResult.valid(), validate);
    }

    @Test
    public void validate_NotCorrectDomainsInTpp_Valid() {
        //Given
        when(scaApproachResolver.resolveScaApproach())
            .thenReturn(ScaApproach.REDIRECT);
        when(tppService.getTppInfo())
            .thenReturn(buildTppInfo("example-TPP", "dns-example-TPP"));
        //When
        ValidationResult validate = tppDomainValidator.validate(URL_HEADER_CORRECT);
        //Then
        assertEquals(ValidationResult.valid(), validate);
    }

    @Test
    public void validate_UrlHeaderCorrect_Valid() {
        //Given
        when(scaApproachResolver.resolveScaApproach())
            .thenReturn(ScaApproach.REDIRECT);
        when(tppService.getTppInfo())
            .thenReturn(buildTppInfo(TPP_NAME_DOMAIN, TPP_DNS_DOMAIN));
        //When
        ValidationResult validate = tppDomainValidator.validate(URL_HEADER_CORRECT);
        //Then
        assertEquals(ValidationResult.valid(), validate);
    }

    @Test
    public void validate_UrlHeaderSubdomainCorrect_Valid() {
        //Given
        when(scaApproachResolver.resolveScaApproach())
            .thenReturn(ScaApproach.REDIRECT);
        when(tppService.getTppInfo())
            .thenReturn(buildTppInfo(TPP_NAME_DOMAIN, TPP_DNS_DOMAIN));
        //When
        ValidationResult validate = tppDomainValidator.validate(URL_HEADER_SUBDOMAIN_CORRECT);
        //Then
        assertEquals(ValidationResult.valid(), validate);
    }

    @Test
    public void validate_UrlHeaderSubdomainCorrectTppWildCardDomain_Valid() {
        //Given
        when(scaApproachResolver.resolveScaApproach())
            .thenReturn(ScaApproach.REDIRECT);
        when(tppService.getTppInfo())
            .thenReturn(buildTppInfo(TPP_NAME_DOMAIN, TPP_WILDCARD_DOMAIN));
        //When
        ValidationResult validate = tppDomainValidator.validate(URL_HEADER_SUBDOMAIN_CORRECT);
        //Then
        assertEquals(ValidationResult.valid(), validate);
    }

    @Test
    public void validate_UrlHeaderWrong_Invalid() {
        //Given
        when(scaApproachResolver.resolveScaApproach())
            .thenReturn(ScaApproach.REDIRECT);
        when(tppService.getTppInfo())
            .thenReturn(buildTppInfo(TPP_NAME_DOMAIN, TPP_DNS_DOMAIN));
        //When
        ValidationResult validate = tppDomainValidator.validate(URL_HEADER_WRONG);
        //Then
        assertTrue(validate.isNotValid());
        MessageError messageError = validate.getMessageError();
        assertNotNull(messageError);
        assertEquals(TPP_MESSAGE_INFORMATION, messageError.getTppMessage());
    }

    @Test
    public void validate_UrlHeaderWrongDomain_Invalid() {
        //Given
        when(scaApproachResolver.resolveScaApproach())
            .thenReturn(ScaApproach.REDIRECT);
        when(tppService.getTppInfo())
            .thenReturn(buildTppInfo(TPP_NAME_DOMAIN, TPP_DNS_DOMAIN));
        //When
        ValidationResult validate = tppDomainValidator.validate(URL_HEADER_WRONG_DOMAIN);
        //Then
        assertTrue(validate.isNotValid());
        MessageError messageError = validate.getMessageError();
        assertNotNull(messageError);
        assertEquals(TPP_MESSAGE_INFORMATION, messageError.getTppMessage());
    }

    @Test
    public void validate_UrlHeaderWrongTld_Invalid() {
        //Given
        when(scaApproachResolver.resolveScaApproach())
            .thenReturn(ScaApproach.REDIRECT);
        when(tppService.getTppInfo())
            .thenReturn(buildTppInfo(TPP_NAME_DOMAIN, TPP_DNS_DOMAIN));
        //When
        ValidationResult validate = tppDomainValidator.validate(URL_HEADER_WRONG_TLD);
        //Then
        assertTrue(validate.isNotValid());
        MessageError messageError = validate.getMessageError();
        assertNotNull(messageError);
        assertEquals(TPP_MESSAGE_INFORMATION, messageError.getTppMessage());
    }

    @Test
    public void validate_nonDomainTppName_valid() {
        //Given
        when(scaApproachResolver.resolveScaApproach())
            .thenReturn(ScaApproach.REDIRECT);
        when(tppService.getTppInfo())
            .thenReturn(buildTppInfo(TPP_NAME_NON_DOMAIN, TPP_DNS_DOMAIN));
        //When
        ValidationResult validate = tppDomainValidator.validate(URL_HEADER_CORRECT_DE);
        //Then
        assertEquals(ValidationResult.valid(), validate);
    }

    @Test
    public void validate_nonDomainTppName_invalid() {
        //Given
        when(scaApproachResolver.resolveScaApproach())
            .thenReturn(ScaApproach.REDIRECT);
        when(tppService.getTppInfo())
            .thenReturn(buildTppInfo(TPP_NAME_NON_DOMAIN, TPP_DNS_DOMAIN));
        //When
        ValidationResult validate = tppDomainValidator.validate(URL_HEADER_WRONG_DOMAIN);
        //Then
        assertTrue(validate.isNotValid());
        MessageError messageError = validate.getMessageError();
        assertNotNull(messageError);
        assertEquals(TPP_MESSAGE_INFORMATION, messageError.getTppMessage());
    }

    private TppInfo buildTppInfo(String name, String dns) {
        TppInfo tppInfo = new TppInfo();
        tppInfo.setTppName(name);
        List<String> dnsList = dns == null
                                   ? Collections.emptyList()
                                   : Collections.singletonList(dns);
        tppInfo.setDnsList(dnsList);

        return tppInfo;
    }
}
