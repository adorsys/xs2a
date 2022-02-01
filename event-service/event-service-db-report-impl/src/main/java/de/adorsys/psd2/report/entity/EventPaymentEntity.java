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

package de.adorsys.psd2.report.entity;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "pis_common_payment")
public class EventPaymentEntity implements EventPsuDataList, Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pis_common_payment_generator")
    @SequenceGenerator(name = "pis_common_payment_generator", sequenceName = "pis_common_payment_id_seq", allocationSize = 1)
    private Long id;

    @Column(name = "payment_id", nullable = false)
    private String paymentId;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "pis_common_payment_psu_data",
        joinColumns = @JoinColumn(name = "pis_common_payment_id"),
        inverseJoinColumns = @JoinColumn(name = "psu_data_id"))
    private List<EventPsuData> psuDataList = new ArrayList<>();
}
