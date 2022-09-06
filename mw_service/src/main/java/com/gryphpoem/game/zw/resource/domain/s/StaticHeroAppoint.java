package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * @author GeYuanpeng
 * @Description 获取武将的初始等级自适应配置
 * @date 2022/9/6
 */
public class StaticHeroAppoint {

    private int heroId; // 武将id

    private String appoint; // 获得该英雄时，英雄等级自适应lordLv的上下限

    private String desc; // 备注

    public int getHeroId() {
        return heroId;
    }

    public void setHeroId(int heroId) {
        this.heroId = heroId;
    }

    public String getAppoint() {
        return appoint;
    }

    public void setAppoint(String appoint) {
        this.appoint = appoint;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    @Override
    public String toString() {
        return "StaticHeroAppoint{" +
                "heroId=" + heroId +
                ", appoint=" + appoint +
                ", desc='" + desc + '\'' +
                '}';
    }
}
