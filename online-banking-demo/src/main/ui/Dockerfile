FROM centos/nginx-112-centos7

CMD ["/usr/libexec/s2i/run"]

WORKDIR /opt/ui-app

COPY ./dist/prototype-banking/ /opt/app-root/src
COPY ./devops/nginx.conf /opt/app-root/etc/nginx.default.d/default.conf

USER root
RUN chgrp -R 0 /opt/app-root/src && \
    chmod -R g=u /opt/app-root/src
USER 1001
