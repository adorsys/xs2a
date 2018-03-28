package de.adorsys.keycloak.extension.clientregistration;

import java.util.HashMap;

import org.junit.Test;

import com.auth0.jwt.algorithms.Algorithm;

public class SSAServiceTest {
	
	@Test
	public void ssaGenerate(){
		
		/*String[] software_roles = {"PISP","AISP"};
		
		try {
		    Algorithm algorithm = Algorithm.HMAC256("secret123");
		    String ssa = JWT.create()  	
		        .withIssuer("TSP")
		        .withClaim("software_id", "65d1f27c-4aea-4549-9c21-60e495a7a86f")
		        .withArrayClaim("software_roles", software_roles)
		        .sign(algorithm);
		    
		    System.out.println(ssa);
		} catch (UnsupportedEncodingException exception){
		    //UTF-8 encoding not supported
		} catch (JWTCreationException exception){
		    //Invalid Signing configuration / Couldn't convert Claims.
		}
		*/
		
		JWTSigner signer = new JWTSigner(getPrivateKey());
        HashMap<String, Object> claims = new HashMap<String, Object>();
        claims.put("sub", "1234567890");
        claims.put("name", "John Doe");
        claims.put("admin", "true");
        JWTSigner.Options options = new JWTSigner.Options();
        options.setAlgorithm(Algorithm.RS256);
        String jwtSigned = signer.sign(claims, options);

        System.out.println("Signed: " + jwtSigned);

		
	}
}
