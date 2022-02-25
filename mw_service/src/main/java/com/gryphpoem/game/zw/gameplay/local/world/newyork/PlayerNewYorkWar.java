package com.gryphpoem.game.zw.gameplay.local.world.newyork;

import com.gryphpoem.game.zw.pb.SerializePb;
import com.gryphpoem.game.zw.resource.util.PbHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Created by pengshuo on 2019/5/10 16:52
 * <br>Description:
 * <br>Modified By:
 * <br>Version:
 *
 * @author pengshuo
 */
public class PlayerNewYorkWar {
    /** 玩家最大杀敌数 */
    private long maxAttack;
    /** 玩家成就领奖数据 */
    private Map<Integer,Integer> achievements = new HashMap<>();

    public long getMaxAttack() {
        return maxAttack;
    }

    public void setMaxAttack(long maxAttack) {
        this.maxAttack = maxAttack;
    }

    public Map<Integer, Integer> getAchievements() {
        return achievements;
    }

    public void setAchievements(Map<Integer, Integer> achievements) {
        this.achievements = achievements;
    }

    public SerializePb.SerPlayerNewYorkWar ser() {
        SerializePb.SerPlayerNewYorkWar.Builder builder  = SerializePb.SerPlayerNewYorkWar.newBuilder();
        builder.setMaxAttack(this.maxAttack);
        builder.addAllAchievements(PbHelper.createTwoIntListByMap(this.achievements));
        return builder.build();
    }

    public void deser(SerializePb.SerPlayerNewYorkWar newYorkWar) {
        this.setMaxAttack(newYorkWar.getMaxAttack());
        Optional.of(newYorkWar.getAchievementsList()).ifPresent(achievementsList ->
            achievementsList.forEach(e-> this.achievements.put(e.getV1(),e.getV2()))
        );
    }
}
