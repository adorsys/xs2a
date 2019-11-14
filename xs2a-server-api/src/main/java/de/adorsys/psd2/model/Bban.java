package de.adorsys.psd2.model;

import io.swagger.annotations.ApiModel;
import org.springframework.validation.annotation.Validated;

import java.util.Objects;

/**
 * Basic Bank Account Number (BBAN) Identifier.  This data element can be used in the body of the Consent request.   Message for retrieving Account access Consent from this Account. This   data elements is used for payment Accounts which have no IBAN.   ISO20022: Basic Bank Account Number (BBAN).    Identifier used nationally by financial institutions, i.e., in individual countries,   generally as part of a National Account Numbering Scheme(s),   which uniquely identifies the account of a customer.
 */
@ApiModel(description = "Basic Bank Account Number (BBAN) Identifier.  This data element can be used in the body of the Consent request.   Message for retrieving Account access Consent from this Account. This   data elements is used for payment Accounts which have no IBAN.   ISO20022: Basic Bank Account Number (BBAN).    Identifier used nationally by financial institutions, i.e., in individual countries,   generally as part of a National Account Numbering Scheme(s),   which uniquely identifies the account of a customer. ")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2019-11-11T13:48:52.194360+02:00[Europe/Kiev]")

public class Bban   {

  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    return Objects.hash();
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Bban {\n");

    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

