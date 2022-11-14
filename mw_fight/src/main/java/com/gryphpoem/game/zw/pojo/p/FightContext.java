package com.gryphpoem.game.zw.pojo.p;

import com.gryphpoem.game.zw.buff.IFightBuff;
import com.gryphpoem.game.zw.pb.BattlePb;
import lombok.Data;

import java.util.HashMap;
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
     * 回合数
     */
    private int roundNum;
    private long fightId;
    private boolean recordFlag = true;
    // TODO 战斗PB信息
    private BattlePb.BattleRoundPb.Builder recordData;          // 总战报
    private BattlePb.SkillAction.Builder skillAction;           // 技能pb
    private BattlePb.OrdinaryAttackAction.Builder attackAction; // 普攻pb

    // 战斗实体
    private List<FightEntity> fightEntityList;
    private BattleLogic battleLogic;
    private int battleType;
    /**
     * 正常回合放技能或普攻时, 攻击方向
     */
    private ActionDirection actionDirection;

    /**
     * 所有触发类型的buff (主动buff-回合开始时触发)
     */
    private HashMap<Integer, List<IFightBuff>> triggerBuffMap;
}
