package com.gryphpoem.game.zw.pojo.p;

import com.gryphpoem.game.zw.pb.BattlePb;
import lombok.Data;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-11-19 18:20
 */
@Data
public class MultiEffectActionPb {
    // 如果是回合还未有任何动作创建而附加了多重效果, 则队列的第一个此处有值
    private BattlePb.RoundAction.Builder roundActionPb;
    // 当前动作效果属于的技能pb
    private BattlePb.SkillAction.Builder lastSkillPb;
    // 当前动作效果组合效果属于的普攻pb
    private BattlePb.OrdinaryAttackAction.Builder lastAttackPb;
    // 当前嵌套效果
    private BattlePb.MultiEffectAction.Builder curMultiEffectActionPb;
    // 当前的技能pb
    private BattlePb.SkillAction.Builder curSkillPb;
    // 当前的技能pb
    private BattlePb.OrdinaryAttackAction.Builder curAttackPb;
    // 是否可以对当前动作进行反击
    private boolean counterattack = true;
}
