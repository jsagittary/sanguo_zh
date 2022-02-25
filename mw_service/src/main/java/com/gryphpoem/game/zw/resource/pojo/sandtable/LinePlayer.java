package com.gryphpoem.game.zw.resource.pojo.sandtable;

import com.gryphpoem.game.zw.pb.SerializePb;
import com.gryphpoem.game.zw.resource.pojo.fight.Fighter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LinePlayer implements Comparable<LinePlayer> {
    public long lordId;
    public List<Integer> heroIds;
    public int fightTimes;
    public int fightVal;
    public int line;
    public int killingNum;

    public Fighter fighter;

    public int tmpLv;
    public int tmpRanks;

    public boolean tmpFight;
    public boolean tmpFought;

    public boolean isAlive = true;//是否存活

    public LinePlayer(){
        this.heroIds = new ArrayList<>();
    }
    public LinePlayer(long lordId){
        this.lordId = lordId;
        this.heroIds = new ArrayList<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LinePlayer that = (LinePlayer) o;
        return lordId == that.lordId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(lordId);
    }

    @Override
    public int compareTo(LinePlayer o) {
        return Integer.compare(this.fightVal,o.fightVal);
    }

    public int getFightVal() {
        return fightVal;
    }

    public void deser(SerializePb.SerLinePlayer serLinePlayer){
        this.lordId = serLinePlayer.getLordId();
        this.heroIds.addAll(serLinePlayer.getHeroIdsList());
        this.fightTimes = serLinePlayer.getFightTimes();
        this.fightVal = serLinePlayer.getFightVal();
        this.line = serLinePlayer.getLine();
        this.killingNum = serLinePlayer.getKillingNum();
    }
}
