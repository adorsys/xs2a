/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.adorsys.keycloak.extension.clientregistration;

import org.keycloak.common.util.Time;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.utils.ModelToRepresentation;
import org.keycloak.models.utils.RepresentationToModel;
import org.keycloak.protocol.oidc.mappers.AbstractPairwiseSubMapper;
import org.keycloak.protocol.oidc.mappers.PairwiseSubMapperHelper;
import org.keycloak.protocol.oidc.mappers.SHA256PairwiseSubMapper;
import org.keycloak.protocol.oidc.utils.SubjectType;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.ProtocolMapperRepresentation;
import org.keycloak.representations.oidc.OIDCClientRepresentation;
import org.keycloak.services.ErrorResponseException;
import org.keycloak.services.ServicesLogger;
import org.keycloak.services.clientregistration.AbstractClientRegistrationProvider;
import org.keycloak.services.clientregistration.ClientRegistrationException;
import org.keycloak.services.clientregistration.ErrorCodes;
import org.keycloak.services.clientregistration.oidc.OIDCClientRegistrationContext;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class OIDCClientRegistrationExtendedProvider extends AbstractClientRegistrationProvider {

	OIDCClientRegistrationExtendedProvider(KeycloakSession session) {
		super(session);
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response createOIDC(OIDCClientRepresentationExtended clientOIDC) {
		if (clientOIDC.getClientId() != null) {
			throw new ErrorResponseException(ErrorCodes.INVALID_CLIENT_METADATA, "Client Identifier included",
					Response.Status.BAD_REQUEST);
		}

		try {

			//this could throw invalid_software_statement or unapproved_software_statement exception
			//https://tools.ietf.org/html/rfc7591#section-3.2.1
			SSAService.validate(clientOIDC);

			//set some attribute of the client(... like role) with ssa attributes

			ClientRepresentation client = DescriptionConverterExt.toInternal(session, clientOIDC);
			OIDCClientRegistrationContext oidcContext = new OIDCClientRegistrationContext(session, client, this,
					clientOIDC);
			client = create(oidcContext);

			ClientModel clientModel = session.getContext().getRealm().getClientByClientId(client.getClientId());
			updatePairwiseSubMappers(clientModel, SubjectType.parse(clientOIDC.getSubjectType()),
					clientOIDC.getSectorIdentifierUri());
			updateClientRepWithProtocolMappers(clientModel, client);

			URI uri = session.getContext().getUri().getAbsolutePathBuilder().path(client.getClientId()).build();
            OIDCClientRepresentationExtended clientOIDCResponse = DescriptionConverterExt.toExternalResponse(session, client, uri);
            clientOIDCResponse.setClientIdIssuedAt(Time.currentTime());
			return Response.created(uri).entity(clientOIDCResponse).build();
		} catch (ClientRegistrationException cre) {
			ServicesLogger.LOGGER.clientRegistrationException(cre.getMessage());
			throw new ErrorResponseException(ErrorCodes.INVALID_CLIENT_METADATA, "Client metadata invalid",
					Response.Status.BAD_REQUEST);
		}
	}

	@GET
    @Path("{clientId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getOIDC(@PathParam("clientId") String clientId) {
        ClientRepresentation client = get(clientId);
        OIDCClientRepresentation clientOIDC = DescriptionConverterExt.toExternalResponse(session, client, session.getContext().getUri().getRequestUri());
        return Response.ok(clientOIDC).build();
    }

    @PUT
    @Path("{clientId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateOIDC(@PathParam("clientId") String clientId, OIDCClientRepresentationExtended clientOIDC) {
        try {
            ClientRepresentation client = DescriptionConverterExt.toInternal(session, clientOIDC);
            OIDCClientRegistrationContext oidcContext = new OIDCClientRegistrationContext(session, client, this, clientOIDC);
            client = update(clientId, oidcContext);

            ClientModel clientModel = session.getContext().getRealm().getClientByClientId(client.getClientId());
            updatePairwiseSubMappers(clientModel, SubjectType.parse(clientOIDC.getSubjectType()), clientOIDC.getSectorIdentifierUri());
            updateClientRepWithProtocolMappers(clientModel, client);

            URI uri = session.getContext().getUri().getAbsolutePathBuilder().path(client.getClientId()).build();
            OIDCClientRepresentationExtended clientOIDCResponse = DescriptionConverterExt.toExternalResponse(session, client, uri);
            return Response.ok(clientOIDCResponse).build();
        } catch (ClientRegistrationException cre) {
            ServicesLogger.LOGGER.clientRegistrationException(cre.getMessage());
            throw new ErrorResponseException(ErrorCodes.INVALID_CLIENT_METADATA, "Client metadata invalid", Response.Status.BAD_REQUEST);
        }
    }

    @DELETE
    @Path("{clientId}")
    public void deleteOIDC(@PathParam("clientId") String clientId) {
        delete(clientId);
    }

	private void updatePairwiseSubMappers(ClientModel clientModel, SubjectType subjectType,
			String sectorIdentifierUri) {
        Set<ProtocolMapperModel> protocolMappers = clientModel.getProtocolMappers();
        if (subjectType == SubjectType.PAIRWISE) {

			// See if we have existing pairwise mapper and update it. Otherwise
			// create new
			AtomicBoolean foundPairwise = new AtomicBoolean(false);

            for (ProtocolMapperModel mapping : protocolMappers) {
                if (mapping.getProtocolMapper().endsWith(AbstractPairwiseSubMapper.PROVIDER_ID_SUFFIX)) {
                    foundPairwise.set(true);
                    PairwiseSubMapperHelper.setSectorIdentifierUri(mapping, sectorIdentifierUri);
                    clientModel.updateProtocolMapper(mapping);
                }
            }

            // We don't have existing pairwise mapper. So create new
			if (!foundPairwise.get()) {
				ProtocolMapperRepresentation newPairwise = SHA256PairwiseSubMapper
						.createPairwiseMapper(sectorIdentifierUri, null);
				clientModel.addProtocolMapper(RepresentationToModel.toModel(newPairwise));
			}

		} else {
			// Rather find and remove all pairwise mappers
            for (ProtocolMapperModel mapping : protocolMappers) {
                if (mapping.getProtocolMapper().endsWith(AbstractPairwiseSubMapper.PROVIDER_ID_SUFFIX)) {
                    protocolMappers.remove(mapping);
                }
            }
		}
	}

	private void updateClientRepWithProtocolMappers(ClientModel clientModel, ClientRepresentation rep) {
		List<ProtocolMapperRepresentation> mappings = new LinkedList<>();
		for (ProtocolMapperModel model : clientModel.getProtocolMappers()) {
			mappings.add(ModelToRepresentation.toRepresentation(model));
		}
		rep.setProtocolMappers(mappings);
	}

}
