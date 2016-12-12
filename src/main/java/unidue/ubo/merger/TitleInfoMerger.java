/**
 * $Revision: 36492 $ 
 * $Date: 2016-11-11 17:31:02 +0100 (Fr, 11 Nov 2016) $
 *
 * This file is part of the MILESS repository software.
 * Copyright (C) 2011 MILESS/MyCoRe developer team
 * See http://duepublico.uni-duisburg-essen.de/ and http://www.mycore.de/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **/

package unidue.ubo.merger;

import org.jdom2.Element;
import org.mycore.common.MCRConstants;

import unidue.ubo.dedup.DeDupCriteriaBuilder;

public class TitleInfoMerger extends Merger {

    private String text;

    public void setElement(Element element) {
        super.setElement(element);

        text = textOf("nonSort") + " " + textOf("title") + " " + textOf("subTitle");
        text = DeDupCriteriaBuilder.normalizeText(text.trim());
    }

    @Override
    public boolean isProbablySameAs(Merger other) {
        if (!(other instanceof TitleInfoMerger))
            return false;
        else
            return text.equals(((TitleInfoMerger) other).text);
    }

    public void mergeFrom(Merger other) {
        mergeAttributes(other);

        // if the other one has a subTitle and we don't, the other one wins
        if (textOf("subTitle").isEmpty() && !((TitleInfoMerger) other).textOf("subTitle").isEmpty()) {
            this.element.setContent(other.element.cloneContent());
        }
    }

    private String textOf(String childName) {
        String text = element.getChildText(childName, MCRConstants.MODS_NAMESPACE);
        return text == null ? "" : text.trim();
    }
}