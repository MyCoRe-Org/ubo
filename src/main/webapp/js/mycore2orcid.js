var orcidStatusURL  = webApplicationBaseURL + "rsc/orcid/status/";
var orcidPublishURL = webApplicationBaseURL + "rsc/orcid/publish/";

var orcidIcon = "<img alt='ORCID iD' src='" + webApplicationBaseURL + "images/orcid_icon.svg' class='orcid-icon' />";

var orcidI18n;

jQuery(document).ready(function() {
	var orcidI18nURL = webApplicationBaseURL + "rsc/locale/translate/" + currentLang + "/orcid.publication.*";
	jQuery.getJSON(orcidI18nURL, function(data) {
		orcidI18n = data;
	});
	
	jQuery('div.orcid-status').each(function() {
		getORCIDPublicationStatus(this);
	});
	jQuery('div.orcid-publish').each(function() {
		showORCIDPublishButton(this);
	});
});

function getORCIDPublicationStatus(div) {
	var id = jQuery(div).data('id');
	jQuery.get(orcidStatusURL + id, function(status) {
		console.log(status);
		setORCIDPublicationStatus(div, status);
	});
}

function setORCIDPublicationStatus(div, status) {
	jQuery(div).empty();
	if (status.user.isORCIDUser && status.isUsersPublication) {
		var html = "<span class='orcid-info' title='" + orcidI18n[ 
			(status.isInORCIDProfile ? 'orcid.publication.inProfile.true' : 'orcid.publication.inProfile.false')
			]	+ "'>";
		html += orcidIcon;
		html += "<span class='glyphicon glyphicon-thumbs-" + (status.isInORCIDProfile ? "up" : "down") 
		        + " orcid-in-profile-" + status.isInORCIDProfile + "' aria-hidden='true' />";
		html += "</span>";
		jQuery(div).html(html);
	}
}

function showORCIDPublishButton(div) {
	var id = jQuery(div).data('id');
	jQuery.get(orcidStatusURL + id, function(status) {
		console.log(status);
		updateORCIDPublishButton(div, status);
	});
}

function updateORCIDPublishButton(div, status) {
	var id = jQuery(div).data('id');
	jQuery(div).empty();
	if (status.user.isORCIDUser && status.user.weAreTrustedParty && status.isUsersPublication) {
		var html = "<button class='orcid-button'>" + orcidI18n[
			(status.isInORCIDProfile ? 'orcid.publication.action.update' : 'orcid.publication.action.create')
			] + "</button>";
		jQuery(div).html(html);
		jQuery(div).find('.orcid-button').click( function() {
					div = this;
					jQuery.get(orcidPublishURL + id, function(newStatus) {
						alert(orcidI18n['orcid.publication.action.confirmation']);
						jQuery("div.orcid-status[data-id='" + id + "']").each(
								function() {
									setORCIDPublicationStatus(this, newStatus);
								});
						jQuery("div.orcid-publish[data-id='" + id + "']").each(
								function() {
									updateORCIDPublishButton(this, newStatus);
								});
					});
				});
	}
}
