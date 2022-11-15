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
     * 战斗唯一id
     */
    private long fightId;
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
    /**
     * 战斗实体排序
     */
    private List<FightEntity> fightEntityList;

    private int battleType;
    /**
     * 正常回合放技能或普攻时, 攻击方向
     */
    private ActionDirection actionDirection;
    /**
     * 战斗逻辑应用类实体
     */
    private BattleLogic battleLogic;
    /**
     * 所有触发类型的buff (主动buff-回合开始时触发)
     */
    private HashMap<Integer, List<IFightBuff>> triggerBuffMap;

    // TODO 战斗PB信息
    private BattlePb.BattleRoundPb.Builder recordData;                      // 总战报
    private BattlePb.BothBattleEntityPb.Builder bothBattleEntity;           // 双方武将战报
    private BattlePb.BattlePreparationStage.Builder preparationStagePb;     // 武将出场
    private BattlePb.SkillAction.Builder skillActionPb;                     // 技能pb
    private BattlePb.OrdinaryAttackAction.Builder ordinaryAttackActionPb;   // 普攻pb
    // 游戏逻辑限制动作嵌套动作, 只嵌套一层
    private BattlePb.MultiEffectAction.Builder multiEffectActionPb;         // 嵌套动作pb
    private BattlePb.SkillAction.Builder effectSkillActionPb;               // 效果技能攻击
    private BattlePb.OrdinaryAttackAction.Builder effectAttackActionPb;     // 效果普攻攻击
}
