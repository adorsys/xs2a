package de.adorsys.aspsp.xs2a.spi.domain.aic;


import de.adorsys.aspsp.xs2a.spi.domain.TPPMessageInformation;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by aro on 27.11.17.
 */
@Data
@ApiModel(description = "Response for the AIC information  request in the AICService")
public class AICGeneralResponseBody {

    @ApiModelProperty(value = "Text to be displayed to the PSU, e.g. in a Decoupled SCA Approach")
    private String psu_message;
    @ApiModelProperty(value = "TPP Message Information")
    private TPPMessageInformation tpp_message;
}


