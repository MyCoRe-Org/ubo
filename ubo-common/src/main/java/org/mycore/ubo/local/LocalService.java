package org.mycore.ubo.local;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.naming.OperationNotSupportedException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Element;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.ubo.picker.IdentityService;
import org.mycore.ubo.picker.PersonSearchResult;
import org.mycore.user2.MCRRealm;
import org.mycore.user2.MCRRealmFactory;
import org.mycore.user2.MCRUser;
import org.mycore.user2.MCRUserAttribute;
import org.mycore.user2.MCRUserManager;

public class LocalService implements IdentityService {
    private final static Logger LOGGER = LogManager.getLogger(LocalService.class);

    private final String USER_FIRST_NAME_ATTR = "firstName";

    private final String USER_LAST_NAME_ATTR = "lastName";

    private final String LEAD_ID = MCRConfiguration2.getStringOrThrow("MCR.user2.matching.lead_id");

    private final boolean ENFORCE_LEAD_ID_PRESENT = MCRConfiguration2.getBoolean(
        "MCR.IdentityPicker.strategy.Local.PID.Filter.enabled").orElse(true);

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
        return searchPerson(query, MCRRealmFactory.getLocalRealm());
    }

    public PersonSearchResult searchPerson(String query, MCRRealm realm) {
        String displayName = "*" + query.trim() + "*";
        final List<MCRUser> matchingUsers = MCRUserManager.listUsers(null, realm.getID(), displayName, null);

        List<PersonSearchResult.PersonResult> personResults = matchingUsers.stream().map(user -> {
                PersonSearchResult.PersonResult personSearchResult = new PersonSearchResult.PersonResult(this);
                List<MCRUserAttribute> leadIdAttributes = user.getAttributes()
                    .stream()
                    .filter(attr -> ("id_" + LEAD_ID).equals(attr.getName()))
                    .toList();

                if (leadIdAttributes.size() > 1) {
                    LOGGER.warn("Found more than one {} for user {}", ("id_" + LEAD_ID), user.getUserID());
                }

                personSearchResult.pid = leadIdAttributes.size() > 0 ? leadIdAttributes.get(0).getValue() : null;
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
            }).filter(psr -> (psr.pid != null && !psr.pid.isEmpty()) || !ENFORCE_LEAD_ID_PRESENT)
            .collect(Collectors.toList());

        PersonSearchResult personSearchResult = new PersonSearchResult();
        personSearchResult.count = personResults.size();
        personSearchResult.personList = personResults;

        return personSearchResult;
    }
}
