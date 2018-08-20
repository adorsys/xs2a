package de.adorsys.aspsp.xs2a.web.filter;

import de.adorsys.aspsp.xs2a.service.profile.AspspProfileService;
import de.adorsys.psd2.validator.certificate.CertificateErrorMsgCode;
import de.adorsys.psd2.validator.signature.TppSignatureValidator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


@Slf4j
@Component
public class SignatureFilter implements Filter {

    @Autowired
    private AspspProfileService aspspProfileService;

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException {

        if (aspspProfileService.getTppSignatureRequired()) {
            if (!(request instanceof HttpServletRequest) || !(response instanceof HttpServletResponse)) {
                throw new ServletException("OncePerRequestFilter just supports HTTP requests");
            }

            HttpServletRequest httpRequest = (HttpServletRequest) request;

            String signature = httpRequest.getHeader("signature");
            if (StringUtils.isBlank(signature)) {
                ((HttpServletResponse) response).sendError(HttpServletResponse.SC_UNAUTHORIZED,
                    CertificateErrorMsgCode.SIGNATURE_MISSING.toString());
                return;
            }

            if (digestContainsErrors(httpRequest)) {
                ((HttpServletResponse) response).sendError(HttpServletResponse.SC_BAD_REQUEST,
                    CertificateErrorMsgCode.FORMAT_ERROR.toString());
                return;
            }

            Map<String, String> headers = obtainRequestHeaders(httpRequest);
            String encodedTppCert = httpRequest.getHeader("tpp-signature-certificate");
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

    private boolean digestContainsErrors(HttpServletRequest httpRequest) {
        String digest = httpRequest.getHeader("digest");
        return StringUtils.isBlank(digest) || !Arrays.asList(64, 128).contains(digest.getBytes().length);
    }

    @Override
    public void destroy() {
    }

    private Map<String, String> obtainRequestHeaders(HttpServletRequest request) {
        return Collections.list(request.getHeaderNames()).stream()
            .collect(Collectors.toMap(Function.identity(), request::getHeader));
    }
}
