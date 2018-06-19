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

package de.adorsys.aspsp.xs2a.service.validator;

import lombok.Data;

import javax.validation.GroupSequence;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
public class ValidationGroup {
    @NotNull(groups = AccountIdGroup.class)
    private String accountId;
    @NotNull(groups = PeriodGroup.class)
    private LocalDate dateFrom;
    @NotNull(groups = PeriodGroup.class)
    private LocalDate dateTo;
    @NotNull(groups = TransactionIdGroup.class)
    private String transactionId;

    interface PeriodGroup {
    }

    interface AccountIdGroup {
    }

    interface TransactionIdGroup {
    }

    @GroupSequence({AccountIdGroup.class, PeriodGroup.class})
    public interface AccountIdAndPeriodIsValid {
    }

    @GroupSequence({AccountIdGroup.class, TransactionIdGroup.class})
    public interface AccountIdAndTransactionIdIsValid {
    }
}
