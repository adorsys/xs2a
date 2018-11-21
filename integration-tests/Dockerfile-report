FROM nginx:1.15.5-alpine

ENV ENV=$env

# support running as arbitrary user which belogs to the root group
RUN chmod 777 -R /var/cache/nginx /var/run /var/log/nginx
# users are not allowed to listen on priviliged ports
RUN sed -i.bak 's/listen\(.*\)80;/listen 8080;/' /etc/nginx/conf.d/default.conf
EXPOSE 8080
# comment user directive as master process is run as user in OpenShift anyhow
RUN sed -i.bak 's/^user/#user/' /etc/nginx/nginx.conf
WORKDIR /www/app
ADD ./target/generated-report /www/app
RUN ls -la
RUN cp -r ./* /usr/share/nginx/html
