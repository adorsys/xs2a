package de.adorsys.aspsp.xs2a.spi.domain.ais.consents;

import com.fasterxml.jackson.annotation.JsonFormat;
import de.adorsys.aspsp.xs2a.spi.domain.TransactionStatus;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@Data
@AllArgsConstructor
@ApiModel(description = "Returns the content of an account information consent object")
public class AccountConsentsStatusResp {
    @ApiModelProperty(value = "Access", required = true)
    private AccountAccess access;

    @ApiModelProperty(value = "'true', if the consent is for recurring access to the account data , 'false', if the consent is for one access to the account data", required = true)
    private boolean recurringIndicator;

    @ApiModelProperty(value = "valid until date for the requested consent. The content is the local ASPSP date in ISODate Format", required = true, example = "2017-10-30")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date validUntil;

    @ApiModelProperty(value = "requested maximum frequency for an access per day. For a once-off access, this attribute is set to 1", required = true, example = "4")
    private int frequencyPerDay;

    @ApiModelProperty(value = "This date is containing the date of the last action on the consent object either through the XS2A interface or the PSU/ASPSP interface having an impact on the status.", required = true,  example = "2017-10-30")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date lastActionDate;

    @ApiModelProperty(value = "Transaction status", required = true, example = "Pending")
    private TransactionStatus transactionStatus;

    @ApiModelProperty(value = "The following code values are permitted 'empty', 'valid', 'blocked', 'expired', 'deleted'. These values might be extended by ASPSP by more values.", required = true)
    private ConsentStatus consentStatus;
    
    /**
     *  Constructor converts AccountConsents to AccountConsentsStatusResp
     *
     * @param consents - account information consent object
     */
    public AccountConsentsStatusResp(AccountConsents consents) {
        this.access = consents.getAccess();
        this.recurringIndicator = consents.isRecurringIndicator();
        this.validUntil = consents.getValidUntil();
        this.frequencyPerDay = consents.getFrequencyPerDay();
        this.transactionStatus = consents.getTransactionStatus();
        this.lastActionDate = consents.getLastActionDate();
        this.consentStatus = consents.getConsentStatus();
    }
}


