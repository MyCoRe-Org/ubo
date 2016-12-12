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

public class UniqueMerger extends Merger {

    public void setElement(Element element) {
        super.setElement(element);
    }

    @Override
    public boolean isProbablySameAs(Merger other) {
        return (other.element.getName().equals(this.element.getName()));
    }
}