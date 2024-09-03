const ModsDisplayUtils = {
  updateLabel: async function (target, i18n) {
    let response = await fetch(webApplicationBaseURL + "rsc/locale/translate/" + $("html").attr("lang") + "/" + i18n);
    let text = await response.text();
    $(target).text(text.replace("{0}", $(target).attr("data-hideable-count")));
  },

  expand: function (source) {
    $('.personalName.d-none').removeClass('d-none').addClass('ubo-hideable');
    $(source).attr("onclick", "ModsDisplayUtils.collapse(this)");
    ModsDisplayUtils.updateLabel(source, "button.hide.authors");
  },

  collapse: function (source) {
    $('.personalName.ubo-hideable').removeClass('ubo-hideable').addClass('d-none');
    $(source).attr("onclick", "ModsDisplayUtils.expand(this)");
    ModsDisplayUtils.updateLabel(source, "button.view.all.authors");
  }
}

$(document).ready(function () {
  $("img[data-src]").each(function () {
    $(this).attr("src", $(this).attr("data-src"));
  })
});
