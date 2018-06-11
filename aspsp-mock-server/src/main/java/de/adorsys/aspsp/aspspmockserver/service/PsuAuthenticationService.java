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

import de.adorsys.aspsp.aspspmockserver.repository.EmailTanRepository;
import de.adorsys.aspsp.aspspmockserver.repository.PsuRepository;
import de.adorsys.aspsp.xs2a.spi.domain.psu.Tan;
import de.adorsys.aspsp.xs2a.spi.domain.psu.TanStatus;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class PsuAuthenticationService {
    private final EmailTanRepository emailTanRepository;
    private final PsuRepository psuRepository;
    private final JavaMailSender emailSender;

    public String generateAndSendTanForPsu(String psuId) {
        return Optional.ofNullable(psuRepository.findOne(psuId))
                   .map(psu -> createAndSendTan(psu.getId(), psu.getEmail()))
                   .orElse(null);
    }

    public boolean isPsuTanNumberValid(String psuId, int tanNumber) {
        return emailTanRepository.findTansByPsuIdIn(psuId)
                   .stream()
                   .filter(t -> t.getTanStatus().equals(TanStatus.UNUSED))
                   .findFirst()
                   .map(t -> validateTanAndUpdateTanStatus(t, tanNumber))
                   .orElse(false);
    }

    private String createAndSendTan(String psuId, String email) {
        Tan tan = new Tan(null, psuId, generateTanNumber(), TanStatus.UNUSED);
        emailTanRepository.save(tan);
        sendTanNumberOnEmail(email, tan.getTanNumber());
        return tan.getPsuId();
    }

    private boolean validateTanAndUpdateTanStatus(Tan originalTan, int givenTanNumber) {
        boolean isValid = originalTan.getTanNumber() == givenTanNumber;
        if (isValid) {
            originalTan.setTanStatus(TanStatus.VALID);
        } else {
            originalTan.setTanStatus(TanStatus.INVALID);
        }
        emailTanRepository.save(originalTan);

        return isValid;
    }

    private int generateTanNumber() {
        return RandomUtils.nextInt(100000, 1000000);
    }

    private void sendTanNumberOnEmail(String email, int tanNumber) {
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setSubject("TAN for authentication to confirm your payment");
        mail.setTo(email);
        mail.setText("Your TAN number is " + tanNumber);
        emailSender.send(mail);
    }
}
