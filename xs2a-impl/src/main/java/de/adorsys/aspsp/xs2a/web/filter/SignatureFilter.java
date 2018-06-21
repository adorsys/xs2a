package de.adorsys.aspsp.xs2a.web.filter;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import de.adorsys.aspsp.xs2a.service.AspspProfileService;
import de.adorsys.psd2.validator.certificate.CertificateErrorMsgCode;
import de.adorsys.psd2.validator.signature.TppSignatureValidator;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Order(3)
public class SignatureFilter implements Filter {

	@Autowired
	private AspspProfileService aspspProfileService;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		if (aspspProfileService.getTppSignatureRequired()) {
			if (!(request instanceof HttpServletRequest) || !(response instanceof HttpServletResponse)) {
				throw new ServletException("OncePerRequestFilter just supports HTTP requests");
			}

			HttpServletRequest httpRequest = (HttpServletRequest) request;

			String encodedTppCert = httpRequest.getHeader("tpp-certificate");
			String signature = httpRequest.getHeader("signature");

			if (StringUtils.isBlank(signature)) {
				((HttpServletResponse) response).sendError(HttpServletResponse.SC_UNAUTHORIZED,
						CertificateErrorMsgCode.SIGNATURE_MISSING.toString());
				return;
			}

			Map<String, String> headers = obtainRequestHeaders(httpRequest);

			TppSignatureValidator tppSignatureValidator = new TppSignatureValidator();
			try {

				if (tppSignatureValidator.verifySignature(signature, encodedTppCert, headers)) {
					chain.doFilter(request, response);
				} else {

					((HttpServletResponse) response).sendError(HttpServletResponse.SC_UNAUTHORIZED,
							CertificateErrorMsgCode.SIGNATURE_INVALID.toString());
					return;
				}

			} catch (NoSuchAlgorithmException | SignatureException e) {
				log.debug(e.getMessage());
				((HttpServletResponse) response).sendError(HttpServletResponse.SC_UNAUTHORIZED,
						CertificateErrorMsgCode.SIGNATURE_INVALID.toString());
				return;
			}
		} else {

			chain.doFilter(request, response);
		}
	}

	@Override
	public void destroy() {
	}

	private Map<String, String> obtainRequestHeaders(HttpServletRequest request) {
		return Collections.list(request.getHeaderNames()).stream()
				.collect(Collectors.toMap(Function.identity(), e -> request.getHeader(e)));
	}

}
