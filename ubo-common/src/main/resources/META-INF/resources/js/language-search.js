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

/**
 * @typedef {Object} ClassificationLabel
 * @property {string} lang
 * @property {string} text
 * @property {string} description
 */

/**
 * @typedef {Object} ClassificationCategory
 * @property {string} ID
 * @property {Array<ClassificationLabel>} labels
 * @property {string} categories
 */

/**
 * @typedef {Object} ClassificationResponse
 * @property {string} ID
 * @property {Array<ClassificationLabel>} labels
 * @property {Array<ClassificationCategory>} categories
 */

/**
 * Class to create a language search input. This input will show a list of languages to choose from. It expects the following
 * structure:
 *  <div class="language-search" data-preferred-languages="de,en,fr">
 *   <input type="hidden" class="language-search-input"/>
 *  </div>
 */
class LanguageSearchInput {

    /**
     * @type {HTMLDataListElement}
     */
    static dataList;
    /**
     * @type {Map<string, string>}
     */
    static labelIdMap;

    /**
     * @type {Map<string, string>}
     */
    static idLabelMap;
    /**
     * @type {HTMLInputElement}
     */
    hiddenInput;
    /**
     * @type {HTMLDivElement}
     */
    root;
    /**
     * @type {HTMLInputElement}
     */
    searchInput;
    /**
     * @type {Array<string>}
     */
    preferredLanguages;

    /**
     * Creates a search input for languages.
     * @param {HTMLDivElement} elem
     */
    constructor(elem) {
        this.root = elem;
        this.hiddenInput = elem.querySelector('input[class="language-search-input"]');

        if (elem.getAttribute("data-preferred-languages") != null) {
            this.preferredLanguages = elem.getAttribute("data-preferred-languages").split(',');
        } else {
            this.preferredLanguages = [];
        }

        if (this.hiddenInput == null) {
            console.error('No input found in language-search-input');
            return;
        }


        this.initializeForm().then(() => {
            console.log('Form initialized');
        });

    }

    /**
     * Finds the best matching label from the given array of labels.
     * @param labelArray {Array<ClassificationLabel>} The array of labels to search in.
     */
    static findBestMatchingLabel(labelArray) {
        let bestMatch = labelArray[0];

        labelArray.forEach(label => {
            if (label.lang === window["currentLang"]) {
                bestMatch = label;
            }
        });

        return bestMatch;
    }

    /**
     * Checks if the given search input is valid.
     * @param searchInput {HTMLInputElement} The search input to check.
     * @returns {boolean|boolean|*} True if the input is valid, false otherwise.
     */
    static isValidInput(searchInput) {
        return LanguageSearchInput.labelIdMap.has(searchInput.value);
    }

    /**
     * Modifies the given search input to indicate if the input is valid or not.
     * @param searchInput {HTMLInputElement} The search input to modify.
     * @param isValid {boolean} True if the input is valid
     */
    static setInputValidation(searchInput, isValid) {
        if (isValid) {
            searchInput.classList.remove('is-invalid');
        } else {
            searchInput.classList.add('is-invalid');
        }
    }

    async initializeForm() {
        if (!LanguageSearchInput.dataList) {
            LanguageSearchInput.dataList = document.createElement('datalist');
            LanguageSearchInput.dataList.id = 'language-search-list';


            const baseURL = window['webApplicationBaseURL'];
            const response = await fetch(baseURL + 'api/v2/classifications/rfc5646', {
                method: 'GET',
                headers: {
                    'Accept': 'application/json'
                }
            });

            /**
             * @type {ClassificationResponse}
             */
            const data = await response.json();


            LanguageSearchInput.labelIdMap = /** @type {Map<string, string>} */ new Map();

            LanguageSearchInput.idLabelMap = /** @type {Map<string, string>} */ new Map();
            /**
             * @type {Array<{el: HTMLOptionElement, category: ClassificationCategory}>}
             */
            const prependLater = [];
            data.categories.forEach(category => {
                const optionElement = document.createElement("option");
                const label = LanguageSearchInput.findBestMatchingLabel(category.labels).text;
                optionElement.value = label;
                optionElement.text = label;
                LanguageSearchInput.labelIdMap.set(label, category.ID);
                LanguageSearchInput.idLabelMap.set(category.ID, label);
                const preferredLanguagePos = this.preferredLanguages.indexOf(category.ID);
                if (preferredLanguagePos == -1) {
                    LanguageSearchInput.dataList.append(optionElement);
                } else {
                    prependLater.push({el:optionElement , category:category});
                }
            });

            prependLater.sort((a,b) => {
                return this.preferredLanguages.indexOf(b.category.ID) - this.preferredLanguages.indexOf(a.category.ID);
            }).forEach((el) => {
                LanguageSearchInput.dataList.prepend(el.el);
            });

            this.root.append(LanguageSearchInput.dataList);
        }

        const searchInput = document.createElement('input');
        searchInput.type = 'text';
        searchInput.classList.add('form-control');
        searchInput.classList.add('language-search-input');
        searchInput.setAttribute('list', 'language-search-list');
        this.root.append(searchInput);

        if(this.hiddenInput.value != null && this.hiddenInput.value.trim().length > 0) {
            if(LanguageSearchInput.idLabelMap.has(this.hiddenInput.value)) {
                searchInput.value = LanguageSearchInput.idLabelMap.get(this.hiddenInput.value.trim());
            }
        }

        searchInput.addEventListener('change', () => {
            if (searchInput.value.trim().length === 0) {
                this.hiddenInput.value = '';
                LanguageSearchInput.setInputValidation(searchInput, true);
                return;
            }

            if (LanguageSearchInput.isValidInput(searchInput)) {
                this.hiddenInput.value = LanguageSearchInput.labelIdMap.get(searchInput.value);
                LanguageSearchInput.setInputValidation(searchInput, true);
                return;
            }

            LanguageSearchInput.setInputValidation(searchInput, false);
        });
    }
}

document.addEventListener('DOMContentLoaded', function () {

    document.querySelectorAll('.language-search').forEach(function (input) {
        new LanguageSearchInput(input);
    });

});
