package com.gryphpoem.game.zw.resource.pojo.sandtable;

import com.gryphpoem.game.zw.pb.SerializePb;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class FightReplay {
    public int onlyIdx;
    public FightObject obj1;
    public FightObject obj2;
    public HisMatch.WarReportInfo warReportInfo;

    public FightReplay(){}

    public FightReplay (FightObject obj1,FightObject obj2,int onlyIdx){
        this.obj1 = obj1;
        this.obj2 = obj2;
        this.onlyIdx = onlyIdx;
    }

    public static class FightObject {
        public long lordId;
        public String lordNick;
        public int attackTimes;
        public boolean isWin;
        public List<FightHeroDetail> heroDetails = new ArrayList<>();

        public void deser(SerializePb.SerFightObject serFightObject){
            this.lordId = serFightObject.getLordId();
            this.lordNick = serFightObject.getLordNick();
            this.attackTimes = serFightObject.getAttackTimes();
            this.isWin = serFightObject.getIsWin();
            Optional.ofNullable(serFightObject.getHeroDetailsList()).ifPresent(tmp -> tmp.forEach(o -> {
                FightHeroDetail detail = new FightHeroDetail(o.getHeroId(),o.getKill(),o.getLost(),o.getHp(),o.getHeroDecorated());
                this.heroDetails.add(detail);
            }));
        }
    }

    public static class FightHeroDetail {
        public int heroId;
        public int kill;
        public int lost;
        public int hp;
        public int heroDecorated;

        public FightHeroDetail(){}

        public FightHeroDetail(int heroId,int kill,int lost,int hp,int heroDecorated){
            this.heroId = heroId;
            this.kill = kill;
            this.lost = lost;
            this.hp = hp;
            this.heroDecorated = heroDecorated;
        }
    }

    public void deser(SerializePb.SerFightReplay serFightReplay){
        if(Objects.nonNull(serFightReplay.getObj1()) && Objects.nonNull(serFightReplay.getObj2())){
            FightObject obj1_ = new FightObject();
            obj1_.deser(serFightReplay.getObj1());
            FightObject obj2_ = new FightObject();
            obj2_.deser(serFightReplay.getObj2());
            this.obj1 = obj1_;
            this.obj2 = obj2_;
        }
        this.onlyIdx = serFightReplay.getOnlyIdx();
        if(serFightReplay.hasInfo()){
            HisMatch.WarReportInfo info = new HisMatch.WarReportInfo();
            info.deser(serFightReplay.getInfo());
            this.warReportInfo = info;
        }
    }
}