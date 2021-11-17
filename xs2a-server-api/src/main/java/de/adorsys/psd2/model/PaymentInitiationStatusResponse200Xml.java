package de.adorsys.psd2.model;

import io.swagger.annotations.ApiModel;
import org.springframework.validation.annotation.Validated;

import java.util.Objects;

/**
 * Body of the response for a successful payment initiation status request in case of an XML based endpoint.  The status is returned as a pain.002 structure.   urn:iso:std:iso:20022:tech:xsd:pain.002.001.03  The chosen XML schema of the status request is following the XML schema definitions of the original pain.001 schema.
 */
@ApiModel(description = "Body of the response for a successful payment initiation status request in case of an XML based endpoint.  The status is returned as a pain.002 structure.   urn:iso:std:iso:20022:tech:xsd:pain.002.001.03  The chosen XML schema of the status request is following the XML schema definitions of the original pain.001 schema. ")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2021-11-05T12:22:49.487689+02:00[Europe/Kiev]")

public class PaymentInitiationStatusResponse200Xml   {

  @Override
  public boolean equals(Object o) {
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
    sb.append("class PaymentInitiationStatusResponse200Xml {\n");

    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

