package de.adorsys.aspsp.xs2a.service.validator;

import de.adorsys.aspsp.xs2a.service.validator.group.AccountIdGroup;
import de.adorsys.aspsp.xs2a.service.validator.group.PeriodGroup;
import de.adorsys.aspsp.xs2a.service.validator.group.TransactionIdGroup;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Date;

@Data
public class FieldsForValidatorGroup {
    @NotNull(groups = AccountIdGroup.class)
    private String accountId;
    @NotNull(groups = PeriodGroup.class)
    private Date dateFrom;
    @NotNull(groups = PeriodGroup.class)
    private Date dateTo;
    @NotNull(groups = TransactionIdGroup.class)
    private String transactionId;
}
