package de.adorsys.psd2.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

/**
 * See document \\\&quot;openFinance API Framework Data Dictionary\\\&quot;,
 * section \\\&quot;Postal Address (Either Structured or Unstructured
 * Address)\\\&quot; for more details.
 */
@Schema(description = "See document \\\"openFinance API Framework Data Dictionary\\\", section \\\"Postal Address (Either Structured or Unstructured Address)\\\" for more details. ")
@Validated
@jakarta.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2025-10-20T11:14:45.442141177+01:00[Africa/Douala]")

@SuppressWarnings("PMD")
public class PostalAddress {
  @JsonProperty("addressLines")
  @Valid
  private List<String> addressLines = null;

  @JsonProperty("department")
  private String department = null;

  @JsonProperty("subDepartment")
  private String subDepartment = null;

  @JsonProperty("streetName")
  private String streetName = null;

  @JsonProperty("buildingNumber")
  private String buildingNumber = null;

  @JsonProperty("buildingName")
  private String buildingName = null;

  @JsonProperty("floor")
  private String floor = null;

  @JsonProperty("postBox")
  private String postBox = null;

  @JsonProperty("room")
  private String room = null;

  @JsonProperty("postCode")
  private String postCode = null;

  @JsonProperty("townName")
  private String townName = null;

  @JsonProperty("townLocationName")
  private String townLocationName = null;

  @JsonProperty("districtName")
  private String districtName = null;

  @JsonProperty("countrySubDivision")
  private String countrySubDivision = null;

  @JsonProperty("country")
  private String country = null;

  public PostalAddress addressLines(List<String> addressLines) {
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
   * At most seven entries are permitted. May only be used, if none of the
   * structured address elements \\\"streetName\\\", \\\"buildingNumber\\\",
   * \\\"postcode\\\" or \\\"townName\\\" is used. *Remark:* For SEPA transactions
   * this is further restricted to a maximum of 1 entry.
   *
   * @return addressLines
   **/
  @Schema(description = "At most seven entries are permitted. May only be used, if none of the structured address elements \\\"streetName\\\", \\\"buildingNumber\\\", \\\"postcode\\\" or \\\"townName\\\" is used. *Remark:* For SEPA transactions this is further restricted to a maximum of 1 entry. ")
  @JsonProperty("addressLines")

  @Size(max = 7)
  public List<String> getAddressLines() {
    return addressLines;
  }

  public void setAddressLines(List<String> addressLines) {
    this.addressLines = addressLines;
  }

  public PostalAddress department(String department) {
    this.department = department;
    return this;
  }

  /**
   * Identification of a division of a large organisation or building.
   *
   * @return department
   **/
  @Schema(description = "Identification of a division of a large organisation or building. ")
  @JsonProperty("department")

  public String getDepartment() {
    return department;
  }

  public void setDepartment(String department) {
    this.department = department;
  }

  public PostalAddress subDepartment(String subDepartment) {
    this.subDepartment = subDepartment;
    return this;
  }

  /**
   * Identification of a sub-division of a large organisation or building.
   *
   * @return subDepartment
   **/
  @Schema(description = "Identification of a sub-division of a large organisation or building. ")
  @JsonProperty("subDepartment")

  public String getSubDepartment() {
    return subDepartment;
  }

  public void setSubDepartment(String subDepartment) {
    this.subDepartment = subDepartment;
  }

  public PostalAddress streetName(String streetName) {
    this.streetName = streetName;
    return this;
  }

  /**
   * Name of a street or thoroughfare.
   *
   * @return streetName
   **/
  @Schema(description = "Name of a street or thoroughfare. ")
  @JsonProperty("streetName")

  public String getStreetName() {
    return streetName;
  }

  public void setStreetName(String streetName) {
    this.streetName = streetName;
  }

  public PostalAddress buildingNumber(String buildingNumber) {
    this.buildingNumber = buildingNumber;
    return this;
  }

  /**
   * Number that identifies the position of a building on a street.
   *
   * @return buildingNumber
   **/
  @Schema(description = "Number that identifies the position of a building on a street. ")
  @JsonProperty("buildingNumber")

  public String getBuildingNumber() {
    return buildingNumber;
  }

  public void setBuildingNumber(String buildingNumber) {
    this.buildingNumber = buildingNumber;
  }

  public PostalAddress buildingName(String buildingName) {
    this.buildingName = buildingName;
    return this;
  }

  /**
   * Name of the building or house.
   *
   * @return buildingName
   **/
  @Schema(description = "Name of the building or house. ")
  @JsonProperty("buildingName")

  public String getBuildingName() {
    return buildingName;
  }

  public void setBuildingName(String buildingName) {
    this.buildingName = buildingName;
  }

  public PostalAddress floor(String floor) {
    this.floor = floor;
    return this;
  }

  /**
   * Floor or storey within a building.
   *
   * @return floor
   **/
  @Schema(description = "Floor or storey within a building. ")
  @JsonProperty("floor")

  public String getFloor() {
    return floor;
  }

  public void setFloor(String floor) {
    this.floor = floor;
  }

  public PostalAddress postBox(String postBox) {
    this.postBox = postBox;
    return this;
  }

  /**
   * Numbered box in a post office, assigned to a person or organisation, where
   * letters are kept until called for.
   *
   * @return postBox
   **/
  @Schema(description = "Numbered box in a post office, assigned to a person or organisation, where letters are kept until called for. ")
  @JsonProperty("postBox")

  public String getPostBox() {
    return postBox;
  }

  public void setPostBox(String postBox) {
    this.postBox = postBox;
  }

  public PostalAddress room(String room) {
    this.room = room;
    return this;
  }

  /**
   * Building room number.
   *
   * @return room
   **/
  @Schema(description = "Building room number. ")
  @JsonProperty("room")

  public String getRoom() {
    return room;
  }

  public void setRoom(String room) {
    this.room = room;
  }

  public PostalAddress postCode(String postCode) {
    this.postCode = postCode;
    return this;
  }

  /**
   * Identifier consisting of a group of letters and/or numbers that is added to a
   * postal address to assist the sorting of mail.
   *
   * @return postCode
   **/
  @Schema(description = "Identifier consisting of a group of letters and/or numbers that is added to a postal address to assist the sorting of mail. ")
  @JsonProperty("postCode")

  public String getPostCode() {
    return postCode;
  }

  public void setPostCode(String postCode) {
    this.postCode = postCode;
  }

  public PostalAddress townName(String townName) {
    this.townName = townName;
    return this;
  }

  /**
   * Name of a built-up area, with defined boundaries, and a local government.
   * *Usage Rule:* If address lines are not used, this attribute is mandatrory.
   *
   * @return townName
   **/
  @Schema(description = "Name of a built-up area, with defined boundaries, and a local government.  *Usage Rule:* If address lines are not used, this attribute is mandatrory. ")
  @JsonProperty("townName")

  public String getTownName() {
    return townName;
  }

  public void setTownName(String townName) {
    this.townName = townName;
  }

  public PostalAddress townLocationName(String townLocationName) {
    this.townLocationName = townLocationName;
    return this;
  }

  /**
   * Specific location name within the town.
   *
   * @return townLocationName
   **/
  @Schema(description = "Specific location name within the town. ")
  @JsonProperty("townLocationName")

  public String getTownLocationName() {
    return townLocationName;
  }

  public void setTownLocationName(String townLocationName) {
    this.townLocationName = townLocationName;
  }

  public PostalAddress districtName(String districtName) {
    this.districtName = districtName;
    return this;
  }

  /**
   * Identifies a subdivision within a country sub-division.
   *
   * @return districtName
   **/
  @Schema(description = "Identifies a subdivision within a country sub-division. ")
  @JsonProperty("districtName")

  public String getDistrictName() {
    return districtName;
  }

  public void setDistrictName(String districtName) {
    this.districtName = districtName;
  }

  public PostalAddress countrySubDivision(String countrySubDivision) {
    this.countrySubDivision = countrySubDivision;
    return this;
  }

  /**
   * Identifies a subdivision of a country such as state, region, county.
   *
   * @return countrySubDivision
   **/
  @Schema(description = "Identifies a subdivision of a country such as state, region, county. ")
  @JsonProperty("countrySubDivision")

  public String getCountrySubDivision() {
    return countrySubDivision;
  }

  public void setCountrySubDivision(String countrySubDivision) {
    this.countrySubDivision = countrySubDivision;
  }

  public PostalAddress country(String country) {
    this.country = country;
    return this;
  }

  /**
   * Nation with its own government. *Usage Rule:* If address lines are not used,
   * this attribute is mandatrory.
   *
   * @return country
   **/
  @Schema(description = "Nation with its own government. *Usage Rule:* If address lines are not used, this attribute is mandatrory. ")
  @JsonProperty("country")

  public String getCountry() {
    return country;
  }

  public void setCountry(String country) {
    this.country = country;
  }

  @Override
  public boolean equals(java.lang.Object o) {
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
    return Objects.hash(addressLines, department, subDepartment, streetName, buildingNumber, buildingName, floor,
        postBox, room, postCode, townName, townLocationName, districtName, countrySubDivision, country);
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
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}
