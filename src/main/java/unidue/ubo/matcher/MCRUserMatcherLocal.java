package unidue.ubo.matcher;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.user2.MCRUser;
import org.mycore.user2.MCRUserManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Given a MCRUser, match against the local MCRUsers, returning the given User or an existing local one if matched.
 * If matched, the returned local MCRUsers attributes are enriched by attributes from the given MCRUser.
 *
 * @author Pascal Rost
 */
public class MCRUserMatcherLocal implements MCRUserMatcher {

    private final static Logger LOGGER = LogManager.getLogger(MCRUserMatcherLocal.class);

    @Override
    public MCRUser matchUser(MCRUser mcrUser) {
        List<MCRUser> matchingUsers = new ArrayList<>(getUsersForGivenAttributes(mcrUser.getAttributes()));
        if(matchingUsers.size() == 0) {
            // no match found, do nothing, return given user unchanged
        } else if(matchingUsers.size() > 1) {
            // TODO: return conflict message/MatchType/exception(?)
        } else if(matchingUsers.size() == 1) {
            MCRUser matchingUser = matchingUsers.get(0);

            LOGGER.debug("Found local matching user! Matched user: {} and attributes: {} with local user: {} and attributes: {}",
                    mcrUser.getUserName(), mcrUser.getAttributes(), matchingUser.getUserName(), matchingUser.getAttributes());

            matchingUser.getAttributes().putAll(mcrUser.getAttributes());
            mcrUser = matchingUser;
        }
        return mcrUser;
    }

    private Set<MCRUser> getUsersForGivenAttributes(Map<String, String> attributes) {
        Set<MCRUser> users = new HashSet<>();
        for(Map.Entry<String, String> attributeEntry : attributes.entrySet()) {
            String attributeName = attributeEntry.getKey();
            String attributeValue = attributeEntry.getValue();
            users.addAll(MCRUserManager.getUsers(attributeName, attributeValue).collect(Collectors.toList()));
        }
        return users;
    }

}