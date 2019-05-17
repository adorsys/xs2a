package de.adorsys.psd2.consent.web.aspsp.controller;

import de.adorsys.psd2.consent.aspsp.api.tpp.CmsAspspTppService;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "aspsp-api/v1/tpp")
@Api(value = "aspsp-api/v1/tpp", tags = "ASPSP TPP Info", description = "Provides access to the consent management system TPP Info")
public class CmsAspspTppInfoController {
    private final CmsAspspTppService cmsAspspTppService;

    @GetMapping(path = "/{tpp-id}")
    @ApiOperation(value = "Returns TPP info by TPP ID")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not Found")})
    public ResponseEntity<TppInfo> getTppInfo(
        @ApiParam(value = "TPP ID", example = "12345987")
        @PathVariable("tpp-id") String tppId,
        @ApiParam(value = "ID of the particular service instance")
        @RequestHeader(value = "instance-id", required = false, defaultValue = "UNDEFINED") String instanceId){
        return cmsAspspTppService.getTppInfo(tppId, instanceId)
            .map(record -> new ResponseEntity<>(record, HttpStatus.OK))
            .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}
