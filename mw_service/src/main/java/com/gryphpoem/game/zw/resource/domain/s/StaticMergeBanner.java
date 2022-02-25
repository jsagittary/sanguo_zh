package com.gryphpoem.game.zw.resource.domain.s;

import java.util.Date;
import java.util.List;

/**
 * ClassName: StaticMergeBanner
 * Date:      2020/12/9 10:30
 * author     shi.pei
 */
public class StaticMergeBanner {
    private int id;
    private List<Integer> server;
    private int bloc;
    private Date timeBegin;
    private Date timeEnd;
    private String desc1;
    private String desc2;
    private String desc3;
    private String opTime;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<Integer> getServer() {
        return server;
    }

    public void setServer(List<Integer> server) {
        this.server = server;
    }

    public int getBloc() {
        return bloc;
    }

    public void setBloc(int bloc) {
        this.bloc = bloc;
    }

    public Date getTimeBegin() {
        return timeBegin;
    }

    public void setTimeBegin(Date timeBegin) {
        this.timeBegin = timeBegin;
    }

    public Date getTimeEnd() {
        return timeEnd;
    }

    public void setTimeEnd(Date timeEnd) {
        this.timeEnd = timeEnd;
    }

    public String getDesc1() {
        return desc1;
    }

    public void setDesc1(String desc1) {
        this.desc1 = desc1;
    }

    public String getDesc2() {
        return desc2;
    }

    public void setDesc2(String desc2) {
        this.desc2 = desc2;
    }

    public String getDesc3() {
        return desc3;
    }

    public void setDesc3(String desc3) {
        this.desc3 = desc3;
    }

    public String getOpTime() {
        return opTime;
    }

    public void setOpTime(String opTime) {
        this.opTime = opTime;
    }
}
