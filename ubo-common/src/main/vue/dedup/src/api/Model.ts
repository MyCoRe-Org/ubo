export type DedupList = DedupListEntry[];

export interface DedupListEntry {
  mcrId1: string;
  mcrId2: string;
  deduplicationType: "ta" | "identifier";
  deduplicationKey: string;
}

export type NoDupList = NoDupListEntry[];

export interface NoDupListEntry {
  id: number;
  mcrId1: string;
  mcrId2: string;
  creator: string;
  date: string;
}
