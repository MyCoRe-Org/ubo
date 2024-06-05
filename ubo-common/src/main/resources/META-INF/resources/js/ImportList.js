$(document).ready(function () {
    $('select[class*="autocomplete"]').chosen({search_contains: true});
});

function toggleEnrichment(select) {
    if ("PPNList DOIList".includes($(select).val())) {
        $("#enrich-yes").click();
        $("#enrich-no").attr("disabled", "disabled");
    } else {
        $("#enrich-no").removeAttr("disabled");
    }
}

function toggleSelect(elementId, value) {
    let element = document.getElementById(elementId);

    if (element.getAttribute("disabled") == null) {
        element.setAttribute("disabled", "disabled");
        element.value = value;
    } else {
        element.removeAttribute("disabled");
        element.selectedIndex = 0;
    }
}

function toggleSubmit() {
    let v = $("textarea").val();

    if (v === "undefined" || v.length == 0) {
        $("#submitBtn").attr("disabled", "disabled");
    } else {
        $("#submitBtn").removeAttr("disabled");
    }
}
