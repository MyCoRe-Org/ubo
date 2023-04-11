package org.mycore.ubo.ldap.picker;

import javax.naming.OperationNotSupportedException;

import org.mycore.ubo.local.LocalService;
import org.mycore.ubo.picker.PersonSearchResult;

public class LDAPWithLocalService extends LDAPService {

    @Override
    public PersonSearchResult searchPerson(String query) throws OperationNotSupportedException {
        PersonSearchResult results = super.searchPerson(query);
        LocalService localService = new LocalService();
        PersonSearchResult personSearchResult = localService.searchPerson(query);

        results.personList.addAll(0, personSearchResult.personList);
        results.count += personSearchResult.count;

        return results;
    }
}
