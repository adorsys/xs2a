package de.adorsys.aspsp.xs2a.service.validator;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.junit4.SpringRunner;

import de.adorsys.psd2.validator.certificate.util.TppRole;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TppRoleValidationServiceTest {

	@Autowired
	private TppRoleValidationService tppRoleValidationService;

	@Test
	public void shouldSuccess_when_correctRole() {

		List<TppRole> roles = new ArrayList<>();
		roles.add(TppRole.AISP);

		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setMethod("GET");
		request.setPathInfo("/accounts");
		request.setServletPath("/api/v1");

		assertThat(tppRoleValidationService.validate(request, roles)).isTrue();

	}

	@Test
	public void shouldFail_when_wrongRole() {

		List<TppRole> roles = new ArrayList<>();
		roles.add(TppRole.PIISP);

		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setMethod("POST");
		request.setPathInfo("/payments/sepa");
		request.setServletPath("/api/v1");

		assertThat(tppRoleValidationService.validate(request, roles)).isFalse();

	}
}
