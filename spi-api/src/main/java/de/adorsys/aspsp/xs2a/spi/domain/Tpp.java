package de.adorsys.aspsp.xs2a.spi.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
public class Tpp {
	//TODO... I defined the Tpp attributes as string because they aren't still defined. https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/41
    //
	//This information will be in the certificate

	private String tpp_provider_identification;
	private String tpp_registration_number;
	private String tpp_name;
	private String tpp_role;
	private String tpp_national_competent_authority;
	private String tpp_certificate;
	private String tpp_signature;
	private String tpp_signature_data;
	private TppMessageInformation tpp_messages;
}
