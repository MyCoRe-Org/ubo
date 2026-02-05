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
MCR.JPA.URL     = jdbc:h2:file:/path/to/configuration/.mycore/ubo/data/h2/mir;AUTO_SERVER=TRUE
MCR.JPA.dialect = org.hibernate.dialect.H2Dialect
```

- copy jdbc driver to ~/.mycore/ubo/lib, eg. for h2
```
cd ~/.mycore/ubo/lib
wget https://repo1.maven.org/maven2/com/h2database/h2/2.2.224/h2-2.2.224.jar
cd -
```

## Solr 
### Setup SOLR 9
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
MCR.Solr.Core.project.Name=ubo-projects

# with cloud
MCR.Solr.Server.Auth.Admin.Class=org.mycore.solr.auth.MCRSolrBasicPropertyAuthentication
MCR.Solr.Server.Auth.Admin.Password=alleswirdgut
MCR.Solr.Server.Auth.Admin.Username=admin
MCR.Solr.Server.Auth.Index.Class=org.mycore.solr.auth.MCRSolrBasicPropertyAuthentication
MCR.Solr.Server.Auth.Index.Password=alleswirdgut
MCR.Solr.Server.Auth.Index.Username=indexer
MCR.Solr.Server.Auth.Search.Class=org.mycore.solr.auth.MCRSolrBasicPropertyAuthentication
MCR.Solr.Server.Auth.Search.Password=alleswirdgut
MCR.Solr.Server.Auth.Search.Username=searcher
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
# only with solr cloud (ingore errors until MCR-3543 is fixed)
ubo-cli/target/bin/ubo.sh upload local config set for main
ubo-cli/target/bin/ubo.sh upload local config set for classification
ubo-cli/target/bin/ubo.sh upload local config set for projects
ubo-cli/target/bin/ubo.sh create collection for core main
ubo-cli/target/bin/ubo.sh create collection for core classification
ubo-cli/target/bin/ubo.sh create collection for core projects

# for all solr installations
ubo-cli/target/bin/ubo.sh reload solr configuration main in core main
```

## Run 
-local web application on port 8080 with tomcat 10:

```
mvn cargo:run -pl ubo-webapp
```

## Rebuild & Run (root directory)
```
mvn clean && mvn install -am -pl ubo-webapp && mvn cargo:run -pl ubo-webapp -DskipTests
```

