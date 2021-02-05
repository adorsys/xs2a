/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
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

package de.adorsys.psd2.consent.domain.consent;

import de.adorsys.psd2.consent.domain.TppInfoEntity;
import de.adorsys.psd2.xs2a.core.profile.NotificationSupportedMode;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.*;
import java.util.List;

@Data
@Entity(name = "consent_tpp_information")
@ApiModel(description = "Consent tpp information", value = "ConsentTppInformationEntity")
public class ConsentTppInformationEntity {

    @Id
    @Column(name = "consent_tpp_information_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "consent_tpp_information_generator")
    @SequenceGenerator(name = "consent_tpp_information_generator", sequenceName = "consent_tpp_info_id_seq", allocationSize = 1)
    private Long id;

    @Column(name = "tpp_redirect_preferred", nullable = false)
    @ApiModelProperty(name = "tppRedirectPreferred", value = "If it equals “true”, the TPP prefers a redirect over an embedded SCA approach.", required = true, example = "false")
    private boolean tppRedirectPreferred;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "tpp_info_id", nullable = false)
    @ApiModelProperty(value = "Information about TPP", required = true)
    private TppInfoEntity tppInfo;

    @Column(name = "tpp_frequency_per_day")
    private int tppFrequencyPerDay;

    @Column(name = "tpp_ntfc_uri")
    private String tppNotificationUri;

    @Column(name = "tpp_brand_log_info")
    private String tppBrandLoggingInformation;

    @ElementCollection
    @CollectionTable(name = "ais_consent_tpp_ntfc", joinColumns = @JoinColumn(name = "id"))
    @Column(name = "notification_mode", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private List<NotificationSupportedMode> tppNotificationContentPreferred;

    @Column(name = "additional_info", nullable = false)
    private String additionalInfo;
}
