# PSD2 Chart

## Chart Parameter

### Generic

| Parameter | Description | Default |
|-----------|-------------|---------|
| `timezone` | Define timezone for all containers | `Europe/Berlin` |

### xs2a

### cms

### aspsp-profile

### mock

### Keycloak

| Parameter | Description | Default |
|-----------|-------------|---------|
| `keycloak.enabled` | Enable Keycloak | `true` |
| `keycloak.externalDomain` | Custom Homename for keycloak | `~` |
| `keycloak.replicaCount` | Set number of replicas | `1` |
| `keycloak.image.name` | Name of the Image | `docker.io/jboss/keycloak-openshift` |
| `keycloak.image.tag` | Tag of the Image | `4.2.1.Final` |
| `keycloak.image.steam` | Use an existing image stream | `~` |
| `keycloak.resources` | Pod resource requests and limits | `{"limits": {"memory": "500Mi"}}` |
| `keycloak.livenessProbe` | Pod liveness probe | `{"httpGet": {"path": "/auth/","port": "http"}}` |
| `keycloak.readinessProbe` | Pod readiness probe | `{"httpGet": {"path": "/auth/","port": "http"}}` |
| `keycloak.deploymentAnnotations` | Annotations for the Deployment | `{}` |
| `keycloak.podAnnotations` | Annotations for the Pods | `{}` |
| `keycloak.serviceAnnotations` | Annotations for the Service | `{}` |
| `keycloak.extraEnv` | Allows the specification of additional environment variables for Keycloak. Passed through the tpl funtion and thus to be configured a string. | `WILDFLY_LOGLEVEL and JAVA_OPTS` |
| `keycloak.admin.username` | Initial admin login name | `admin` |
| `keycloak.admin.password` | Initial admin login password | `admin` |
| `keycloak.realms` | Configuration of realms. See below | `{}` |
| `keycloak.dbVendor` | DB Vendor of Keycloak (`h2` or `postgres` supported). | `postgres` |

## Keycloak DB

| Parameter | Description | Default |
|-----------|-------------|---------|
| `keycloak.db.image.name` | Name of the Image | `docker.io/centos/postgresql-96-centos7` |
| `keycloak.db.image.tag` | Tag of the Image | `latest` |
| `keycloak.db.image.steam` | Use an existing image stream | `~` |
| `keycloak.db.resources` | Pod resource requests and limits | `{"limits": {"memory": "200Mi"}}` |
| `keycloak.db.persistence.enabled` | Enable persistence | `true` |
| `keycloak.db.persistence.size` | Persistence requested size | `1Gi` |
| `keycloak.db.persistence.storageClass` | Persistence requested storageClass | `~` |
| `keycloak.db.deploymentAnnotations` | Annotations for the Deployment | `{}` |
| `keycloak.db.podAnnotations` | Annotations for the Pods | `{}` |
| `keycloak.db.serviceAnnotations` | Annotations for the Service | `{}` |
| `keycloak.db.livenessProbe` | Pod liveness probe | `{"exec": {"command": [/usr/libexec/check-container, --live]}}` |
| `keycloak.db.readinessProbe` | Pod readiness probe | `{"exec": {"command": [/usr/libexec/check-container]}}` |
| `keycloak.db.admin.password` | Password for the admin user | `root` |
| `keycloak.db.user.username` | Username for the application user | `keycloak` |
| `keycloak.db.user.password` | Password for the application user | `keycloak` |
| `keycloak.db.database.name` | Name of the database | `keycloak` |
| `keycloak.db.metrics.enabled` | Enable metric exporter for postgres | `false` |
| `keycloak.db.metrics.image.name` | Name of the Image | `docker.io/wrouesnel/postgres_exporter` |
| `keycloak.db.metrics.image.tag` | Tag of the Image | `v0.4.6` |
| `keycloak.db.metrics.image.steam` | Use an existing image stream | `~` |
| `keycloak.db.metrics.resources` | Pod resource requests and limits | `{"limits": {"memory": "250Mi", "cpu": "100m"}}` |
| `keycloak.db.backup.enabled` | Enable backup of postgresql | `false` |
| `keycloak.db.backup.schedule` | Times for the backup in crontab format | `0 1 * * *` |
| `keycloak.db.backup.retention` | Rotation for backup in days | `7` |
| `keycloak.db.backup.resources` | Pod resource requests and limits |  `{"limits": {"memory": "100Mi", "cpu": "100m"}}` |
| `keycloak.db.backup.persistence.size` | Backup persistence size | `10Gi` |
| `keycloak.db.backup.persistence.storageClass` | Backup persistence storageClass | `~` |


## Realm Configuration

Example of a Realm:
```yaml
keycloak:
  realms:
    multibanking:
      displayName: PSD2
      smtpServer:
        from: realm@example.com
        host: mailout
      clients:
        application:
          secret: 9e61fd36-ee68-4573-a317-daf4d6b9bebd
      users:
        test:
          firstName: S 
          lastName: schäfer
          email: s.schäfer@psd2test.de
          credentials: [{ type: password, value: "TEST" }]
```
