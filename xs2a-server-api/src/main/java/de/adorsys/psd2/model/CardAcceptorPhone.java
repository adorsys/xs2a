package de.adorsys.psd2.model;

import io.swagger.annotations.ApiModel;
import org.springframework.validation.annotation.Validated;

import java.util.Objects;

/**
 * Merchant phone number It consists of a \&quot;+\&quot; followed by the country code (from 1 to 3 characters) then a \&quot;-\&quot; and finally, any combination of numbers, \&quot;(\&quot;, \&quot;)\&quot;, \&quot;+\&quot; and \&quot;-\&quot; (up to 30 characters). pattern according to ISO20022 \\+[0-9]{1,3}-[0-9()+\\-]{1,30}
 */
@ApiModel(description = "Merchant phone number It consists of a \"+\" followed by the country code (from 1 to 3 characters) then a \"-\" and finally, any combination of numbers, \"(\", \")\", \"+\" and \"-\" (up to 30 characters). pattern according to ISO20022 \\+[0-9]{1,3}-[0-9()+\\-]{1,30} ")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2021-11-05T12:22:49.487689+02:00[Europe/Kiev]")

public class CardAcceptorPhone   {

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
    sb.append("class CardAcceptorPhone {\n");

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

