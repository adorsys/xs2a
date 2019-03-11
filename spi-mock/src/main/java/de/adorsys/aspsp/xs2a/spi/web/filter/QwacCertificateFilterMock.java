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

package de.adorsys.aspsp.xs2a.spi.web.filter;

import de.adorsys.psd2.xs2a.service.validator.tpp.TppInfoHolder;
import de.adorsys.psd2.xs2a.service.validator.tpp.TppRoleValidationService;
import de.adorsys.psd2.xs2a.web.filter.QwacCertificateFilter;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * The intend of this class is to return a mock certificate, when we don't want
 * to enter manually everytime the qwac certificate in case of test.
 * launch it with the "mockspi" profile.
 */
@Profile("mockspi")
@Component
public class QwacCertificateFilterMock extends QwacCertificateFilter {

    public QwacCertificateFilterMock(TppRoleValidationService tppRoleMatcher, TppInfoHolder tppInfoHolder) {
        super(tppRoleMatcher, tppInfoHolder);
    }

    @Override
    public String getEncodedTppQwacCert(HttpServletRequest httpRequest) {

        return "-----BEGIN CERTIFICATE-----MIIFQTCCAymgAwIBAgIESLvdaTANBgkqhkiG9w0BAQsFADB4MQswCQYDVQQGEwJERTEQMA4GA1UECAwHQkFWQVJJQTESMBAGA1UEBwwJTnVyZW1iZXJnMSIwIAYDVQQKDBlUcnVzdCBTZXJ2aWNlIFByb3ZpZGVyIEFHMR8wHQYDVQQLDBZJbmZvcm1hdGlvbiBUZWNobm9sb2d5MB4XDTE5MDMwNTE1MTIwN1oXDTIwMDMwNDAwMDAwMFowgcwxITAfBgNVBAoMGEZpY3Rpb25hbCBDb3Jwb3JhdGlvbiBBRzEJMAcGA1UEAwwAMSUwIwYKCZImiZPyLGQBGRYVcHVibGljLmNvcnBvcmF0aW9uLmRlMR8wHQYDVQQLDBZJbmZvcm1hdGlvbiBUZWNobm9sb2d5MRAwDgYDVQQGEwdHZXJtYW55MQ8wDQYDVQQIDAZCYXllcm4xEjAQBgNVBAcMCU51cmVtYmVyZzEdMBsGA1UEYQwUUFNEREUtRkFLRU5DQS04N0IyQUMwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQCeDcYlVutZeGFtOkonIMGHwway2ASZl8p7/v7USIxeMo/5ppbAa6Ei7i7jH9ORBoHV6qxAwNFkdd8JDneNiNn0NSvoYTemr5mqyXYhwpzLueXth1oBgjLYvcaLFXXQGS0dd6sDcaCTbw9xdDmap+6xYDRzIrdviyiph1ewpUXlrEHNu5Oomk7R5Dpv4gM9uRwYiskRigZdnArfyQ3ZYW4VZvMlFW3t1IVvSiOWvruF24w+j1g3BOHNM7tIAOlOUYQpHV1G1ChcFt5/ICArtsGAd4/ZUzlmujktdO+hNA70fDHUxkG6vQRFQhSnszzJ/C/g632nMTJbAaGtO2OvdL9DAgMBAAGjfjB8MHoGCCsGAQUFBwEDBG4wbAYGBACBmCcCMGIwOTARBgcEAIGYJwEDDAZQU1BfQUkwEQYHBACBmCcBAgwGUFNQX1BJMBEGBwQAgZgnAQQMBlBTUF9JQwwZVHJ1c3QgU2VydmljZSBQcm92aWRlciBBRwwKREUtRkFLRU5DQTANBgkqhkiG9w0BAQsFAAOCAgEAK07yQviS7/zKm1EqQyyGkEbf/1sHb9FLPBr/BicYxc3IQGd4xG1SJ1uLudX37Yq/o6exjixZ8ywib27jNLCpsF1dEQabHNXgS4enojf7CVTyKjDkKqE1mwqPmGeoWWwaWOUsWQ2/Ja/UTW5Bn5iA+nHCXVrkcjFVnRvi+dSsRm4J3E0EdAAwBkSEqHGDZO1ZiAh20YkNExx8MKKiHAVZ0ZFCXzYcaWzaK6yeCarvyPNCb+BAsc1wf3/88tLT9Nof/Ctzv2L9OjGHcalCLf/g/qTr6/50J4IMVdBwoVkg27yRE5EC3RKJE5BFx6TNWeNGs7r8HpAhO/6hLKzVHjrsA8/SAwTWNQNWdP/azSV42DuVMjDi5o5Ax9RkHXRvjsuwTR19AKvIc6nv/8XUtwORjHW+FTXTGa28PqCD1ZACiHytIBXrETevmLIlFuh6ZaWKBYPUc3DmJbFSZkhRFybh1SEtl/WzeQjIKqkRw0MGzDIRwD0sYqeE8ENkJbXJG+Cy4c42mZmEwG6E7HQQtiT9Irt1cnUiFDRe6g+h4GaxhOC5Pluxhij4DaNHCIZm30IHcyA4vZOyj7rXcvvfGMwPgbSdqSdEeNB25FEmFmJnavESxyJKYNx3JONm//0yRpacfWos/MjmbLWynYz8Bv8EK7mCS84bmSlUrUgHoNvDeBc=-----END CERTIFICATE-----";
    }
}
