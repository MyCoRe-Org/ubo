# University Bibliography Online: Installation

## Setup

### Checkout source code from GitHub
git clone https://github.com/MyCoRe-Org/ubo.git

### Build the web application
mvn install

### Create configuration directory
```
cd ubo-cli
target/bin/ubo.sh create configuration directory
```

- TODO: Build a database and edit persistence.xml
- For example, in MySQL, do
```
create database ubo;
grant all privileges on ubo.* to ubo@localhost identified by 'ubo';
```
- setup your database and JDBC configuration in persistence.xml
- remove viewer mapping entry <mapping-file>META-INF/mycore-viewer-mappings.xml</mapping-file>
```
vi ~/.mycore/ubo/resources/META-INF/persistence.xml
```

- copy jdbc driver to ~/.mycore/ubo/lib, eg. for h2
cd ~/.mycore/ubo/lib
wget https://repo1.maven.org/maven2/com/h2database/h2/1.4.200/h2-1.4.200.jar
cd -

## Solr 
### Setup SOLR 8 
- described here:
https://www.mycore.de/documentation/getting_started/gs_solr8/
https://www.mycore.de/documentation/search/search_solr_use/

### or use solr runner plugin for development (in directory ubo-webapp)
 - install solr with the command: `mvn solr-runner:copyHome`
 - run solr with the command: `mvn solr-runner:start`
 - stop solr with the command: `mvn solr-runner:stop`
 - update solr with the command: `mvn solr-runner:stop solr-runner:copyHome solr-runner:start`

### Configure SOLR URL in mycore.properties
vi ~/.mycore/ubo/mycore.properties

  MCR.Solr.ServerURL=http://localhost:8983/
  MCR.Solr.Core.main.Name=ubo
  MCR.Solr.Core.classification.Name=ubo_classifications

## Setup Superuser

- Create the superuser "administrator"
```
ubo-cli/target/bin/ubo.sh init superuser
```
## Classifications
```
ubo-cli/target/bin/ubo.sh update all classifications from directory src/main/setup/classifications
```

## ACL
```
ubo-cli/target/bin/ubo.sh update permission create-mods for id POOLPRIVILEGE with rulefile src/main/resources/acl-rule-always-allowed.xml described by always allowed
ubo-cli/target/bin/ubo.sh update permission read for id default with rulefile src/main/resources/acl-rule-always-allowed.xml described by always allowed
ubo-cli/target/bin/ubo.sh update permission read for id restapi:/ with rulefile src/main/resources/acl-rule-always-allowed.xml described by always allowed
```

## MyCoRe-Solr-Configuration
```
ubo-cli/target/bin/ubo.sh reload solr configuration main in core main
```

## Run 
- local web application on port 8080 with tomcat 9 (in ubo-webapp directory):
```
mvn cargo:run -Dtomcat=9
```
- or jetty: (does not work currently)
```
mvn cargo:run -Djetty
```
## Rebuild & Run (root directory)
```
mvn clean && mvn install -am -pl ubo-webapp && mvn -Pdev -Dtomcat=9 org.codehaus.cargo:cargo-maven2-plugin:run -pl ubo-webapp -DskipTests
```

