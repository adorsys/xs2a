package de.adorsys.keycloak.extension.clientregistration;

import java.util.HashSet;
import java.util.Set;

import org.keycloak.models.KeycloakSession;
import org.keycloak.protocol.oidc.utils.SubjectType;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.oidc.OIDCClientRepresentation;
import org.keycloak.services.clientregistration.AbstractClientRegistrationContext;
import org.keycloak.services.clientregistration.ClientRegistrationProvider;
import org.keycloak.services.validation.PairwiseClientValidator;
import org.keycloak.services.validation.ValidationMessages;

public class OIDCClientRegistrationContext extends AbstractClientRegistrationContext {

    private final OIDCClientRepresentation oidcRep;

    public OIDCClientRegistrationContext(KeycloakSession session, ClientRepresentation client, ClientRegistrationProvider provider, OIDCClientRepresentation oidcRep) {
        super(session, client, provider);
        this.oidcRep = oidcRep;
    }

    @Override
    public boolean validateClient(ValidationMessages validationMessages) {
        boolean valid = super.validateClient(validationMessages);
      
        String rootUrl = client.getRootUrl();
        Set<String> redirectUris = new HashSet<>();
        if (client.getRedirectUris() != null) redirectUris.addAll(client.getRedirectUris());

        SubjectType subjectType = SubjectType.parse(oidcRep.getSubjectType());
        String sectorIdentifierUri = oidcRep.getSectorIdentifierUri();

        // If sector_identifier_uri is in oidc config, then always validate it
        if (SubjectType.PAIRWISE == subjectType || (sectorIdentifierUri != null && !sectorIdentifierUri.isEmpty())) {
            valid = valid && PairwiseClientValidator.validate(session, rootUrl, redirectUris, oidcRep.getSectorIdentifierUri(), validationMessages);
        }
        return valid;
    }
}
