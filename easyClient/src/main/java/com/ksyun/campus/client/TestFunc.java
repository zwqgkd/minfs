package com.ksyun.campus.client;

public class TestFunc {
    public static void main(String[] args) throws Exception {
        EFileSystem eFileSystem = new EFileSystem();
        boolean res = eFileSystem.mkdir("/abc/123/");
        System.out.println(res);
        FSOutputStream fsOutputStream = eFileSystem.create("/abc/123/rrrr.txt");
        for (int i = 0; i < 1; ++i) {
            fsOutputStream.write("abcv".getBytes());
        }
        fsOutputStream.close();
        System.out.println("////////////");
    }
}
