package com.gryphpoem.game.zw.resource.domain.s;

import com.gryphpoem.game.zw.gameplay.local.constant.cross.NewCrossConstant;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.GamePb6;
import com.gryphpoem.game.zw.resource.util.CheckNull;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StaticCrossGroup {
    private int group;

    private List<StaticElementServer> red;
    private List<StaticElementServer> yellow;
    private List<StaticElementServer> blue;

    public int getGroup() {
        return group;
    }

    public void setGroup(int group) {
        this.group = group;
    }

    public List<StaticElementServer> getRed() {
        return red;
    }

    public void setRed(List<StaticElementServer> red) {
        this.red = red;
    }

    public List<StaticElementServer> getYellow() {
        return yellow;
    }

    public void setYellow(List<StaticElementServer> yellow) {
        this.yellow = yellow;
    }

    public List<StaticElementServer> getBlue() {
        return blue;
    }

    public void setBlue(List<StaticElementServer> blue) {
        this.blue = blue;
    }

    /**
     * 三方组成的全部的跨服单位
     *
     * @return 全部的跨服单位
     */
    public List<StaticElementServer> allElementServer() {
        return Stream.of(red, yellow, blue).flatMap(Collection::stream).collect(Collectors.toList());
    }

    /**
     * 获取势力
     *
     * @param sid  本服区服
     * @param camp 本服阵营
     * @return 跨服势力
     */
    public int getForceBySidAndCamp(int sid, int camp) {
        if (red.stream().anyMatch(element -> element.getServerId() == sid && element.getCamp() == camp)) {
            return ForceDefine.RED.force;
        } else if (yellow.stream().anyMatch(element -> element.getServerId() == sid && element.getCamp() == camp)) {
            return ForceDefine.YELLOW.force;
        } else if (blue.stream().anyMatch(element -> element.getServerId() == sid && element.getCamp() == camp)) {
            return ForceDefine.BLUE.force;
        }
        return ForceDefine.NONE.force;
    }

    /**
     * 跨服势力定义
     */
    public enum ForceDefine {
        NONE(0),
        RED(1),
        BLUE(2),
        YELLOW(3),;

        private int force;

        ForceDefine(int force) {
            this.force = force;
        }

        public int getForce() {
            return force;
        }
    }

    public void createPb(GamePb6.GetCrossGroupInfoRs.Builder builder) throws IllegalAccessException {
        CommonPb.CrossGroupInfoPb.Builder pb = CommonPb.CrossGroupInfoPb.newBuilder();
        for (Field field : StaticCrossGroup.class.getDeclaredFields()) {
            NewCrossConstant.CrossGroup crossGroup = NewCrossConstant.CrossGroup.convertTo(field.getName());
            if (CheckNull.isNull(crossGroup))
                continue;

            pb.setGroup(crossGroup.getType());
            List<StaticElementServer> list = (List<StaticElementServer>) field.get(this);
            pb.addAllGroupInfo(list.stream().map(StaticElementServer::createPb).collect(Collectors.toList()));

            builder.addCrossGroupInfoPb(pb.build());
            pb.clear();
        }
    }
}
