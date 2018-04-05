package de.adorsys.aspsp.xs2a.service.validator;

import lombok.Data;

import javax.validation.GroupSequence;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Data
public class ValidationGroup {
    @NotNull(groups = AccountIdGroup.class)
    private String accountId;
    @NotNull(groups = PeriodGroup.class)
    private Date dateFrom;
    @NotNull(groups = PeriodGroup.class)
    private Date dateTo;
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
    public interface  AccountIdAndTransactionIdIsValid{
    }
}
