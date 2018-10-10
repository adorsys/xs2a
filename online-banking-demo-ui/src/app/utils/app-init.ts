import { KeycloakService } from 'keycloak-angular';
import { ConfigService } from '../service/config.service';
import { Config } from '../model/Config';
import { environment } from '../../environments/environment';

// export function initializer(keycloakService: KeycloakService, configService: ConfigService): () => Promise<any> {
//   return (): Promise<any> => configService.loadConfig().then((config: Config) => {
//     configService.setConfig(config);
//     keycloakInit(keycloakService, configService).then();
//   });
// }

export function initializer(keycloak: KeycloakService): () => Promise<any> {
  return (): Promise<any> => {
    return new Promise(async (resolve, reject) => {
      try {
        await keycloak.init({
          config: environment.keycloak,
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
  };
}
