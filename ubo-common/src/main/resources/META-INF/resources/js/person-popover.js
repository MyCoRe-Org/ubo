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
$( document ).ready(function() {
    $(".personPopover").each(function (i, popoverElement) {
        let id = popoverElement.getAttribute("id");
        let contentID = id + "-content";
        let content$ = $("#" + contentID);
        content$.detach();
        content$.removeClass("d-none");
        popoverElement.setAttribute("title", popoverElement.getAttribute("title") + '<div class="popoverclose btn btn-xs"><i class="fa fa-times"></i></div>');
        $(popoverElement).popover({
            content: content$,
            html: true
        })
    });

    $("body").on("click", ".popoverclose", function(e){
        $(this).parents(".popover").popover("hide");
    });
});