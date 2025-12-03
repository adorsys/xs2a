package de.adorsys.psd2.consent.domain.payment;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;

import java.util.List;

@Data
@Entity(name = "pis_address")
@Schema(description = "Pis address", name = "PIS address")
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PisAddress {
    @Id
    @Column(name = "address_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pis_address_generator")
    @SequenceGenerator(name = "pis_address_generator", sequenceName = "pis_address_id_seq", allocationSize = 1)
    private Long id;

    @ElementCollection
    @CollectionTable(name = "pis_address_lines", joinColumns = @JoinColumn(name = "address_id"))
    @Column(name = "address_line")
    @OrderColumn(name = "line_order")
    @JsonProperty("addressLines")
    @Schema(description = "Address lines")
    private List<String> addressLines;

    @Schema(description = "Department")
    private String department;

    @Column(name = "sub_department")
    @Schema(description = "Sub department")
    private String subDepartment;

    @Schema(description = "Street", example = "Herrnstraße")
    private String street;

    @Column(name = "building_number")
    @Schema(description = "Building number", example = "123-34")
    private String buildingNumber;

    @Column(name = "building_name")
    @Schema(description = "Building name")
    private String buildingName;

    @Schema(description = "Floor")
    private String floor;

    @Column(name = "post_box")
    @Schema(description = "Post box")
    private String postBox;

    @Schema(description = "Room")
    private String room;

    @Schema(description = "City", example = "Nürnberg")
    private String city;

    @Column(name = "postal_code")
    @Schema(description = "Postal code", example = "90431")
    private String postalCode;

    @Column(name = "town_location_name")
    @Schema(description = "Town location name")
    private String townLocationName;

    @Column(name = "district_name")
    @Schema(description = "District name")
    private String districtName;

    @Column(name = "country_sub_division")
    @Schema(description = "Country sub division")
    private String countrySubDivision;

    @Schema(description = "Country")
    private String country;
}
