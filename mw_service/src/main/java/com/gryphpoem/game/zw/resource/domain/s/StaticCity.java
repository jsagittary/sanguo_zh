package com.gryphpoem.game.zw.resource.domain.s;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticNpcDataMgr;
import com.gryphpoem.game.zw.resource.common.ServerSetting;
import com.gryphpoem.game.zw.resource.constant.Constant;
import com.gryphpoem.game.zw.resource.constant.ScheduleConstant;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.MapHelper;
import com.gryphpoem.game.zw.resource.util.RandomHelper;
import com.gryphpoem.game.zw.resource.util.Turple;
import org.springframework.util.ObjectUtils;
import com.gryphpoem.game.zw.service.WorldScheduleService;

import java.util.ArrayList;
import java.util.List;

public class StaticCity {
    /**
     * 城池id
     */
    private int cityId;
    /**
     * 城池名称,
     */
    private String desc;
    /**
     * 城池所属的区（州郡）
     */
    private int area;
    /**
     * 城池所在的世界地图坐标
     */
    private int cityPos;
    /**
     * 城池在地图上占有的所有坐标点，格式:[pos1,pos2,pos3...]
     */
    private List<Integer> posList;
    /**
     * 城池等级
     */
    private int lv;
    /**
     * 发起国战的倒计时准备时间，单位：秒
     */
    private int countdown;
    /**
     * 产出一次的时间
     */
    private int produceTime;
    /**
     * 阵营归属是非npc时 城池初始兵力阵型，格式：[npcId,npcId...]
     */
    private List<Integer> form;
    /**
     * 阵营归属是npc城时 首杀初始兵力阵型，格式：[npcId,npcId...]
     */
    private List<Integer> firstForm;
    /**
     * 阵营归属是非npc时 更具服务器id来读取此字段
     */
    private List<Integer> combineForm;
    /**
     * 阵营归属是npc城时 更具服务器id来读取此字段
     */
    private List<Integer> combineFirstForm;
    /**
     * 阵营归属是非npc时 世界进程11阶段读取此字段
     */
    private List<Integer> form_11;
    /**
     * 阵营归属是npc时 世界进程11阶段读取此字段
     */
    private List<Integer> form_11first;
    /**
     * 阵营归属是非npc时 世界进程12阶段读取此字段
     */
    private List<Integer> form_12;
    /**
     * 阵营归属是npc时 世界进程12阶段读取此字段
     */
    private List<Integer> form_12first;
    /**
     * 阵营归属是非npc时 世界进程13阶段读取此字段
     */
    private List<Integer> form_13;
    /**
     * 阵营归属是npc时 世界进程13阶段读取此字段
     */
    private List<Integer> form_13first;
    /**
     * 城池占领后的产出列表，从列表中随机，格式：[[type,id,count,weight]]
     */
    private List<List<Integer>> dropList = new ArrayList<>();
    private int dropTotalWeight;
    /**
     * 征收消耗
     */
    private List<List<Integer>> levy;
    /**
     * 重建消耗
     */
    private List<List<Integer>> rebuild;
    /**
     * 单次修复消耗
     */
    private List<List<Integer>> repair;
    /**
     * 城主额外奖励
     */
    private List<List<Integer>> extraRewad;
    /**
     * 城的类型，郡的营县郡城为123，州的郡府州为456，皇城的都名城洛阳为789
     */
    private int type;
    /**
     * 对应s_city_buff表中的ID, 格式[[增益],[减益]]
     */
    private List<List<Integer>> buff;
    /**
     * 军威值类型,会加到阵营中
     */
    private int cityPointType;
    /**
     * 产出的军威值点数
     */
    private int cityPoint;

    /**
     * 攻打前置条件，填写城市ID，[Id1,Id2,Id3...]
     */
    private List<Integer> attackPrecondition;
    /** 默认的阵营 */
    private int camp;
    /** 开放程度 新地图使用 */
    private int open;
    /** 拥有的地块 */
    private List<Integer> ownCellId;

    //竞选奖励
    private List<Integer> inAward;
    //重建奖励
    private List<Integer> outAward;

    private Turple<Integer, Integer> xy;

    private int totalArm = -1;

    public int getCityId() {
        return cityId;
    }

    public void setCityId(int cityId) {
        this.cityId = cityId;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public int getArea() {
        return area;
    }

    public void setArea(int area) {
        this.area = area;
    }

    public int getCityPos() {
        return cityPos;
    }

    public void setCityPos(int cityPos) {
        this.cityPos = cityPos;
    }

    public List<Integer> getPosList() {
        return posList;
    }

    public void setPosList(List<Integer> posList) {
        this.posList = posList;
    }

    public int getLv() {
        return lv;
    }

    public void setLv(int lv) {
        this.lv = lv;
    }

    public int getCountdown() {
        return countdown;
    }

    public void setCountdown(int countdown) {
        this.countdown = countdown;
    }

    public int getProduceTime() {
        return produceTime;
    }

    public void setProduceTime(int produceTime) {
        this.produceTime = produceTime;
    }

    public List<Integer> getForm() {
        return form;
    }

    public void setForm(List<Integer> form) {
        this.form = form;
    }

    public List<Integer> getFirstForm() {
        return firstForm;
    }

    public void setFirstForm(List<Integer> firstForm) {
        this.firstForm = firstForm;
    }

    public void setDropList(List<List<Integer>> dropList) {
        if (ObjectUtils.isEmpty(dropList)) {
            LogUtil.error("城池随机奖励配置, StaticCity:", this.toString());
            return;
        }
        this.dropList = dropList;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public List<List<Integer>> getBuff() {
        return buff;
    }

    public void setBuff(List<List<Integer>> buff) {
        this.buff = buff;
    }

    public int getCityPointType() {
        return cityPointType;
    }

    public void setCityPointType(int cityPointType) {
        this.cityPointType = cityPointType;
    }

    public int getCityPoint() {
        return cityPoint;
    }

    public void setCityPoint(int cityPoint) {
        this.cityPoint = cityPoint;
    }

    public List<Integer> getBuff(boolean isUp) {
        List<Integer> list = null;
        if (!CheckNull.isEmpty(buff)) {
            list = isUp ? buff.get(0) : buff.get(1);
        }
        return list;
    }

    public void calcWeight(String operation) {
        if (!dropList.isEmpty()) {
            if (dropTotalWeight != 0) {
                LogUtil.error("城池随机奖励权重数据错误!, StaticCity:", this.toString(), ", operation:", operation);
                return;
            }

            for (List<Integer> list : dropList) {
                if (list.size() > 2) {
                    dropTotalWeight += list.get(3);
                }
            }
        }
    }

    /**
     * 随机获取一个产出
     *
     * @return
     */
    public List<Integer> randomDropReward() {
        // 如果总权重为0, 就重新计算一下总权重
        if (!dropList.isEmpty() && dropTotalWeight == 0) {
            calcWeight("runtime");
        }
        int random = RandomHelper.randomInSize(dropTotalWeight);
        List<Integer> randomList = new ArrayList<>();
        int temp = 0;
        if (dropList != null) {
            for (List<Integer> list : dropList) {
                if (list.size() > 2) {
                    temp += list.get(3);
                    if (temp >= random) {
                        LogUtil.debug(
                                "城池获得奖励dropTotalWeight=" + dropTotalWeight + ",random=" + random + ",temp=" + temp);
                        randomList.addAll(list.subList(0, 3));
                        break;
                    }
                }
            }
        }

        if (randomList.isEmpty()) {
            LogUtil.error("城池随机奖励失败!, StaticCity:", this.toString(), ", random:", random);
        }
        return randomList;
    }

    public List<List<Integer>> getLevy() {
        return levy;
    }

    public void setLevy(List<List<Integer>> levy) {
        this.levy = levy;
    }

    public List<List<Integer>> getRebuild() {
        return rebuild;
    }

    public void setRebuild(List<List<Integer>> rebuild) {
        this.rebuild = rebuild;
    }

    public List<List<Integer>> getRepair() {
        return repair;
    }

    public void setRepair(List<List<Integer>> repair) {
        this.repair = repair;
    }

    public List<List<Integer>> getExtraRewad() {
        return extraRewad;
    }

    public void setExtraRewad(List<List<Integer>> extraRewad) {
        this.extraRewad = extraRewad;
    }

    /**
     * 获取城池当前的阵型配置
     * @param npcCity 是否是npc城池
     * @return 阵营配置
     */
    public List<Integer> getFormList(boolean npcCity) {
        WorldScheduleService scheduleService = DataResource.ac.getBean(WorldScheduleService.class);
        // 当前的世界进程阶段
        int curSchId = scheduleService.getCurrentSchduleId();
        List<Integer> formList = null;
        if (curSchId > ScheduleConstant.SCHEDULE_BERLIN_ID) {
            // 大于第10进程阶段
            switch (curSchId) {
                case ScheduleConstant.SCHEDULE_ID_11:
                    formList = npcCity ? getForm_11first() : getForm_11();
                    break;
                case ScheduleConstant.SCHEDULE_ID_12:
                    formList = npcCity ? getForm_12first() : getForm_12();
                    break;
                case ScheduleConstant.SCHEDULE_ID_13:
                    formList = npcCity ? getForm_13first() : getForm_13();
                    break;
            }
        } else {
            formList = npcCity ? getFirstForm() : getForm();
        }
        return formList;
    }

    /**
     * 当前服务器id 是否在 system表 id=292的配置中
     *
     * @return true 在配置的区服区间内
     */
    private boolean isInServerList() {
        // 服务器Id判断
        List<List<Integer>> serverIdList = Constant.CITY_FORM_SERVERID;
        if (!CheckNull.isEmpty(serverIdList)) {
            final int selfServerId = DataResource.ac.getBean(ServerSetting.class).getServerID();
            return serverIdList.stream()
                    .filter(l -> !CheckNull.isEmpty(l) && l.get(0) <= selfServerId && selfServerId <= l.get(1))
                    .findFirst().orElse(null) != null;
        }
        return false;
    }

    /**
     * 获取城池NPC总兵力
     *
     * @return
     */
    public int getTotalArm() {
        if (totalArm < 0) {
            totalArm = 0;
            for (Integer npcId : form) {
                StaticNpc npc = StaticNpcDataMgr.getNpcMap().get(npcId);
                totalArm += npc.getTotalArm();
            }
        }
        return totalArm;
    }

    /**
     * 获取城池所在坐标的XY轴数据
     *
     * @return
     */
    public Turple<Integer, Integer> getCityPosXy() {
        if (null == xy) {
            xy = MapHelper.reducePos(cityPos);
        }
        return xy;
    }

    public List<Integer> getCombineForm() {
        return combineForm;
    }

    public void setCombineForm(List<Integer> combineForm) {
        this.combineForm = combineForm;
    }

    public List<Integer> getCombineFirstForm() {
        return combineFirstForm;
    }

    public void setCombineFirstForm(List<Integer> combineFirstForm) {
        this.combineFirstForm = combineFirstForm;
    }

    public List<Integer> getAttackPrecondition() {
        return attackPrecondition;
    }

    public void setAttackPrecondition(List<Integer> attackPrecondition) {
        this.attackPrecondition = attackPrecondition;
    }

    public int getCamp() {
        return camp;
    }

    public void setCamp(int camp) {
        this.camp = camp;
    }

    public int getOpen() {
        return open;
    }

    public void setOpen(int open) {
        this.open = open;
    }

    public List<Integer> getOwnCellId() {
        return ownCellId;
    }

    public void setOwnCellId(List<Integer> ownCellId) {
        this.ownCellId = ownCellId;
    }

    public List<Integer> getForm_11() {
        return form_11;
    }

    public void setForm_11(List<Integer> form_11) {
        this.form_11 = form_11;
    }

    public List<Integer> getForm_11first() {
        return form_11first;
    }

    public void setForm_11first(List<Integer> form_11first) {
        this.form_11first = form_11first;
    }

    public List<Integer> getForm_12() {
        return form_12;
    }

    public void setForm_12(List<Integer> form_12) {
        this.form_12 = form_12;
    }

    public List<Integer> getForm_12first() {
        return form_12first;
    }

    public void setForm_12first(List<Integer> form_12first) {
        this.form_12first = form_12first;
    }

    @Override
    public String toString() {
        return "StaticCity{" +
                "cityId=" + cityId +
                ", desc='" + desc + '\'' +
                ", area=" + area +
                ", cityPos=" + cityPos +
                ", posList=" + posList +
                ", lv=" + lv +
                ", countdown=" + countdown +
                ", produceTime=" + produceTime +
                ", form=" + form +
                ", firstForm=" + firstForm +
                ", combineForm=" + combineForm +
                ", combineFirstForm=" + combineFirstForm +
                ", form_11=" + form_11 +
                ", form_11first=" + form_11first +
                ", form_12=" + form_12 +
                ", form_12first=" + form_12first +
                ", dropList=" + (((ArrayList) dropList).toString()) +
                ", dropTotalWeight=" + dropTotalWeight +
                ", levy=" + levy +
                ", rebuild=" + rebuild +
                ", repair=" + repair +
                ", extraRewad=" + extraRewad +
                ", type=" + type +
                ", buff=" + buff +
                ", cityPointType=" + cityPointType +
                ", cityPoint=" + cityPoint +
                ", attackPrecondition=" + attackPrecondition +
                ", camp=" + camp +
                ", open=" + open +
                ", ownCellId=" + ownCellId +
                ", xy=" + xy +
                ", totalArm=" + totalArm +
                '}';
    }
    public List<Integer> getInAward() {
        return inAward;
    }

    public void setInAward(List<Integer> inAward) {
        this.inAward = inAward;
    }

    public List<Integer> getOutAward() {
        return outAward;
    }

    public void setOutAward(List<Integer> outAward) {
        this.outAward = outAward;
    }

    public List<Integer> getForm_13() {
        return form_13;
    }

    public void setForm_13(List<Integer> form_13) {
        this.form_13 = form_13;
    }

    public List<Integer> getForm_13first() {
        return form_13first;
    }

    public void setForm_13first(List<Integer> form_13first) {
        this.form_13first = form_13first;
    }
}
