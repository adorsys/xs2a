import { KeycloakService } from 'keycloak-angular';
import { ConfigService } from '../service/config.service';
import { Config } from '../model/Config';

export function initializer(keycloakService: KeycloakService, configService: ConfigService): () => Promise<any> {
  return (): Promise<any> => configService.loadConfig().then((config: Config) => {
    configService.setConfig(config);
    keycloakInit(keycloakService, configService).then();
  });
}

export function keycloakInit(keycloak: KeycloakService, configService: ConfigService): Promise<any> {
    return new Promise(async (resolve, reject) => {
      try {
        await keycloak.init({
          config: configService.getConfig().keycloakConfig,
          initOptions: {
            onLoad: 'login-required',
            checkLoginIframe: false,
            flow: 'implicit',
          },
          enableBearerInterceptor: true,
          bearerExcludedUrls: []
        });
        resolve();
      } catch (error) {
        reject(error);
      }
    });
}
