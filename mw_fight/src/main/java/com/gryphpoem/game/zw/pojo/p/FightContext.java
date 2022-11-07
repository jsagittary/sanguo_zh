package com.gryphpoem.game.zw.pojo.p;

import com.gryphpoem.game.zw.pb.CommonPb;
import lombok.Data;

import java.util.List;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-10-31 15:41
 */
@Data
public class FightContext {
    /**
     * 进攻战斗方
     */
    private Fighter atkFighter;
    /**
     * 防守战斗方
     */
    private Fighter defFighter;
    /**
     * 进攻方玩家id
     */
    private long atkRoleId;
    /**
     * 进攻势力
     */
    private Force attacker;
    /**
     * 防守势力
     */
    private Force defender;
    /**
     * 回合数
     */
    private int roundNum;
    private long fightId;
    private boolean recordFlag = true;
    // TODO 战斗PB信息
    private CommonPb.Record.Builder recordData;// 总战报
    // 战斗实体
    private List<FightEntity> fightEntityList;
    private BattleLogic battleLogic;
}
