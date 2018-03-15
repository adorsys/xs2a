package de.adorsys.aspsp.xs2a.spi.domain.ais.consents;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "AccountAccess type", value = "AccountAccessType")
public enum AccountAccessType {
	ALL_ACCOUNTS("all-accounts");
    
    @ApiModelProperty(value = "description", example = "all-accounts")
    private String description;
    
    @JsonCreator
    AccountAccessType(String description) {
        this.description = description;
    }
    
    @JsonValue
    public String getDescription() {
        return description;
    }
}
