package de.adorsys.psd2.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.*;

/**
 * Address
 */
@Validated
@jakarta.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2025-10-20T11:14:45.442141177+01:00[Africa/Douala]")

public class Address {
    @JsonProperty("addressLines")
    private java.util.List<String> addressLines = null;

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

    public Address addressLines(java.util.List<String> addressLines) {
        this.addressLines = addressLines;
        return this;
    }

    public Address addAddressLinesItem(String addressLinesItem) {
        if (this.addressLines == null) {
            this.addressLines = new java.util.ArrayList<String>();
        }
        this.addressLines.add(addressLinesItem);
        return this;
    }

    /**
     * At most seven entries are permitted. May only be used, if none of the
     * structured address elements \"streetName\", \"buildingNumber\", \"postcode\"
     * or \"townName\" is used. *Remark:* For SEPA transactions this is further
     * restricted to a maximum of 1 entry.
     *
     * @return addressLines
     **/
    @Schema(description = "At most seven entries are permitted. May only be used, if none of the structured address elements \"streetName\", \"buildingNumber\", \"postcode\" or \"townName\" is used. *Remark:* For SEPA transactions this is further restricted to a maximum of 1 entry.")
    @JsonProperty("addressLines")

    @Size(max = 7)
    public java.util.List<String> getAddressLines() {
        return addressLines;
    }

    public void setAddressLines(java.util.List<String> addressLines) {
        this.addressLines = addressLines;
    }

    public Address department(String department) {
        this.department = department;
        return this;
    }

    /**
     * Get department
     *
     * @return department
     **/
    @Schema(description = "")
    @JsonProperty("department")

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public Address subDepartment(String subDepartment) {
        this.subDepartment = subDepartment;
        return this;
    }

    /**
     * Get subDepartment
     *
     * @return subDepartment
     **/
    @Schema(description = "")
    @JsonProperty("subDepartment")

    public String getSubDepartment() {
        return subDepartment;
    }

    public void setSubDepartment(String subDepartment) {
        this.subDepartment = subDepartment;
    }

    public Address streetName(String streetName) {
        this.streetName = streetName;
        return this;
    }

    /**
     * Get streetName
     *
     * @return streetName
     **/
    @Schema(description = "")
    @JsonProperty("streetName")

    public String getStreetName() {
        return streetName;
    }

    public void setStreetName(String streetName) {
        this.streetName = streetName;
    }

    public Address buildingNumber(String buildingNumber) {
        this.buildingNumber = buildingNumber;
        return this;
    }

    /**
     * Get buildingNumber
     *
     * @return buildingNumber
     **/
    @Schema(description = "")
    @JsonProperty("buildingNumber")

    public String getBuildingNumber() {
        return buildingNumber;
    }

    public void setBuildingNumber(String buildingNumber) {
        this.buildingNumber = buildingNumber;
    }

    public Address buildingName(String buildingName) {
        this.buildingName = buildingName;
        return this;
    }

    /**
     * Get buildingName
     *
     * @return buildingName
     **/
    @Schema(description = "")
    @JsonProperty("buildingName")

    public String getBuildingName() {
        return buildingName;
    }

    public void setBuildingName(String buildingName) {
        this.buildingName = buildingName;
    }

    public Address floor(String floor) {
        this.floor = floor;
        return this;
    }

    /**
     * Get floor
     *
     * @return floor
     **/
    @Schema(description = "")
    @JsonProperty("floor")

    public String getFloor() {
        return floor;
    }

    public void setFloor(String floor) {
        this.floor = floor;
    }

    public Address postBox(String postBox) {
        this.postBox = postBox;
        return this;
    }

    /**
     * Get postBox
     *
     * @return postBox
     **/
    @Schema(description = "")
    @JsonProperty("postBox")

    public String getPostBox() {
        return postBox;
    }

    public void setPostBox(String postBox) {
        this.postBox = postBox;
    }

    public Address room(String room) {
        this.room = room;
        return this;
    }

    /**
     * Get room
     *
     * @return room
     **/
    @Schema(description = "")
    @JsonProperty("room")

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public Address postCode(String postCode) {
        this.postCode = postCode;
        return this;
    }

    /**
     * Get postCode
     *
     * @return postCode
     **/
    @Schema(description = "")
    @JsonProperty("postCode")

    public String getPostCode() {
        return postCode;
    }

    public void setPostCode(String postCode) {
        this.postCode = postCode;
    }

    public Address townName(String townName) {
        this.townName = townName;
        return this;
    }

    /**
     * Get townName
     *
     * @return townName
     **/
    @Schema(description = "")
    @JsonProperty("townName")

    public String getTownName() {
        return townName;
    }

    public void setTownName(String townName) {
        this.townName = townName;
    }

    public Address townLocationName(String townLocationName) {
        this.townLocationName = townLocationName;
        return this;
    }

    /**
     * Get townLocationName
     *
     * @return townLocationName
     **/
    @Schema(description = "")
    @JsonProperty("townLocationName")

    public String getTownLocationName() {
        return townLocationName;
    }

    public void setTownLocationName(String townLocationName) {
        this.townLocationName = townLocationName;
    }

    public Address districtName(String districtName) {
        this.districtName = districtName;
        return this;
    }

    /**
     * Get districtName
     *
     * @return districtName
     **/
    @Schema(description = "")
    @JsonProperty("districtName")

    public String getDistrictName() {
        return districtName;
    }

    public void setDistrictName(String districtName) {
        this.districtName = districtName;
    }

    public Address countrySubDivision(String countrySubDivision) {
        this.countrySubDivision = countrySubDivision;
        return this;
    }

    /**
     * Get countrySubDivision
     *
     * @return countrySubDivision
     **/
    @Schema(description = "")
    @JsonProperty("countrySubDivision")

    public String getCountrySubDivision() {
        return countrySubDivision;
    }

    public void setCountrySubDivision(String countrySubDivision) {
        this.countrySubDivision = countrySubDivision;
    }

    public Address country(String country) {
        this.country = country;
        return this;
    }

    /**
     * ISO 3166 ALPHA2 country code.
     *
     * @return country
     **/
    @Schema(example = "SE", required = true, description = "ISO 3166 ALPHA2 country code.")
    @JsonProperty("country")
    @NotNull

    @Pattern(regexp = "[A-Z]{2}")
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
        Address address = (Address) o;
        return Objects.equals(this.addressLines, address.addressLines) &&
                Objects.equals(this.department, address.department) &&
                Objects.equals(this.subDepartment, address.subDepartment) &&
                Objects.equals(this.streetName, address.streetName) &&
                Objects.equals(this.buildingNumber, address.buildingNumber) &&
                Objects.equals(this.buildingName, address.buildingName) &&
                Objects.equals(this.floor, address.floor) &&
                Objects.equals(this.postBox, address.postBox) &&
                Objects.equals(this.room, address.room) &&
                Objects.equals(this.postCode, address.postCode) &&
                Objects.equals(this.townName, address.townName) &&
                Objects.equals(this.townLocationName, address.townLocationName) &&
                Objects.equals(this.districtName, address.districtName) &&
                Objects.equals(this.countrySubDivision, address.countrySubDivision) &&
                Objects.equals(this.country, address.country);
    }

    @Override
    public int hashCode() {
        return Objects.hash(addressLines, department, subDepartment, streetName, buildingNumber, buildingName, floor,
                postBox, room, postCode, townName, townLocationName, districtName, countrySubDivision, country);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Address {\n");

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
