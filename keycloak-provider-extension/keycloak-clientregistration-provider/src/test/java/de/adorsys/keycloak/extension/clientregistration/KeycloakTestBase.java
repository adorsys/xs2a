package de.adorsys.keycloak.extension.clientregistration;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.apache.http.HttpRequest;
import org.keycloak.authentication.ClientAuthenticator;
import org.keycloak.authentication.ClientAuthenticatorFactory;
import org.keycloak.events.EventBuilder;
import org.keycloak.models.KeycloakContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserCredentialManager;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserProvider;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.services.clientregistration.ClientRegistrationAuth;
import org.keycloak.services.clientregistration.ClientRegistrationContext;
import org.keycloak.services.clientregistration.policy.RegistrationAuth;
import org.keycloak.storage.adapter.InMemoryUserAdapter;
import org.keycloak.theme.Theme;
import org.keycloak.theme.ThemeProvider;

public abstract class KeycloakTestBase {
    protected final KeycloakSession session = mock(KeycloakSession.class);
    protected final HttpRequest request = mock(HttpRequest.class);
    protected final RealmModel realm = mock(RealmModel.class);
    protected final UserProvider userProvider = mock(UserProvider.class);
    protected final UserCredentialManager userCredentialManager = mock(UserCredentialManager.class);
    protected final EventBuilder event = mock(EventBuilder.class);
    protected final ThemeProvider themeProvider = mock(ThemeProvider.class);
    protected final Theme theme = mock(Theme.class);
    protected final KeycloakContext context = mock(KeycloakContext.class);
    protected final ClientRegistrationAuth auth = mock(ClientRegistrationAuth.class);
    protected final KeycloakSessionFactory keycloakSessionFactory = mock(KeycloakSessionFactory.class);
    protected final ClientAuthenticatorFactory clientAuthenticatorFactory = mock(ClientAuthenticatorFactory.class);

    protected void init() {
        when(session.userLocalStorage()).thenReturn(userProvider);
        when(session.userCredentialManager()).thenReturn(userCredentialManager);
        when(session.getContext()).thenReturn(context);
        when(session.getKeycloakSessionFactory()).thenReturn(keycloakSessionFactory);
        

        when(keycloakSessionFactory.getProviderFactory(
				ClientAuthenticator.class, KeycloakModelUtils.getDefaultClientAuthenticatorType())).thenReturn(clientAuthenticatorFactory);
        
        when(context.getRealm()).thenReturn(realm);
        //when(context.getUri()).thenCallRealMethod();

        when(realm.addClient(anyString())).thenReturn(new ClientModelImpl());
        when(realm.addClient(anyString(),anyString())).thenReturn(new ClientModelImpl());
        
        when(auth.requireCreate(any(ClientRegistrationContext.class))).thenReturn(RegistrationAuth.ANONYMOUS);

    }

    protected UserModel getRandomUser() {
        return spy(new InMemoryUserAdapter(session, realm, UUID.randomUUID().toString()));
    }
}
