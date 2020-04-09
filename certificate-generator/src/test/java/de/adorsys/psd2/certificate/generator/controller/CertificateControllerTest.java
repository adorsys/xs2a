package de.adorsys.psd2.certificate.generator.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.psd2.certificate.generator.config.CorsConfigProperties;
import de.adorsys.psd2.certificate.generator.model.CertificateRequest;
import de.adorsys.psd2.certificate.generator.model.CertificateResponse;
import de.adorsys.psd2.certificate.generator.model.PspRole;
import de.adorsys.psd2.certificate.generator.service.CertificateService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@EnableWebMvc
@WebMvcTest(value = CertificateController.class)
class CertificateControllerTest1 {
    private static final String CERTIFICATE = "-----BEGIN CERTIFICATE-----Stuff-----END CERTIFICATE-----";
    private static final String PRIVATE_KEY = "-----BEGIN RSA PRIVATE KEY-----Stuff-----END RSA PRIVATE KEY-----";

    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CertificateService certificateService;

    @MockBean
    private CorsConfigProperties properties;

    @Test
    void createCert() throws Exception {
        CertificateResponse certificateResponse = CertificateResponse.builder()
                                                      .encodedCert(CERTIFICATE)
                                                      .privateKey(PRIVATE_KEY)
                                                      .build();

        given(certificateService.newCertificate(any())).willReturn(certificateResponse);

        CertificateRequest certificateRequest = CertificateRequest.builder()
                                                    .authorizationNumber("87B2AC")
                                                    .commonName("XS2A Sandbox")
                                                    .organizationName("Fictional Corporation AG")
                                                    .roles(Collections.singletonList(PspRole.AISP))
                                                    .build();

        mockMvc.perform(
            post("/api/cert-generator").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(certificateRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.encodedCert").value(CERTIFICATE))
            .andExpect(jsonPath("$.privateKey").value(PRIVATE_KEY));
    }

    @Test
    void createCertWithoutRoles() throws Exception {
        CertificateRequest certificateRequest = CertificateRequest.builder()
                                                    .authorizationNumber("87B2AC")
                                                    .commonName("XS2A Sandbox")
                                                    .organizationName("Fictional Corporation AG")
                                                    .build();

        MockHttpServletResponse response = mockMvc.perform(
            post("/api/cert-generator").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(certificateRequest)))
                                               .andExpect(status().isBadRequest())
                                               .andReturn().getResponse();

        assertThat(response.getContentAsString(), is(""));
    }

    @Test
    void createCertWithoutAuthorisationNumber() throws Exception {
        CertificateRequest certificateRequest = CertificateRequest.builder()
                                                    .organizationName("Fictional Corporation AG")
                                                    .commonName("XS2A Sandbox")
                                                    .roles(Collections.singletonList(PspRole.AISP))
                                                    .build();

        MockHttpServletResponse response = mockMvc.perform(
            post("/api/cert-generator").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(certificateRequest)))
                                               .andExpect(status().isBadRequest())
                                               .andReturn().getResponse();

        assertThat(response.getContentAsString(), is(""));
    }

    @Test
    void createCertWithoutOrganizationName() throws Exception {
        CertificateRequest certificateRequest = CertificateRequest.builder()
                                                    .authorizationNumber("87B2AC")
                                                    .commonName("XS2A Sandbox")
                                                    .roles(Collections.singletonList(PspRole.AISP))
                                                    .build();

        MockHttpServletResponse response = mockMvc.perform(
            post("/api/cert-generator").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(certificateRequest)))
                                               .andExpect(status().isBadRequest())
                                               .andReturn().getResponse();

        assertThat(response.getContentAsString(), is(""));
    }
}

