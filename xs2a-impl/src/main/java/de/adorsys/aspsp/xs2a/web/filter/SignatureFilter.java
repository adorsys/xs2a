package de.adorsys.aspsp.xs2a.web.filter;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import de.adorsys.aspsp.xs2a.service.validator.TppRoleValidationService;
import de.adorsys.psd2.validator.signature.TppSignatureValidator;

//@Component
//@Order(3)
public class SignatureFilter implements Filter {

	@Autowired
	TppRoleValidationService tppRoleValidationService;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		HttpServletRequest httpRequest = (HttpServletRequest) request;

		String encodedTppCert = httpRequest.getHeader("tpp-certificate");
		String signature = httpRequest.getHeader("signature");
		Map<String, String> headers = new HashMap<>();

		Enumeration<String> headerNames = httpRequest.getHeaderNames();
		while (headerNames.hasMoreElements()) {
			String key = headerNames.nextElement();
			String value = httpRequest.getHeader(key);
			headers.put(key, value);
		}

		TppSignatureValidator tppSignatureValidator = new TppSignatureValidator();
		try {

			tppSignatureValidator.verifySignature(signature, encodedTppCert, headers);
			chain.doFilter(request, response);

		} catch (NoSuchAlgorithmException | SignatureException e) {
			e.printStackTrace();
			((HttpServletResponse) response).sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
		}

	}

	@Override
	public void destroy() {
	}

}
