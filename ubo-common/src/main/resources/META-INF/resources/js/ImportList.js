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

function toggleSubmit() {
    let v = $("textarea").val();

    if (v === "undefined" || v.length == 0) {
        $("#submitBtn").attr("disabled", "disabled");
    } else {
        $("#submitBtn").removeAttr("disabled");
    }
}
