package org.mycore.ubo.picker;


import java.util.Map;

import org.jdom2.Element;

import javax.naming.OperationNotSupportedException;

public interface IdentityService {

    public Element getPersonDetails(Map<String, String> paramMap);

    public Element searchPerson(Map<String, String> paramMap);

    public PersonSearchResult searchPerson(String query) throws OperationNotSupportedException;
}
