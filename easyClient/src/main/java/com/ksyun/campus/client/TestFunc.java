package com.ksyun.campus.client;

import com.ksyun.campus.client.domain.StatInfo;

import java.util.List;
import java.util.Arrays;

public class TestFunc {
    public static void main(String[] args) throws Exception {
        EFileSystem eFileSystem = new EFileSystem();

        // delete功能
        eFileSystem.delete("/read/anothermsg.txt");

        FSOutputStream fsOutputStream = eFileSystem.create("/read/anothermsg.txt");
        byte[] buf = new byte[1024];
        Arrays.fill(buf, (byte) 114);

        fsOutputStream.write(buf);
        fsOutputStream.close();

        FSInputStream fsInputStream = eFileSystem.open("/read/anothermsg.txt");

        int bytesRead = 0;
        while ((bytesRead = fsInputStream.read()) != -1) {
            System.out.print(bytesRead);
        }
        System.out.println();
        fsInputStream.close();

//        boolean res = eFileSystem.mkdir("/read/123/");
//        System.out.println(res);
//        boolean res1 = eFileSystem.mkdir("/read/456/");
//        System.out.println(res1);
//        boolean res1 = eFileSystem.mkdir("/read/123");
//        boolean res2 = eFileSystem.mkdir("/read/1234");
//        FSOutputStream fsOutputStream = eFileSystem.create("/read/anotherTest2.txt");

//        byte[] buf = new byte[1024];
//        Arrays.fill(buf, (byte) 100);
//
//        fsOutputStream.write(buf);
//        fsOutputStream.close();

//        FSInputStream fsInputStream = eFileSystem.open("/read/anotherTest2.txt");
//        byte[] buffer = new byte[300];
//
//        for (int i = 0; i < 5; i++) {
//            int bytesRead;
//            bytesRead = fsInputStream.read(buffer); // 最多读取5个字节
//
//            if (bytesRead != -1) {
//                String data = new String(buffer, 0, bytesRead);
//                System.out.println("Read data: " + data);
//            } else {
//                System.out.println("No more data to read.");
//            }
//        }
//        fsInputStream.close();

//        eFileSystem.delete("/readd/");
//        eFileSystem.delete("/anotherTest2.txt");
//        FSOutputStream fsOutputStream = eFileSystem.create("/anotherTest2.txt");
//        byte[] buf = new byte[256];
//        Arrays.fill(buf, (byte) 107);f
//
//        fsOutputStream.write(buf);
//        fsOutputStream.close();

//        FSInputStream fsInputStream = eFileSystem.open("/read/anotherTest.txt");
//        byte[] buffer = new byte[300];
//
//        int bytesRead;
//        bytesRead = fsInputStream.read(buffer); // 最多读取5个字节
//
//        if (bytesRead != -1) {
//            String data = new String(buffer, 0, bytesRead);
//            System.out.println("Read data: " + data);
//        } else {
//            System.out.println("No more data to read.");
//        }
//        fsInputStream.close();

        // System.out.println(eFileSystem.getClusterInfo().toString());
//        System.out.println(eFileSystem.getFileStats("/read"));
//        List<StatInfo> ll = eFileSystem.listFileStats("/read");
//        for (StatInfo statInfo : ll) {
//            System.out.println(statInfo);
//        }

//        FSOutputStream fsOutputStream = eFileSystem.create("/read/test.txt");
//        for (int i = 0; i < 1; ++i) {
//            fsOutputStream.write("abcv".getBytes());
//        }
//        fsOutputStream.close();
//
//        FSInputStream fsInputStream = eFileSystem.open("/read/test.txt");
//
//        int byteValue;
//        int count = 5;
//        while (count > 0 && (byteValue = fsInputStream.read()) != -1 ) {
//            System.out.println((char) byteValue);
//            count--;
//        }
//
//        byte[] buffer = new byte[60];
//
//        int bytesRead;
//        bytesRead = fsInputStream.read(buffer); // 最多读取5个字节
//
//        if (bytesRead != -1) {
//            String data = new String(buffer, 0, bytesRead);
//            System.out.println("Read data: " + data);
//        } else {
//            System.out.println("No more data to read.");
//        }
//        fsInputStream.close();

//        FSOutputStream fsOutputStream = eFileSystem.create("/abc/123/rrrr.txt");
//        for (int i = 0; i < 1; ++i) {
//            fsOutputStream.write("abcv".getBytes());
//        }
//        fsOutputStream.close();
        System.out.println("////////////");
    }
}
