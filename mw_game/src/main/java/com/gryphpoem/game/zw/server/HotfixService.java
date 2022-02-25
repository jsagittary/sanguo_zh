package com.gryphpoem.game.zw.server;

import com.hotfix.HotfixInStaticClass;
import com.gryphpoem.game.zw.core.util.FileUtil;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author: ZhouJie
 * @Date: Create in 2017-12-21 15:19
 * @Description: 热更
 * @Modified By:
 */
@Service
public class HotfixService {
    //KEY:热更类全名,VALUE:文件最后修改时间
    private Map<String, Long> hotfixMap = new HashMap<>();

    //    @PostConstruct
    public void init() {
        try {
            //清空热更class文件
            File hotfixDir = new FileSystemResource("hotfix/").getFile();
            FileUtil.readHotfixDir(null, hotfixDir, hotfixMap, true);
            hotfixMap.clear();
        } catch (Exception e) {
            LogUtil.error("", e);
        }
    }

    public void hotfixWithTimeLogic() {
        try {
            Resource resource = new FileSystemResource("hotfix/");
            File hotfixDir = resource.getFile();
            Map<String, Long> hotfixTimeMap = new HashMap<>();
            FileUtil.readHotfixDir(null, hotfixDir, hotfixTimeMap, false);
            Date now = new Date();
            int nowSec = TimeHelper.getCurrentSecond();
            for (Map.Entry<String, Long> entry : hotfixTimeMap.entrySet()) {
                Long modifyTime = hotfixMap.get(entry.getKey());
                if (modifyTime == null || modifyTime.longValue() != entry.getValue()) {
                    HotfixInStaticClass.redefineClass(String.valueOf(nowSec), entry.getKey(), now);
                    hotfixMap.put(entry.getKey(), entry.getValue());
                }
            }
        } catch (Exception e) {
            LogUtil.error("", e);
        }
    }

    /**
     *
     * @Title: hotfixWithId
     * @Description: 热更新入口
     * @param hotfixId
     * void
     * @throws
     */
    public void hotfixWithId(String hotfixId) {
        Date now = new Date();
        try {
            //读取热更新文件夹
            Resource resource = new FileSystemResource("hotfix/");
            File hotfixDir = resource.getFile();
            // KEY:类全名 VALUE:文件最后修改时间
            Map<String, Long> hotfixTimeMap = new HashMap<>();
            FileUtil.readHotfixDir(null, hotfixDir, hotfixTimeMap, false);
            //遍历包里所有类 比较最后修改时间 修改时间不一样则执行静态类方法重新定义
            for (Map.Entry<String, Long> entry : hotfixTimeMap.entrySet()) {
                Long modifyTime = hotfixMap.get(entry.getKey());
                if (modifyTime == null || modifyTime.longValue() != entry.getValue()) {
                    HotfixInStaticClass.redefineClass(hotfixId, entry.getKey(), now);
                    hotfixMap.put(entry.getKey(), entry.getValue());
                }
            }
        } catch (Exception e) {
            LogUtil.error("", e);
            // ServerHotfix hotfix = new ServerHotfix(hotfixId, "", now);
            // hotfix.setResultInfo(HotfixInStaticClass.printStackTraceToString(e));
            // DataRepairDao dataRepairDao = DataResource.ac.getBean(DataRepairDao.class);
            // dataRepairDao.insertHotfifxResult(hotfix);
        }
    }
}
