package com.example.subversionMavenIncrement.run;

import com.example.subversionMavenIncrement.StaticValue;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 执行命令
 *
 * @author 蚕豆的生活
 */
public class ExecuteCommand {

    /**
     * 是以cmd开头还是sh开头
     */
    private static final String COMMAND_HEADER;

    /**
     * /c或者-c
     */
    private static final String HEADER_C;

    static {

        // 初始化参数
        String os = System.getProperty("os.name");
        if (os != null && os.toUpperCase().contains(StaticValue.WINDOWS.toString())) {
            COMMAND_HEADER = "cmd";
            HEADER_C = "/c";
        }else {
            COMMAND_HEADER = "sh";
            HEADER_C = "-c";
        }
    }

    /**
     * 执行命令行命令
     *
     * @param cmdS 命令 传入方式："svn log", "-v"
     * @param file 在什么位置执行命令
     * @return 执行的字符串
     * @throws IOException io异常
     */
    public static String carrOut(File file, String... cmdS) throws IOException, InterruptedException {

        List<String> list = new ArrayList<>();
        Collections.addAll(list, COMMAND_HEADER, HEADER_C);
        list.addAll(List.of(cmdS));

        String[] cmd = list.toArray(String[]::new);

        Runtime runtime = Runtime.getRuntime();
        Process process;
        if(null == file){
            process = runtime.exec(cmd);
            return IOUtils.toString(process.getInputStream(), StandardCharsets.UTF_8);
        }else {
            process = runtime.exec(cmd, null, file);
            InputStream ers= process.getErrorStream();
            InputStream ins= process.getInputStream();
            new Thread(new inputStreamThread(ins)).start();
            process.waitFor();
            ers.close();
            return null;
        }
    }

    static class inputStreamThread implements Runnable{
        private InputStream ins = null;
        private BufferedReader bfr = null;
        public inputStreamThread(InputStream ins){
            this.ins = ins;
            this.bfr = new BufferedReader(new InputStreamReader(ins));
        }
        @Override
        public void run() {
            String line = null;
            byte[] b = new byte[100];
            int num = 0;
            try {
                while((num=ins.read(b))!=-1){
                    System.out.println(new String(b,"gb2312"));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
