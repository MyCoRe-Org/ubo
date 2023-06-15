FROM tomcat:jdk17-temurin-focal
RUN groupadd -r mcr -g 501 && \
    useradd -d /home/mcr -u 501 -m -s /bin/bash -g mcr mcr
WORKDIR /usr/local/tomcat/
ARG PACKET_SIZE="65536"
ENV APP_CONTEXT="ubo" \
 MCR_CONFIG_DIR="/mcr/home/" \
 MCR_DATA_DIR="/mcr/data/" \
 MCR_LOG_DIR="/mcr/logs/" \
 SOLR_CORE="ubo" \
 SOLR_CLASSIFICATION_CORE="ubo-classifications" \
 XMX="1g" \
 XMS="1g"
COPY --from=regreb/bibutils --chown=mcr:mcr /usr/local/bin/* /usr/local/bin/
COPY --chown=root:root docker-entrypoint.sh /usr/local/bin/ubo.sh

RUN set -eux; \
    chmod 555 /usr/local/bin/ubo.sh; \
	apt-get update; \
	apt-get install -y gosu; \
	rm -rf /var/lib/apt/lists/*;
RUN rm -rf /usr/local/tomcat/webapps/* && \
    mkdir /opt/ubo/ && \
    chown mcr:mcr -R /opt/ubo/ && \
    sed -ri "s/<\/Service>/<Connector protocol=\"AJP\/1.3\" packetSize=\"$PACKET_SIZE\" tomcatAuthentication=\"false\" scheme=\"https\" secretRequired=\"false\" allowedRequestAttributesPattern=\".*\" encodedSolidusHandling=\"decode\" address=\"0.0.0.0\" port=\"8009\" redirectPort=\"8443\" \/>&/g" /usr/local/tomcat/conf/server.xml
COPY --chown=mcr:mcr ubo-webapp/target/ubo-*.war /opt/ubo/ubo.war
COPY --chown=mcr:mcr ubo-cli /opt/ubo/ubo-cli
COPY --chown=mcr:mcr docker-log4j2.xml /opt/ubo/log4j2.xml
RUN chown mcr:mcr -R /opt/ubo/ /usr/local/tomcat/webapps/
CMD ["bash", "/usr/local/bin/ubo.sh"]
