package com.ksyun.campus.metaserver.domain;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class StatInfo
{
    public String path;

    public long size;

    public long mtime;

    public FileType type;

    private List<ReplicaData> replicaData;
}
