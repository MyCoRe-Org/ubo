const orcidObjectStatusURL = webApplicationBaseURL + "api/orcid/v1/member/{orcid}/works/object/";
const orcidUserStatusURL = webApplicationBaseURL + "api/orcid/v1/user-status/";
const orcidPublishURL = webApplicationBaseURL + "api/orcid/v1/member/{orcid}/works/object/";

let orcidI18n;
let userStatus = {orcids: [], trustedOrcids: []};

$(document).ready(async function () {
    const jwt = await fetchJWT();
    const headers = function build(httpMethod) {
        return {
            method: httpMethod,
            headers: {Authorization: `Bearer ${jwt}`}
        };
    }

    // load i18n key/values
    let orcidI18nURL = webApplicationBaseURL + "rsc/locale/translate/" + currentLang + "/orcid.publication.*";
    await fetch(orcidI18nURL)
        .then(data => data.json())
        .then(json => orcidI18n = json)
        .catch(error => console.error(error));

    await updateUI(headers);
});


async function updateUI(headers) {
    // get user status
    console.debug("Getting ORCID user status...");
    const userStatusResp = await fetch(orcidUserStatusURL, headers('GET'));
    if (!userStatusResp.ok) {
        return;
    }

    // if user has no orcid at all do not display orcid icons
    userStatus = await userStatusResp.json();
    if (userStatus.orcids.length == 0) {
        console.debug("Current user does not have any orcid. Nothing to do.");
        return;
    }

    $('div.orcid-status').each(function () {
        getORCIDPublicationStatus(this, headers);
    });

    $('div.orcid-publish').each(function () {
        showORCIDPublishButton(this, headers);
    });
}

async function getORCIDPublicationStatus(div, headers) {
    let id = $(div).data('id');
    let url = orcidObjectStatusURL.replace("{orcid}", userStatus.trustedOrcids[0]) + id;

    console.debug(id + " Fetching publication/object status");

    const response = await fetch(url, headers('GET'));

    if (!response.ok) {
        return;
    }
    const objectStatus = await response.json();
    console.debug(id + " Publication/object status is: ");
    console.debug(objectStatus);
    setORCIDPublicationStatus(id, div, objectStatus);
}

function setORCIDPublicationStatus(id, div, objectStatus) {
    console.debug(id + " Setting publication status icon");
    div.innerHTML = '';
    let text = orcidI18n[(objectStatus.hasOwnProperty("own")? 'orcid.publication.inProfile.true' : 'orcid.publication.inProfile.false')];
    let span = document.createElement("span");
    span.title = text;
    span.textContent = text;
    span.classList.add('orcid-info');
    span.classList.add('orcid-in-profile-' + objectStatus.hasOwnProperty("own"));
    span.classList.add('badge');
    span.classList.add('badge-' + (objectStatus.hasOwnProperty("own") ? "success" : "secondary"));
    span.classList.add('badge-orcid-in-profile-' + objectStatus.hasOwnProperty("own"));
    div.appendChild(span);
}

async function showORCIDPublishButton(div, headers) {
    let id = $(div).data('id');
    let url = orcidObjectStatusURL.replace("{orcid}", userStatus.trustedOrcids[0]) + id;

    console.debug(id + " Showing ORCID publish button");

    const objectStatusResponse = await fetch(url, headers('GET'));
    if (!objectStatusResponse.ok) {
        return;
    }

    const objectStatus = await objectStatusResponse.json();

    if (objectStatus.hasOwnProperty("own") == true) {
        console.debug(id + " Publication is already in profile of current user");
    }
    updateORCIDPublishOrUpdateButton(div, objectStatus, headers);
}

function updateORCIDPublishOrUpdateButton(div, objectStatus, headers) {
    let id = $(div).data('id');
    $(div).empty();

    let isInProfile= objectStatus.hasOwnProperty("own");
    if (userStatus.trustedOrcids.length > 0) {
        let html = "<button class='orcid-button btn btn-sm btn-outline-secondary'>" +
            orcidI18n[(isInProfile ? 'orcid.publication.action.update' : 'orcid.publication.action.create')] +
            "</button>";
        $(div).html(html);

        $(div).find('.orcid-button').one("click", async function () {
            $(this).attr("disabled", "disabled");
            div = this;

            const resp = await fetch(orcidPublishURL.replace("{orcid}", userStatus.trustedOrcids[0]) + id, (isInProfile ? headers('PUT') : headers('POST')));

            if (resp.ok) {
                $("#notification-dialog-success").modal('show');
                await updateUI(headers);
            } else {
                $("#notification-dialog-fail").modal('show');
            }
        });
    }
}

async function fetchJWT() {
    const response = await fetch(`${webApplicationBaseURL}rsc/jwt`);
    if (!response.ok) {
        throw new Error(`Cannot fetch JWT: ${response.status}`);
    }
    const result = await response.json();
    if (!result.login_success) {
        throw new Error("Login failed");
    }
    return result.access_token;
}
