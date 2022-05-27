package com.gryphpoem.game.zw.resource.domain.s;

import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.resource.pojo.GamePb;
import com.gryphpoem.game.zw.resource.util.ListUtils;
import com.gryphpoem.game.zw.resource.util.PbHelper;

import java.util.List;
import java.util.Objects;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-05-26 11:06
 */
public class StaticActQuestionnaire implements GamePb<CommonPb.QuestionnaireActData> {
    private int platNo;
    private int activityId;
    private String url;
    private String desc;
    private List<List<Integer>> awards;
    private int status;
    private int lv;

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

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getLv() {
        return lv;
    }

    public void setLv(int lv) {
        this.lv = lv;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StaticActQuestionnaire that = (StaticActQuestionnaire) o;
        return platNo == that.platNo && activityId == that.activityId && status == that.status && lv == that.lv && url.equals(that.url) && Objects.equals(desc, that.desc) && Objects.equals(awards, that.awards);
    }

    @Override
    public int hashCode() {
        return Objects.hash(platNo, activityId, url, desc, awards, status, lv);
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

    @Override
    public CommonPb.QuestionnaireActData createPb(boolean isSaveDb) {
        CommonPb.QuestionnaireActData.Builder builder = CommonPb.QuestionnaireActData.newBuilder();
        builder.setPlatNo(platNo);
        builder.setUrl(url);
        builder.setDesc(desc);
        builder.addAllAwards(PbHelper.createAwardsPb(awards));
        return builder.build();
    }
}
