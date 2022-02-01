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

import de.adorsys.psd2.validator.certificate.util.URLDecodingUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class URLDecodingUtilTest {
    public static final String URL_ENCODED_EXAMPLE = "http://example.com/query?q=random%20word%20500%20bank%20%24";
    public static final String URL_DECODED_EXAMPLE = "http://example.com/query?q=random word 500 bank $";

    public static final String URL_ENCODED_CERTIFICATE = "-----BEGIN%20CERTIFICATE-----%0AMIIETzCCAzegAwIBAgIEJo%2BCgTANBgkqhkiG9w0BAQsFADB5MQswCQYDVQQGEwJE%0ARTEQMA4GA1UECAwHQkFWQVJJQTESMBAGA1UEBwwJTnVyZW1iZXJnMSMwIQYDVQQK%0ADBpUcnVzdCBTZXJ2aWNlIFByb3ZpZGVyIEFHCjEfMB0GA1UECwwWSW5mb3JtYXRp%0Ab24gVGVjaG5vbG9neTAeFw0yMDA4MTQwOTI5MTNaFw0yMTA4MTQwMDAwMDBaMIHY%0AMSEwHwYDVQQKDBhGaWN0aW9uYWwgQ29ycG9yYXRpb24gQUcxFTATBgNVBAMMDFhT%0AMkEgU2FuZGJveDElMCMGCgmSJomT8ixkARkWFXB1YmxpYy5jb3Jwb3JhdGlvbi5k%0AZTEfMB0GA1UECwwWSW5mb3JtYXRpb24gVGVjaG5vbG9neTEQMA4GA1UEBhMHR2Vy%0AbWFueTEPMA0GA1UECAwGQmF5ZXJuMRIwEAYDVQQHDAlOdXJlbWJlcmcxHTAbBgNV%0ABGEMFFBTRERFLUZBS0VOQ0EtODdCMkFDMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8A%0AMIIBCgKCAQEAiAAqEdAeL0znpsQeIAMpITE5mu%2BupjrlXuXmsrkUxRczXJwfuCez%0ArPnBt%2B7IeNt4P0Embv%2FlEzgAl6EZNRz%2Ff3YvWK%2BdJz7qoD9PFhn4Axk6P%2Fi9QrS4%0A3nlrWFUOqy7VxrQNSCeHuCILTXFk%2BKeJVZqgZYWnSgDPa6WuzCXW6w6qgsNb1qaz%0A6a52HLQbLjy0UiZPRpxC3Y9Y8W9Jap1f4GCsOkEL7LrYfkNxxwfGLFK%2BGI8%2F4%2Bti%0AAULBxvj8V4lN5Dv3GgpnNhfNSM7JnCB6mnYFNJDM2iOm0FUt9Twz7ENW5LLL5qWX%0AWPh8POtn1XaYw5L1IzoO0b5TAyKtKiV9iwIDAQABo38wfTB7BggrBgEFBQcBAwRv%0AMG0GBgQAgZgnAjBjMDkwEQYHBACBmCcBAwwGUFNQX0FJMBEGBwQAgZgnAQIMBlBT%0AUF9QSTARBgcEAIGYJwEEDAZQU1BfSUMMGlRydXN0IFNlcnZpY2UgUHJvdmlkZXIg%0AQUcKDApERS1GQUtFTkNBMA0GCSqGSIb3DQEBCwUAA4IBAQCFMtn9XQQxSKrBw%2Fn0%0AXQ0mD1tObKy5omZc8OR%2Fz7QXneyi%2Ffdju0HLq1g4XUB2C1AYyU2Rr7MOEkYyOoI3%0Asi4xp8o%2FjSXKy4kgXkgqTiQEsEgxtDpDJX7T6ciNnShXVnRmBN3zzkn%2B0RjIlFKZ%0ARPB79BxuOPzF%2BPCe9Qbh9KAnl30ihB9%2Br8LTaiAQUDEiPTsexw6WE1Yq00ej2pod%0AVNEZDTfunerkT9jfaX3MJLRBIZAEul1ieSyCK8PSEl48nNFTuKrAwjSpPGo8%2Fm88%0A3sVD8s8ZylEdD1PXNEqTvfUXQGksD2dqT2mYZrwFBmECScQ%2B%2BgjLNi23NrbjbZM6%0AgrMQ%0A-----END%20CERTIFICATE-----%0A";
    public static final String URL_DECODED_CERTIFICATE = "-----BEGIN CERTIFICATE-----\n" +
                                                             "MIIETzCCAzegAwIBAgIEJo+CgTANBgkqhkiG9w0BAQsFADB5MQswCQYDVQQGEwJE\n" +
                                                             "RTEQMA4GA1UECAwHQkFWQVJJQTESMBAGA1UEBwwJTnVyZW1iZXJnMSMwIQYDVQQK\n" +
                                                             "DBpUcnVzdCBTZXJ2aWNlIFByb3ZpZGVyIEFHCjEfMB0GA1UECwwWSW5mb3JtYXRp\n" +
                                                             "b24gVGVjaG5vbG9neTAeFw0yMDA4MTQwOTI5MTNaFw0yMTA4MTQwMDAwMDBaMIHY\n" +
                                                             "MSEwHwYDVQQKDBhGaWN0aW9uYWwgQ29ycG9yYXRpb24gQUcxFTATBgNVBAMMDFhT\n" +
                                                             "MkEgU2FuZGJveDElMCMGCgmSJomT8ixkARkWFXB1YmxpYy5jb3Jwb3JhdGlvbi5k\n" +
                                                             "ZTEfMB0GA1UECwwWSW5mb3JtYXRpb24gVGVjaG5vbG9neTEQMA4GA1UEBhMHR2Vy\n" +
                                                             "bWFueTEPMA0GA1UECAwGQmF5ZXJuMRIwEAYDVQQHDAlOdXJlbWJlcmcxHTAbBgNV\n" +
                                                             "BGEMFFBTRERFLUZBS0VOQ0EtODdCMkFDMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8A\n" +
                                                             "MIIBCgKCAQEAiAAqEdAeL0znpsQeIAMpITE5mu+upjrlXuXmsrkUxRczXJwfuCez\n" +
                                                             "rPnBt+7IeNt4P0Embv/lEzgAl6EZNRz/f3YvWK+dJz7qoD9PFhn4Axk6P/i9QrS4\n" +
                                                             "3nlrWFUOqy7VxrQNSCeHuCILTXFk+KeJVZqgZYWnSgDPa6WuzCXW6w6qgsNb1qaz\n" +
                                                             "6a52HLQbLjy0UiZPRpxC3Y9Y8W9Jap1f4GCsOkEL7LrYfkNxxwfGLFK+GI8/4+ti\n" +
                                                             "AULBxvj8V4lN5Dv3GgpnNhfNSM7JnCB6mnYFNJDM2iOm0FUt9Twz7ENW5LLL5qWX\n" +
                                                             "WPh8POtn1XaYw5L1IzoO0b5TAyKtKiV9iwIDAQABo38wfTB7BggrBgEFBQcBAwRv\n" +
                                                             "MG0GBgQAgZgnAjBjMDkwEQYHBACBmCcBAwwGUFNQX0FJMBEGBwQAgZgnAQIMBlBT\n" +
                                                             "UF9QSTARBgcEAIGYJwEEDAZQU1BfSUMMGlRydXN0IFNlcnZpY2UgUHJvdmlkZXIg\n" +
                                                             "QUcKDApERS1GQUtFTkNBMA0GCSqGSIb3DQEBCwUAA4IBAQCFMtn9XQQxSKrBw/n0\n" +
                                                             "XQ0mD1tObKy5omZc8OR/z7QXneyi/fdju0HLq1g4XUB2C1AYyU2Rr7MOEkYyOoI3\n" +
                                                             "si4xp8o/jSXKy4kgXkgqTiQEsEgxtDpDJX7T6ciNnShXVnRmBN3zzkn+0RjIlFKZ\n" +
                                                             "RPB79BxuOPzF+PCe9Qbh9KAnl30ihB9+r8LTaiAQUDEiPTsexw6WE1Yq00ej2pod\n" +
                                                             "VNEZDTfunerkT9jfaX3MJLRBIZAEul1ieSyCK8PSEl48nNFTuKrAwjSpPGo8/m88\n" +
                                                             "3sVD8s8ZylEdD1PXNEqTvfUXQGksD2dqT2mYZrwFBmECScQ++gjLNi23NrbjbZM6\n" +
                                                             "grMQ\n" +
                                                             "-----END CERTIFICATE-----\n";

    @Test
    void testCertificateDecode() {
        byte[] decoded = URLDecodingUtil.decode(URL_ENCODED_CERTIFICATE.getBytes());
        assertArrayEquals(URL_DECODED_CERTIFICATE.getBytes(), decoded);
    }

    @Test
    void testStringDecode() {
        byte[] decoded = URLDecodingUtil.decode(URL_ENCODED_EXAMPLE.getBytes());
        assertArrayEquals(URL_DECODED_EXAMPLE.getBytes(), decoded);
    }
}
