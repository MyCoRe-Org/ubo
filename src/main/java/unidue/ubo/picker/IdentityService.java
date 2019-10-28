package unidue.ubo.picker;


import org.jdom2.Element;

import java.util.Map;

public interface IdentityService {

    public Element getPersonDetails(Map<String, String> paramMap);

    public Element searchPerson(Map<String, String> paramMap);
}
