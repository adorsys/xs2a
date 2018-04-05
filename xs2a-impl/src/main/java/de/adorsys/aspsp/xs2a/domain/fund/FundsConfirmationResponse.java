package de.adorsys.aspsp.xs2a.domain.fund;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "FundsConfirmationResponse", value = "Funds confirmation response")
public class FundsConfirmationResponse {
    @ApiModelProperty(value = "Equals 'true' if sufficient funds are available at the time of the request, 'false' otherwise.", example = "true")
    private boolean fundsAvailable;
}
