package com.ksyun.campus.client;

public class TestFunc {
    public static void main(String[] args) {
        EFileSystem eFileSystem = new EFileSystem();
        boolean res = eFileSystem.mkdir("/abc/def/");
        System.out.println(res);
    }
}
