package com.example.subversionMavenIncrement.util;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.jar.JarArchiveEntry;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.*;

public class WarUtil {

    /**
     * war文件格式的过滤器
     */
    private static FileNameExtensionFilter filter = new FileNameExtensionFilter(
            "WAR压缩文件(*.war)", "war");
    /**
     * 解压缩war包
     * @param filePath:war包所在路径，包含本身文件名
     * @param destPath:解压缩到路径
     * */
    public static void unCompressWar(String filePath, String destPath) throws IOException, ArchiveException {
        unCompressWar(new FileInputStream(filePath),destPath);
    }

    /**
     * 解压缩war包
     * @param srcFile:war包文件
     * @param destPath:解压缩到路径
     * */
    public static void unCompressWar(File srcFile, String destPath) throws IOException, ArchiveException {
        unCompressWar(new FileInputStream(srcFile),destPath);
    }

    /**
     * 解压缩war包
     * @param warInputStream:war包输入流
     * @param destPath:解压缩到路径
     * */
    public static void unCompressWar(InputStream warInputStream, String destPath) throws IOException, ArchiveException {
        try (BufferedInputStream bufferedInputStream = new BufferedInputStream(warInputStream);
             ArchiveInputStream in = new ArchiveStreamFactory().createArchiveInputStream(ArchiveStreamFactory.JAR, bufferedInputStream);){
            JarArchiveEntry entry = null;
            while ((entry = (JarArchiveEntry) in.getNextEntry()) != null) {

                if (entry.isDirectory()) {
                    File newFile = newFileWithCheck(destPath,  entry.getName());
                    if(!newFile.exists()){
                        newFile.mkdir();
                    }
                }else {
                    File outFile = newFileWithCheck(destPath,  entry.getName());
                    if(!outFile.getParentFile().exists()){
                        outFile.getParentFile().mkdirs();
                    }
                    OutputStream out = FileUtils.openOutputStream(outFile);
                    IOUtils.copy(in, out);
                    out.close();
                }
            }
        } catch (FileNotFoundException e) {
            throw new FileNotFoundException("未找到war文件");
        } catch (ArchiveException e) {
            throw new ArchiveException("不支持的压缩格式");
        } catch (IOException e) {
            throw new IOException("文件写入发生错误");
        }
    }
    public FileNameExtensionFilter getFileFilter() {
        return filter;
    }

    public static final File newFileWithCheck(final String destinationDirName, final String name) throws IOException {
        File destinationDir = new File(destinationDirName);

        return newFileWithCheck(destinationDir, name);
    }

    public static final File newFileWithCheck(final File destinationDir, final String name) throws IOException {

        File destFile = new File(destinationDir, name);
        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + name);
        }
        return destFile;
    }

}
