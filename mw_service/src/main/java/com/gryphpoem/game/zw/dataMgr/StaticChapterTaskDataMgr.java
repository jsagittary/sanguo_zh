package com.gryphpoem.game.zw.dataMgr;

import com.gryphpoem.game.zw.resource.domain.s.StaticTaskChapter;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * desc: 章节任务配置
 * author: huangxm
 * date: 2022/5/25 10:16
 **/
@Component
public class StaticChapterTaskDataMgr extends AbsStaticIniService {
    /**
     * 章节对用的代数配置
     * k:章节id v:配置
     */
    private static Map<Integer, StaticTaskChapter> staticTaskChapterMap;


    @Override
    public void load() {
        staticTaskChapterMap = staticDataDao.selectTaskChapterMap();
    }

    @Override
    public void check() {

    }


    public static Map<Integer, StaticTaskChapter> getStaticTaskChapterMap() {
        return staticTaskChapterMap;
    }

    /**
     * 根据章节拿到章节配置
     */
    public static StaticTaskChapter getStaticTaskChapter(int chapter) {
        return staticTaskChapterMap.get(chapter);
    }

}
