package com.gryphpoem.game.zw.mgr;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.protobuf.InvalidProtocolBufferException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticCrossDataMgr;
import com.gryphpoem.game.zw.model.CrossPlayer;
import com.gryphpoem.game.zw.model.fort.Fortress;
import com.gryphpoem.game.zw.model.fort.RoleForce;
import com.gryphpoem.game.zw.model.global.BaseGlobalSaveModel;
import com.gryphpoem.game.zw.model.global.GlobalModelType;
import com.gryphpoem.game.zw.model.player.CrossHero;
import com.gryphpoem.game.zw.pb.GamePb5.SyncCrossFortRs;
import com.gryphpoem.game.zw.pb.SerializePb.DbFortress;
import com.gryphpoem.game.zw.pb.SerializePb.SerCrossFort;
import com.gryphpoem.game.zw.util.CrossWarFinlishClear;
import com.gryphpoem.game.zw.util.PbMsgUtil;

/**
 * @ClassName FortressMgr.java
 * @Description
 * @author QiuKun
 * @date 2019年5月16日
 */
@Component
public class FortressMgr implements CrossWarFinlishClear, BaseGlobalSaveModel {

    @Autowired
    private PlayerMgr playerMgr;
    @Autowired
    private SessionMgr sessionMgr;

    /** 堡垒信息 <堡垒id,Fortress> */
    private final Map<Integer, Fortress> fortresses = new ConcurrentHashMap<>();

    public void init() {
        // 初始化堡垒
        fortresses.clear();
        StaticCrossDataMgr.getFortMap().values().forEach(cfg -> {
            fortresses.put(cfg.getId(), Fortress.createFortressByCfg(cfg));
        });
    }

    public Map<Integer, Fortress> getFortresses() {
        return fortresses;
    }

    public Fortress getFortress(int id) {
        return fortresses.get(id);
    }

    /**
     * 广播当前所有堡垒信息
     */
    public void broadcastCrossFort() {
        SyncCrossFortRs rsPb = SyncCrossFortRs.newBuilder()
                .addAllFortress(fortresses.values().stream().map(Fortress::toFortressPb).collect(Collectors.toList()))
                .build();
        playerMgr.getPlayerMap().values().stream().filter(CrossPlayer::isFouce).forEach(p -> {
            sessionMgr.sendMsg(PbMsgUtil
                    .okBase(SyncCrossFortRs.EXT_FIELD_NUMBER, p.getLordId(), SyncCrossFortRs.ext, rsPb).build(),
                    p.getMainServerId());
        });
    }

    /**
     * 加入堡垒 (新加入, 或复活后加入)
     * 
     * @param crossHeroList
     * @param fortId
     * @param camp
     */
    public void joinCrossHeroFortess(List<CrossHero> crossHeroList, int fortId, int camp) {
        Fortress fortress = getFortress(fortId);
        if (fortress == null) {
            LogUtil.error("未初始化对应的堡垒 fortId:", fortId);
            return;
        }
        for (CrossHero crossHero : crossHeroList) {
            RoleForce f = RoleForce.createInstance(crossHero, camp);
            fortress.joinRoleForce(f);
        }
        broadcastCrossFort();
    }

    /**
     * 将领 从一个堡垒移动到另一个堡垒 ,用于 前往，进攻，偷袭，回防
     * 
     * @param crossHero
     * @param fortId
     * @param camp
     */
    public void moveToFortess(CrossPlayer crossPlayer, CrossHero crossHero, int fortId) {
        Fortress reqFortress = getFortress(fortId);
        Fortress heroFortess = getFortress(crossHero.getFortId());
        crossHero.setFortId(fortId);
        // 从原来的地方移除
        RoleForce roleForce = heroFortess.removeRoleForce(crossPlayer.getLordId(), crossHero.getHeroId());
        if (roleForce == null) {
            roleForce = RoleForce.createInstance(crossHero, crossPlayer.getCamp());
        }
        // 添加到新的地方
        reqFortress.joinRoleForce(roleForce);
        broadcastCrossFort();
    }

    @Override
    public void clear() {
        fortresses.clear();
        StaticCrossDataMgr.getFortMap().values().forEach(cfg -> {
            fortresses.put(cfg.getId(), Fortress.createFortressByCfg(cfg));
        });
        LogUtil.debug("堡垒数据重置");
    }

    @Override
    public byte[] getData() {
        if (fortresses.isEmpty()) {
            return null;
        }
        SerCrossFort.Builder builder = SerCrossFort.newBuilder();
        List<DbFortress> fList = fortresses.values().stream().map(Fortress::toDbFortressPb)
                .collect(Collectors.toList());
        builder.addAllFort(fList);
        return builder.build().toByteArray();
    }

    @Override
    public void loadData(byte[] data) throws InvalidProtocolBufferException {
        if (data == null) { // 没有数据直接创建数据
            fortresses.clear();
            StaticCrossDataMgr.getFortMap().values().forEach(cfg -> {
                fortresses.put(cfg.getId(), Fortress.createFortressByCfg(cfg));
            });
        } else {
            SerCrossFort serPb = SerCrossFort.parseFrom(data);
            serPb.getFortList().stream().map(fPb -> Fortress.createFortressByDb(fPb)).forEach(fortress -> {
                fortresses.put(fortress.getId(), fortress);
            });
        }
    }

    @Override
    public GlobalModelType getModelType() {
        return GlobalModelType.FORT_MODEL;
    }

}
