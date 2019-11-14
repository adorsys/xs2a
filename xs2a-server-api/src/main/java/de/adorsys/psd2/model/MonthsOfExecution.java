package de.adorsys.psd2.model;

import io.swagger.annotations.ApiModel;
import org.springframework.validation.annotation.Validated;

import java.util.Objects;

/**
 * The format is following the regular expression \\d{1,2}. The array is restricted to 11 entries. The values contained in the array entries shall all be different and the maximum value of one entry is 12. This attribute is contained if and only if the frequency equals \&quot;MonthlyVariable\&quot;. Example: An execution on January, April and October each year is addressed by [\&quot;1\&quot;, \&quot;4\&quot;, \&quot;10\&quot;].
 */
@ApiModel(description = "The format is following the regular expression \\d{1,2}. The array is restricted to 11 entries. The values contained in the array entries shall all be different and the maximum value of one entry is 12. This attribute is contained if and only if the frequency equals \"MonthlyVariable\". Example: An execution on January, April and October each year is addressed by [\"1\", \"4\", \"10\"]. ")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2019-11-11T13:48:52.194360+02:00[Europe/Kiev]")

public class MonthsOfExecution   {

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
    sb.append("class MonthsOfExecution {\n");

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

