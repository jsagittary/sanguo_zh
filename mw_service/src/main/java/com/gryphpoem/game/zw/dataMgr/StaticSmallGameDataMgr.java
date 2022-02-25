package com.gryphpoem.game.zw.dataMgr;

import com.gryphpoem.game.zw.resource.domain.s.StaticSmallGame;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @Description
 * @Author zhangdh
 * @Date 2021-11-24 11:38
 */
@Component
public class StaticSmallGameDataMgr extends AbsStaticIniService {
    private static Map<Integer, StaticSmallGame> smallGameMap;

    @Override
    public void load() {
        smallGameMap = staticIniDao.selectSmallGameMap();
    }
    @Override
    public void check() {

    }

    public static StaticSmallGame getStaticSmallGame(int id){
        return smallGameMap.get(id);
    }
}
