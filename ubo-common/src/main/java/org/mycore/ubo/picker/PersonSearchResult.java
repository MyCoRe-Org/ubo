/*
 * This file is part of ***  M y C o R e  ***
 * See http://www.mycore.de/ for details.
 *
 * MyCoRe is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MyCoRe is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MyCoRe.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mycore.ubo.picker;

import java.util.List;

public class PersonSearchResult {

    public int count = 0;

    public List<PersonResult> personList;


    public static class  PersonResult {
        /**
         * The Identifier of the person [Required]
         */
        public String pid;

        /**
         * The name which should be displayed. [Required]
         */
        public String displayName;

        /**
         * [Optional]
         */
        public String firstName;

        /**
         * [Optional]
         */
        public String lastName;

        /**
         * [Optional]
         */
        public List<String> affiliation;

        /**
         * [Optional]
         */
        public List<String> information;
    }
}

