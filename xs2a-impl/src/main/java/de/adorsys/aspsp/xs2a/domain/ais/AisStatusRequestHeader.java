package de.adorsys.aspsp.xs2a.domain.ais;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class AisStatusRequestHeader extends AisGeneralRequestHeader {

	@ApiModelProperty(value = "Might be mandated in the ASPSP's documentation. Only used in a corporate ")
	private String psuCorporateId;
}
