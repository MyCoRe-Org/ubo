package unidue.ubo.matcher;

import org.jdom2.Element;
import org.mycore.user2.MCRUser;

public interface MCRAuthorMatcher {

    MCRUser matchModsAuthor(Element modsAuthor);
}
