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
     * Sometimes the labelIdMap and idLabelMap are not yet available. This promise will resolve once they are.
     * @type {Promise<{
     *  labelIdMap: Map<string, string>,
     *  idLabelMap: Map<string, string>,
     * }>}
     */
    static mapPromise;

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
        const baseURL = window['webApplicationBaseURL'];
        const currentLang = window["currentLang"];

        if (!LanguageSearchInput.dataList) {
            LanguageSearchInput.dataList = document.createElement('datalist');
            LanguageSearchInput.dataList.id = 'language-search-list';

            /**
             * Used to queue up the resolvers for the mapPromise
             * @type {Array<Function<{
             *  labelIdMap: Map<string, string>,
             *  idLabelMap: Map<string, string>,
             * }>>}
             */
            const resolverList = [];

            LanguageSearchInput.mapPromise = new Promise(async (resolve, reject) => {
                    if(LanguageSearchInput.labelIdMap && LanguageSearchInput.idLabelMap){
                        resolve({
                            labelIdMap: LanguageSearchInput.labelIdMap,
                            idLabelMap: LanguageSearchInput.idLabelMap
                        });
                        return;
                    }
                    resolverList.push(resolve);
            });

            const baseURL = window['webApplicationBaseURL'];
            const response = await fetch(baseURL + 'api/v2/classifications/rfc5646', {
                method: 'GET',
                credentials: 'omit',
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

            resolverList.forEach((resolve) => {
                resolve({
                    labelIdMap: LanguageSearchInput.labelIdMap,
                    idLabelMap: LanguageSearchInput.idLabelMap
                });
            });

            prependLater.sort((a,b) => {
                return this.preferredLanguages.indexOf(b.category.ID) - this.preferredLanguages.indexOf(a.category.ID);
            }).forEach((el) => {
                LanguageSearchInput.dataList.prepend(el.el);
            });

            this.root.append(LanguageSearchInput.dataList);
        }

        this.searchInput = document.createElement('input');
        this.searchInput.type = 'text';
        this.searchInput.classList.add('form-control');
        this.searchInput.classList.add('language-search-input');
        this.searchInput.classList.add('mycore-form-input');
        this.searchInput.setAttribute('list', 'language-search-list');
        this.root.append(this.searchInput);

        while (this.root.firstChild) {
            this.root.parentNode.insertBefore(this.root.firstChild, this.root);
        }
        this.root.parentNode.removeChild(this.root);

        fetch(`${baseURL}rsc/locale/translate/${currentLang}/edit.language.placeholder`)
            .then(response => response.text())
            .then(translation => {
                this.searchInput.placeholder = translation;
            });


        if(this.hiddenInput.value != null && this.hiddenInput.value.trim().length > 0) {
            let idLabelMap = LanguageSearchInput.idLabelMap;
            if(!LanguageSearchInput.idLabelMap){
                const maps = await LanguageSearchInput.mapPromise;
                idLabelMap = maps.idLabelMap;
            }
            if(idLabelMap.has(this.hiddenInput.value)) {
                this.searchInput.value = idLabelMap.get(this.hiddenInput.value.trim());
            }
        }

        this.searchInput.addEventListener('change', () => {
            if (this.searchInput.value.trim().length === 0) {
                this.hiddenInput.value = '';
                LanguageSearchInput.setInputValidation(this.searchInput, true);
                return;
            }

            if (LanguageSearchInput.isValidInput(this.searchInput)) {
                this.hiddenInput.value = LanguageSearchInput.labelIdMap.get(this.searchInput.value);
                LanguageSearchInput.setInputValidation(this.searchInput, true);
                return;
            }

            LanguageSearchInput.setInputValidation(this.searchInput, false);
        });
    }
}

document.addEventListener('DOMContentLoaded', function () {

    document.querySelectorAll('.language-search').forEach(function (input) {
        new LanguageSearchInput(input);
    });

});
