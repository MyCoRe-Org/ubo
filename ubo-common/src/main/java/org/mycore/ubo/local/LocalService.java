package org.mycore.ubo.local;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jdom2.Element;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.ubo.picker.IdentityService;
import org.mycore.ubo.picker.PersonSearchResult;
import org.mycore.user2.MCRUser;
import org.mycore.user2.MCRUserAttribute;
import org.mycore.user2.MCRUserManager;

import javax.naming.OperationNotSupportedException;

public class LocalService implements IdentityService {

    private final String USER_FIRST_NAME_ATTR = "firstName";

    private final String USER_LAST_NAME_ATTR = "lastName";

    private final String LEAD_ID = MCRConfiguration2.getStringOrThrow("MCR.user2.matching.lead_id");

    @Override
    public Element getPersonDetails(Map<String, String> paramMap) {
        return null;
    }

    @Override
    public Element searchPerson(Map<String, String> paramMap) {
        final Element search = new LocalSearcher().search(paramMap.get("firstName"), paramMap.get("lastName"));

        return search;
    }

    @Override
    public PersonSearchResult searchPerson(String query) throws OperationNotSupportedException {
        String displayName = "*" + query.trim() + "*";
        final List<MCRUser> matchingUsers = MCRUserManager.listUsers(null, "local", displayName, null);

        List<PersonSearchResult.PersonResult> personResults = matchingUsers.stream().map(user -> {
            PersonSearchResult.PersonResult personSearchResult = new PersonSearchResult.PersonResult();
            personSearchResult.pid = user.getUserAttribute("id_" + LEAD_ID);
            personSearchResult.displayName = user.getRealName().length() > 0 ? user.getRealName() : user.getUserName();

            user.getAttributes().stream()
                .filter(attr -> attr.getName().equals(USER_FIRST_NAME_ATTR))
                .findFirst()
                .map(MCRUserAttribute::getValue)
                .ifPresent(attrVal -> {
                    personSearchResult.firstName = attrVal;
                });

            user.getAttributes().stream()
                .filter(attr -> attr.getName().equals(USER_LAST_NAME_ATTR))
                .findFirst()
                .map(MCRUserAttribute::getValue)
                .ifPresent(attrVal -> {
                    personSearchResult.lastName = attrVal;
                });

            if (user.getEMailAddress() != null) {
                personSearchResult.information = new ArrayList<>();
                personSearchResult.information.add(user.getEMailAddress());
            }

            return personSearchResult;
        }).collect(Collectors.toList());

        PersonSearchResult personSearchResult = new PersonSearchResult();
        personSearchResult.count = personResults.size();
        personSearchResult.personList = personResults;

        return personSearchResult;
    }
}
