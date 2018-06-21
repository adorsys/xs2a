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
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;

import de.adorsys.aspsp.xs2a.service.AspspProfileService;
import de.adorsys.psd2.validator.signature.TppSignatureValidator;
import lombok.extern.slf4j.Slf4j;

//NOPMD TODO implement http signature filter, https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/141
@WebFilter(urlPatterns ="/api/v1/*")
@Order(3)
@Slf4j
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

			Map<String, String> headers = obtainRequestHeaders(httpRequest);

			TppSignatureValidator tppSignatureValidator = new TppSignatureValidator();
			try {

				tppSignatureValidator.verifySignature(signature, encodedTppCert, headers);
				chain.doFilter(request, response);

			} catch (NoSuchAlgorithmException | SignatureException e) {
				log.debug(e.getMessage());
				((HttpServletResponse) response).sendError(HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
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
