package de.adorsys.psd2.validator.signature;

import org.junit.Before;
import org.junit.Test;

import junit.framework.Assert;

public class TppSignatureValidatorTest {

	private String signature;

	//tpp cert example coming from e-Szigno
	private String tppEncodedCert = "-----BEGIN CERTIFICATE-----\n" +
"MIIKEDCCCPigAwIBAgINOFNxaoP6/Za+1YqpCjANBgkqhkiG9w0BAQsFADBqMQsw\n" +
"CQYDVQQGEwJIVTERMA8GA1UEBwwIQnVkYXBlc3QxFjAUBgNVBAoMDU1pY3Jvc2Vj\n" +
"IEx0ZC4xFDASBgNVBAsMC2UtU3ppZ25vIENBMRowGAYDVQQDDBFlLVN6aWdubyBU\n" +
"ZXN0IENBMzAeFw0xODAzMjExNjA2MjRaFw0xODA2MTkxNjA2MjRaMIG8MQswCQYD\n" +
"VQQGEwJERTEPMA0GA1UEBwwGQmVybGluMRQwEgYDVQQKDAtFeGFtcGxlIFRQUDEZ\n" +
"MBcGA1UEYQwQUFNERVUtTkNBLTFERkQyMTEcMBoGA1UEAwwTd3d3LmV4YW1wbGUu\n" +
"dHBwLmNvbTEkMCIGCSqGSIb3DQEJARYVZXhhbXBsZS50cHBAZ21haWwuY29tMScw\n" +
"JQYDVQQFEx4xLjMuNi4xLjQuMS4yMTUyOC4yLjIuOTkuMTE1MzQwggEiMA0GCSqG\n" +
"SIb3DQEBAQUAA4IBDwAwggEKAoIBAQDGAzkeZgKRpqaEyT+ZuLhMuHXfDaGgimCQ\n" +
"SToqGORHQPeDqy6g1CfT9sEYDQzSIA10oMI7c0kuxhNX4/FW7yXeT4Dxw4LuprnU\n" +
"UovZas2lAhiK7XVR6ppRUvHMFTqWHsURQQgXYWJFcAo46sIOatIgNfepw08nw6RM\n" +
"ZDhaA4g2XIPRF+w525W0cDAAD1BcKUIxW7rud0NPO150VY/M7uCS+ZiWxpfzEkBH\n" +
"kIs839PXICaDJJm051pFVpKy2aShkXt6SBkJy0RMrAxIL5zpH8+Se8nFc5YDwNow\n" +
"gsDyooz2qbcbINNBboK2X3P7QflY9JATUKdwywaj391dwYoT+5ZRAgMBAAGjggZg\n" +
"MIIGXDAOBgNVHQ8BAf8EBAMCBkAwggQlBgNVHSAEggQcMIIEGDCCBBQGDCsGAQQB\n" +
"gagYAgEBZDCCBAIwJgYIKwYBBQUHAgEWGmh0dHA6Ly9jcC5lLXN6aWduby5odS9x\n" +
"Y3BzMIGXBggrBgEFBQcCAjCBigyBh1Rlc3QgcXVhbGlmaWVkIGNlcnRpZmljYXRl\n" +
"IGZvciBlbGVjdHJvbmljIHNlYWwgKEJyb256ZSkuIFRoZSBwcml2YXRlIGtleSBy\n" +
"ZXNpZGVzIGluIGEgcXVhbGlmaWVkIGVsZWN0cm9uaWMgc2VhbCBjcmVhdGlvbiBk\n" +
"ZXZpY2UgKFFTQ0QpLjCBpQYIKwYBBQUHAgIwgZgMgZVUaGUgcHJvdmlkZXIgcHJl\n" +
"c2VydmVzIHJlZ2lzdHJhdGlvbiBkYXRhIGZvciAxMCB5ZWFycyBhZnRlciB0aGUg\n" +
"ZXhwaXJhdGlvbiBvZiB0aGUgY2VydGlmaWNhdGUuIFRoZSBzdWJqZWN0IG9mIHRo\n" +
"ZSB0ZXN0IGNlcnRpZmljYXRlIGlzIGEgbGVnYWwgcGVyc29uLjCBlQYIKwYBBQUH\n" +
"AgIwgYgMgYVURVNUIGNlcnRpZmljYXRlIGlzc3VlZCBvbmx5IGZvciB0ZXN0aW5n\n" +
"IHB1cnBvc2VzLiBUaGUgaXNzdWVyIGlzIG5vdCBsaWFibGUgZm9yIGFueSBkYW1h\n" +
"Z2VzIGFyaXNpbmcgZnJvbSB0aGUgdXNlIG9mIHRoaXMgY2VydGlmaWNhdGUhMIGk\n" +
"BggrBgEFBQcCAjCBlwyBlFRlc3p0IGVsZWt0cm9uaWt1cyBiw6lseWVnesWRIG1p\n" +
"bsWRc8OtdGV0dCB0YW7DunPDrXR2w6FueWEgKEJyb256KS4gQSBtYWfDoW5rdWxj\n" +
"c290IG1pbsWRc8OtdGV0dCBlbGVrdHJvbmlrdXMgYsOpbHllZ3rFkXQgbMOpdHJl\n" +
"aG96w7MgZXN6a8O2eiB2w6lkaS4wgaYGCCsGAQUFBwICMIGZDIGWQSByZWdpc3p0\n" +
"csOhY2nDs3MgYWRhdG9rYXQgYSBzem9sZ8OhbHRhdMOzIGEgdGFuw7pzw610dsOh\n" +
"bnkgbGVqw6FydMOhdMOzbCBzesOhbcOtdG90dCAxMCDDqXZpZyDFkXJ6aSBtZWcu\n" +
"IEEgdGVzenQgdGFuw7pzw610dsOhbnkgYWxhbnlhIGpvZ2kgc3plbcOpbHkuMIGt\n" +
"BggrBgEFBQcCAjCBoAyBnVRlc3p0ZWzDqXNpIGPDqWxyYSBraWFkb3R0IFRFU1pU\n" +
"IHRhbsO6c8OtdHbDoW55LiBBIGhhc3puw6FsYXTDoXZhbCBrYXBjc29sYXRvc2Fu\n" +
"IGZlbG1lcsO8bMWRIGvDoXJva8OpcnQgYSBTem9sZ8OhbHRhdMOzIHNlbW1pbHll\n" +
"biBmZWxlbMWRc3PDqWdldCBuZW0gdsOhbGxhbCEwHQYDVR0OBBYEFAIkBN+l5zM/\n" +
"lIo0DJOkXBoGPmCWMB8GA1UdIwQYMBaAFNzmAijvNzCPiT6grSBV8+826PDNMDsG\n" +
"A1UdEQQ0MDKBFWV4YW1wbGUudHBwQGdtYWlsLmNvbaAZBggrBgEFBQcIA6ANMAsG\n" +
"CSsGAQQBgagYAjAyBgNVHR8EKzApMCegJaAjhiFodHRwOi8vdGVzenQuZS1zemln\n" +
"bm8uaHUvVENBMy5jcmwwbwYIKwYBBQUHAQEEYzBhMDAGCCsGAQUFBzABhiRodHRw\n" +
"Oi8vdGVzenQuZS1zemlnbm8uaHUvdGVzdGNhM29jc3AwLQYIKwYBBQUHMAKGIWh0\n" +
"dHA6Ly90ZXN6dC5lLXN6aWduby5odS9UQ0EzLmNydDCB/gYIKwYBBQUHAQMEgfEw\n" +
"ge4wCAYGBACORgEBMAsGBgQAjkYBAwIBCjAIBgYEAI5GAQQwUwYGBACORgEFMEkw\n" +
"JBYeaHR0cHM6Ly9jcC5lLXN6aWduby5odS9xY3BzX2VuEwJFTjAhFhtodHRwczov\n" +
"L2NwLmUtc3ppZ25vLmh1L3FjcHMTAkhVMBMGBgQAjkYBBjAJBgcEAI5GAQYCMGEG\n" +
"BgQAgZgnAjBXMDkwEQYHBACBmCcBAQwGUFNQX0FTMBEGBwQAgZgnAQIMBlBTUF9Q\n" +
"STARBgcEAIGYJwEDDAZQU1BfQUkMEkV1cm9wZWFuIEF1dGhvcml0eQwGRVUtTkNB\n" +
"MA0GCSqGSIb3DQEBCwUAA4IBAQCgBHJZwC3iDwDDmDQqNepxt1J+maU5nBuwYRmx\n" +
"j95wxuPKk9mAJVQqR1lC4xj1ZC9SLJXGNsESOirjua116qlgGEnozVnPg5+6bJiI\n" +
"YlJYS+vv/4ssclXnKtk3L5y0AawzHPdrNRfSoRihvQibSzkUBO0cJnef/xd0CJeJ\n" +
"vgDcoevoGUAVvF98eJnPbYgAWiiK4PrrLTrQzSZ3UtLmde/LWZLBAA4vWCgXj9t0\n" +
"YECEexIWZVg1hch8G3gauhPczCCIe9jGn8zYeW30ewQ3KL97sBTNWoSfSk5C7iQ8\n" +
"cyb+bQbyMI8P40FgPzKHhPiIzb+KCGkLQqUDg4gs3yd22NER\n" +
"-----END CERTIFICATE-----";
	
	//wrong public key
	 private final String publicKeyPem = "-----BEGIN PUBLIC KEY-----\n" +
	            "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDCFENGw33yGihy92pDjZQhl0C3\n" +
	            "6rPJj+CvfSC8+q28hxA161QFNUd13wuCTUcq0Qd2qsBe/2hFyc2DCJJg0h1L78+6\n" +
	            "Z4UMR7EOcpfdUE9Hf3m/hs+FUR45uBJeDK1HSFHD8bHKD6kv8FPGfJTotc+2xjJw\n" +
	            "oYi+1hqp1fIekaxsyQIDAQAB\n" +
	            "-----END PUBLIC KEY-----\n";

	@Before
	public void init() {

		signature = SignatureGeneratorUtil.generateSignature();
	}

	@Test
	public void when_ValidSignature_Expected_true() throws Exception {

		Assert.assertEquals(TppSignatureValidator.verifySignature(signature, tppEncodedCert), true);
	}
	
	@Test(expected = Exception.class)
	public void when_ValidSignatureAndFalseCert_Expected_true() throws Exception {

		TppSignatureValidator.verifySignature(signature, publicKeyPem);
	}
}
