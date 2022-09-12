package com.gryphpoem.game.zw.resource.domain;

import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.resource.domain.p.*;

import java.util.Objects;

public class Role {
    public static final int SAVE_MODE_DEFAULT = 0;
    public static final int SAVE_MODE_TIMER = 1; // 定时器保存
    private long roleId;
    private DataNew data;
    private Lord lord;
    private Building building;
    private Resource resource;
    private Common common;
    private MailData mailData;
    private int saveMode = SAVE_MODE_DEFAULT; // 保存的的模式, 0默认模式, 1.不会保存战报信息
    private DbPlayerHero dbPlayerHero;

    public Role(Player player, int saveMode) {
        roleId = player.roleId;
        if (player.building != null) {
            building = (Building) player.building.clone();
        }
        if (player.resource != null) {
            resource = (Resource) player.resource.clone();
        }
        if (player.common != null) {
            common = (Common) player.common.clone();
        } else {
            LogUtil.error("Role保存前数据common = null" + ", roleId=" + roleId);
        }
        data = player.serNewData();
        mailData = player.serMailData();
        lord = (Lord) player.lord.clone();
        this.saveMode = saveMode;
        if (Objects.nonNull(player.playerHero)) {
            this.dbPlayerHero = player.playerHero.createPb(true);
        } else {
            LogUtil.error("Role保存前数据 playerHero = null" + ", roleId=" + roleId);
        }
    }

    public Role(Player player) {
        this(player, SAVE_MODE_DEFAULT);
    }

    public MailData getMailData() {
        return mailData;
    }

    public void setMailData(MailData mailData) {
        this.mailData = mailData;
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public DataNew getData() {
        return data;
    }

    public void setData(DataNew data) {
        this.data = data;
    }

    public Lord getLord() {
        return lord;
    }

    public void setLord(Lord lord) {
        this.lord = lord;
    }

    public Building getBuilding() {
        return building;
    }

    public void setBuilding(Building building) {
        this.building = building;
    }

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public Common getCommon() {
        return common;
    }

    public void setCommon(Common common) {
        this.common = common;
    }

    public int getSaveMode() {
        return saveMode;
    }

    public DbPlayerHero getDbPlayerHero() {
        return dbPlayerHero;
    }

    public void setDbPlayerHero(DbPlayerHero dbPlayerHero) {
        this.dbPlayerHero = dbPlayerHero;
    }
}
