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

package de.adorsys.psd2.xs2a.web.mapper;

import de.adorsys.psd2.model.ScaStatus;
import de.adorsys.psd2.xs2a.core.sca.ChallengeData;
import de.adorsys.psd2.xs2a.core.sca.OtpFormat;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
public class CoreObjectsMapper {
    @Nullable
    public ScaStatus mapToModelScaStatus(@NotNull de.adorsys.psd2.xs2a.core.sca.ScaStatus scaStatus) {
        return ScaStatus.fromValue(scaStatus.getValue());
    }

    @Nullable
    public de.adorsys.psd2.model.ChallengeData mapToChallengeData(ChallengeData challengeData) {
        return Optional.ofNullable(challengeData)
                   .map(cd -> new de.adorsys.psd2.model.ChallengeData()
                                  .image(cd.getImage())
                                  .data(cd.getData())
                                  .imageLink(cd.getImageLink())
                                  .otpMaxLength(cd.getOtpMaxLength())
                                  .otpFormat(mapToOtpFormat(cd.getOtpFormat()))
                                  .additionalInformation(cd.getAdditionalInformation())
                       ).orElse(null);
    }

    @Nullable
    private de.adorsys.psd2.model.ChallengeData.OtpFormatEnum mapToOtpFormat(OtpFormat otpFormat) {
        return Optional.ofNullable(otpFormat)
                   .map(OtpFormat::getValue)
                   .map(de.adorsys.psd2.model.ChallengeData.OtpFormatEnum::fromValue)
               .orElse(null);
    }

}
