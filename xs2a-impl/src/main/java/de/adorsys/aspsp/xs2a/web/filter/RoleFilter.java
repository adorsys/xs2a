package de.adorsys.aspsp.xs2a.web.filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

import de.adorsys.aspsp.xs2a.domain.TppRole;
import de.adorsys.aspsp.xs2a.service.validator.TppRoleValidationService;
import de.adorsys.psd2.validator.certificate.util.TppCertData;

@WebFilter(urlPatterns ="/api/v1/*")
@Order(2)
public class RoleFilter implements Filter {

	@Autowired
	TppRoleValidationService tppRoleValidationService;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		HttpServletRequest httpRequest = (HttpServletRequest) request;

		TppCertData tppCertData = (TppCertData) request.getAttribute("tppCertData");

		String[] pspRoles = tppCertData.getPspRoles();
		List<TppRole> roles = new ArrayList<>();

		for (String role : pspRoles) {
			roles.add(TppRole.valueOf(role));
		}

		if (tppRoleValidationService.validate(httpRequest, roles)) {
			chain.doFilter(request, response);
		} else {
			((HttpServletResponse) response).sendError(HttpServletResponse.SC_BAD_REQUEST,
					"TPP doesn't have conform role to access this service");
		}

	}

	@Override
	public void destroy() {
	}

}
