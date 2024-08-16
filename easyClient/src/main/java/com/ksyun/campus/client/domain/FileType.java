package com.ksyun.campus.client.domain;

import lombok.Getter;

@Getter
public enum FileType
{
    Unknown(0),  Volume(1),  File(2),  Directory(3);

    private final int code;
    FileType(int code) {
        this.code=code;
    }

    public static FileType get(int code){
        switch (code){
            case 1:
                return Volume;
            case 2:
                return File;
            case 3:
                return Directory;
            default:
                return Unknown;
        }
    }
}
