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

// The file contents for the current environment will overwrite these during build.
// The build system defaults to the dev environment which uses `environment.ts`, but if you do
// `ng build --env=prod` then `environment.prod.ts` will be used instead.
// The list of which env maps to which file can be found in `.angular-cli.json`.
import {KeycloakConfig} from 'keycloak-angular';


export function saveServerUrls(urls) {
  environment.mockServerUrl = urls.MOCK_CONSENT_CONFIRMATION_URI;
  environment.aspspXs2aServerUrl = urls.Xs2aServerUrl;
  environment.consentManagementServerUrl = urls.CM_CONSENT_URI;
  environment.profileServerUrl = urls.aspspProfileServerUrl;
  environment.keycloak = urls.keyloakConfig;
}

// Add here your keycloak setup infos
const keycloakConfig: KeycloakConfig = {
  url: 'keycloak-server/auth/',
  realm: 'xs2a',
  clientId: 'aspsp-mock'
};

export const environment = {
  production: false,
  consentManagementServerUrl: 'http://localhost:38080/api/v1',
  aspspXs2aServerUrl: 'http://localhost:8080',
  mockServerUrl: 'http://localhost:28080/consent/confirmation',
  profileServerUrl: 'http://localhost:48080/api/v1/aspsp-profile',
  onlineBankingUrl: 'http://localhost:28081',
  assets: { dotaImages: 'https://api.opendota.com/apps/dota2/images' },
  apis: { dota: 'https://api.opendota.com/api' },
  keycloak: keycloakConfig,
  xRequestId: '2f77a125-aa7a-45c0-b414-cea25a116035',
  tppQwacCertificate: 'qwac certificate'
};
