package com.gryphpoem.game.zw.resource.domain.p;

import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.resource.pojo.army.Army;
import com.gryphpoem.game.zw.resource.util.CheckNull;

import java.util.LinkedList;

public class MineData {

    private int mineId;

    private LinkedList<Long> collectTeam;

    public MineData() {
    }

    public MineData(int mineId) {
        this.mineId = mineId;
        this.collectTeam = new LinkedList<>();
    }

    public int getMineId() {
        return mineId;
    }

    public void setMineId(int mineId) {
        this.mineId = mineId;
    }

    public LinkedList<Long> getCollectTeam() {
        return collectTeam;
    }

    public void setCollectTeam(LinkedList<Long> collectTeam) {
        this.collectTeam = collectTeam;
    }

    public static MineData serMineData(CommonPb.MineDataPb mineDataPb) {
        if (CheckNull.isNull(mineDataPb))
            return null;

        MineData mineData = new MineData();
        mineData.setMineId(mineDataPb.getMineId());
        mineData.setCollectTeam(new LinkedList<>(mineDataPb.getCollectTeamList()));
        return mineData;
    }

    public static MineData serMineData(Integer mineId) {
        if (CheckNull.isNull(mineId))
            return null;

        MineData mineData = new MineData();
        mineData.setMineId(mineId);
        mineData.setCollectTeam(new LinkedList<>());
        return mineData;
    }

    public synchronized void addCollectTeam(Long lordId) {
        this.collectTeam.offer(lordId);
    }

    public synchronized void removeCollectTeam(Long lordId) {
        this.collectTeam.remove(lordId);
    }

    public synchronized void clearCollectTeam() {
        this.collectTeam.clear();
    }
}
