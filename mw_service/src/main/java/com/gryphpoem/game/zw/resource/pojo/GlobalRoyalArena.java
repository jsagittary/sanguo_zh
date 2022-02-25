package com.gryphpoem.game.zw.resource.pojo;

import com.google.protobuf.InvalidProtocolBufferException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticActivityDataMgr;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.SerializePb;
import com.gryphpoem.game.zw.resource.constant.Constant;
import com.gryphpoem.game.zw.resource.domain.ActivityBase;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.GlobalActivity;
import com.gryphpoem.game.zw.resource.domain.s.StaticRoyalArenaTask;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.RandomUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 阵营对拼的活动
 * User:        zhoujie
 * Date:        2020/4/2 12:59
 * Description:
 */
public class GlobalRoyalArena extends GlobalActivityData {

    private Map<Long, PersonRoyalArena> personRoyalArena = new HashMap<>();

    private Map<Integer, CampRoyalArena> campCampRoyalArena = new HashMap<>();

    @Override
    public boolean isReset(int begin, Player player) {
        boolean reset = super.isReset(begin, player);
        if (!reset) {
            return false;
        }
        LogUtil.debug("阵营对拼的活动重开 clearData");
        this.personRoyalArena.clear();
        this.campCampRoyalArena.clear();
        for (int camp : Constant.Camp.camps) {
            this.campCampRoyalArena.put(camp, new CampRoyalArena(camp));
        }
        return true;
    }

    /**
     * 转点清除数据
     */
    public void clearTaskAndData() {
        this.personRoyalArena.values().forEach(PersonRoyalArena::clearTaskAndData);
        this.campCampRoyalArena.values().forEach(CampRoyalArena::clearTaskAndData);
    }


    /**
     * 初始化
     *
     * @param activityBase
     * @param begin
     */
    public GlobalRoyalArena(ActivityBase activityBase, int begin) {
        super(activityBase, begin);
        // 初始化
        if (CheckNull.isEmpty(this.campCampRoyalArena)) {
            for (int camp : Constant.Camp.camps) {
                this.campCampRoyalArena.put(camp, new CampRoyalArena(camp));
            }
        }
    }

    /**
     * 反序列化
     *
     * @param globalActivity 数据库数据
     * @throws InvalidProtocolBufferException
     */
    public GlobalRoyalArena(GlobalActivity globalActivity) throws InvalidProtocolBufferException {
        super(globalActivity);
        if (globalActivity.getRoyalArena() != null) {
            SerializePb.DbGlobalRoyalArenaData ser = SerializePb.DbGlobalRoyalArenaData.parseFrom(globalActivity.getRoyalArena());
            for (CommonPb.PersonRoyalData personInfo : ser.getPersonInfoList()) {
                this.personRoyalArena.put(personInfo.getRoleId(), new PersonRoyalArena(personInfo));
            }
            for (CommonPb.CampRoyalData campRoyalData : ser.getCampInfoList()) {
                this.campCampRoyalArena.put(campRoyalData.getCamp(), new CampRoyalArena(campRoyalData));
            }
        }
    }


    /**
     * 序列化
     *
     * @return
     */
    @Override
    public GlobalActivity copyData() {
        GlobalActivity globalActivity = super.copyData();
        SerializePb.DbGlobalRoyalArenaData.Builder builder = SerializePb.DbGlobalRoyalArenaData.newBuilder();
        this.personRoyalArena.values().forEach(p -> builder.addPersonInfo(p.ser()));
        this.campCampRoyalArena.values().forEach(c -> builder.addCampInfo(c.ser()));
        globalActivity.setRoyalArena(builder.build().toByteArray());
        return globalActivity;
    }

    /**
     * 获取玩家数据
     *
     * @param roleId
     * @return
     */
    public PersonRoyalArena getPersonInfoById(long roleId) {
        PersonRoyalArena personInfo = this.personRoyalArena.get(roleId);
        if (personInfo == null) {
            personInfo = new PersonRoyalArena(roleId);
            // 初始化玩家的任务
            refreshTask(personInfo);
            this.personRoyalArena.put(roleId, personInfo);
        }
        return personInfo;
    }

    /**
     * 刷新玩家任务
     *
     * @param personInfo 个人数据
     */
    public void refreshTask(PersonRoyalArena personInfo) {
        int activityId = this.getActivityId();

        // 活动的配置
        List<StaticRoyalArenaTask> royalArenaTaskList = StaticActivityDataMgr.getRoyalArenaTaskList(activityId);
        if (CheckNull.isEmpty(royalArenaTaskList)) {
            LogUtil.error("阵营对拼活动， 活动配置错误!!! actId:", activityId);
        }
        if (!CheckNull.isNull(personInfo)) {
            personInfo.refreshTask(RandomUtil.getListRandom(royalArenaTaskList, 1).get(0));
        }
    }

    public Map<Integer, CampRoyalArena> getCampCampRoyalArena() {
        return campCampRoyalArena;
    }

    public CampRoyalArena getCampCampRoyalArenaByCamp(int camp) {
        return campCampRoyalArena.get(camp);
    }

    public Map<Long, PersonRoyalArena> getPersonRoyalArena() {
        return personRoyalArena;
    }


}
