package de.adorsys.psd2.model;

import io.swagger.annotations.ApiModel;
import org.springframework.validation.annotation.Validated;

import java.util.Objects;

/**
 * Message codes defined for signing baskets for HTTP Error code 400 (BAD_REQUEST).
 */
@ApiModel(description = "Message codes defined for signing baskets for HTTP Error code 400 (BAD_REQUEST).")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2019-11-11T13:48:52.194360+02:00[Europe/Kiev]")

public class MessageCode400SBS   {

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
    sb.append("class MessageCode400SBS {\n");

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

