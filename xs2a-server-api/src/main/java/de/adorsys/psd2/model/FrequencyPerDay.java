package de.adorsys.psd2.model;

import java.util.Objects;
import io.swagger.annotations.ApiModel;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * This field indicates the requested maximum frequency for an access without PSU involvement per day. For a one-off access, this attribute is set to \&quot;1\&quot;.  The frequency needs to be greater equal to one.   If not otherwise agreed bilaterally between TPP and ASPSP, the frequency is less equal to 4.
 */
@ApiModel(description = "This field indicates the requested maximum frequency for an access without PSU involvement per day. For a one-off access, this attribute is set to \"1\".  The frequency needs to be greater equal to one.   If not otherwise agreed bilaterally between TPP and ASPSP, the frequency is less equal to 4. ")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2020-08-25T18:03:04.675305+03:00[Europe/Kiev]")

public class FrequencyPerDay   {

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
    sb.append("class FrequencyPerDay {\n");

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

