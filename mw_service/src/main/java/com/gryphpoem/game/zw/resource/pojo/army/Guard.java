package com.gryphpoem.game.zw.resource.pojo.army;

import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.CommonPb.Award;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.pojo.hero.Hero;
import com.gryphpoem.game.zw.resource.util.PbHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName Guard.java
 * @Description 矿点驻军信息
 * @author TanDonghai
 * @date 创建时间：2017年4月11日 上午11:26:27
 *
 */
public class Guard {
    private Player player;
    private Army army;

    public Army getArmy() {
        return army;
    }

    public void setArmy(Army army) {
        this.army = army;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public Guard() {
    }

    /**
     * @param army
     * @param player
     */
    public Guard(Player player, Army army) {
        this.army = army;
        this.player = player;
    }

    public int getPos() {
        return army.getTarget();
    }

    public List<CommonPb.TwoInt> getForm() {
        // 因为部队的兵力可变,需要获取最新兵力情况
        List<CommonPb.TwoInt> form = new ArrayList<>();
        for (CommonPb.TwoInt twonInt : army.getHero()) {
            int heroId = twonInt.getV1();
            Hero hero = player.heros.get(heroId);
            if (hero == null) continue;
            int cnt = hero.getCount();
            form.add(PbHelper.createTwoIntPb(heroId, cnt));
        }
        return form;
    }

    public int getCamp() {
        return player.lord.getCamp();
    }

    public int getLv() {
        return player.lord.getLevel();
    }

    public long getRoleId() {
        return player.lord.getLordId();
    }

    public String getNick() {
        return player.lord.getNick();
    }

    public int getHeroId() {
        return army.getHero().get(0).getV1();
    }

    public int getHeroLv() {
        int heroId = getHeroId();
        Hero hero = player.heros.get(heroId);
        return hero.getLevel();
    }

    public int getArmCount() {
        int heroId = getHeroId();
        Hero hero = player.heros.get(heroId);
        return hero.getCount();
    }

    public int getBeginTime() {
        return army.getEndTime() - army.getDuration();
    }

    public int getEndTime() {
        return army.getEndTime();
    }

    public List<Award> getGrab() {
        return army.getGrab();
    }

    @Override
    public String toString() {
        return "Guard [roleId=" + player.roleId + ", name=" + player.lord.getNick() + ", army=" + army + "]";
    }
}
