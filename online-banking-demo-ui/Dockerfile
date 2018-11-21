# Stage SERVE
FROM nginx:1.15.5
MAINTAINER https://github.com/adorsys/xs2a

COPY ./nginx.conf /etc/nginx/conf.d/default.conf
COPY entry.sh /opt/entry.sh
ADD dist/ui /usr/share/nginx/html

RUN chgrp -R root /var/cache/nginx && \
    find /var/cache/nginx -type d -exec chmod 775 {} \; && \
    find /var/cache/nginx -type f -exec chmod 664 {} \; && \
    chmod 775 /var/run && \
    chmod 775 /opt/entry.sh && \
    chmod -R 777 /etc/nginx/conf.d

EXPOSE 4200

ENTRYPOINT /opt/entry.sh
