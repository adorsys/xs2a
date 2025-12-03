package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * See document \\\&quot;openFinance API Framework Data Dictionary\\\&quot;, section \\\&quot;Private Identification\\\&quot; for more details.
 */

@Schema(name = "privateIdentification", description = "See document \\\"openFinance API Framework Data Dictionary\\\", section \\\"Private Identification\\\" for more details. ")
@JsonTypeName("privateIdentification")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2025-09-18T08:00:00.499860948+01:00[Africa/Douala]", comments = "Generator version: 7.15.0")
public class PrivateIdentification {

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  private Optional<LocalDate> birthDate = Optional.empty();

  private Optional<@Size(max = 35) String> provinceOfBirth = Optional.empty();

  private Optional<@Size(max = 35) String> cityOfBirth = Optional.empty();

  private Optional<@Pattern(regexp = "[A-Z]{2}") String> countryOfBirth = Optional.empty();

  @Valid
  private List<@Valid PersonIdentification> others = new ArrayList<>();

  public PrivateIdentification birthDate(LocalDate birthDate) {
    this.birthDate = Optional.ofNullable(birthDate);
    return this;
  }

  /**
   *
   * @return birthDate
   */
  @Valid
  @Schema(name = "birthDate", example = "2020-01-01", description = "", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("birthDate")
  public Optional<LocalDate> getBirthDate() {
    return birthDate;
  }

  public void setBirthDate(Optional<LocalDate> birthDate) {
    this.birthDate = birthDate;
  }

  public PrivateIdentification provinceOfBirth(String provinceOfBirth) {
    this.provinceOfBirth = Optional.ofNullable(provinceOfBirth);
    return this;
  }

  /**
   *
   * @return provinceOfBirth
   */

  @Schema(name = "provinceOfBirth", example = "Text, maximum of 35 characters.", description = "", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("provinceOfBirth")
  public Optional<@Size(max = 35) String> getProvinceOfBirth() {
    return provinceOfBirth;
  }

  public void setProvinceOfBirth(Optional<String> provinceOfBirth) {
    this.provinceOfBirth = provinceOfBirth;
  }

  public PrivateIdentification cityOfBirth(String cityOfBirth) {
    this.cityOfBirth = Optional.ofNullable(cityOfBirth);
    return this;
  }

  /**
   *
   * @return cityOfBirth
   */

  @Schema(name = "cityOfBirth", example = "Text, maximum of 35 characters.", description = "", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("cityOfBirth")
  public Optional<@Size(max = 35) String> getCityOfBirth() {
    return cityOfBirth;
  }

  public void setCityOfBirth(Optional<String> cityOfBirth) {
    this.cityOfBirth = cityOfBirth;
  }

  public PrivateIdentification countryOfBirth(String countryOfBirth) {
    this.countryOfBirth = Optional.ofNullable(countryOfBirth);
    return this;
  }

  /**
   *
   * @return countryOfBirth
   */

  @Schema(name = "countryOfBirth", example = "SE", description = "", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("countryOfBirth")
  public Optional<@Pattern(regexp = "[A-Z]{2}") String> getCountryOfBirth() {
    return countryOfBirth;
  }

  public void setCountryOfBirth(Optional<String> countryOfBirth) {
    this.countryOfBirth = countryOfBirth;
  }

  public PrivateIdentification others(List<@Valid PersonIdentification> others) {
    this.others = others;
    return this;
  }

  public PrivateIdentification addOthersItem(PersonIdentification othersItem) {
    if (this.others == null) {
      this.others = new ArrayList<>();
    }
    this.others.add(othersItem);
    return this;
  }

  /**
   * Unique identification of a person, as assigned by an institution, using an identification scheme.  Shall be used if none of the above attributes is used.
   * @return others
   */
  @Valid
  @Schema(name = "others", description = "Unique identification of a person, as assigned by an institution, using an identification scheme.  Shall be used if none of the above attributes is used. ", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("others")
  public List<@Valid PersonIdentification> getOthers() {
    return others;
  }

  public void setOthers(List<@Valid PersonIdentification> others) {
    this.others = others;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PrivateIdentification privateIdentification = (PrivateIdentification) o;
    return Objects.equals(this.birthDate, privateIdentification.birthDate) &&
        Objects.equals(this.provinceOfBirth, privateIdentification.provinceOfBirth) &&
        Objects.equals(this.cityOfBirth, privateIdentification.cityOfBirth) &&
        Objects.equals(this.countryOfBirth, privateIdentification.countryOfBirth) &&
        Objects.equals(this.others, privateIdentification.others);
  }

  @Override
  public int hashCode() {
    return Objects.hash(birthDate, provinceOfBirth, cityOfBirth, countryOfBirth, others);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PrivateIdentification {\n");
    sb.append("    birthDate: ").append(toIndentedString(birthDate)).append("\n");
    sb.append("    provinceOfBirth: ").append(toIndentedString(provinceOfBirth)).append("\n");
    sb.append("    cityOfBirth: ").append(toIndentedString(cityOfBirth)).append("\n");
    sb.append("    countryOfBirth: ").append(toIndentedString(countryOfBirth)).append("\n");
    sb.append("    others: ").append(toIndentedString(others)).append("\n");
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

