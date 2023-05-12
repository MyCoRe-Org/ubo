FROM solr:8.11
USER root
COPY --chown=solr:solr configsets/ubo_main /opt/solr/server/solr/configsets/ubo_main
COPY --chown=solr:solr configsets/ubo_classification /opt/solr/server/solr/configsets/ubo_classification
COPY --chown=root:root docker-entrypoint.sh ./

RUN apt-get update && \
    apt-get -y install sudo && \
    sed -ri 's/ class="solr.[Fast]*LRUCache"//' /opt/solr/server/solr/configsets/ubo_main/conf/solrconfig.xml && \
    sed -ri 's/ class="solr.[Fast]*LRUCache"//' /opt/solr/server/solr/configsets/ubo_classification/conf/solrconfig.xml && \
    chmod 555 docker-entrypoint.sh
CMD ["bash", "docker-entrypoint.sh"]