package com.gryphpoem.game.zw.core.util;

import org.apache.curator.shaded.com.google.common.io.CharStreams;
import org.apache.log4j.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class FileUtil {
    private static final Logger logger = Logger.getLogger(FileUtil.class);

    /**
     * 读取一个本地文件
     * 
     * @param path
     * @return
     */
    public static String readFile(String path) {
        File file = new File(path);
        Long filelength = file.length(); // 获取文件长度
        byte[] filecontent = new byte[filelength.intValue()];
        try {
            FileInputStream in = new FileInputStream(file);
            in.read(filecontent);
            in.close();
        } catch (Exception e) {
            logger.error(e);
            return null;
        }

        return new String(filecontent);// 返回文件内容,默认编码
    }

    public static String readClassPathFileStr(String resourceLocation) {
        try (InputStream in = FileUtil.class.getResourceAsStream(resourceLocation)) {
            return CharStreams.toString(new InputStreamReader(in, StandardCharsets.UTF_8));
        } catch (IOException e) {
            LogUtil.error(e);
        }
        return "";
    }

    /**
     *
     * @param parent
     * @param curFile
     * @param fileTimeMap
     * @param bDelete
     */
    public static void readHotfixDir(String parent, File curFile, Map<String, Long> fileTimeMap, boolean bDelete) {
        if (curFile.isDirectory()) {
            File[] files = curFile.listFiles();
            if (files != null && files.length > 0) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        String packageName = parent != null ? parent + "." + file.getName() : file.getName();
                        readHotfixDir(packageName, file, fileTimeMap, bDelete);
                    } else {
                        readHotfixDir(parent, file, fileTimeMap, bDelete);
                    }
                }
            }
        } else {
            try {
                int classNameIdx = curFile.getName().indexOf(".class");
                if (classNameIdx >= 0) {
                    String className = curFile.getName().substring(0, classNameIdx);
                    String clsFullName = parent != null ? parent + "." + className : className;
                    fileTimeMap.put(clsFullName, curFile.lastModified());
                }
                if (bDelete && curFile.delete()) {
                    LogUtil.hotfix("delete class file :" + curFile.getName());
                }

            } catch (Exception e) {
                LogUtil.hotfix(String.format("parent :%s, file :%s", parent, curFile.getName()), e);
            }

        }
    }

    public static void wirteFile(String path, String content) {
        Path fpath = Paths.get(path);
        // 创建文件
        if (!Files.exists(fpath)) {
            try {
                Files.createFile(fpath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // 创建BufferedWriter
        try (BufferedWriter bfw = Files.newBufferedWriter(fpath)) {
            bfw.write(content);
            bfw.flush();
            bfw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean isWindows() {
        return System.getProperties().getProperty("os.name").toUpperCase().contains("WINDOWS");
    }

}
