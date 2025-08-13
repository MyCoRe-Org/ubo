package org.mycore.ubo.picker;

import org.jdom2.Element;

import javax.naming.OperationNotSupportedException;
import java.util.Map;

public interface IdentityService {

    public Element getPersonDetails(Map<String, String> paramMap);

    public Element searchPerson(Map<String, String> paramMap);

    public PersonSearchResult searchPerson(String query) throws OperationNotSupportedException;
}
