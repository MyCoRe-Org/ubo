/**
 * Copyright (c) 2016 Duisburg-Essen University Library
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 **/

package unidue.ubo.merger;

import org.apache.commons.lang.StringUtils;
import org.jdom2.Element;
import org.mycore.common.MCRConstants;
import org.mycore.common.config.MCRConfiguration;

import unidue.ubo.dedup.DeDupCriteriaBuilder;

public class AbstractMerger extends Merger {

    /** Maximum Levenshtein distance to accept two abstracts as equal, in percent */
    private static final int MAX_DISTANCE_PERCENT = MCRConfiguration.instance().getInt("UBO.MODSMerger.AbstractMerger.MaxDistancePercent");

    /** Maximum number of characters to compare from two abstracts */
    private static final int MAX_COMPARE_LENGTH = MCRConfiguration.instance().getInt("UBO.MODSMerger.AbstractMerger.MaxCompareLength");

    private String text;

    public void setElement(Element element) {
        super.setElement(element);
        text = DeDupCriteriaBuilder.normalizeText(element.getText());
        text += element.getAttributeValue("href", MCRConstants.XLINK_NAMESPACE, "");
        text = text.substring(0, Math.min(text.length(), MAX_COMPARE_LENGTH));
    }

    @Override
    public boolean isProbablySameAs(Merger other) {
        if (!(other instanceof AbstractMerger))
            return false;

        String textOther = ((AbstractMerger) other).text;
        int length = Math.min(text.length(), textOther.length());
        int distance = StringUtils.getLevenshteinDistance(text, textOther);
        System.out.println(distance);
        return (distance * 100 / length) < MAX_DISTANCE_PERCENT;
    }
}
