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

const cache: { [key: string]: string | null | Promise<string|null>; } = {};

export function resolveiI18N(baseurl: string, i18n: { [key: string]: string | null; }): void {
  Object.keys(i18n)
    .map(k => {
      if (cache[k] !== undefined) {
        if(cache[k] instanceof Promise) {
          cache[k].then(translation => {
            i18n[k] = translation;
          });
          return;
        }
        i18n[k] = cache[k];
        return;
      }
      const url = baseurl + "rsc/locale/translate/" + k;
      const promise = fetch(url)
        .then(response => {
          if (!response.ok) {
            return null;
          }
          return response.text();
        });
      cache[k] = promise;
      promise.then(translation => {
        i18n[k] = translation;
        cache[k] = translation;
      });
    });
}
