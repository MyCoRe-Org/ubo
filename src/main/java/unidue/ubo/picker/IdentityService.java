package unidue.ubo.picker;


import java.util.Map;

import org.jdom2.Element;

public interface IdentityService {

    public Element getPersonDetails(Map<String, String> paramMap);

    public Element searchPerson(Map<String, String> paramMap);
}
