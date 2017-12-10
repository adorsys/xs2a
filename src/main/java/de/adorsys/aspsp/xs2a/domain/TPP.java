package de.adorsys.aspsp.xs2a.domain;

import java.util.List;
import java.util.Map;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by aro on 23.11.17.
 */

@Data

public class TPP {
    //TO DO... I defined the TPP attributes as string because they aren't still defined. 
	//This information will be in the certificate 
	
	private String tpp_provider_identification;
	private String tpp_registration_number;
	private String tpp_name;
	private String tpp_role;
	private String tpp_national_competent_authority;
	
	private TPPMessageInformation tpp_messages;
	private String tpp_signature;
	
	// private ..... certificate;
	
}