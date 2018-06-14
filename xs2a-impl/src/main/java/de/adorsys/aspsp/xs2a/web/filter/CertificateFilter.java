package de.adorsys.aspsp.xs2a.web.filter;

import java.io.IOException;
import java.security.cert.CertificateException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.core.annotation.Order;

import de.adorsys.psd2.validator.certificate.CertificateValidatorFactory;
import de.adorsys.psd2.validator.certificate.util.CertificateExtractorUtil;
import de.adorsys.psd2.validator.certificate.util.CertificateUtils;
import de.adorsys.psd2.validator.certificate.util.TppCertificateData;
import lombok.extern.slf4j.Slf4j;
import no.difi.certvalidator.api.CertificateValidationException;
import no.difi.certvalidator.util.SimpleCertificateBucket;

@Slf4j
@Order(1)
@WebFilter(urlPatterns = "/api/v1/*")
public class CertificateFilter implements Filter {

	private SimpleCertificateBucket blockedCertBucket;
	private SimpleCertificateBucket rootCertBucket;
	private SimpleCertificateBucket intermediateCertBucket;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {

		blockedCertBucket = new SimpleCertificateBucket(CertificateUtils.getCertificates("blockedcert"));
		rootCertBucket = new SimpleCertificateBucket(CertificateUtils.getCertificates("rootcert", "MyRootCA.pem"));
		intermediateCertBucket = new SimpleCertificateBucket(CertificateUtils.getCertificates("intermediatecert"));
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		if (!(request instanceof HttpServletRequest) || !(response instanceof HttpServletResponse)) {
			throw new ServletException("OncePerRequestFilter just supports HTTP requests");
		}

		HttpServletRequest httpRequest = (HttpServletRequest) request;
		String encodedTppCert = httpRequest.getHeader("tpp-certificate");

		CertificateValidatorFactory validatorFactory = new CertificateValidatorFactory(blockedCertBucket,
				rootCertBucket, intermediateCertBucket);
		try {
			validatorFactory.validate(encodedTppCert);

			TppCertificateData tppCertData = CertificateExtractorUtil.extract(encodedTppCert);
			request.setAttribute("tppCertData", tppCertData);

			chain.doFilter(request, response);
		} catch (CertificateException | CertificateValidationException e) {
			log.debug(e.getMessage());
			((HttpServletResponse) response).sendError(HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
		}
	}

	@Override
	public void destroy() {
	}

}
