package com.gryphpoem.game.zw.resource.domain.s;

import com.gryphpoem.game.zw.resource.util.ListUtils;

import java.util.List;
import java.util.Objects;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-05-26 11:06
 */
public class StaticActQuestionnaire {
    private int platNo;
    private int activityId;
    private String url;
    private String desc;
    private List<List<Integer>> awards;

    public int getPlatNo() {
        return platNo;
    }

    public void setPlatNo(int platNo) {
        this.platNo = platNo;
    }

    public int getActivityId() {
        return activityId;
    }

    public void setActivityId(int activityId) {
        this.activityId = activityId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public List<List<Integer>> getAwards() {
        return awards;
    }

    public void setAwards(List<List<Integer>> awards) {
        this.awards = awards;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StaticActQuestionnaire that = (StaticActQuestionnaire) o;
        return platNo == that.platNo && activityId == that.activityId && url.equals(that.url) && Objects.equals(desc, that.desc) && Objects.equals(awards, that.awards);
    }

    @Override
    public int hashCode() {
        return Objects.hash(platNo, activityId, url, desc, awards);
    }

    @Override
    public String toString() {
        return "StaticActQuestionnaire{" +
                "platNo=" + platNo +
                ", activityId=" + activityId +
                ", url='" + url + '\'' +
                ", desc='" + desc + '\'' +
                ", awards=" + ListUtils.toString(awards) +
                '}';
    }
}
