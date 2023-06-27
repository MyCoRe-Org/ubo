/*
 * This file is used to display the list of orcid ids of the user and give them the ability to revoke them. It also
 * allows the user to change orcid specific settings.
 */

/**
 * @typedef JWTResponse
 * @property {bool} login_success True if the token is valid.
 * @property {string} access_token The access token.
 * @property {string} token_type The token type. e.g. "Bearer"
 */

/**
 * @typedef UserStatus
 * @property {Array<string>} orcids a list of orcids of the user.
 * @property {Array<string>} trustedOrcids a list of orcids which are trusted by orcid.
 */

/**
 * @typedef UserProperties
 * @property {boolean} alwaysUpdateWork True if the user wants to always update the work.
 * @property {boolean} createDuplicateWork True if the user wants to create a duplicate work.
 * @property {boolean} createFirstWork True if the user wants to create a first work.
 * @property {boolean} recreateDeletedWork True if the user wants to recreate a deleted work.
 */

/**
 * This class is used to interact with the server. It is used to get the user status, revoke an orcid and set the user
 * properties.
 */
class OrcidProfile {
    /**
     * This function is used to get the JWT token object from the server.
     * @param {string} baseURL The base URL of the server.
     * @returns {Promise<JWTResponse>}
     */
    static async getJWTTokenObject(baseURL) {
        if (!baseURL) {
            throw new Error("baseURL is undefined");
        }
        const requestURL = `${baseURL}rsc/jwt`;
        const response = await fetch(requestURL);
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        let responseJson = await response.json();
        if (!responseJson.login_success) {
            throw new Error(`Login failed!`);
        }
        return responseJson;
    }

    /**
     * Turns the token response into an authorization header.
     * @param tokenResponse {JWTResponse}
     * @returns {string}
     */
    static getAuthorizationHeader(tokenResponse) {
        if (!tokenResponse) {
            throw new Error("tokenResponse is undefined");
        }
        return `${tokenResponse.token_type} ${tokenResponse.access_token}`;
    }

    /**
     * Fetches the user status from the server.
     * @param {string} baseURL The base URL of the server.
     * @returns {Promise<UserStatus>} The user status.
     */
    static async getUserStatus(baseURL) {
        if (!baseURL) {
            throw new Error("baseURL is undefined");
        }
        const tokenObject = await OrcidProfile.getJWTTokenObject(baseURL);
        const authHeader = OrcidProfile.getAuthorizationHeader(tokenObject);
        const requestURL = `${baseURL}api/orcid/v1/user-status`;
        const response = await fetch(requestURL, {headers: {Authorization: authHeader}});
        return await response.json();
    }

    /**
     * Revokes the given orcid.
     * @param {string} baseURL The base URL of the server.
     * @param {string} orcid The orcid to revoke.
     * @returns {Promise<boolean>} True if the orcid was revoked successfully.
     */
    static async revokeOrcid(baseURL, orcid) {
        if (!baseURL) {
            throw new Error("baseURL is undefined");
        }
        if (!orcid) {
            throw new Error("orcid is undefined");
        }
        const tokenObject = await OrcidProfile.getJWTTokenObject(baseURL);
        const authHeader = OrcidProfile.getAuthorizationHeader(tokenObject);
        const requestURL = `${baseURL}api/orcid/v1/revoke/${orcid}`;
        const response = await fetch(requestURL, {headers: {Authorization: authHeader}, method: "POST"});
        if (!response.ok) {
            console.warn(`HTTP error! status: ${response.status}`);
        }
        return response.ok;
    }

    /**
     * Gets the user properties.
     * @param baseURL {string} The base URL of the server.
     * @param orcid The orcid of the user.
     * @returns {Promise<UserProperties>} The user properties.
     */
    static async getUserProperties(baseURL, orcid) {
        if (!baseURL) {
            throw new Error("baseURL is undefined");
        }
        if (!orcid) {
            throw new Error("orcid is undefined");
        }
        const tokenObject = await OrcidProfile.getJWTTokenObject(baseURL);
        const authHeader = OrcidProfile.getAuthorizationHeader(tokenObject);
        const requestURL = `${baseURL}api/orcid/v1/${orcid}/user-properties`;
        const response = await fetch(requestURL, {headers: {Authorization: authHeader}});
        return await response.json();
    }


    /**
     * Sets the user properties.
     * @param baseURL {string} The base URL of the server.
     * @param orcid The orcid of the user.
     * @param userProperties {UserProperties} The user properties.
     * @returns {Promise<boolean>} True if the user properties were set successfully.
     */
    static async setUserProperties(baseURL, orcid, userProperties) {
        if (!baseURL) {
            throw new Error("baseURL is undefined");
        }
        if (!orcid) {
            throw new Error("orcid is undefined");
        }
        if (!userProperties) {
            throw new Error("userProperties is undefined");
        }
        const tokenObject = await OrcidProfile.getJWTTokenObject(baseURL);
        const authHeader = OrcidProfile.getAuthorizationHeader(tokenObject);
        const requestURL = `${baseURL}api/orcid/v1/${orcid}/user-properties`;
        const response = await fetch(requestURL, {
            headers: {
                Authorization: authHeader,
                "Content-Type": "application/json"
            },
            method: "PUT",
            body: JSON.stringify(userProperties)
        });
        if (!response.ok) {
            console.warn(`HTTP error! status: ${response.status}`);
        }
        return response.ok;
    }
}

/**
 * This class is used to show the user interface. It is used to show the user settings and the revoke dialog.
 */
class OrcidProfileGUI {
    /**
     * Shows a confirm dialog
     * @param title {string} The title of the dialog.
     * @param message {string} The message of the dialog.
     * @param cancelButtonText {string|null} The text of the cancel button.
     * @param confirmButtonText {string|null} The text of the confirm button.
     * @returns {Promise<boolean>} True if the confirm button was clicked.
     */
    static async bootstrapConfirm(title, message, cancelButtonText, confirmButtonText) {
        return new Promise((resolve, reject) => {
            // create the modal
            let modal = document.createElement("div");
            const modalId = "id" + Math.random().toString(36).substring(2, 15);
            modal.setAttribute("id", modalId);
            modal.classList.add("modal");
            modal.classList.add("fade");
            modal.setAttribute("tabindex", "-1");
            modal.setAttribute("role", "dialog");
            document.body.appendChild(modal);


            let modalDialog = document.createElement("div");
            modalDialog.classList.add("modal-dialog");
            modalDialog.classList.add("modal-dialog-centered");
            modal.appendChild(modalDialog);

            let modalContent = document.createElement("div");
            modalContent.classList.add("modal-content");
            modalDialog.appendChild(modalContent);

            let modalHeader = document.createElement("div");
            modalHeader.classList.add("modal-header");
            modalContent.appendChild(modalHeader);

            let modalTitle = document.createElement("h5");
            modalTitle.classList.add("modal-title");
            modalTitle.textContent = title;
            modalHeader.appendChild(modalTitle);

            let modalBody = document.createElement("div");
            modalBody.classList.add("modal-body");
            modalBody.textContent = message;
            modalContent.appendChild(modalBody);

            let modalFooter = document.createElement("div");
            modalFooter.classList.add("modal-footer");
            modalContent.appendChild(modalFooter);

            let resolved = false;

            let $modelJq = $(`#${modalId}`);
            if (cancelButtonText) {
                let modalCancelButton = document.createElement("button");
                modalCancelButton.classList.add("btn");
                modalCancelButton.classList.add("btn-secondary");
                modalCancelButton.setAttribute("type", "button");
                modalCancelButton.setAttribute("data-dismiss", "modal");
                modalCancelButton.textContent = cancelButtonText;
                modalFooter.appendChild(modalCancelButton);
                modalCancelButton.addEventListener("click", () => {
                    resolved = true;
                    resolve(false);
                    $modelJq.modal("hide");
                    modal.remove();
                });
            }

            if (confirmButtonText) {
                let modalConfirmButton = document.createElement("button");
                modalConfirmButton.classList.add("btn");
                modalConfirmButton.classList.add("btn-primary");
                modalConfirmButton.setAttribute("type", "button");
                modalConfirmButton.textContent = confirmButtonText;
                modalFooter.appendChild(modalConfirmButton);
                modalConfirmButton.addEventListener("click", () => {
                    resolved = true;
                    resolve(true);
                    $modelJq.modal("hide");
                    modal.remove();
                });
            }


            $modelJq.modal();
            $modelJq.modal("show");

            $modelJq.on('hidden.bs.modal', function () {
                if (!resolved) {
                    resolve(false);
                    modal.remove();
                }
            });
        });
    }

    /**
     * Shows the settings dialog.
     * @param baseURL {string} The base URL of the server.
     * @param language {string} The language of the dialog.
     * @param current {UserProperties} The current settings.
     * @returns {Promise<UserProperties>} The updated settings.
     */
    static async settingsDialog(baseURL, language, current) {
        const [save, cancel, title,
            alwaysUpdateWorkLabel,
            createDuplicateWorkLabel,
            createFirstWorkLabel,
            recreateDeletedWorkLabel] =
            await Promise.all([
                OrcidProfileGUI.translate(baseURL, language, "button.save"),
                OrcidProfileGUI.translate(baseURL, language, "button.cancel"),
                OrcidProfileGUI.translate(baseURL, language, "orcid.integration.settings.title"),
                OrcidProfileGUI.translate(baseURL, language, "orcid.integration.settings.alwaysUpdateWork"),
                OrcidProfileGUI.translate(baseURL, language, "orcid.integration.settings.createDuplicateWork"),
                OrcidProfileGUI.translate(baseURL, language, "orcid.integration.settings.createFirstWork"),
                OrcidProfileGUI.translate(baseURL, language, "orcid.integration.settings.recreateDeletedWork"),
            ]);

        return new Promise((resolve, reject) => {
            const modal = document.createElement("div");
            const modalId = "id" + Math.random().toString(36).substring(2, 15);
            modal.innerHTML = `
<div class="modal fade" id="${modalId}" tabindex="-1" role="dialog" aria-labelledby="settingsDialogLabel" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered" role="document">
        <div class="modal-content">
            <!-- Header -->
            <div class="modal-header">
                <h5 class="modal-title" id="settingsDialogLabel">${title}</h5>
            </div>
            <!-- Body -->
            <div class="modal-body">
                <form id="${modalId}Settings">
                    <div class="form-check">
                        <input class="form-check-input" type="checkbox" value="" id="${modalId}createFirstWork">
                        <label class="form-check-label" for="${modalId}createFirstWork">
                            ${createFirstWorkLabel}
                        </label>
                    </div>
                    <div class="form-check mt-1">
                        <input class="form-check-input" type="checkbox" value="" id="${modalId}alwaysUpdateWork">
                        <label class="form-check-label" for="${modalId}alwaysUpdateWork">
                            ${alwaysUpdateWorkLabel}
                        </label>
                    </div>
                    <div class="form-check mt-1">
                        <input class="form-check-input" type="checkbox" value="" id="${modalId}createDuplicateWork">
                        <label class="form-check-label" for="${modalId}createDuplicateWork">
                            ${createDuplicateWorkLabel}
                        </label>
                    </div>
                    <div class="form-check mt-1">
                        <input class="form-check-input" type="checkbox" value="" id="${modalId}recreateDeletedWork">
                        <label class="form-check-label" for="${modalId}recreateDeletedWork">
                            ${recreateDeletedWorkLabel}
                        </label>
                    </div>
                </form>
            </div>
            <!-- Footer -->
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary cancel" data-dismiss="modal">${cancel}</button>
                <button type="button" class="btn btn-primary save">${save}</button>
            </div>
        </div>
    </div>
</div>
`;
            window.document.body.appendChild(modal);

            let $modelJq = $(`#${modalId}`);

            let alwaysUpdateWorkElement = modal.querySelector(`#${modalId}alwaysUpdateWork`);
            alwaysUpdateWorkElement.checked = current.alwaysUpdateWork;

            let createDuplicateWorkElement = modal.querySelector(`#${modalId}createDuplicateWork`);
            createDuplicateWorkElement.checked = current.createDuplicateWork;

            let createFirstWorkElement = modal.querySelector(`#${modalId}createFirstWork`);
            createFirstWorkElement.checked = current.createFirstWork;

            let recreateDeletedWorkElement = modal.querySelector(`#${modalId}recreateDeletedWork`);
            recreateDeletedWorkElement.checked = current.recreateDeletedWork;

            let saveButton = modal.querySelector(".save");
            let resolved = false;
            saveButton.addEventListener("click", () => {
                let settings = {
                    alwaysUpdateWork: alwaysUpdateWorkElement.checked,
                    createDuplicateWork: createDuplicateWorkElement.checked,
                    createFirstWork: createFirstWorkElement.checked,
                    recreateDeletedWork: recreateDeletedWorkElement.checked
                }

                if (current.alwaysUpdateWork !== settings.alwaysUpdateWork ||
                    current.createDuplicateWork !== settings.createDuplicateWork ||
                    current.createFirstWork !== settings.createFirstWork ||
                    current.recreateDeletedWork !== settings.recreateDeletedWork) {
                    resolved = true;
                    resolve(settings);
                } else {
                    reject();
                }

                $modelJq.modal("hide");
                modal.remove();
            });

            $modelJq.modal();
            $modelJq.modal("show");

            $modelJq.on('hidden.bs.modal', function () {
                if (!resolved) {
                    reject();
                    modal.remove();
                }
            });

        });
    }

    /**
     * Translates the given key.
     * @param baseURL The base URL of the server.
     * @param lang The language to translate to.
     * @param key The key to translate.
     * @returns {Promise<string>} the translated key.
     */
    static async translate(baseURL, lang, key) {
        const requestURL = `${baseURL}rsc/locale/translate/${lang}/${key}`;
        const response = await fetch(requestURL);
        return await response.text();
    }

}

window.addEventListener('DOMContentLoaded', async () => {
    let orcidList = document.querySelector(".orcid-list-gui");
    if (!orcidList) {
        return;
    }


    const baseURL = window.webApplicationBaseURL;
    const lang = window.currentLang;

    const [revokeText,
        revokeConfirmText,
        cancelText,
        unlinkSuccessText,
        unlinkErrorText,
        okText,
        settingsSuccessText,
        settingsErrorText,
        settingsText
    ] = await Promise.all([
        OrcidProfileGUI.translate(baseURL, lang, "orcid.integration.unlink"),
        OrcidProfileGUI.translate(baseURL, lang, "orcid.integration.unlink.confirmation"),
        OrcidProfileGUI.translate(baseURL, lang, "button.cancel"),
        OrcidProfileGUI.translate(baseURL, lang, "orcid.integration.unlink.success"),
        OrcidProfileGUI.translate(baseURL, lang, "orcid.integration.unlink.error"),
        OrcidProfileGUI.translate(baseURL, lang, "button.ok"),
        OrcidProfileGUI.translate(baseURL, lang, "orcid.integration.settings.success"),
        OrcidProfileGUI.translate(baseURL, lang, "orcid.integration.settings.error"),
        OrcidProfileGUI.translate(baseURL, lang, "orcid.integration.settings.title"),
    ]);

    const userStatus = await OrcidProfile.getUserStatus(baseURL);

    // Add the orcid list to the page.
    userStatus.trustedOrcids.forEach(orcid => {
        let orcidListElement = document.createElement("li");

        let orcidTextElement = document.createElement("span");
        orcidTextElement.textContent = orcid;
        orcidListElement.appendChild(orcidTextElement);
        orcidList.appendChild(orcidListElement);

        orcidListElement.classList.add("trusted");

        // make the trusted orcid list revoke able.
        let revokeButton = document.createElement("button");

        let cl = revokeButton.classList;
        cl.add("btn");
        cl.add("btn-primary");
        cl.add("btn-xs");
        cl.add("ml-2");

        revokeButton.textContent = revokeText;
        revokeButton.addEventListener("click", async () => {
            const revokeConfirm = await OrcidProfileGUI.bootstrapConfirm(revokeText,
                revokeConfirmText.replaceAll("{0}", orcid),
                cancelText,
                okText);
            if (revokeConfirm) {
                let revoked = await OrcidProfile.revokeOrcid(baseURL, orcid);
                if (revoked) {
                    orcidListElement.classList.remove("trusted");
                    revokeButton.remove();
                }
                await OrcidProfileGUI.bootstrapConfirm(revokeText,
                    (revoked ? unlinkSuccessText : unlinkErrorText).replaceAll("{0}", orcid),
                    null,
                    okText);
                window.location.reload();
            }
        });
        orcidListElement.appendChild(revokeButton);

        let settingsButton = document.createElement("button");
        cl = settingsButton.classList;
        cl.add("btn");
        cl.add("btn-secondary");
        cl.add("btn-xs");
        cl.add("ml-2");

        orcidListElement.appendChild(settingsButton);
        settingsButton.textContent = settingsText;
        settingsButton.addEventListener("click", async () => {
            let currentSettings = await OrcidProfile.getUserProperties(baseURL, orcid);
            let newSettings = await OrcidProfileGUI.settingsDialog(baseURL, lang, currentSettings);
            if (newSettings) {
                const success = await OrcidProfile.setUserProperties(baseURL, orcid, newSettings);
                await OrcidProfileGUI.bootstrapConfirm(settingsText, success ? settingsSuccessText : settingsErrorText, null, okText);
            }
        });
    });
});


