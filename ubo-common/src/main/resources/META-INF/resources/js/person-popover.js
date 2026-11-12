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
document.addEventListener('DOMContentLoaded', async function () {
    document.querySelectorAll(".ubo-person-popover").forEach((popoverElement) => {
        const contentElement = document.getElementById(popoverElement.id + "-content");
        if (!contentElement) {
            return;
        }

        contentElement.remove();
        contentElement.classList.remove("d-none");

        popoverElement.setAttribute(
          "title",
          popoverElement.getAttribute("title") +
          '<div class="ubo-person-popover-close btn btn-xs">' +
          '<i class="fa fa-times"/>' +
          '</div>'
        );

        new bootstrap.Popover(popoverElement, {
            content: contentElement,
            html: true
        });
    });

    /** Since the close button lives inside the popover markup — which Bootstrap builds and destroys on every show/hide
     *  — you can't bind to it directly at init time. Use a delegated listener on the document instead
     *  */
    document.addEventListener("click", (event) => {
        const closeBtn = event.target.closest(".ubo-person-popover-close");
        if (!closeBtn) return;

        const tip = closeBtn.closest(".popover");
        if (!tip) return;

        const trigger = document.querySelector('[aria-describedby="' + tip.id + '"]');
        if (!trigger) {
            return;
        }
        bootstrap.Popover.getInstance(trigger)?.hide();
    });
});
