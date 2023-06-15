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

    public static class PersonResult {

        public PersonResult(IdentityService identityService){
            this.service = identityService.getClass().getSimpleName();
        }

        /**
         * The service this result was provided by
         * */
        public String service;

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

    /**
     * Joins another {@link PersonSearchResult} with the current {@link PersonSearchResult}. The count field is updated
     * automatically.
     *
     * @param other the {@link PersonSearchResult} to join.
     * */
    public void join(PersonSearchResult other) {
        join(other, personList.size() - 1);
    }

    /**
     * Joins another {@link PersonSearchResult} with the current {@link PersonSearchResult}. The count field is updated
     * automatically.
     *
     * @param other the {@link PersonSearchResult} to join.
     * @param index index at which to insert the first element from the specified {@link PersonSearchResult}
     * */
    public void join(PersonSearchResult other, int index) {
        this.personList.addAll(index, other.personList);
        this.count += other.count;
    }

}

