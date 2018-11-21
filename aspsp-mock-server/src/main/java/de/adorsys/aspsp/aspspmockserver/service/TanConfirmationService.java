/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.adorsys.aspsp.aspspmockserver.service;

import de.adorsys.aspsp.aspspmockserver.domain.ConfirmationType;
import de.adorsys.aspsp.aspspmockserver.exception.ApiError;
import de.adorsys.aspsp.aspspmockserver.repository.TanRepository;
import de.adorsys.psd2.aspsp.mock.api.consent.AspspConsentStatus;
import de.adorsys.psd2.aspsp.mock.api.psu.AspspAuthenticationObject;
import de.adorsys.psd2.aspsp.mock.api.psu.Tan;
import de.adorsys.psd2.aspsp.mock.api.psu.TanStatus;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

@Slf4j
@Service
@RequiredArgsConstructor
public class TanConfirmationService {

    @Value("${maximum-number-of-tan-attempts}")
    private int maximumNumberOfTanAttempts;

    private final TanRepository tanRepository;
    private final static String EMAIL_TEMPLATE_PATH = "email/email-template.html";
    private final JavaMailSender emailSender;
    private final Configuration fmConfiguration;
    private final AccountService accountService;
    private final PaymentService paymentService;
    private final ConsentService consentService;

    /**
     * Sends Authorization Request to user with selected Sca Method
     *
     * @param psuId                  PSU id
     * @param authenticationMethodId An Id of Sca method selected by PSU
     * @return true if PSU was found and Authorisation request sent successfully
     */
    public boolean sendUserAuthRequestWithPreSelectedScaMethod(String psuId, String authenticationMethodId) {
        return accountService.getPsuByPsuId(psuId)
                   .map(psu -> {
                       Optional<AspspAuthenticationObject> scaMethod = psu.getScaMethods().stream()
                                                                           .filter(m -> authenticationMethodId.equals(m.getAuthenticationMethodId()))
                                                                           .findFirst();
                       if (scaMethod.isPresent()) {
                           return generateAndSendTanForPsuById(psuId);
                       }
                       return false;
                   })
                   .orElse(false);
    }

    /**
     * Generates new Tan and sends it to psu's email for payment confirmation
     *
     * @param psuId PSU id
     * @return true if psu was found and new Tan was sent successfully, otherwise return false
     */
    public boolean generateAndSendTanForPsuById(String psuId) {
        return accountService.getPsuByPsuId(psuId)
                   .map(psu -> createAndSendTan(psu.getPsuId(), psu.getEmail()))
                   .orElse(false);
    }

    /**
     * Gets new Tan and sends it to psu's email for payment confirmation
     *
     * @param psuId     PSU id
     * @param tanNumber TAN
     * @return true if Tan has status UNUSED, otherwise return false
     */
    public ResponseEntity confirmTan(String psuId, String tanNumber, String consentId, ConfirmationType confirmationType) {
        if (isTanNumberValid(psuId, tanNumber)) {
            return new ResponseEntity(HttpStatus.OK);
        } else if (getTanNumberOfAttempts(psuId) < maximumNumberOfTanAttempts) {
            ApiError error = new ApiError(HttpStatus.BAD_REQUEST, "WRONG_TAN", "Bad request");
            return new ResponseEntity<>(error, error.getStatus());
        }
        changeConsentStatusToRejected(consentId, confirmationType);
        ApiError error = new ApiError(HttpStatus.BAD_REQUEST, "LIMIT_EXCEEDED", "Bad request");
        return new ResponseEntity<>(error, error.getStatus());
    }

    private boolean isTanNumberValid(String psuId, String tanNumber) {
        return accountService.getPsuByPsuId(psuId)
                   .map(psu -> isPsuTanNumberValid(psu.getPsuId(), tanNumber))
                   .orElse(false);
    }

    private int getTanNumberOfAttempts(String psuId) {
        return accountService.getPsuByPsuId(psuId)
                   .flatMap(psu -> tanRepository.findByPsuIdAndTanStatus(psu.getPsuId(), TanStatus.UNUSED).stream()
                                       .findFirst()
                                       .map(Tan::getNumberOfAttempts))
                   .orElse(maximumNumberOfTanAttempts);
    }

    private boolean isPsuTanNumberValid(String psuId, String tanNumber) {
        return tanRepository.findByPsuIdAndTanStatus(psuId, TanStatus.UNUSED).stream()
                   .findFirst()
                   .map(t -> validateTanAndUpdateTanStatus(t, tanNumber))
                   .orElse(false);
    }

    private boolean createAndSendTan(String psuId, String email) {
        changeOldTansToInvalid(psuId);
        Tan tan = new Tan(psuId, generateTanNumber());
        tan = tanRepository.save(tan);

        return sendTanNumberOnEmail(email, tan.getTanNumber());
    }

    private void changeOldTansToInvalid(String psuId) {
        List<Tan> tans = tanRepository.findByPsuIdAndTanStatus(psuId, TanStatus.UNUSED);
        if (isNotEmpty(tans)) {
            for (Tan oldTan : tans) {
                oldTan.setTanStatus(TanStatus.INVALID);
                oldTan.setNumberOfAttempts(maximumNumberOfTanAttempts);
            }
            tanRepository.save(tans);
        }
    }

    private boolean validateTanAndUpdateTanStatus(Tan originalTan, String givenTanNumber) {
        boolean isTanValid = originalTan.getTanNumber().equals(givenTanNumber);
        if (isTanValid) {
            originalTan.setTanStatus(TanStatus.VALID);
        } else {
            if (originalTan.getNumberOfAttempts() == maximumNumberOfTanAttempts - 1) {
                originalTan.setTanStatus(TanStatus.INVALID);
            }
            originalTan.incrementNumberOfAttempts();
        }
        tanRepository.save(originalTan);
        return isTanValid;
    }

    private String generateTanNumber() {
        return RandomStringUtils.random(6, true, true);
    }

    private boolean sendTanNumberOnEmail(String email, String tanNumber) {
        if (emailSender == null) {
            log.warn("Email properties has not been set");
            return false;
        }

        MimeMessage mimeMessage = emailSender.createMimeMessage();
        try {
            MimeMessageHelper mail = new MimeMessageHelper(mimeMessage, true);
            mail.setSubject("Your TAN for payment confirmation");
            mail.setFrom(email);
            mail.setTo(email);
            mail.setText(getEmailContentFromTemplate(tanNumber), true);

            emailSender.send(mail.getMimeMessage());
            log.info("Generated Tan: {}", tanNumber);
            return true;
        } catch (MessagingException e) {
            log.warn("Problem with creating or sanding email: {}", e);
            return false;
        }
    }

    private String getEmailContentFromTemplate(String tanNumber) {
        StringBuilder content = new StringBuilder();
        try {
            Template emailTemplate = fmConfiguration.getTemplate(EMAIL_TEMPLATE_PATH);
            content.append(FreeMarkerTemplateUtils.processTemplateIntoString(emailTemplate, Collections.singletonMap("tan", tanNumber)));
        } catch (Exception e) {
            log.warn("Problem with reading email template : {}", e);
            return "Your TAN number is " + tanNumber;
        }
        return content.toString();
    }

    private void changeConsentStatusToRejected(String consentId, ConfirmationType confirmationType) {
        if (confirmationType == ConfirmationType.PAYMENT) {
            paymentService.updatePaymentConsentStatus(consentId, AspspConsentStatus.REJECTED);
        } else {
            consentService.updateAisConsentStatus(consentId, AspspConsentStatus.REJECTED);
        }
    }
}
