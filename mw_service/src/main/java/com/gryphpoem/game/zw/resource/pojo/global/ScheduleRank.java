package com.gryphpoem.game.zw.resource.pojo.global;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.s.StaticSchedule;
import com.gryphpoem.game.zw.resource.util.CheckNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author QiuKun
 * @ClassName ScheduleRank.java
 * @Description 世界进度排行榜
 * @date 2019年2月21日
 */
public class ScheduleRank {

    /**
     * 排行榜类型
     */
    private int rankType;

    /**
     * 没有就是 0
     */
    private int rankParam;

    /**
     * 排行榜是否区分区域 0 不区分, 1 区分
     */
    private int rankArea;

    /**
     * 排行榜
     */
    private List<ScheduleRankItem> rank = new ArrayList<>();

    /**
     * 个人排行
     */
    private List<ScheduleRankItem> personRank = new ArrayList<>();

    public ScheduleRank() {
    }

    public ScheduleRank(StaticSchedule sSch) {
        this();
        this.rankType = sSch.getRankType();
        this.rankParam = sSch.getRankParam();
        this.rankArea = sSch.getRankArea();
    }

    public int getRankType() {
        return rankType;
    }

    public void setRankType(int rankType) {
        this.rankType = rankType;
    }

    public int getRankParam() {
        return rankParam;
    }

    public void setRankParam(int rankParam) {
        this.rankParam = rankParam;
    }

    public int getRankArea() {
        return rankArea;
    }

    public void setRankArea(int rankArea) {
        this.rankArea = rankArea;
    }

    public List<ScheduleRankItem> getRank() {
        return rank;
    }

    public void setRank(List<ScheduleRankItem> rank) {
        this.rank = rank;
    }

    public List<ScheduleRankItem> getPersonRank() {
        return personRank;
    }

    public void setPersonRank(List<ScheduleRankItem> personRank) {
        this.personRank = personRank;
    }

    /**
     * 序列化
     *
     * @return
     * @param isSer
     */
    public CommonPb.ScheduleRank ser(boolean isSer) {
        CommonPb.ScheduleRank.Builder serRankBuild = CommonPb.ScheduleRank.newBuilder();
        serRankBuild.setRankParam(this.getRankParam());
        serRankBuild.setRankType(this.getRankType());
        serRankBuild.setRankArea(this.getRankArea());
        List<ScheduleRankItem> rankItems = this.getRank();
        if (rankItems != null && !rankItems.isEmpty()) {
            rankItems.forEach(rankItem -> {
                CommonPb.ScheduleRankItem.Builder serRankItem = CommonPb.ScheduleRankItem.newBuilder();
                serRankItem.setCamp(rankItem.getCamp());
                serRankItem.setArea(rankItem.getArea());
                serRankItem.setValue(rankItem.getValue());
                serRankBuild.addItem(serRankItem.build());
            });
        }
        // 个人排行
        List<ScheduleRankItem> personRank = this.getPersonRank();
        if (!CheckNull.isEmpty(personRank)) {
            // 给客户端的只发10条
            if (!isSer) {
                personRank = personRank.stream().limit(10).collect(Collectors.toList());
            }
            PlayerDataManager playerDataManager = DataResource.ac.getBean(PlayerDataManager.class);
            personRank.forEach(rankItem -> {
                CommonPb.ScheduleRankItem.Builder serRankItem = CommonPb.ScheduleRankItem.newBuilder();
                serRankItem.setValue(rankItem.getValue());
                serRankItem.setRoleId(rankItem.getRoleId());
                // 把玩家的昵称发给客户端
                Player p = playerDataManager.getPlayer(rankItem.getRoleId());
                if (!CheckNull.isNull(p)) {
                    serRankItem.setNick(p.lord.getNick());
                    serRankItem.setCamp(p.lord.getCamp());
                }
                serRankBuild.addPersonItem(serRankItem.build());
            });
        }
        return serRankBuild.build();

    }

    /**
     * 反序列化排行榜
     *
     * @param serRank
     */
    public ScheduleRank(CommonPb.ScheduleRank serRank) {
        this();
        if (serRank != null) {
            this.setRankParam(serRank.getRankParam());
            this.setRankArea(serRank.getRankArea());
            this.setRankType(serRank.getRankType());
            List<CommonPb.ScheduleRankItem> itemList = serRank.getItemList();
            if (itemList != null && !itemList.isEmpty()) {
                List<ScheduleRankItem> collect = itemList.stream()
                        .filter(item -> item != null)
                        .map(item -> new ScheduleRankItem(item.getCamp(), item.getArea(), item.getValue()))
                        .collect(Collectors.toList());
                this.setRank(collect);
            }
            List<CommonPb.ScheduleRankItem> pItemList = serRank.getPersonItemList();
            if (pItemList != null && !pItemList.isEmpty()) {
                List<ScheduleRankItem> collect = pItemList.stream()
                        .filter(Objects::nonNull)
                        .map(item -> new ScheduleRankItem(item.getRoleId(), item.getValue()))
                        .collect(Collectors.toList());
                this.setPersonRank(collect);
            }
        }
    }


    @Override
    public String toString() {
        return "ScheduleRank{" +
                "rankType=" + rankType +
                ", rankParam=" + rankParam +
                ", rankArea=" + rankArea +
                ", rank=" + rank +
                '}';
    }
}
