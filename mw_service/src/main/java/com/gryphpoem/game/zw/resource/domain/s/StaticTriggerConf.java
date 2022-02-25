package com.gryphpoem.game.zw.resource.domain.s;

import com.gryphpoem.game.zw.resource.constant.PlayerConstant;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.DateHelper;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

/**
 * @Author: ZhouJie
 * @Date: Create in 2018-02-28 17:04
 * @Description: 触发式礼包配置
 * @Modified By:
 */
public class StaticTriggerConf {

    private int id;
    private int giftId;//   礼包Id
    private int triggerId;//    触发条件Id
    private List<Integer> level;//  等级限制 [等级下限，等级上限]'
    private int interval;// 触发间隔(秒)
    private int continueTime;// 持续时间(秒)
    private List<Integer> cond;//   参数 cond.get(0): 建筑类型, cond.get(1): 升级后的建筑等级 [目前是为TriggerId = 4,升级建筑物提供的扩展字段]
    private List<Integer> vip;//  vip等级限制 [等级下限，等级上限]'
    private List<Integer> createRoleDate;// 创角时间
    private List<Integer> triGift;// 触发源ID  [触发礼包ID，购买情况]  购买情况 1已购买 0未购买 [354,1]： 表示礼包ID 354已购买后触发
    private int triGiftTime;// 配合triGift字段，触发时间间隔，单位为秒
    private List<Integer> spendPower;// 消费水平参数 [7,200] X天内充值数额大于XX美元  最大追溯1周时间

    public int getGiftId() {
        return giftId;
    }

    public void setGiftId(int giftId) {
        this.giftId = giftId;
    }

    public int getTriggerId() {
        return triggerId;
    }

    public void setTriggerId(int triggerId) {
        this.triggerId = triggerId;
    }

    public List<Integer> getLevel() {
        return level;
    }

    public void setLevel(List<Integer> level) {
        this.level = level;
    }

    /**
     * CD时间
     * @return 0 不会重复触发
     */
    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public int getContinueTime() {
        return continueTime;
    }

    public void setContinueTime(int continueTime) {
        this.continueTime = continueTime;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<Integer> getCond() {
        return cond;
    }

    public void setCond(List<Integer> cond) {
        this.cond = cond;
    }

    public List<Integer> getVip() {
        return vip;
    }

    public void setVip(List<Integer> vip) {
        this.vip = vip;
    }

    public List<Integer> getCreateRoleDate() {
        return createRoleDate;
    }

    public void setCreateRoleDate(List<Integer> createRoleDate) {
        this.createRoleDate = createRoleDate;
    }

    public List<Integer> getTriGift() {
        return triGift;
    }

    public void setTriGift(List<Integer> triGift) {
        this.triGift = triGift;
    }

    public int getTriGiftTime() {
        return triGiftTime;
    }

    public void setTriGiftTime(int triGiftTime) {
        this.triGiftTime = triGiftTime;
    }

    public List<Integer> getSpendPower() {
        return spendPower;
    }

    public void setSpendPower(List<Integer> spendPower) {
        this.spendPower = spendPower;
    }

    /**
     * 检测玩家是否满足条件
     *
     * @param player 玩家对象
     * @return true 满足触发条件 false 不满足
     */
    public boolean checkTriggerOpenCnf(Player player) {
        // 角色等级
        int level = player.lord.getLevel();
        // 角色Vip
        int vip = player.lord.getVip();
        // 角色的创角第几天
        int createRoleDate = DateHelper.dayiy(player.account.getCreateDate(), new Date());
        // 判断等级 并且 配置不为空
        return (!CheckNull.isEmpty(getLevel()) && getLevel().get(0) <= level && getLevel().get(1) >= level) &&
                // 判断Vip 并且 配置不为空
                (!CheckNull.isEmpty(getVip()) && getVip().get(0) <= vip && getVip().get(1) >= vip) &&
                // 判断创建角色时间 并且 配置不为空
                (!CheckNull.isEmpty(getCreateRoleDate()) && getCreateRoleDate().get(0) <= createRoleDate && getCreateRoleDate().get(1) >= createRoleDate)
                // 判断最近的充值的金额 并且配置不为空
                && checkTriggerSpendPower(player);
    }

    /**
     * 检测最近的充值水平
     * @param player 玩家对象
     * @return true 达到 false 未达到
     */
    private boolean checkTriggerSpendPower(Player player) {
        if (!CheckNull.isEmpty(this.spendPower)) {
            int day = this.spendPower.get(0);
            int need = this.spendPower.get(1);
            LocalDateTime now = LocalDateTime.now();
            // 总充值金额
            int sum =  Stream.iterate(0, i -> ++i)
                    .limit(day)
                    // 往前取几天的数据, 栗子: 星期一的充值数据  10000 (前置key) + 1 (星期)
                    .map(sub -> now.minusDays(sub).getDayOfWeek().getValue() + PlayerConstant.RECENTLY_PAY)
                    // 充值金额
                    .mapToInt(player::getMixtureDataById)
                    .sum();
            // 大于配置的付费能力
            return sum >= need;
        }
        return true;
    }
}
