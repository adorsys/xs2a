package de.adorsys.psd2.model;

import java.util.Objects;
import io.swagger.annotations.ApiModel;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * This parameter is defining a valid until date (including the mentioned date) for the requested consent.  The content is the local ASPSP date in ISO-Date format, e.g. 2017-10-30.  Future dates might get adjusted by ASPSP.   If a maximal available date is requested, a date in far future is to be used: \&quot;9999-12-31\&quot;.   In both cases the consent object to be retrieved by the get consent request will contain the adjusted date.
 */
@ApiModel(description = "This parameter is defining a valid until date (including the mentioned date) for the requested consent.  The content is the local ASPSP date in ISO-Date format, e.g. 2017-10-30.  Future dates might get adjusted by ASPSP.   If a maximal available date is requested, a date in far future is to be used: \"9999-12-31\".   In both cases the consent object to be retrieved by the get consent request will contain the adjusted date. ")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2020-08-31T16:39:54.348465+03:00[Europe/Kiev]")

public class ValidUntil   {

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
    sb.append("class ValidUntil {\n");

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

