package org.mycore.ubo;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.mycore.common.MCRConstants;
import org.mycore.common.MCRSessionMgr;
import org.mycore.services.i18n.MCRTranslation;
import org.mycore.tools.MyCoReWebPageProvider;
import org.mycore.webtools.vue.MCRVueRootServlet;

import java.io.IOException;
import java.io.Reader;
import java.util.Optional;

public class HeadingExtendedVueRootServlet extends MCRVueRootServlet {

    private String heading;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        heading = config.getInitParameter("heading");
    }

    @Override
    protected Document buildMCRWebpage(String absoluteServletPath, Reader reader) throws JDOMException, IOException {
        Document document = super.buildMCRWebpage(absoluteServletPath, reader);


        Element child = document.getRootElement().getChild(MyCoReWebPageProvider.XML_SECTION);
        String currentLanguage = MCRSessionMgr.getCurrentSession().getCurrentLanguage();
        child.getAttributeValue(MyCoReWebPageProvider.XML_LANG, MCRConstants.XML_NAMESPACE, currentLanguage);

        Optional.ofNullable(heading)
                .map(MCRTranslation::translate)
                .ifPresent(heading -> child.setAttribute(MyCoReWebPageProvider.XML_TITLE, heading));

        return document;
    }
}
