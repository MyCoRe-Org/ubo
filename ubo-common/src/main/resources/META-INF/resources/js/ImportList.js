$(document).ready(function () {
    $('select[class*="autocomplete"]').selectpicker({
      liveSearch:true,
      liveSearchNormalize:true,
      virtualScroll:true,
      showSubtext:true,
      size:10,
      width:'auto',
      dropupAuto: false
    });
    
});

const UBOImportList = {
    toggleEnrichment: function (select) {
        if ("PPNList DOIList".includes($(select).val())) {
            $("#enrich-yes").click();
            $("#enrich-no").attr("disabled", "disabled");
        } else {
            $("#enrich-no").removeAttr("disabled");
        }
    },

    toggleSelect: function (elementId, value) {
        let element = document.getElementById(elementId);

        if (element.getAttribute("disabled") == null) {
            element.setAttribute("disabled", "disabled");
            element.value = value;
        } else {
            element.removeAttribute("disabled");
            element.selectedIndex = 0;
        }
    },

    toggleSubmit: function () {
        let v = $("textarea").val();

        if (v === "undefined" || v.length == 0) {
            $("#submitBtn").attr("disabled", "disabled");
        } else {
            $("#submitBtn").removeAttr("disabled");
        }
    }
}
