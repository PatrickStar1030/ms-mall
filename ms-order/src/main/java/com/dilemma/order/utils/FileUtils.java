package com.dilemma.order.utils;

import java.io.*;
import java.net.URISyntaxException;

public class FileUtils {
    public static String getScript(String fileName) throws URISyntaxException {
        String path = FileUtils.class.getClassLoader().getResource(fileName).toURI().getPath();
        System.out.println(path);
        return readFileByLines(path);
    }

    private static String readFileByLines(String path) {
        FileInputStream fileInputStream;
        BufferedReader reader = null;
        InputStreamReader inputFileReader;
        String content = "";
        String tempString;

        try {
            fileInputStream = new FileInputStream(path);
            inputFileReader = new InputStreamReader(fileInputStream);
            reader = new BufferedReader(inputFileReader);
            //一次读入一行，直到读入null为文件结束
            while ((tempString = reader.readLine()) != null){
                content += tempString;
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (reader != null){
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return content;
    }
}
