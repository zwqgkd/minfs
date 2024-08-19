package com.ksyun.campus.client;

import com.ksyun.campus.client.domain.StatInfo;

import java.util.List;

public class TestFunc {
    public static void main(String[] args) throws Exception {
        EFileSystem eFileSystem = new EFileSystem();
        boolean res = eFileSystem.mkdir("/read/123/");
        System.out.println(res);
        boolean res1 = eFileSystem.mkdir("/read/456/");
        System.out.println(res1);

        // System.out.println(eFileSystem.getClusterInfo().toString());
        System.out.println(eFileSystem.getFileStats("/read"));
        List<StatInfo> ll = eFileSystem.listFileStats("/read");
        for (StatInfo statInfo : ll) {
            System.out.println(statInfo);
        }

        FSOutputStream fsOutputStream = eFileSystem.create("/read/test.txt");
        for (int i = 0; i < 1; ++i) {
            fsOutputStream.write("abcv".getBytes());
        }
        fsOutputStream.close();

        FSInputStream fsInputStream = eFileSystem.open("/read/test.txt");

        int byteValue;
        int count = 5;
        while (count > 0 && (byteValue = fsInputStream.read()) != -1 ) {
            System.out.println((char) byteValue);
            count--;
        }

        byte[] buffer = new byte[60];

        int bytesRead;
        bytesRead = fsInputStream.read(buffer); // 最多读取5个字节

        if (bytesRead != -1) {
            String data = new String(buffer, 0, bytesRead);
            System.out.println("Read data: " + data);
        } else {
            System.out.println("No more data to read.");
        }
        fsInputStream.close();

//        FSOutputStream fsOutputStream = eFileSystem.create("/abc/123/rrrr.txt");
//        for (int i = 0; i < 1; ++i) {
//            fsOutputStream.write("abcv".getBytes());
//        }
//        fsOutputStream.close();
        System.out.println("////////////");
    }
}
