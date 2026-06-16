import { request } from "./request";

export interface RegionNode {
  id: number;
  parentId: number;
  level: number;
  code?: string;
  name: string;
  sortNo: number;
  enabled: number;
  children?: RegionNode[];
}

export function fetchRegionTree() {
  return request<RegionNode[]>({ url: "/common/regions/tree", method: "GET" });
}

export function fetchRegionChildren(parentId: number) {
  return request<RegionNode[]>({
    url: `/common/regions/children?parentId=${parentId}`,
    method: "GET",
  });
}
