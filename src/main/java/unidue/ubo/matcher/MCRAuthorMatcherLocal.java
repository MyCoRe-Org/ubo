package unidue.ubo.matcher;

import org.jdom2.Element;
import org.mycore.user2.MCRUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Given a mods:name-element (as an author of a publication in MODS-format) match against the local MCRUsers, returning
 * an existing one if matched. The returned MCRUsers attributes are enriched by attributes from the mods:nameIdentifier
 * if applicable.
 *
 * @author Pascal Rost
 */
public class MCRAuthorMatcherLocal implements MCRAuthorMatcher {

    @Override
    public MCRUser matchModsAuthor(Element modsAuthor) {
        return matchByNameIdentifiers(MCRAuthorMatcherUtils.getNameIdentifiers(modsAuthor));
    }

    public MCRUser matchByNameIdentifiers(Map<String, String> nameIdentifiers) {
        MCRUser user = null;
        List<MCRUser> users = new ArrayList<>(MCRAuthorMatcherUtils.getUsersForGivenNameIdentifiers(nameIdentifiers));
        if(users.size() == 0) {
            // no match found
            // TODO: return appropriate message/MatchType/exception(?)
        } else if(users.size() > 1) {
            // TODO: return conflict message/MatchType/exception(?)
        } else if(users.size() == 1) {
            user = users.get(0);
            MCRAuthorMatcherUtils.enrichUserWithGivenNameIdentifiers(user, nameIdentifiers);
        }
        return user;
    }

}
