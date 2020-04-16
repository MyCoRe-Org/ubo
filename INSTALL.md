# University Bibliography Online: Installation

```
# Checkout source code from GitHub
git clone https://github.com/MyCoRe-Org/ubo.git

# Build the web application
mvn install

# Create configuration directory
target/bin/ubo.sh create configuration directory

# TODO: Build a database and edit persistence.xml
# For example, in MySQL, do
create database ubo;
grant all privileges on ubo.* to ubo@localhost identified by 'ubo';

# setup your database and JDBC configuration in persistence.xml and
# remove viewer mapping entry <mapping-file>META-INF/mycore-viewer-mappings.xml</mapping-file>
vi ~/.mycore/ubo/resources/META-INF/persistence.xml

# copy jdbc driver to ~/.mycore/ubo/lib, eg. for h2
cd ~/.mycore/ubo/lib
wget https://repo1.maven.org/maven2/com/h2database/h2/1.4.200/h2-1.4.200.jar
cd -

# Setup SOLR 7.7 as described here:
https://www.mycore.de/documentation/getting_started/gs_solr7/
https://www.mycore.de/documentation/search/search_solr_use/

# or use solr runner plugin for development
 - install solr with the command: `mvn solr-runner:copyHome`
 - run solr with the command: `mvn solr-runner:start`
 - stop solr with the command: `mvn solr-runner:stop`
 - update solr with the command: `mvn solr-runner:stop solr-runner:copyHome solr-runner:start`

# Configure SOLR URL in mycore.properties
vi ~/.mycore/ubo/mycore.properties

  MCR.Solr.ServerURL=http://localhost:8983/
  MCR.Solr.Core.main.Name=ubo
  MCR.Solr.Core.classification.Name=ubo_classifications


# FIXME: as current workaround we need to copy jdbc driver also to target dir
cp ~/.mycore/ubo/lib/h2-1.4.196.jar target/ubo-1.0-SNAPSHOT/WEB-INF/lib/

# Create the superuser "administrator" 
target/bin/ubo.sh init superuser

# Load classifications
target/bin/ubo.sh update all classifications from directory src/main/setup/classifications

# Configure ACLs
target/bin/ubo.sh update permission create-mods for id POOLPRIVILEGE with rulefile src/main/resources/acl-rule-always-allowed.xml described by always allowed
target/bin/ubo.sh update permission read for id default with rulefile src/main/resources/acl-rule-always-allowed.xml described by always allowed
target/bin/ubo.sh update permission read for id restapi:/ with rulefile src/main/resources/acl-rule-always-allowed.xml described by always allowed

# Load solr configuration
target/bin/ubo.sh reload solr configuration main in core main

# Run local web application on port 8080:
mvn cargo:run -Dtomcat=9
```
