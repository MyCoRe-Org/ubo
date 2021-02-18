package org.mycore.ubo.local;

import org.jdom2.Element;
import org.mycore.user2.MCRUser;
import org.mycore.user2.MCRUserAttribute;
import org.mycore.user2.MCRUserManager;

import java.util.List;
import java.util.Optional;

public class LocalSearcher {

    private static final String FIRST_NAME_ELEMENT_NAME = "firstName";

    private static final String LAST_NAME_ELEMENT_NAME = "lastName";

    private final String USER_FIRST_NAME_ATTR = "firstName";

    private final String USER_LAST_NAME_ATTR = "lastName";

    public Element search(String forename, String surename) {
        return search(forename + " " + surename);
    }

    public Element search(String term) {
        final Element results = new Element("results");

        String displayName = "*" + term.trim() + "*";
        final List<MCRUser> matchingUsers = MCRUserManager.listUsers(null, "local", displayName, null);

        matchingUsers.stream().map(this::createPersonElement).forEach(results::addContent);

        return results;
    }

    private Element createPersonElement(MCRUser mcrUser) {
        final Element person = new Element("person");

        final Element idElement = new Element("id");
        idElement.addContent(mcrUser.getUserName());
        person.addContent(idElement);

        final Element realNameElement = new Element("realName");
        final String realName = mcrUser.getRealName();
        realNameElement.addContent(realName);
        person.addContent(realNameElement);

        final String[] realNameSplit = realName.split(" ", 2);

        mapUserAttrToElement(mcrUser, USER_FIRST_NAME_ATTR, FIRST_NAME_ELEMENT_NAME, person, realNameSplit[0]);
        mapUserAttrToElement(mcrUser, USER_LAST_NAME_ATTR, LAST_NAME_ELEMENT_NAME, person,
            realNameSplit.length > 1 ? realNameSplit[1] : null);

        return person;
    }

    private void mapUserAttrToElement(MCRUser user, String attrName, String elemName, Element personElement,
        String alternative) {
        user.getAttributes().stream()
            .filter(attr -> attr.getName().equals(attrName))
            .findFirst()
            .map(MCRUserAttribute::getValue)
            .or(() -> Optional.ofNullable(alternative))
            .ifPresent(attrVal -> {
                final Element element = new Element(elemName);
                element.addContent(attrVal);
                personElement.addContent(element);
            });
    }
}
