package com.ksyun.campus.client;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FSInputStream extends InputStream {

    private String path;
    private FileSystem fileSystem;
    private String dataUrl;
    private int filePos;

    private List<Byte> readBuffer = new ArrayList<>();
    private int readBufferPos;

    public FSInputStream(String path, FileSystem fileSystem, String dataUrl) {
        this.path = path;
        this.fileSystem = fileSystem;
        this.dataUrl = dataUrl;
        this.filePos = 0;
        this.readBufferPos = 0;
    }

    @Override
    public int read() throws IOException {
        loadReadBuffer(1);
        if (readBufferPos == -1) {
            cleanUpReadBuffer();
            return -1;
        } else {
            int res = readBuffer.get(readBufferPos - 1);
            cleanUpReadBuffer();
            return res;
        }
    }

    @Override
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        loadReadBuffer(len);

        System.out.println(readBufferPos);
        if (readBufferPos == -1) {
            cleanUpReadBuffer();
            return -1;
        } else {
            if (readBufferPos == len) {
                for (int i = off; i < len + off; i++) {
                    b[i] = readBuffer.get(i - off);
                }
            } else {
                for (int i = off; i < readBufferPos + off; i++) {
                    b[i] = readBuffer.get(i - off);
                }
            }
            int res = readBufferPos;
            cleanUpReadBuffer();
            return res;
        }
    }

    public void loadReadBuffer(int length) throws IOException {
        HashMap<String, Object> data = new HashMap<>();
        data.put("path", path);
        data.put("offset", filePos);
        data.put("length", length);

        ResponseEntity<String> response = fileSystem.sendPostRequest(dataUrl, "read", data, String.class);
        System.out.println(response.getBody());

        if (response.getStatusCode() == HttpStatus.OK) {
            String str = response.getBody();
//            String trimmedStr = response.getBody().substring(1, str.length() - 1);
//
//            // 按逗号分割字符串，得到每个数字的字符串
//            String[] stringValues = trimmedStr.split("\\s*,\\s*");
//
//            // 转换为字节数组
//            byte[] byteArray = new byte[stringValues.length];


            // 转换每个数字为字节
//            for (int i = 0; i < stringValues.length; i++) {
//                byteArray[i] = Byte.parseByte(stringValues[i]);
//            }

//            if (byteArray.length > 0) {
            if (str != null) {
                byte[] byteArray = str.getBytes();
                int count = 0;
                for (byte b : byteArray) {
                    readBuffer.add(b);
                    count++;
                }
                readBufferPos = count;
                filePos += byteArray.length; // 更新已读取的位置
            } else {
                readBufferPos = -1;
            }
        }
    }

    public void cleanUpReadBuffer() {
        readBufferPos = 0;
        readBuffer.clear();
    }

    @Override
    public void close() throws IOException {
        cleanUpReadBuffer();
        super.close();
    }
}
