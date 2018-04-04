package de.adorsys.aspsp.xs2a.service.validator;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Date;

@Data
public class FieldValidation {
    @NotNull(groups = Group.AccountIdGroup.class)
    private String accountId;
    @NotNull(groups = Group.PeriodGroup.class)
    private Date dateFrom;
    @NotNull(groups = Group.PeriodGroup.class)
    private Date dateTo;
    @NotNull(groups = Group.TransactionIdGroup.class)
    private String transactionId;
}
