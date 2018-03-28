package de.adorsys.aspsp.xs2a.domain.ais;

import de.adorsys.aspsp.xs2a.domain.TppMessageInformation;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "Response for the Ais information  request in the AICService")
public class AisGeneralResponseBody {

	@ApiModelProperty(value = "Text to be displayed to the Psu, e.g. in a Decoupled SCA Approach")
	private String psu_message;
	@ApiModelProperty(value = "Tpp Message Information")
	private TppMessageInformation tpp_message;
}


