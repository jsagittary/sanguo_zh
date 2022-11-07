package com.gryphpoem.game.zw.pojo.p;

import com.gryphpoem.game.zw.util.FightUtil;
import lombok.Data;

import java.util.List;
import java.util.Objects;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-11-01 14:42
 */
@Data
public class FightEffectData {
    private long effectKeyId;
    private long buffKeyId;
    private int buffId;
    /**
     * element:<万分比数值, 固定数值>
     */
    private List<Integer> data;
    /**
     * 效果固定值
     */
    private int value;
    /**
     * 效果施加顺序
     */
    private int index;

    public FightEffectData(long buffKeyId, int buffId) {
        this.effectKeyId = FightUtil.uniqueId();
        this.buffKeyId = buffKeyId;
        this.buffId = buffId;
    }

    public FightEffectData(long buffKeyId, int buffId, int value) {
        this.effectKeyId = FightUtil.uniqueId();
        this.buffKeyId = buffKeyId;
        this.buffId = buffId;
        this.value = value;
    }

    public FightEffectData(long buffKeyId, int buffId, List<Integer> data) {
        this.effectKeyId = FightUtil.uniqueId();
        this.buffKeyId = buffKeyId;
        this.buffId = buffId;
        this.data = data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FightEffectData data = (FightEffectData) o;
        return effectKeyId == data.effectKeyId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(effectKeyId);
    }
}
