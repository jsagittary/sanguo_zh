package com.gryphpoem.game.zw.dataMgr;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.resource.dao.impl.s.StaticDataDao;
import com.gryphpoem.game.zw.resource.domain.s.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class StaticLordDataMgr {
    private StaticLordDataMgr() {
    }

    private static StaticDataDao staticDataDao = DataResource.ac.getBean(StaticDataDao.class);

    // 指挥官等级
    private static Map<Integer, StaticLordLv> lordLvMap;

    private static Map<Integer, StaticPortrait> portraitMap;
    // key:cond
    private static Map<Integer, StaticGuidAward> guidAwardMap;

    // 推荐阵营配置
    private static List<StaticRecommend> recommendCamp;
    // 聊天气泡
    private static Map<Integer, StaticChatBubble> chatBubbleMap;
    // 玩家形象
    private static Map<Integer, StaticBodyImage> bodyImageMap;
    // 柏林霸主的形象
    private static Set<Integer> berlinWinnerBodyImage;
    // 城堡皮肤
    private static Map<Integer, StaticCastleSkin> castleSkinMap;
    // 称号 key==id
    private static Map<Integer, StaticTitle> titleMap;
    // 称号类型分组 key==taskId
    private static Map<Integer, List<StaticTitle>> titleTaskMap = new HashMap<>();;
    private static Map<Integer, StaticCastleSkinStar> skinStarMap = new HashMap<>();
    private static List<StaticCastleSkinStar> skinStarList;
    // 小号清除
    private static List<StaticSmallClear> smallClearList;

    // 头像框配置
    private static Map<Integer, StaticPortraitFrame> portraitFrameMap;

    private static Map<Integer, StaticNameplate> nameplateMap;

    // 行军线皮肤配置
    private static Map<Integer, StaticMarchLine> marchLineMap;

    public static void init() {
        Map<Integer, StaticLordLv> lordLvMap = staticDataDao.selectLordLv();
        StaticLordDataMgr.lordLvMap = lordLvMap;

        StaticLordDataMgr.portraitMap = staticDataDao.selectPortrait();
        StaticLordDataMgr.guidAwardMap = staticDataDao.selectGuidAward();
        StaticLordDataMgr.recommendCamp = staticDataDao.selectRecommend();
        StaticLordDataMgr.chatBubbleMap = staticDataDao.selectChatBubbleMap();
        StaticLordDataMgr.bodyImageMap = staticDataDao.selectBodyImageMap();
        StaticLordDataMgr.castleSkinMap = staticDataDao.selectCastleSkinMap();
        StaticLordDataMgr.titleMap = staticDataDao.selectTitleMap();
        titleTaskMap.clear();
        StaticLordDataMgr.titleMap.values().forEach(e -> {
            if (null != e.getTaskId() && e.getTaskId() > 0) {
                if (null == titleTaskMap.get(e.getTaskId())) {
                    List<StaticTitle> titleList = new ArrayList<>();
                    titleList.add(e);
                    titleTaskMap.put(e.getTaskId(), titleList);
                } else {
                    if (titleTaskMap.get(e.getTaskId()).stream().filter(title -> {
                        if (title.getId() != e.getId()) {
                            return true;
                        }
                        return false;
                    }).count() == 0) {
                        titleTaskMap.get(e.getTaskId()).add(e);
                    } else {
                        List<StaticTitle> titleList = new ArrayList<>();
                        titleList.add(e);
                        titleTaskMap.put(e.getTaskId(), titleList);
                    }
                }
            }
        });
        // 柏林霸主形象
        StaticLordDataMgr.berlinWinnerBodyImage = portraitMap.values().stream()
                .filter(sp -> StaticPortrait.UNLOCK_TYPE_WINNER == sp.getUnlock())
                .map(sp -> getBodyImageByPortraitId(sp.getId())).filter(sbi -> sbi != null).map(sbi -> sbi.getId())
                .collect(Collectors.toSet());

        StaticLordDataMgr.smallClearList = staticDataDao.selectSmallClearList();

        //城堡皮肤升星配表
        StaticLordDataMgr.skinStarList = staticDataDao.selectCastleSkinStarList();
        if (skinStarList != null) {
            skinStarList.forEach(o ->
                    skinStarMap.put(o.getId(), o)
            );
        }

        // 头像框, 铭牌, 行军特效
        StaticLordDataMgr.portraitFrameMap = staticDataDao.selectPortraitFrameMap();
        StaticLordDataMgr.nameplateMap = staticDataDao.selectNameplateMap();
        StaticLordDataMgr.marchLineMap = staticDataDao.selectMarchLineMap();
    }

    public static StaticPortraitFrame getPortraitFrame(int id) {
        if (portraitFrameMap != null) {
            return portraitFrameMap.get(id);
        }
        return null;
    }

    public static StaticNameplate getNameplate(int id) {
        if (nameplateMap != null) {
            return nameplateMap.get(id);
        }
        return null;
    }

    public static StaticMarchLine getMarchLine(int id) {
        if (marchLineMap != null) {
            return marchLineMap.get(id);
        }
        return null;
    }

    public static StaticSmallClear getSmallClearListByLv(int lv) {
        return smallClearList.stream().filter(ssc -> lv >= ssc.getLevel().get(0) && lv <= ssc.getLevel().get(1)).findFirst().orElse(null);
    }

    public static List<StaticSmallClear> getSmallClearList() {
        return smallClearList;
    }

    public static StaticLordLv getStaticLordLv(int lv) {
        return lordLvMap.get(lv);
    }

    public static StaticPortrait getPortrait(int portraitId) {
        if (portraitMap != null) {
            return portraitMap.get(portraitId);
        }
        return null;
    }

    public static Map<Integer, StaticPortrait> getPortraitMap() {
        return portraitMap;
    }

    public static List<StaticPortrait> getPortraitByUnlock(final int unlock) {
        return StaticLordDataMgr.getPortraitMap().values().stream().filter(sp -> sp.getUnlock() == unlock)
                .collect(Collectors.toList());
    }

    // public static boolean addExp(Lord lord, long add) {
    // int lv = lord.getLevel();
    //
    // boolean up = false;
    // long exp = lord.getExp() + add;
    // while (true) {
    // if (lv >= Constant.ROLE_MAX_LV) {
    // break;
    // }
    //
    // StaticLordLv staticLordLv = lordLvMap.get(lv + 1);
    // if (exp >= staticLordLv.getNeedExp()) {
    // up = true;
    // exp -= staticLordLv.getNeedExp();
    // lv++;
    // continue;
    // } else {
    // break;
    // }
    // }
    //
    // lord.setLevel(lv);
    // lord.setExp(exp);
    // return up;
    // }
    public static StaticGuidAward getStaticGuidAward(int cond) {
        return guidAwardMap.get(cond);
    }

    public static Map<Integer, StaticGuidAward> getGuidAwardMap() {
        return guidAwardMap;
    }

    public static List<StaticRecommend> getRecommendCamp() {
        return recommendCamp;
    }

    public static StaticRecommend getRecommendCampById(int id) {
        return recommendCamp.stream().filter(sr -> sr.getKeyId() == id).findFirst().orElse(null);
    }

    public static Map<Integer, StaticChatBubble> getChatBubbleMap() {
        return chatBubbleMap;
    }

    public static StaticChatBubble getChatBubbleMapById(int id) {
        return chatBubbleMap.get(id);
    }

    public static Map<Integer, StaticBodyImage> getBodyImageMap() {
        return bodyImageMap;
    }

    public static StaticBodyImage getBodyImageById(int id) {
        return bodyImageMap.get(id);
    }

    /**
     * 根据头像id获取对应的形象
     *
     * @param portraitId
     * @return 返回 null 说明没有此形象
     */
    public static StaticBodyImage getBodyImageByPortraitId(int portraitId) {
        return bodyImageMap.values().stream()
                .filter(sbi -> sbi.getType() == StaticBodyImage.TYPE_PORTRAIT && sbi.getParam() == portraitId)
                .findFirst().orElse(null);
    }

    /**
     * 获取霸主的形象
     *
     * @return
     */
    public static Set<Integer> getBerlinWinnerBodyImage() {
        return berlinWinnerBodyImage;
    }

    public static StaticCastleSkin getCastleSkinMapById(int id) {
        return castleSkinMap.get(id);
    }

    public static StaticTitle getTitleMapById(int id) {
        return titleMap.get(id);
    }

    public static Map<Integer, StaticTitle> getTitleMap() {
        return titleMap;
    }

    public static Map<Integer, List<StaticTitle>> getTitleTaskMap() {
        return titleTaskMap;
    }

    public static List<StaticTitle> getTitleTypeMapByTypeId(int typeId) {
        return titleTaskMap.get(typeId);
    }
    public static StaticCastleSkinStar getCastleSkinStarById(int id) {
        return skinStarMap.get(id);
    }

}
