
export interface StyleDescription {
  id: string,
  title: string
}

export interface PropResponse {
  "MCR.user2.matching.lead_id"?: string;
  "UBO.Search.PersonalList.Roles"?: string;
  "UBO.Editor.PartOf.Enabled"?: string;
  "UBO.Search.PersonalList.Ids"?: string;
}

export interface SearchModel {
  text: string,
  searchResultUsers: User[],
  searching: boolean,
  noresults: boolean,
  errored: boolean
}

export type SortField = { active: boolean, field: string, asc: boolean, i18nKey: string };

export interface ExportModel {
  format: string;
  style: string;
  sort: SortField[];
  year: string,
  partOf: boolean
  yearPeriod: false,
  yearFrom: string,
  yearTo: string
}



export interface User {
  name: string;
  pid: string;
  otherIds: Identifier;
}

export type Identifier = Record<string, string[]>;
