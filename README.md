# University Bibliography Online: Installation

## Setup

### Checkout source code from GitHub
`git clone git@github.com:MyCoRe-Org/ubo.git`

### Build the web application
`mvn install`

### Create configuration directory
```
ubo-cli/target/bin/ubo.sh create configuration directory
```

- TODO: Create a database and edit mycore.properties
- For example, in MySQL, do
```
CREATE DATABASE ubo;
GRANT ALL PRIVELEGES ON ubo.* to ubo@localhost IDENTIFIED BY 'ubo';
```
- setup your database and JDBC configuration in `mycore.properties`
```
MCR.JPA.Driver  = org.h2.Driver
MCR.JPA.URL     = jdbc:h2:file:/path/to/configuration/.mycore/ubo/data/h2/mir;AUTO_SERVER=TRUE"
MCR.JPA.dialect = org.hibernate.dialect.H2Dialect
```

- copy jdbc driver to ~/.mycore/ubo/lib, eg. for h2
```
cd ~/.mycore/ubo/lib
wget https://repo1.maven.org/maven2/com/h2database/h2/2.2.224/h2-2.2.224.jar
cd -
```

## Solr 
### Setup SOLR 8 
- described here:
  - https://www.mycore.de/documentation/getting_started/gs_solr8/
  - https://www.mycore.de/documentation/search/search_solr_use/

### or use solr runner plugin for development
 - install solr with the command: `mvn solr-runner:copyHome -pl ubo-webapp`
 - run solr with the command: `mvn solr-runner:start -pl ubo-webapp`
 - stop solr with the command: `mvn solr-runner:stop -pl ubo-webapp`
 - update solr with the command: `mvn solr-runner:stop solr-runner:copyHome solr-runner:start -pl ubo-webapp`

### Configure SOLR URL in mycore.properties
vi ~/.mycore/ubo/mycore.properties
```
MCR.Solr.ServerURL=http://localhost:8983/
MCR.Solr.Core.main.Name=ubo
MCR.Solr.Core.classification.Name=ubo-classifications
```
## Setup Superuser

- Create the superuser "administrator"
```
ubo-cli/target/bin/ubo.sh init superuser
```
## Classifications
```
ubo-cli/target/bin/ubo.sh update all classifications from directory ubo-cli/src/main/setup/classifications
```

## ACL
```
ubo-cli/target/bin/ubo.sh update permission create-mods for id POOLPRIVILEGE with rulefile ubo-cli/src/main/setup/acl/acl-rule-always-allowed.xml described by always allowed
ubo-cli/target/bin/ubo.sh update permission create-users for id POOLPRIVILEGE with rulefile ubo-cli/src/main/setup/acl/acl-rule-administrators-only.xml described by administrators only
ubo-cli/target/bin/ubo.sh update permission administrate-users for id POOLPRIVILEGE with rulefile ubo-cli/src/main/setup/acl/acl-rule-administrators-only.xml described by administrators only
ubo-cli/target/bin/ubo.sh update permission read for id default with rulefile ubo-cli/src/main/setup/acl/acl-rule-always-allowed.xml described by always allowed
ubo-cli/target/bin/ubo.sh update permission writedb for id default with rulefile ubo-cli/src/main/setup/acl/acl-rule-administrators-only.xml described by administrators only
ubo-cli/target/bin/ubo.sh update permission deletedb for id default with rulefile ubo-cli/src/main/setup/acl/acl-rule-administrators-only.xml described by administrators only
ubo-cli/target/bin/ubo.sh update permission read for id restapi:/ with rulefile ubo-cli/src/main/setup/acl/acl-rule-always-allowed.xml described by always allowed
ubo-cli/target/bin/ubo.sh update permission read for id restapi:/classifications with rulefile ubo-cli/src/main/setup/acl/acl-rule-always-allowed.xml described by always allowed
```

## MyCoRe-Solr-Configuration
```
ubo-cli/target/bin/ubo.sh reload solr configuration main in core main
```

## Run 
- local web application on port 8080 with tomcat 10:
```
mvn cargo:run -Dtomcat=9 -pl ubo-webapp
```
- or jetty: (does not work currently)
```
mvn cargo:run -Djetty -pl ubo-webapp
```
## Rebuild & Run (root directory)
```
mvn clean && mvn install -am -pl ubo-webapp && mvn -Dtomcat=9 org.codehaus.cargo:cargo-maven2-plugin:run -pl ubo-webapp -DskipTests
```

