package org.mycore.ubo.local;

import org.jdom2.Element;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mycore.common.MCRJPATestCase;
import org.mycore.common.MCRTestCase;
import org.mycore.user2.MCRRealmFactory;
import org.mycore.user2.MCRUser;
import org.mycore.user2.MCRUserManager;

import java.util.Objects;
import java.util.stream.Stream;

import static org.junit.Assert.*;

public class LocalSearcherTest extends MCRJPATestCase {

    @Before
    public void setUp() throws Exception {
        super.setUp();

        createTestUsers();
        endTransaction();
        beginTransaction();
    }

    MCRUser u1;

    MCRUser u2;

    MCRUser u3;

    private void createTestUsers() {
        u1 = new MCRUser("test1", MCRRealmFactory.getLocalRealm());
        u1.setRealName("Daniel Reimann");

        u2 = new MCRUser("test2", MCRRealmFactory.getLocalRealm());
        u2.setRealName("Christian Schlösser");

        u3 = new MCRUser("test3", MCRRealmFactory.getLocalRealm());
        u3.setRealName("Olga Lange");

        Stream.of(u1, u2, u3).forEach(MCRUserManager::createUser);
    }

    @Test
    public void search() {
        testWith("Daniel Reim", u1);
        testWith("Christian Schlösser", u2);
        testWith("Olga", u3);
        testWith("Lange", u3);
    }

    private void testWith(String term, MCRUser u1) {
        final LocalSearcher localSearcher = new LocalSearcher();

        final Element search = localSearcher.search(term);
        Assert.assertTrue("Expect the user to appear in list", search.getChildren("person").stream()
            .map(el -> el.getChild("id"))
            .filter(Objects::nonNull)
            .map(Element::getText)
            .anyMatch(id -> u1.getUserName().equals(id)));

        Assert.assertEquals("Expect just one person as result", 1, search.getChildren("person").size());
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }
}
