package de.adorsys.aspsp.xs2a.service.validator;

import de.adorsys.psd2.validator.certificate.util.TppRole;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class TppRoleValidationServiceTest {

    @InjectMocks
    private TppRoleValidationService tppRoleValidationService;

    @Before
    public void setUp() {
        tppRoleValidationService.initCertificatePathMatchers();
    }

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
