package com.gryphpoem.game.zw.data.p;

import com.gryphpoem.push.util.CheckNull;
import lombok.Data;

import java.util.Map;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-10-20 16:02
 */
@Data
public class FightResult {
    /**
     * buff与effect map
     */
    private Map<Long, Map<Integer, Integer>> buffEffectMap;

    /**
     * 清除当前这回合中buff 效果信息
     */
    public void clear() {
        if (!CheckNull.isEmpty(buffEffectMap))
            buffEffectMap.clear();
    }
}
