package de.adorsys.keycloak.extension.clientregistration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.keycloak.models.ClientModel;
import org.keycloak.models.ClientTemplateModel;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;

public class ClientModelImpl implements ClientModel{

	@Override
	public RoleModel getRole(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RoleModel addRole(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RoleModel addRole(String id, String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean removeRole(RoleModel role) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Set<RoleModel> getRoles() {
		
		return new HashSet<>();
	}

	@Override
	public List<String> getDefaultRoles() {
		// TODO Auto-generated method stub
		return new ArrayList();
	}

	@Override
	public void addDefaultRole(String name) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateDefaultRoles(String... defaultRoles) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeDefaultRoles(String... defaultRoles) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Set<ProtocolMapperModel> getProtocolMappers() {
		// TODO Auto-generated method stub
		return new HashSet<>();
	}

	@Override
	public ProtocolMapperModel addProtocolMapper(ProtocolMapperModel model) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void removeProtocolMapper(ProtocolMapperModel mapping) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateProtocolMapper(ProtocolMapperModel mapping) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ProtocolMapperModel getProtocolMapperById(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ProtocolMapperModel getProtocolMapperByName(String protocol, String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isFullScopeAllowed() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setFullScopeAllowed(boolean value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Set<RoleModel> getScopeMappings() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addScopeMapping(RoleModel role) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteScopeMapping(RoleModel role) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Set<RoleModel> getRealmScopeMappings() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasScope(RoleModel role) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void updateClient() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getClientId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setClientId(String clientId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setName(String name) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setDescription(String description) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isEnabled() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setEnabled(boolean enabled) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isSurrogateAuthRequired() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setSurrogateAuthRequired(boolean surrogateAuthRequired) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Set<String> getWebOrigins() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setWebOrigins(Set<String> webOrigins) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addWebOrigin(String webOrigin) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeWebOrigin(String webOrigin) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Set<String> getRedirectUris() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setRedirectUris(Set<String> redirectUris) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addRedirectUri(String redirectUri) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeRedirectUri(String redirectUri) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getManagementUrl() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setManagementUrl(String url) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getRootUrl() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setRootUrl(String url) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getBaseUrl() {
		// TODO Auto-generated method stub
		return new String();
	}

	@Override
	public void setBaseUrl(String url) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isBearerOnly() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setBearerOnly(boolean only) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getNodeReRegistrationTimeout() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setNodeReRegistrationTimeout(int timeout) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getClientAuthenticatorType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setClientAuthenticatorType(String clientAuthenticatorType) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean validateSecret(String secret) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getSecret() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setSecret(String secret) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getRegistrationToken() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setRegistrationToken(String registrationToken) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getProtocol() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setProtocol(String protocol) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setAttribute(String name, String value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeAttribute(String name) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getAttribute(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, String> getAttributes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isFrontchannelLogout() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setFrontchannelLogout(boolean flag) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isPublicClient() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setPublicClient(boolean flag) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isConsentRequired() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setConsentRequired(boolean consentRequired) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isStandardFlowEnabled() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setStandardFlowEnabled(boolean standardFlowEnabled) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isImplicitFlowEnabled() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setImplicitFlowEnabled(boolean implicitFlowEnabled) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isDirectAccessGrantsEnabled() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setDirectAccessGrantsEnabled(boolean directAccessGrantsEnabled) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isServiceAccountsEnabled() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setServiceAccountsEnabled(boolean serviceAccountsEnabled) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public RealmModel getRealm() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ClientTemplateModel getClientTemplate() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setClientTemplate(ClientTemplateModel template) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean useTemplateScope() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setUseTemplateScope(boolean flag) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean useTemplateMappers() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setUseTemplateMappers(boolean flag) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean useTemplateConfig() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setUseTemplateConfig(boolean flag) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getNotBefore() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setNotBefore(int notBefore) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Map<String, Integer> getRegisteredNodes() {
		// TODO Auto-generated method stub
		return new HashMap<>();
	}

	@Override
	public void registerNode(String nodeHost, int registrationTime) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void unregisterNode(String nodeHost) {
		// TODO Auto-generated method stub
		
	}

}
