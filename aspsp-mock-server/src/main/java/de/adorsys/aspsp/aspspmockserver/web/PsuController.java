package de.adorsys.aspsp.aspspmockserver.web;

import de.adorsys.aspsp.aspspmockserver.service.PsuService;
import de.adorsys.aspsp.xs2a.spi.domain.psu.Psu;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/psu")
public class PsuController {
    private final PsuService psuService;

    @ApiOperation(value = "", authorizations = {@Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "read", description = "Access read API")})})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = List.class),
        @ApiResponse(code = 204, message = "Not Content")})
    @GetMapping(path = "/")
    public ResponseEntity<List<Psu>> readAllPsuList() {
        return ResponseEntity.ok(psuService.getAllPsuList());
    }

    @ApiOperation(value = "", authorizations = {@Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "read", description = "Access read API")})})
    @GetMapping(path = "/{id}")
    public ResponseEntity<Psu> readPsuById(@PathVariable("id") String id) {
        return psuService.getPsuById(id)
                   .map(ResponseEntity::ok)
                   .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @ApiOperation(value = "", authorizations = {@Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "read", description = "Access read API")})})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = URI.class),
        @ApiResponse(code = 400, message = "Bad Request")})
    @PostMapping(path = "/")
    public ResponseEntity createPsu(HttpServletRequest request, @RequestBody Psu psu) throws Exception {
        String uriString = getUriString(request);
        String saved = psuService.createPsuAndReturnId(psu);
        return saved == null
                   ? ResponseEntity.badRequest().build()
                   : ResponseEntity.created(new URI(uriString + saved)).build();
    }

    @ApiOperation(value = "", authorizations = {@Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "read", description = "Access read API")})})
    @ApiResponses(value = {
        @ApiResponse(code = 204, message = "No Content"),
        @ApiResponse(code = 404, message = "Not Found")})
    @DeleteMapping(path = "/{id}")
    public ResponseEntity deletePsu(@PathVariable("id") String id) {
        if (psuService.deletePsuById(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    private String getUriString(HttpServletRequest request) {
        return UriComponentsBuilder.fromHttpRequest(new ServletServerHttpRequest(request)).build().toUriString();
    }

}
