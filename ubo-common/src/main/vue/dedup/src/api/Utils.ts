import type {MODSMyCoReObject} from "@/api/ModsModel.ts";

export function getWebApplicationBaseURL(): string {
  if (import.meta.env.DEV) {
    return import.meta.env.BASE_URL;
  }
  if ((window as never)["webApplicationBaseURL"]) {
    return (window as never)["webApplicationBaseURL"];
  }
  throw "Fatal error: 'mycore.webApplicationBaseURL' is not set.";
}


export function getMCRApplicationBaseURL(): string {
  if (import.meta.env.DEV) {
    return "http://localhost:8291/ubo/"
  }
  return getWebApplicationBaseURL();
}


export function isDev(){
  return import.meta.env.DEV;
}

export function getMODSTitle (obj: MODSMyCoReObject) {
  const mods = obj.metadata["def.modsContainer"].modsContainer["mods"];
  const titleInfos = mods["titleInfo"];
  if (titleInfos && titleInfos.length > 0) {
    const titleInfo = titleInfos[0];
    const title = titleInfo["title"].map((t) => t.text).join(" ");
    const subTitle = titleInfo["subTitle"]?.map((t) => t.text).join(" ");
    if (subTitle) {
      return title + " - " + subTitle;
    } else {
      return title;
    }
  }
}

export function getMODSPersonName(obj: MODSMyCoReObject) {
  const mods = obj.metadata["def.modsContainer"].modsContainer["mods"];
  const modsPersons = mods["name"];
  if (modsPersons && modsPersons.length > 0) {
    const modsPerson = modsPersons[0];
    const givenPart = modsPerson.namePart.find(
      (part) => part["@type"] === "given"
    );
    const givenName = givenPart?.text ?? "";
    const familyPart = modsPerson.namePart.find(
      (part) => part["@type"] === "family"
    );
    const familyName = familyPart?.text ?? "";
    return familyName + ", " + givenName;
  }
}


export function getShortID(mcrId: string) {
  return parseInt(mcrId.split("_")[2]).toString();
}
