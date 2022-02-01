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

package de.adorsys.psd2.validator.certificate;

import de.adorsys.psd2.validator.certificate.util.CertificateExtractorUtil;
import de.adorsys.psd2.validator.certificate.util.CertificateUtils;
import de.adorsys.psd2.validator.certificate.util.TppCertificateData;
import no.difi.certvalidator.api.CertificateValidationException;
import org.junit.jupiter.api.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.*;

class CertificateExtractorUtilTest {

    @Test
    void testExtractCertData() throws CertificateValidationException {

        String encodedCert = "-----BEGIN CERTIFICATE-----MIIEBjCCAu6gAwIBAgIEejybVTANBgkqhkiG9w0BAQsFADCBlDELMAkGA1UEBhMCREUxDzANBgNVBAgMBkhlc3NlbjESMBAGA1UEBwwJRnJhbmtmdXJ0MRUwEwYDVQQKDAxBdXRob3JpdHkgQ0ExCzAJBgNVBAsMAklUMSEwHwYDVQQDDBhBdXRob3JpdHkgQ0EgRG9tYWluIE5hbWUxGTAXBgkqhkiG9w0BCQEWCmNhQHRlc3QuZGUwHhcNMTgwNzAyMTMzMDUyWhcNMTgwNzE5MTQxMTIxWjB6MRMwEQYDVQQDDApkb21haW5OYW1lMQwwCgYDVQQKDANvcmcxCzAJBgNVBAsMAm91MRAwDgYDVQQGEwdHZXJtYW55MQ8wDQYDVQQIDAZCYXllcm4xEjAQBgNVBAcMCU51cmVtYmVyZzERMA8GA1UEYQwIMTIzNDU5ODcwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQCSYxs2Fhpkt0zweiLxiJ3+QwpENoiIyxtNKehU/oqc/V6mns8c2N1BgxDEUixAugf257zkwNn39PZKuV8S4T61GeT4UUvf5DySYvgI63XpbRF3hj0KJk/oZDanidFZ86X7kW7TcTetPX+AqGiCLQEIKbnyISh1qeij6SmMr2j6IXVBzSP9dnxif+nf2poj0kCAz69eCI2zHufz8VJuIf9RhFychuuPO/NCQXuKL7KFeEf5uLhDGPeGqeWs1EBCAECuBOSAOwX+uFwyn/Jw/HfzH0ZQtod7qaNs4/fvjtZOekOtBGYGFOaZlqNvmadiR9mMJTbSXmBY1A5DMdBGcPAzAgMBAAGjeTB3MHUGCCsGAQUFBwEDBGkwZwYGBACBmCcCMF0wTDARBgcEAIGYJwEBDAZQU1BfQVMwEQYHBACBmCcBAgwGUFNQX1BJMBEGBwQAgZgnAQMMBlBTUF9BSTARBgcEAIGYJwEEDAZQU1BfSUMMBEF1dGgMB0dlcm1hbnkwDQYJKoZIhvcNAQELBQADggEBAKXgO8FvKBmTOGn2gWlasZtSsIWbzexR0IMU1AuGNmkMF2BGEkBTQJ1bGpCd5+eBVUkbIQkxP0Oqyb/moNty/7c9gym1GpEzZ5Cs1Zgtne8EnOKh8sGtI9L0AFVSTFHPHt9qj4o5TMgzdU9Q4cq4bq9bSZiwo3btwgIkBn7yetvfo8fSADBSYwOlqUAv+mROjEID772owEOgn0fLZmlIdZl8rpYQlzbHpcvlTTdnQiidjdO4Xln3+NIp7ifIfBUoCei0AP8rAFhq14GIE1YweY9efa1QPEm/a9EN3f22Psyd/jrQqnWVLsIJIEPVth3gdish6tHDfED1PpZVtMxKHqE=-----END CERTIFICATE-----";

        TppCertificateData extract = CertificateExtractorUtil.extract(encodedCert);

        assertEquals(4, extract.getPspRoles().size());
    }

    @Test
    void testExtractCertDataValidCert() throws CertificateValidationException {

        String encodedCert = CertificateUtils.getCertificateByName("certificateValid.crt");

        TppCertificateData extract = CertificateExtractorUtil.extract(encodedCert);

        assertEquals(3, extract.getPspRoles().size());
    }

    @Test
    void testExtractCertDataInvalidCert() {
        String encodedCert = CertificateUtils.getCertificateByName("certificateInvalid.crt");

        assertThrows(CertificateValidationException.class, () -> CertificateExtractorUtil.extract(encodedCert));
    }

    @Test
    void testExtractCertData_shouldExtractNotAfterDate() throws CertificateValidationException {
        // Given
        String encodedCert = CertificateUtils.getCertificateByName("certificateValid.crt");

        // When
        TppCertificateData extractedData = CertificateExtractorUtil.extract(encodedCert);

        // Then
        Date extractedNotAfter = extractedData.getNotAfter();
        assertNotNull(extractedNotAfter);

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.set(2018, Calendar.JUNE, 19, 16, 6, 24);
        calendar.set(Calendar.MILLISECOND, 0);
        Date expectedDate = calendar.getTime();

        assertEquals(expectedDate, extractedNotAfter);
    }

    @Test
    void testExtractCertDataWithNDSValidCert() throws CertificateValidationException {

        String encodedCert = CertificateUtils.getCertificateByName("certificateWithDNSValid.crt");

        TppCertificateData extract = CertificateExtractorUtil.extract(encodedCert);

        List<String> dnsList = extract.getDnsList();
        assertNotNull(dnsList);
        assertEquals(2, dnsList.size());
    }
}
