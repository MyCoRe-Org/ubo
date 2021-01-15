package unidue.ubo.ldap;

/**
 * Abstraction class of a LDAP 'labeledUri' attribute parse process result.
 *
 * @author Pascal Rost
 */
public class LDAPParsedLabeledURI {

    private String uri;
    private String identifierName;
    private String identifierValue;

    public LDAPParsedLabeledURI(String uri, String identifierName, String identifierValue) {
        this.uri = uri;
        this.identifierName = identifierName;
        this.identifierValue = identifierValue;
    }

    public String getUri() {
        return uri;
    }

    public String getIdentifierName() {
        return identifierName;
    }

    public String getIdentifierValue() {
        return identifierValue;
    }
}
