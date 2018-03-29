package de.adorsys.aspsp.xs2a.domain.entityValidator.impl;

import de.adorsys.aspsp.xs2a.domain.entityValidator.EntityValidator;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * Is uses for validation arguments
 */
@Data
public class TransactionByPeriodRequestValidator implements EntityValidator {
    @NotNull
    @NotEmpty
    private final String accountId;

    @NotNull
    private final Date dateFrom;

    @NotNull
    private final Date dateTo;
}
