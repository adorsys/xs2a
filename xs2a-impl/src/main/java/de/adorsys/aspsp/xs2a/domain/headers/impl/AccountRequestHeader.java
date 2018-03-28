package de.adorsys.aspsp.xs2a.domain.headers.impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.adorsys.aspsp.xs2a.spi.domain.ContentType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@ApiModel(description = "Account request header", value = "AccountRequestHeader")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountRequestHeader extends CommonRequestHeader {

    @ApiModelProperty(value = "ID of the account consent", required = true, example = "5845f9b0-cef6-4f2d-97e0-ed1e0469a907")
    @JsonProperty(value = "consent-id")
    @NotNull
    private String consentId;

    @ApiModelProperty(value = "Is contained only, if an OAuth2 based authentication was performed in a pre-step or an OAuth2 based SCA was performed in the related consent authorisation", required = false, example = "5845f9b0-cef6-4f2d-97e0-ed1e0469a907")
    @JsonProperty(value = "authorization bearer")
    private String authorizationBearer;

    @ApiModelProperty(value = "Indicates the formats of account reports supported together with a prioritisation following the http header definition", required = false, example = "application/json")
    @JsonProperty(value = "accept")
    private ContentType accept;
}
