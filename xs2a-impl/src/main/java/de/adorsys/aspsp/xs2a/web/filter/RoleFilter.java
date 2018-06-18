package de.adorsys.aspsp.xs2a.web.filter;

import java.io.IOException;

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
import de.adorsys.aspsp.xs2a.service.validator.TppRoleValidationService;
import de.adorsys.psd2.validator.certificate.util.TppCertificateData;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@WebFilter(urlPatterns = "/api/v1/*")
@Order(2)
public class RoleFilter implements Filter {

	@Autowired
	TppRoleValidationService tppRoleValidationService;
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

			TppCertificateData tppCertData = (TppCertificateData) request.getAttribute("tppCertData");

			if (tppRoleValidationService.validate(httpRequest, tppCertData.getPspRoles())) {
				chain.doFilter(request, response);
			} else {
				// NOPMD TODO define conform error msg,
				// https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/142
				log.debug(
						"Returned if the resource that was referenced in the path exists but cannot be accessed by the TPP or the PSU");
				((HttpServletResponse) response).sendError(HttpServletResponse.SC_FORBIDDEN,
						"Returned if the resource that was referenced in the path exists but cannot be accessed by the TPP or the PSU");
			}
		}
	}

	@Override
	public void destroy() {
	}

}
