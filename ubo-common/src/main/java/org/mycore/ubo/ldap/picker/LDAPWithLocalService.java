package org.mycore.ubo.ldap.picker;

import java.util.Optional;

import javax.naming.OperationNotSupportedException;

import org.mycore.common.config.MCRConfiguration2;
import org.mycore.ubo.local.LocalService;
import org.mycore.ubo.picker.PersonSearchResult;
import org.mycore.user2.MCRRealmFactory;

public class LDAPWithLocalService extends LDAPService {

    public static final String LDAP_REALM = MCRConfiguration2.getString(
        "MCR.user2.IdentityManagement.UserCreation.LDAP.Realm").orElse(null);

    @Override
    public PersonSearchResult searchPerson(String query) throws OperationNotSupportedException {
        PersonSearchResult results = super.searchPerson(query);
        LocalService localService = new LocalService();
        PersonSearchResult personSearchResult = localService.searchPerson(query);
        results.join(personSearchResult, 0);

        if (LDAP_REALM != null) {
            Optional.ofNullable(MCRRealmFactory.getRealm(LDAP_REALM))
                .ifPresent(realm -> results.join(localService.searchPerson(query, realm), 0));
        }

        return results;
    }
}
