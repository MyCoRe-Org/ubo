package org.mycore.ubo.local;

import java.util.Map;

import org.jdom2.Element;
import org.mycore.ubo.picker.IdentityService;

public class LocalService implements IdentityService {
    @Override
    public Element getPersonDetails(Map<String, String> paramMap) {
        return null;
    }

    @Override
    public Element searchPerson(Map<String, String> paramMap) {
        final Element search = new LocalSearcher().search(paramMap.get("firstName"), paramMap.get("lastName"));

        return search;
    }
}
