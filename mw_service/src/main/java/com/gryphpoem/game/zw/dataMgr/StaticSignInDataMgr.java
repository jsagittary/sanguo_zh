package com.gryphpoem.game.zw.dataMgr;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.resource.dao.impl.s.StaticDataDao;
import com.gryphpoem.game.zw.resource.domain.s.StaticActSign;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @Description 签到
 */
public class StaticSignInDataMgr {

    private static StaticDataDao staticDataDao = DataResource.ac.getBean(StaticDataDao.class);

    /**
     *
     */
    private static Map<Integer, Map<String, Map<Integer, StaticActSign>>> signConfig;

    public static void init() {

        Map<Integer, Map<String, Map<Integer, StaticActSign>>> tempSignConfig = new HashMap<>();

        Map<Integer, StaticActSign> actSignMap = staticDataDao.selectStaticActSign();

        for (StaticActSign config : actSignMap.values()) {


            int page = config.getTurn();

            if (!tempSignConfig.containsKey(page)) {
                tempSignConfig.put(page, new HashMap<>());
            }
            List<List<Integer>> level = config.getLevel();
            String key = getKey(level);
            if (!tempSignConfig.get(page).containsKey(key)) {
                tempSignConfig.get(page).put(key, new HashMap<>());
            }

            tempSignConfig.get(page).get(key).put(config.getTime(), config);

        }
        signConfig = tempSignConfig;
    }


    private static String getKey(List<List<Integer>> level) {
        return level.get(0).get(0) + "-" + level.get(0).get(1);
    }

    /**
     * 获取签到配置
     *
     * @param level
     * @param time
     * @return
     */
    public static StaticActSign getSignConfig(Integer page, int level, int time) {

        Map<String, Map<Integer, StaticActSign>> stringMapMap = signConfig.get(page);

        if (stringMapMap == null) {
            return null;
        }

        Set<String> stringSet = stringMapMap.keySet();
        for (String key : stringSet) {
            String[] split = key.split("-");
            if (level >= Integer.valueOf(split[0]) && level <= Integer.valueOf(split[1])) {
                Map<Integer, StaticActSign> staticActSignMap = stringMapMap.get(key);
                return staticActSignMap.get(time);
            }
        }
        return null;
    }
}
