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

package de.adorsys.psd2.consent.domain;

import de.adorsys.psd2.xs2a.core.tpp.TppStatus;
import lombok.*;
import org.jetbrains.annotations.Nullable;

import javax.persistence.*;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Optional;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "tpp_stop_list")
public class TppStopListEntity extends InstanceDependableEntity {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tpp_stop_list_generator")
    @SequenceGenerator(name = "tpp_stop_list_generator", sequenceName = "tpp_stop_list_id_seq", allocationSize = 1)
    private Long id;

    @Column(name = "tpp_authorisation_number", nullable = false)
    private String tppAuthorisationNumber;

    @Setter(AccessLevel.NONE)
    @Column(name = "status", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private TppStatus status;

    @Setter(AccessLevel.NONE)
    @Column(name = "expiration_timestamp")
    private OffsetDateTime blockingExpirationTimestamp;

    public void block(@Nullable Duration lockPeriod) {
        this.status = TppStatus.BLOCKED;
        this.blockingExpirationTimestamp = lockPeriod != null
                                               ? OffsetDateTime.now().plus(lockPeriod)
                                               : null;
    }

    public void unblock() {
        this.status = TppStatus.ENABLED;
        this.blockingExpirationTimestamp = null;
    }

    public boolean isBlocked() {
        return status == TppStatus.BLOCKED;
    }

    public boolean isBlockingExpired() {
        return Optional.ofNullable(blockingExpirationTimestamp)
                   .map(timestamp -> timestamp.isBefore(OffsetDateTime.now()))
                   .orElse(false);
    }
}
