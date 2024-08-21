package com.ksyun.campus.client;

import com.ksyun.campus.client.domain.StatInfo;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.Random;

public class TestFunc {
    public static void main(String[] args) throws Exception {
        EFileSystem eFileSystem = new EFileSystem();

        System.out.println(eFileSystem.getClusterInfo());
//        FSOutputStream fsOutputStream1 = eFileSystem.create("/test/newDir/File1.txt");
//        fsOutputStream1.write("Hello World and ppppppppppppppppp!!!".getBytes(StandardCharsets.UTF_8));
//        fsOutputStream1.close();



        // mkdir功能
        eFileSystem.mkdir("/test/newDir/");
        eFileSystem.mkdir("/test/deleteDir/");

        // create功能
        FSOutputStream fsOutputStream1 = eFileSystem.create("/test/newDir/File1.txt");
        FSOutputStream fsOutputStream2 = eFileSystem.create("/test/deleteDir/File2.txt");
        fsOutputStream2.close();

        // write功能
        String charset = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()_+-=[]{}|;':,.<>/?`~";
        Random random = new Random();

        // 创建一个StringBuilder来生成字符串
        StringBuilder sb = new StringBuilder();

        // 根据需要的大小生成字符串
        int targetLength = 1 * 10 * 1024;
        while (sb.length() < targetLength) {
            sb.append(charset.charAt(random.nextInt(charset.length())));
        }

        // 将生成的字符串转换为UTF-8字节数组
        byte[] byteArray = sb.toString().getBytes(StandardCharsets.UTF_8);

        fsOutputStream1.write(byteArray);
        fsOutputStream1.close();

        // 计算MD5
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] md5Bytes = md.digest(byteArray);

        // 转换为十六进制字符串
        StringBuilder md5Hex = new StringBuilder();
        for (byte b : md5Bytes) {
            md5Hex.append(String.format("%02x", b));
        }

        System.out.println("write MD5: " + md5Hex.toString());

        FSInputStream fsInputStream = eFileSystem.open("/test/newDir/File1.txt");
        byte[] buf = new byte[1024];
        StringBuilder res = new StringBuilder();

        int bytesRead = 0;
        while ((bytesRead = fsInputStream.read(buf)) != -1) {
            System.out.println(bytesRead);
            String data = new String(buf, 0, bytesRead);
            System.out.println("Read data: " + data);
            res.append(data);
        }
        fsInputStream.close();

        // 计算MD5
        MessageDigest mdr = MessageDigest.getInstance("MD5");
        byte[] md5Bytesr = md.digest(res.toString().getBytes());

        // 转换为十六进制字符串
        StringBuilder md5Hexr = new StringBuilder();
        for (byte b : md5Bytesr) {
            md5Hexr.append(String.format("%02x", b));
        }

        System.out.println("read MD5: " + md5Hexr.toString());
        System.out.println("Compare write and read is : " + md5Hexr.toString().contentEquals(md5Hex));

        // delete功能
        eFileSystem.delete("/test/deleteDir/");

        // 有问题
        // System.out.println(eFileSystem.getClusterInfo().toString());
        System.out.println(eFileSystem.getFileStats("/test/newDir/File1.txt"));
        List<StatInfo> ll = eFileSystem.listFileStats("/test/");
        for (StatInfo statInfo : ll) {
            System.out.println(statInfo);
        }

        System.out.println("////////////");
    }
}
