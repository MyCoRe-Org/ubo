


export interface MODSMyCoReObject {
  "@xsi:noNamespaceSchemaLocation": string;
  "@ID": string;
  "@version": string;
  "@label": string;
  structure: MODSMyCoReObjectStructure;
  metadata: MODSMyCoReObjectMetadata;
  service: MODSMyCoReObjectService;
}

export interface MODSMyCoReObjectStructure {
  parents: {
    "@class": string;
    parent: {
      "@inherited": number;
      "@type": string;
      "@href": string;
    };
  };
}

export interface MODSMyCoReObjectMetadata {
  "def.modsContainer": {
    "@class": string;
    "@heritable": boolean;
    "@notinherit": boolean;
    modsContainer: {
      "@inherited": number;
      "mods": MODSMods;
    };
  };
}

export interface MODSMods {
  "genre"?: MODSGenre[];
  "titleInfo"?: MODSTitleInfo[];
  "name"?: MODSName[];
  "relatedItem"?: MODSRelatedItem[];
  "identifier"?: MODSIdentifier[];
  "originInfo"?: MODSOriginInfo[];
  "subject"?: MODSSubject[];
  "language"?: MODSLanguage[];
  "extension"?: MODSExtension[];
  "classification"?: MODSClassification[];
  "typeOfResource"?: MODSTypeOfResource[];
}

export interface MODSGenre {
  "@authorityURI": string;
  "@valueURI": string;
  "@type": string;
}

export interface MODSTitleInfo {
  "@xml:lang": string;
  "title": MODSTextObject[];
  "subTitle"?: MODSTextObject[];
}

export interface MODSName {
  "@type": string;
  "namePart": MODSNamePart[];
  "nameIdentifier"?: MODSNameIdentifier[];
  "role"?: MODSRole[];
  "affiliation"?: MODSTextObject[];
}

export interface MODSNamePart {
  "@type": string;
  text?: string;
  "$index"?: number;
}

export interface MODSNameIdentifier {
  "@type": string;
  text?: string;
  "$index"?: number;
}

export interface MODSRole {
  "roleTerm": MODSRoleTerm[];
  "$index"?: number;
}

export interface MODSRoleTerm {
  "@authority": string;
  "@type": string;
  text: string;
}

export interface MODSRelatedItem {
  "@type": string;
  "@href": string;
  "part"?: MODSPart[];
  "genre"?: MODSGenre[];
  "titleInfo"?: MODSTitleInfo[];
  "originInfo"?: MODSOriginInfo[];
  "identifier"?: MODSIdentifier[];
  "typeOfResource"?: MODSTypeOfResource[];
  "extension"?: MODSExtension[];
  "classification"?: MODSClassification[];
}

export interface MODSPart {
  "detail": MODSDetail[];
}

export interface MODSDetail {
  "@type": string;
  "number": MODSTextObject[];
}

export interface MODSIdentifier {
  "@type": string;
  text: string;
}

export interface MODSOriginInfo {
  "dateIssued"?: MODSDateIssued[];
  "publisher"?: MODSTextObject[];
}

export interface MODSDateIssued {
  "@encoding": string;
  text: string;
}

export interface MODSSubject {
  "topic": MODSTextObject[];
}

export interface MODSLanguage {
  "languageTerm": MODSLanguageTerm[];
}

export interface MODSLanguageTerm {
  "@type": string;
  "@authority": string;
  text: string;
}

export interface MODSExtension {
  "$content": any[];
}

export interface MODSClassification {
  "@valueURI": string;
  "@authorityURI": string;
}

export interface MODSTypeOfResource {
  text: string;
}

export interface MODSTextObject {
  text: string;
  "$index"?: number;
}

export interface MODSMyCoReObjectService {
  servdates: {
    "@class": string;
    servdate: MODSServDate[];
  };
  servflags: {
    "@class": string;
    servflag: MODSServFlag[];
  };
}

export interface MODSServDate {
  "@type": string;
  "@inherited": number;
  text: string;
}

export interface MODSServFlag {
  "@type": string;
  "@inherited": number;
  "@form": string;
  text: string;
}

