package com.ksyun.campus.metaserver.entry;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DataServerInfo {
    public String id;
    public String dsNode;
}
