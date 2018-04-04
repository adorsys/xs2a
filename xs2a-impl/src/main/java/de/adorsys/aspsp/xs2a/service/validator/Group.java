package de.adorsys.aspsp.xs2a.service.validator;

import javax.validation.GroupSequence;

public class Group {
    public interface PeriodGroup {
    }

    public interface AccountIdGroup {
    }

    public interface TransactionIdGroup {
    }

    @GroupSequence({AccountIdGroup.class, PeriodGroup.class})
    public interface AccountIdAndPeriodIsValid {
    }

    @GroupSequence({AccountIdGroup.class, TransactionIdGroup.class})
    public interface  AccountIdAndTransactionIdIsValid{
    }
}
