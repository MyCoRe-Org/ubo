package org.mycore.ubo.ldap.picker;

import org.mycore.common.config.MCRConfiguration2;
import org.mycore.ubo.local.LocalService;
import org.mycore.ubo.picker.PersonSearchResult;
import org.mycore.user2.MCRRealmFactory;

import javax.naming.OperationNotSupportedException;
import java.util.List;
import java.util.Optional;

public class LDAPWithLocalService extends LDAPService {

    public static final String LDAP_REALM = MCRConfiguration2.getString(
        "MCR.user2.IdentityManagement.UserCreation.LDAP.Realm").orElse(null);

    @Override
    public PersonSearchResult searchPerson(String query) throws OperationNotSupportedException {
        PersonSearchResult results = super.searchPerson(query);
        LocalService localService = new LocalService();
        PersonSearchResult personSearchResult = localService.searchPerson(query);
        flipNameParts(personSearchResult.personList);
        results.join(personSearchResult, 0);

        if (LDAP_REALM != null) {
            Optional.ofNullable(MCRRealmFactory.getRealm(LDAP_REALM))
                .ifPresent(
                    realm -> results.join(localService.searchPerson(modifyLocalQueryWithRealm(query), realm), 0));
        }

        return results;
    }

    protected void flipNameParts(List<PersonSearchResult.PersonResult> list) {
        for (PersonSearchResult.PersonResult p : list) {
            // split at ',' and remove leading and trailing spaces
            String[] parts = p.displayName.split("\\s*,\\s*");

            if (parts.length > 1) {
                String firstName = parts[1];
                String lastName = parts[0];

                // set name parts with new values
                p.firstName = firstName;
                p.lastName = lastName;
                p.displayName = p.firstName + " " + p.lastName;
            }
        }
    }

    /**
     * Allow to modify the query when the {@link LocalService} is used for searching but a realm (different to
     * {@link MCRRealmFactory#getLocalRealm()}) is provided. This is useful when the name format of the stored users
     * by MyCoRe differs from that one provided by ldap.
     *
     * @param query
     * @return
     */
    protected String modifyLocalQueryWithRealm(String query) {
        return query;
    }
}
