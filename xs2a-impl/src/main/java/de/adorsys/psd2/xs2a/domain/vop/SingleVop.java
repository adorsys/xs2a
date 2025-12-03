/*
 * Copyright 2018-2025 adorsys GmbH & Co KG
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
 * contact us at sales@adorsys.com.
 */

package de.adorsys.psd2.xs2a.domain.vop;

import de.adorsys.psd2.xs2a.core.vop.AccountReference;
import de.adorsys.psd2.xs2a.core.vop.AgentDescription;
import de.adorsys.psd2.xs2a.core.vop.PartyDescription4;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * Single VOP domain class for single party verification
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SingleVop extends CommonVop {
    @NotNull
    @Valid
    private AccountReference partyAccount;

    @Valid
    private AgentDescription partyAgent;

    @NotNull
    @Valid
    private PartyDescription4 party;

    @Valid
    private List<String> unstructuredRemittanceInformation;

    @Valid
    private AgentDescription requestingAgent;
}
