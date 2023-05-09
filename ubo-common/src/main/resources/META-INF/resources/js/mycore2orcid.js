const orcidObjectStatusURL = webApplicationBaseURL + "rsc/orcid/objectStatus/";
const orcidUserStatusURL = webApplicationBaseURL + "rsc/orcid/userStatus/";
const orcidPublishURL = webApplicationBaseURL + "rsc/orcid/publish/";
const orcidIcon = "<img alt='ORCID iD' src='" + webApplicationBaseURL + "images/orcid_icon.svg' class='orcid-icon' />";

let orcidI18n;
let userStatus = {orcids: [], trustedOrcids: []};

$(document).ready(async function () {
    let orcidI18nURL = webApplicationBaseURL + "rsc/locale/translate/" + currentLang + "/orcid.publication.*";

    // load i18n key/values
    await fetch(orcidI18nURL)
        .then(data => data.json())
        .then(json => orcidI18n = json)
        .catch(error => console.error(error));

    await updateUI();
});

async function updateUI() {
    // get user status
    console.info("Getting ORCID user status...");
    const userStatusResp = await fetch(orcidUserStatusURL);
    if (!userStatusResp.ok) {
        return;
    }

    // if user has no orcid at all do not display orcid icons
    userStatus = await userStatusResp.json();
    if (userStatus.orcids.length == 0) {
        console.info("Current user does not have any orcid. Nothing to do.");
        return;
    }

    $('div.orcid-status').each(function () {
        getORCIDPublicationStatus(this);
    });

    $('div.orcid-publish').each(function () {
        showORCIDPublishButton(this);
    });
}

async function getORCIDPublicationStatus(div) {
    console.info("Fetching publication status");
    let id = $(div).data('id');
    let url = orcidObjectStatusURL + id;

    const response = await fetch(url);

    if (!response.ok) {
        return;
    }
    const objectStatus = await response.json();
    setORCIDPublicationStatus(div, objectStatus);
}

function setORCIDPublicationStatus(div, objectStatus) {
    console.info("Setting publication status");
    $(div).empty();

    if (objectStatus.usersPublication) {
        let html = "<span class='orcid-info' title='" + orcidI18n[
            (objectStatus.isInORCIDProfile ? 'orcid.publication.inProfile.true' : 'orcid.publication.inProfile.false')
            ] + "'>";
        html += orcidIcon;
        html += "<span class='far fa-thumbs-" + (objectStatus.inORCIDProfile ? "up" : "down")
            + " orcid-in-profile-" + objectStatus.inORCIDProfile + "' aria-hidden='true' />";
        html += "</span>";
        $(div).html(html);
    }
}

async function showORCIDPublishButton(div) {
    console.info("Showing ORCID publish button.");
    let id = $(div).data('id');
    let url = orcidObjectStatusURL + id;

    const objectStatusResponse = await fetch(url);
    if (!objectStatusResponse.ok) {
        return;
    }

    const objectStatus = await objectStatusResponse.json();
    updateORCIDPublishButton(div, objectStatus);
}

function updateORCIDPublishButton(div, objectStatus) {
    let id = $(div).data('id');
    $(div).empty();

    if (userStatus.trustedOrcids.length > 0 && objectStatus.usersPublication) {
        let html = "<button class='orcid-button btn btn-sm btn-outline-primary'>" +
            orcidI18n[(objectStatus.inORCIDProfile ? 'orcid.publication.action.update' : 'orcid.publication.action.create')] +
            "</button>";
        $(div).html(html);

        $(div).find('.orcid-button').click(async function () {
            div = this;

            const resp = await fetch(orcidPublishURL + id)
                .then(resp => alert(orcidI18n['orcid.publication.action.confirmation']))
                .then(() => updateUI())
                .catch(error => {
                    console.error(error);
                });
        });
    }
}
