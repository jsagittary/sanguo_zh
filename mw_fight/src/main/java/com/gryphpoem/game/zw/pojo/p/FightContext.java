package com.gryphpoem.game.zw.pojo.p;

import com.gryphpoem.game.zw.buff.IFightBuff;
import com.gryphpoem.game.zw.listener.Listener;
import com.gryphpoem.game.zw.pb.BattlePb;
import lombok.Data;

import java.util.HashMap;
import java.util.LinkedList;
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
    /**
     * 监听器
     */
    private List<Listener> listeners;

    // TODO 战斗PB信息
    private BattlePb.BattleRoundPb.Builder recordDataPb;                              // 总战报
    private BattlePb.BattlePreparationStage.Builder preparationStagePb;               // 武将出场
    private BattlePb.BattleRoundStage.Builder battleRoundStagePb;                     // 战斗回合阶段
    private BattlePb.BattleSettlementLossStage.Builder battleSettlementLoseStagePb;   // 结算损兵阶段
    private BattlePb.BattleRegroupStage.Builder battleRegroupStagePb;                 // 重振旗鼓阶段
    private BattlePb.BattleEndStage.Builder battleEndStagePb;                         // 战斗结束阶段

    private BattlePb.BothBattleEntityPb.Builder bothBattleEntityPb;                   // 双方武将战报
    private BattlePb.RoundAction.Builder roundActionPb;                               // 回合动作pb
    private BattlePb.SkillAction.Builder skillActionPb;                               // 技能pb
    private BattlePb.OrdinaryAttackAction.Builder ordinaryAttackActionPb;             // 普攻pb
    // 游戏逻辑限制动作嵌套动作, 无限嵌套, 策划配置规避无限循环
    private LinkedList<MultiEffectActionPb> multiEffectActionPbList;                  // 无限嵌套的动作
}
