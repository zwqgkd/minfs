package com.ksyun.campus.metaserver.domain;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class StatInfoWithFSName extends StatInfo{
    private String fileSystemName;

    public StatInfoWithFSName(StatInfo curStatInfo, String fileSystemName) {
        this.path = curStatInfo.getPath();
        this.size = curStatInfo.getSize();
        this.mtime = curStatInfo.getMtime();
        this.type = curStatInfo.getType();
        this.setReplicaData(curStatInfo.getReplicaData());
        this.fileSystemName = fileSystemName;
    }
}
