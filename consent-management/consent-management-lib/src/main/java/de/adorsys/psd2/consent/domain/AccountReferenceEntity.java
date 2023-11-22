/*
 * Copyright 2018-2023 adorsys GmbH & Co KG
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

import lombok.Data;

import jakarta.persistence.*;
import java.util.Currency;

@Data
@Entity(name = "account_reference")
public class AccountReferenceEntity {
    @Id
    @Column(name = "account_reference_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "account_reference_generator")
    @SequenceGenerator(name = "account_reference_generator", sequenceName = "account_reference_id_seq", allocationSize = 1)
    private Long id;

    private String iban;

    private String bban;

    private String pan;

    @Column(name = "masked_pan")
    private String maskedPan;

    private String msisdn;

    private Currency currency;

    @Column(name = "aspsp_account_id", length = 100)
    private String aspspAccountId;
}
