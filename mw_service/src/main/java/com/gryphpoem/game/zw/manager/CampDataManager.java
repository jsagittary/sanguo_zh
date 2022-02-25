package com.gryphpoem.game.zw.manager;

import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticBuildingDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticPartyDataMgr;
import com.gryphpoem.game.zw.resource.constant.Constant;
import com.gryphpoem.game.zw.resource.constant.DataSaveConstant;
import com.gryphpoem.game.zw.resource.dao.impl.p.CampDao;
import com.gryphpoem.game.zw.resource.dao.impl.p.CampMemberDao;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.CampMember;
import com.gryphpoem.game.zw.resource.domain.p.DbParty;
import com.gryphpoem.game.zw.resource.pojo.party.Camp;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import com.gryphpoem.game.zw.server.SavePartyServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @ClassName PartyDataManager.java
 * @Description 阵营数据管理类
 * @author TanDonghai
 * @date 创建时间：2017年4月25日 下午3:37:01
 *
 */
@Component
public class CampDataManager {

    @Autowired
    private CampDao campDao;

    @Autowired
    private CampMemberDao campMemberDao;

    @Autowired
    private PlayerDataManager playerDataManager;

    private Map<Integer, Camp> partyMap = new ConcurrentHashMap<>();

    private Map<Long, CampMember> memberMap = new ConcurrentHashMap<>();

    public void init() {
        List<DbParty> partyList = campDao.selectParty();
        if (CheckNull.isEmpty(partyList)) {// 首次创建军团数据
            Camp party;
            int partyLv = StaticPartyDataMgr.getMinPartyLv();
            int cabinetLv = StaticBuildingDataMgr.getCabinetMinLv();
            for (int camp = Constant.Camp.EMPIRE; camp <= Constant.Camp.UNION; camp++) {
                party = new Camp();
                party.setCamp(camp);
                party.setPartyLv(partyLv);
                party.setPartyExp(0);
                party.setSlogan("");
                party.setAuthor("");
                party.setQq("");
                party.setWx("");
                party.setCabinetLeadLv(cabinetLv);
                party.setCabinetLeadExp(0L);
                partyMap.put(camp, party);

                campDao.insertParty(party.ser());
            }
        } else {
            for (DbParty dbParty : partyList) {
                partyMap.put(dbParty.getCamp(), new Camp(dbParty));
            }
        }

        List<CampMember> campMemberList = campMemberDao.load();
        if (!CheckNull.isEmpty(campMemberList)) {
            for (CampMember dbCampMember : campMemberList) {
                memberMap.put(dbCampMember.getRoleId(), dbCampMember);
            }
        }

        // 初始化军团战力排行榜
        for (Player player : playerDataManager.getPlayers().values()) {
            addPartyFightRank(player.lord.getCamp(), player.roleId, player.lord.getFight());
        }
    }

    private void addPartyFightRank(int camp, long roleId, long fight) {
        Camp party = getParty(camp);
        if (null == party) {
            LogUtil.error("添加军团战力排行榜，未找到Party, camp:", camp, ", roleId:", roleId);
            return;
        }

        party.addFightRank(roleId, fight);
    }

    private int lastPartySaveTime;// 记录最后一次保存数据的时间

    public void savePartyTimerLogic() {
        int now = TimeHelper.getCurrentSecond();
        if (now - lastPartySaveTime >= DataSaveConstant.PARTY_DATA_SAVE_INTERVAL_SECOND) {
            for (Camp camp : partyMap.values()) {
                try {
                    SavePartyServer.getIns().saveData(camp.ser());
                } catch (Exception e) {
                    LogUtil.error("Party数据保存定时任务出错", e);
                }
            }

            lastPartySaveTime = now;
        }
    }

    public void updateParty(DbParty dbParty) {
        campDao.save(dbParty);
    }

    public void updatePartyMember(long roleId) {
        CampMember member = getCampMember(roleId);
        if (null != member) {
            if (campMemberDao.updatePartyMember(member) == 0) {
                campMemberDao.insertPartyMember(member);
            }
        }
    }

    public Camp getParty(int camp) {
        return partyMap.get(camp);
    }

    public Map<Integer, Camp> getPartyMap() {
        return partyMap;
    }

    public CampMember getCampMember(long roleId) {
        CampMember member = memberMap.get(roleId);
        if (null == member) {
            member = new CampMember();
            member.setRoleId(roleId);
            memberMap.put(roleId, member);
        }
        return member;
    }

    public Map<Long, CampMember> getMemberMap() {
        return memberMap;
    }

}
