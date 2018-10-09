# Stage BUILD
FROM alexsuch/angular-cli:6.0.1 as builder
MAINTAINER https://git.adorsys.de/fibi/fibi4

WORKDIR /opt/online-banking-demo-ui

COPY package*.json ./

RUN npm install

COPY . .

RUN npm run build

# Stage SERVE
FROM nginx:1.15.5
MAINTAINER https://git.adorsys.de/fibi/fibi4

COPY --from=builder /opt/online-banking-demo-ui/dist/ui /usr/share/nginx/html
COPY ./nginx.conf /etc/nginx/conf.d/default.conf
COPY entry.sh /opt/entry.sh

RUN chgrp -R root /var/cache/nginx && \
    find /var/cache/nginx -type d -exec chmod 775 {} \; && \
    find /var/cache/nginx -type f -exec chmod 664 {} \; && \
    chmod 775 /var/run && \
    chmod 775 /opt/entry.sh

EXPOSE 4200

ENTRYPOINT /opt/entry.sh


