package com.gryphpoem.game.zw.resource.pojo.fight;

/**
 * @author: ZhouJie
 * @date: Create in 2018-09-21 14:35
 * @description: 光环信息
 * @modified By:
 */
public class AuraInfo {
    private int heroId;
    private int medalAuraId;
    private String nick;

    public int getHeroId() {
        return heroId;
    }

    public void setHeroId(int heroId) {
        this.heroId = heroId;
    }

    public int getMedalAuraId() {
        return medalAuraId;
    }

    public void setMedalAuraId(int medalAuraId) {
        this.medalAuraId = medalAuraId;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public AuraInfo(int heroId, int medalAuraId, String nick) {
        this.heroId = heroId;
        this.medalAuraId = medalAuraId;
        this.nick = nick;
    }
}
