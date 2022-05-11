package com.gryphpoem.game.zw.server;

import com.gryphpoem.game.zw.core.util.FileUtil;
import com.gryphpoem.game.zw.core.util.JavaHotUpdateUtil;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import com.hotfix.HotfixInStaticClass;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-05-11 13:44
 */
@Component
public class HotUpdateService {
    //KEY:热更类全名,VALUE:文件最后修改时间
    private Map<String, Long> hotfixMap = new HashMap<>();
    private static final String fileNameSuffix = ".java";

    public void init() {
        try {
            //清空热更class文件
            File hotfixDir = new FileSystemResource("hotUpdate/").getFile();
            FileUtil.readHotfixDir(null, hotfixDir, hotfixMap, true, fileNameSuffix);
            hotfixMap.clear();
        } catch (Exception e) {
            LogUtil.error("", e);
        }
    }

    public void hotUpdateWithTimeLogic() {
        try {
            Resource resource = new FileSystemResource("hotUpdate/");
            File hotfixDir = resource.getFile();
            Map<String, File> hotfixTimeMap = new HashMap<>();
            FileUtil.readHotUpdateDir(null, hotfixDir, hotfixTimeMap, false, fileNameSuffix);
            Date now = new Date();
            int nowSec = TimeHelper.getCurrentSecond();
            for (Map.Entry<String, File> entry : hotfixTimeMap.entrySet()) {
                Long modifyTime = hotfixMap.get(entry.getKey());
                if (modifyTime == null || modifyTime.longValue() != entry.getValue().lastModified()) {
                    HotfixInStaticClass.runJavaFile(String.valueOf(nowSec), entry.getValue(), now, fileNameSuffix);
                    hotfixMap.put(entry.getKey(), entry.getValue().lastModified());
                }
            }
        } catch (Exception e) {
            LogUtil.error("", e);
        }
    }
}
