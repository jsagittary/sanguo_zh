package com.gryphpoem.game.zw.manager.s;

import com.gryphpoem.game.zw.data.s.StaticBuff;
import org.springframework.stereotype.Component;

import java.util.HashMap;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-10-22 20:49
 */
@Component
public class StaticFightManager {
    private HashMap<Integer, StaticBuff> buffHashMap = new HashMap<>();

    public StaticBuff getStaticBuff(int id) {
        return buffHashMap.get(id);
    }
}
