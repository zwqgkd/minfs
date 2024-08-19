package com.ksyun.campus.dataserver.services;

import com.ksyun.campus.dataserver.domain.DataServerInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

@Service
public class DataService {

    @Autowired
    private RegistService registService;

    public int write(String fileSystemName, String path, int offset, int length, byte[] data){
        try{

            // 写入数据为空
            if (data == null) {
                System.out.println("Data array is null, skipping write operation.");
                return 1;
            }

            // 检查 offset 是否合理
            if (offset < 0 || offset >= data.length) {
                System.out.println("Offset is out of bounds, skipping write operation.");
                return 2;
            }

            // 检查 length 是否合理
            if (length < 0) {
                System.out.println("Length cannot be negative, skipping write operation.");
                return 3;
            }

            // 计算实际可写入的字节数
            int writableLength = Math.min(length, data.length - offset);

            if (writableLength <= 0) {
                System.out.println("No data available for writing, skipping write operation.");
                return 4;
            }

            File file = new File(fileSystemName + "/" + path);
            // 确保父目录存在
            file.getParentFile().mkdirs();
            // 确保目标文件存在
            file.createNewFile();

            FileOutputStream fos = new FileOutputStream(file, true);
            fos.write(data, offset, writableLength);
            fos.close();
            System.out.println("Data written successfully.");

            // 更新 zk 侧 ds 的数据
            DataServerInfo currentNodeData = registService.getServerInfo();
            currentNodeData.setUseCapacity(currentNodeData.getUseCapacity() + writableLength);
            registService.updateServerInfo(currentNodeData);

            return 0;
        }catch (Exception e){
            e.printStackTrace();
            return -1;
        }

    }
    public byte[] read(String fileSystemName ,String path,int offset,int length){
        try{
            File file = new File(fileSystemName + "/" + path);

            // 检查文件是否存在并且可读
            if (!file.exists() || !file.canRead()) {
                System.out.println("File does not exist or cannot be read.");
                return null;
            }


            // 检查 length 是否合理
            if (length < 0) {
                System.out.println("Length cannot be negative, skipping write operation.");
                return null;
            }

            // 获取文件的长度
            long fileLength = file.length();

            // 检查 offset 是否合理
            if (offset < 0 || offset >= fileLength) {
                System.out.println("Offset is out of bounds, skipping read operation.");
                return null;
            }

            // 计算实际可读取的字节数
            int readableLength = Math.min(length, (int) (fileLength - offset));

            // 准备字节数组存储读取的数据
            byte[] data = new byte[readableLength];

            FileInputStream fis = new FileInputStream(file);
            // 跳过 offset 前的字节
            fis.skip(offset);
            // 读取数据到字节数组中
            int bytesRead = fis.read(data);
            if (bytesRead != readableLength) {
                System.out.println("Unexpected number of bytes read.");
            }
            return data;
        }catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    public boolean mkdir(String fileSystemName, String path) {
        try {
            File directory = new File(fileSystemName + "/" + path);
            directory.mkdirs();
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean create(String fileSystemName, String path) {
        try {
            File file = new File(fileSystemName + "/" + path);
            // 确保父目录存在
            file.getParentFile().mkdirs();
            // 确保目标文件存在
            file.createNewFile();
            DataServerInfo currentNodeData = registService.getServerInfo();
            currentNodeData.setFileTotal(currentNodeData.getFileTotal() + 1);
            registService.updateServerInfo(currentNodeData);
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(String fileSystemName, String path){
        try {
            File file = new File(fileSystemName + "/" + path);
            if (file.exists()) {
                // 如果是目录，递归删除目录下的所有文件和子目录
                if (file.isDirectory()) {
                    File[] files = file.listFiles();
                    if (files != null) { // 处理空目录的情况
                        for (File subFile : files) {
                            // 递归调用删除方法
                            delete(fileSystemName, path + "/" + subFile.getName());
                        }
                    }
                } else if (file.isFile()) {
                    DataServerInfo currentNodeData = registService.getServerInfo();
                    currentNodeData.setFileTotal(currentNodeData.getFileTotal() - 1);
                    currentNodeData.setUseCapacity((int) (currentNodeData.getUseCapacity() - file.length()));
                    registService.updateServerInfo(currentNodeData);
                }
                // 删除文件或空目录
                return file.delete();
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
