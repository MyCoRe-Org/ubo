# University Bibliography Online: Installation

```
# Checkout source code from GitHub
git clone https://github.com/MyCoRe-Org/ubo.git

# Build the web application
mvn install

# Create configuration directory
target/bin/ubo.sh create configuration directory

# TODO: Edit mycore.properties

# TODO: Build a database and edit persistence.xml
# For example, in MySQL, do
create database ubo;
grant all privileges on ubo.* to ubo@localhost identified by 'ubo';

vi ~/.mycore/ubo/resources/META-INF/persistence.xml
# fix mapping-file entries there:
<mapping-file>META-INF/mycore-base-mappings.xml</mapping-file>
<mapping-file>META-INF/mycore-ifs-mappings.xml</mapping-file>
<mapping-file>META-INF/mycore-pi-mappings.xml</mapping-file>
<mapping-file>META-INF/mycore-user2-mappings.xml</mapping-file>
<mapping-file>META-INF/mycore-iview2-mappings.xml</mapping-file>

# Setup SOLR 4.10 as described here:
http://www.mycore.de/documentation/getting_started/solr_4.html

# Rename directory "collection1" to "ubo"
cd ~/.mycore/ubo/data/solr
mv collection1 ubo
vi ~/.mycore/ubo/data/solr/ubo/core.properties
name=ubo

# Copy SOLR configuration of core "ubo"
cp src/main/setup/solr/* ~/.mycore/ubo/data/solr/ubo/conf/

# Configure SOLR URL in mycore.properties
vi ~/.mycore/ubo/mycore.properties
MCR.Solr.ServerURL=http://127.0.0.1
MCR.Solr.Core.main.Name=ubo

# Create the superuser "administrator" 
target/bin/ubo.sh init superuser

# Load classifications
target/bin/ubo.sh update all classifications from directory src/main/setup/classifications

# Configure ACLs
target/bin/ubo.sh update permission create-mods for id POOLPRIVILEGE with rulefile src/main/resources/acl-rule-always-allowed.xml described by always allowed
target/bin/ubo.sh update permission read for id default with rulefile src/main/resources/acl-rule-always-allowed.xml described by always allowed
target/bin/ubo.sh update permission read for id restapi:/ with rulefile src/main/resources/acl-rule-always-allowed.xml described by always allowed

# Run local web application on port 8080:
mvn cargo:run
```
