package de.adorsys.aspsp.xs2a.web.filter;

import java.io.IOException;
import java.security.cert.CertificateException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import de.adorsys.psd2.validator.certificate.CertificateUtils;
import de.adorsys.psd2.validator.certificate.CertificateValidatorFactory;
import de.adorsys.psd2.validator.certificate.util.CertificateExtractorUtil;
import de.adorsys.psd2.validator.certificate.util.TppCertData;
import no.difi.certvalidator.api.CertificateValidationException;
import no.difi.certvalidator.util.SimpleCertificateBucket;

@Component
@Order(1)
public class CertificateFilter implements Filter {

	private SimpleCertificateBucket blockedCertBucket;
	private SimpleCertificateBucket rootCertBucket;
	private SimpleCertificateBucket intermediateCertBucket;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {

		blockedCertBucket = new SimpleCertificateBucket(CertificateUtils.getCertificates("blockedcert"));
		rootCertBucket = new SimpleCertificateBucket(CertificateUtils.getCertificates("rootcert"));
		intermediateCertBucket = new SimpleCertificateBucket(CertificateUtils.getCertificates("intermediatecert"));
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		HttpServletRequest httpRequest = (HttpServletRequest) request;
		String encodedTppCert = httpRequest.getHeader("tpp-certificate");

		CertificateValidatorFactory validatorFactory = new CertificateValidatorFactory(blockedCertBucket,
				rootCertBucket, intermediateCertBucket);
		try {
			validatorFactory.validate(encodedTppCert);

			TppCertData tppCertData = CertificateExtractorUtil.extract(encodedTppCert);
			request.setAttribute("tppCertData", tppCertData);

			chain.doFilter(request, response);
		} catch (CertificateException | CertificateValidationException e) {
			e.printStackTrace();
			((HttpServletResponse) response).sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
		}
	}

	@Override
	public void destroy() {
	}

}
