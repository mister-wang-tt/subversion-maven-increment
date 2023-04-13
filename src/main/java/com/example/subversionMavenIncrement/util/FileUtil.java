package com.example.subversionMavenIncrement.util;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.stream.Stream;

/**
 * 文件处理工具类
 */
public class FileUtil {

    /**
     * 复制单级文件夹
     *
     * @param srcFolder  源文件夹
     * @param destFolder 目的文件夹
     */
    public static void copyFolder(File srcFolder, File destFolder) {
        try {
            // 判断路径是否存在
            if (!destFolder.exists()) {
                destFolder.mkdirs();
            }
            // 获取目的文件列表
            File[] listFiles = srcFolder.listFiles();
            // 遍历目的文件列表
            for (File file : listFiles) {
                if (file.isFile()) {
                    copyFile(file, new File(destFolder, file.getName()));
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 复制文件
     *
     * @param srcFile  源文件
     * @param destFile 目的文件
     * @throws IOException
     */
    private static void copyFile(File srcFile, File destFile) throws IOException {
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(srcFile));
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(destFile));
        byte[] bys = new byte[1024];
        int len;
        while ((len = bis.read(bys)) != -1) {
            bos.write(bys, 0, len);
        }
        bos.close();
        bis.close();
    }

    /**
     * 删除文件 或者 清除文件夹下的所有文件
     *
     * @param filePath
     */
    public static void deleteDirectoryStream(String filePath) {
        Path path = Paths.get(filePath);

        if(!path.toFile().exists()) {
            return;
        }

        try (Stream<Path> walk = Files.walk(path)) {
            walk.sorted(Comparator.reverseOrder())
                    .forEach(e -> {
                        try {
                            Files.delete(e);
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 删除文件 或者 清除文件夹下的所有文件
     *
     * @param filePath
     */
    public static void deleteFile(String filePath)
    {
        File file = new File(filePath);

        if(!file.exists()) {
            return;
        }

        //判断是否为文件，是，则删除
        if (file.isFile()) {
            file.delete();
        }
        //不为文件，则为文件夹
        else {
            //获取文件夹下所有文件相对路径
            String[] childFilePath = file.list();
            for (String path:childFilePath) {
                //递归，对每个都进行判断
                deleteFile(file.getAbsoluteFile()+"/"+path);
            }
            file.delete();
        }
    }

}
