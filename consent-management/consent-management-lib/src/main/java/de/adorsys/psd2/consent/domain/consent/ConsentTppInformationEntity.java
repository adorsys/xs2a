/*
 * Copyright 2018-2022 adorsys GmbH & Co KG
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version. This program is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 *
 * This project is also available under a separate commercial license. You can
 * contact us at psd2@adorsys.com.
 */

package de.adorsys.psd2.consent.domain.consent;

import de.adorsys.psd2.consent.api.ais.AdditionalTppInfo;
import de.adorsys.psd2.consent.domain.TppInfoEntity;
import de.adorsys.psd2.xs2a.core.profile.NotificationSupportedMode;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.*;
import java.util.List;
import java.util.Objects;

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

    @PrePersist
    public void consentPrePersist() {
        if (Objects.isNull(additionalInfo)) {
            additionalInfo = AdditionalTppInfo.NONE;
        }
    }
}
