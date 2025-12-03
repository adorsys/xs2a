package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * See document \\\&quot;openFinance API Framework Data Dictionary\\\&quot;, section \\\&quot;Postal Address (Either Structured or Unstructured Address)\\\&quot; for more details.
 */

@Schema(name = "postalAddress", description = "See document \\\"openFinance API Framework Data Dictionary\\\", section \\\"Postal Address (Either Structured or Unstructured Address)\\\" for more details. ")
@JsonTypeName("postalAddress")
@SuppressWarnings("PMD")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2025-09-18T08:00:00.499860948+01:00[Africa/Douala]", comments = "Generator version: 7.15.0")
public class PostalAddress {

  @Valid
  private List<@Size(max = 140)String> addressLines = new ArrayList<>();

  private Optional<@Size(max = 70) String> department = Optional.empty();

  private Optional<@Size(max = 70) String> subDepartment = Optional.empty();

  private Optional<@Size(max = 70) String> streetName = Optional.empty();

  private Optional<@Size(max = 16) String> buildingNumber = Optional.empty();

  private Optional<@Size(max = 35) String> buildingName = Optional.empty();

  private Optional<@Size(max = 70) String> floor = Optional.empty();

  private Optional<@Size(max = 16) String> postBox = Optional.empty();

  private Optional<@Size(max = 70) String> room = Optional.empty();

  private Optional<@Size(max = 16) String> postCode = Optional.empty();

  private Optional<@Size(max = 35) String> townName = Optional.empty();

  private Optional<@Size(max = 35) String> townLocationName = Optional.empty();

  private Optional<@Size(max = 35) String> districtName = Optional.empty();

  private Optional<@Size(max = 35) String> countrySubDivision = Optional.empty();

  private Optional<@Pattern(regexp = "[A-Z]{2}") String> country = Optional.empty();

  public PostalAddress addressLines(List<@Size(max = 140)String> addressLines) {
    this.addressLines = addressLines;
    return this;
  }

  public PostalAddress addAddressLinesItem(String addressLinesItem) {
    if (this.addressLines == null) {
      this.addressLines = new ArrayList<>();
    }
    this.addressLines.add(addressLinesItem);
    return this;
  }

  /**
   * At most seven entries are permitted. May only be used, if none of the structured address elements \\\"streetName\\\", \\\"buildingNumber\\\", \\\"postcode\\\" or \\\"townName\\\" is used. *Remark:* For SEPA transactions this is further restricted to a maximum of 1 entry.
   * @return addressLines
   */
  @Size(max = 7)
  @Schema(name = "addressLines", description = "At most seven entries are permitted. May only be used, if none of the structured address elements \\\"streetName\\\", \\\"buildingNumber\\\", \\\"postcode\\\" or \\\"townName\\\" is used. *Remark:* For SEPA transactions this is further restricted to a maximum of 1 entry. ", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("addressLines")
  public List<@Size(max = 140)String> getAddressLines() {
    return addressLines;
  }

  public void setAddressLines(List<@Size(max = 140)String> addressLines) {
    this.addressLines = addressLines;
  }

  public PostalAddress department(String department) {
    this.department = Optional.ofNullable(department);
    return this;
  }

  /**
   * Identification of a division of a large organisation or building.
   * @return department
   */

  @Schema(name = "department", example = "Text, maximum of 70 characters.", description = "Identification of a division of a large organisation or building. ", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("department")
  public Optional<@Size(max = 70) String> getDepartment() {
    return department;
  }

  public void setDepartment(Optional<String> department) {
    this.department = department;
  }

  public PostalAddress subDepartment(String subDepartment) {
    this.subDepartment = Optional.ofNullable(subDepartment);
    return this;
  }

  /**
   * Identification of a sub-division of a large organisation or building.
   * @return subDepartment
   */

  @Schema(name = "subDepartment", example = "Text, maximum of 70 characters.", description = "Identification of a sub-division of a large organisation or building. ", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("subDepartment")
  public Optional<@Size(max = 70) String> getSubDepartment() {
    return subDepartment;
  }

  public void setSubDepartment(Optional<String> subDepartment) {
    this.subDepartment = subDepartment;
  }

  public PostalAddress streetName(String streetName) {
    this.streetName = Optional.ofNullable(streetName);
    return this;
  }

  /**
   * Name of a street or thoroughfare.
   * @return streetName
   */

  @Schema(name = "streetName", example = "Text, maximum of 70 characters.", description = "Name of a street or thoroughfare. ", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("streetName")
  public Optional<@Size(max = 70) String> getStreetName() {
    return streetName;
  }

  public void setStreetName(Optional<String> streetName) {
    this.streetName = streetName;
  }

  public PostalAddress buildingNumber(String buildingNumber) {
    this.buildingNumber = Optional.ofNullable(buildingNumber);
    return this;
  }

  /**
   * Number that identifies the position of a building on a street.
   * @return buildingNumber
   */

  @Schema(name = "buildingNumber", example = "16 Chars at most", description = "Number that identifies the position of a building on a street. ", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("buildingNumber")
  public Optional<@Size(max = 16) String> getBuildingNumber() {
    return buildingNumber;
  }

  public void setBuildingNumber(Optional<String> buildingNumber) {
    this.buildingNumber = buildingNumber;
  }

  public PostalAddress buildingName(String buildingName) {
    this.buildingName = Optional.ofNullable(buildingName);
    return this;
  }

  /**
   * Name of the building or house.
   * @return buildingName
   */

  @Schema(name = "buildingName", example = "Text, maximum of 35 characters.", description = "Name of the building or house. ", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("buildingName")
  public Optional<@Size(max = 35) String> getBuildingName() {
    return buildingName;
  }

  public void setBuildingName(Optional<String> buildingName) {
    this.buildingName = buildingName;
  }

  public PostalAddress floor(String floor) {
    this.floor = Optional.ofNullable(floor);
    return this;
  }

  /**
   * Floor or storey within a building.
   * @return floor
   */

  @Schema(name = "floor", example = "Text, maximum of 70 characters.", description = "Floor or storey within a building. ", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("floor")
  public Optional<@Size(max = 70) String> getFloor() {
    return floor;
  }

  public void setFloor(Optional<String> floor) {
    this.floor = floor;
  }

  public PostalAddress postBox(String postBox) {
    this.postBox = Optional.ofNullable(postBox);
    return this;
  }

  /**
   * Numbered box in a post office, assigned to a person or organisation, where letters are kept until called for.
   * @return postBox
   */

  @Schema(name = "postBox", example = "16 Chars at most", description = "Numbered box in a post office, assigned to a person or organisation, where letters are kept until called for. ", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("postBox")
  public Optional<@Size(max = 16) String> getPostBox() {
    return postBox;
  }

  public void setPostBox(Optional<String> postBox) {
    this.postBox = postBox;
  }

  public PostalAddress room(String room) {
    this.room = Optional.ofNullable(room);
    return this;
  }

  /**
   * Building room number.
   * @return room
   */

  @Schema(name = "room", example = "Text, maximum of 70 characters.", description = "Building room number. ", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("room")
  public Optional<@Size(max = 70) String> getRoom() {
    return room;
  }

  public void setRoom(Optional<String> room) {
    this.room = room;
  }

  public PostalAddress postCode(String postCode) {
    this.postCode = Optional.ofNullable(postCode);
    return this;
  }

  /**
   * Identifier consisting of a group of letters and/or numbers that is added to a postal address to assist the sorting of mail.
   * @return postCode
   */

  @Schema(name = "postCode", example = "16 Chars at most", description = "Identifier consisting of a group of letters and/or numbers that is added to a postal address to assist the sorting of mail. ", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("postCode")
  public Optional<@Size(max = 16) String> getPostCode() {
    return postCode;
  }

  public void setPostCode(Optional<String> postCode) {
    this.postCode = postCode;
  }

  public PostalAddress townName(String townName) {
    this.townName = Optional.ofNullable(townName);
    return this;
  }

  /**
   * Name of a built-up area, with defined boundaries, and a local government.  *Usage Rule:* If address lines are not used, this attribute is mandatrory.
   * @return townName
   */

  @Schema(name = "townName", example = "Text, maximum of 35 characters.", description = "Name of a built-up area, with defined boundaries, and a local government.  *Usage Rule:* If address lines are not used, this attribute is mandatrory. ", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("townName")
  public Optional<@Size(max = 35) String> getTownName() {
    return townName;
  }

  public void setTownName(Optional<String> townName) {
    this.townName = townName;
  }

  public PostalAddress townLocationName(String townLocationName) {
    this.townLocationName = Optional.ofNullable(townLocationName);
    return this;
  }

  /**
   * Specific location name within the town.
   * @return townLocationName
   */

  @Schema(name = "townLocationName", example = "Text, maximum of 35 characters.", description = "Specific location name within the town. ", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("townLocationName")
  public Optional<@Size(max = 35) String> getTownLocationName() {
    return townLocationName;
  }

  public void setTownLocationName(Optional<String> townLocationName) {
    this.townLocationName = townLocationName;
  }

  public PostalAddress districtName(String districtName) {
    this.districtName = Optional.ofNullable(districtName);
    return this;
  }

  /**
   * Identifies a subdivision within a country sub-division.
   * @return districtName
   */

  @Schema(name = "districtName", example = "Text, maximum of 35 characters.", description = "Identifies a subdivision within a country sub-division. ", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("districtName")
  public Optional<@Size(max = 35) String> getDistrictName() {
    return districtName;
  }

  public void setDistrictName(Optional<String> districtName) {
    this.districtName = districtName;
  }

  public PostalAddress countrySubDivision(String countrySubDivision) {
    this.countrySubDivision = Optional.ofNullable(countrySubDivision);
    return this;
  }

  /**
   * Identifies a subdivision of a country such as state, region, county.
   * @return countrySubDivision
   */

  @Schema(name = "countrySubDivision", example = "Text, maximum of 35 characters.", description = "Identifies a subdivision of a country such as state, region, county. ", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("countrySubDivision")
  public Optional<@Size(max = 35) String> getCountrySubDivision() {
    return countrySubDivision;
  }

  public void setCountrySubDivision(Optional<String> countrySubDivision) {
    this.countrySubDivision = countrySubDivision;
  }

  public PostalAddress country(String country) {
    this.country = Optional.ofNullable(country);
    return this;
  }

  /**
   * Nation with its own government. *Usage Rule:* If address lines are not used, this attribute is mandatrory.
   * @return country
   */

  @Schema(name = "country", example = "SE", description = "Nation with its own government. *Usage Rule:* If address lines are not used, this attribute is mandatrory. ", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("country")
  public Optional<@Pattern(regexp = "[A-Z]{2}") String> getCountry() {
    return country;
  }

  public void setCountry(Optional<String> country) {
    this.country = country;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PostalAddress postalAddress = (PostalAddress) o;
    return Objects.equals(this.addressLines, postalAddress.addressLines) &&
        Objects.equals(this.department, postalAddress.department) &&
        Objects.equals(this.subDepartment, postalAddress.subDepartment) &&
        Objects.equals(this.streetName, postalAddress.streetName) &&
        Objects.equals(this.buildingNumber, postalAddress.buildingNumber) &&
        Objects.equals(this.buildingName, postalAddress.buildingName) &&
        Objects.equals(this.floor, postalAddress.floor) &&
        Objects.equals(this.postBox, postalAddress.postBox) &&
        Objects.equals(this.room, postalAddress.room) &&
        Objects.equals(this.postCode, postalAddress.postCode) &&
        Objects.equals(this.townName, postalAddress.townName) &&
        Objects.equals(this.townLocationName, postalAddress.townLocationName) &&
        Objects.equals(this.districtName, postalAddress.districtName) &&
        Objects.equals(this.countrySubDivision, postalAddress.countrySubDivision) &&
        Objects.equals(this.country, postalAddress.country);
  }

  @Override
  public int hashCode() {
    return Objects.hash(addressLines, department, subDepartment, streetName, buildingNumber, buildingName, floor, postBox, room, postCode, townName, townLocationName, districtName, countrySubDivision, country);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PostalAddress {\n");
    sb.append("    addressLines: ").append(toIndentedString(addressLines)).append("\n");
    sb.append("    department: ").append(toIndentedString(department)).append("\n");
    sb.append("    subDepartment: ").append(toIndentedString(subDepartment)).append("\n");
    sb.append("    streetName: ").append(toIndentedString(streetName)).append("\n");
    sb.append("    buildingNumber: ").append(toIndentedString(buildingNumber)).append("\n");
    sb.append("    buildingName: ").append(toIndentedString(buildingName)).append("\n");
    sb.append("    floor: ").append(toIndentedString(floor)).append("\n");
    sb.append("    postBox: ").append(toIndentedString(postBox)).append("\n");
    sb.append("    room: ").append(toIndentedString(room)).append("\n");
    sb.append("    postCode: ").append(toIndentedString(postCode)).append("\n");
    sb.append("    townName: ").append(toIndentedString(townName)).append("\n");
    sb.append("    townLocationName: ").append(toIndentedString(townLocationName)).append("\n");
    sb.append("    districtName: ").append(toIndentedString(districtName)).append("\n");
    sb.append("    countrySubDivision: ").append(toIndentedString(countrySubDivision)).append("\n");
    sb.append("    country: ").append(toIndentedString(country)).append("\n");
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

