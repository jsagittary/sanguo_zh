package com.gryphpoem.game.zw.resource.pojo.daily;

import java.util.List;
import java.util.stream.Collectors;

import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.resource.pojo.daily.DailyReport.ReportInfo;
import com.gryphpoem.game.zw.resource.util.CheckNull;

/**
 * @ClassName HonorReport2.java
 * @Description 荣耀日报(其他类别)
 * @author QiuKun
 * @date 2018年8月25日
 */
public class HonorReport2 implements IHonorDailyReport {

    private int createTime; // 创建时间
    private ReportInfo role;// 玩家信息
    private int condId; // 条件id
    private List<String> param; // 参数信息

    public HonorReport2() {
    }

    public HonorReport2(CommonPb.HonorReport2 ser) {
        this.createTime = ser.getCreateTime();
        this.condId = ser.getCondId();
        this.role = new ReportInfo(ser.getRole());
        this.param = ser.getParamList().stream().collect(Collectors.toList());
    }

    public CommonPb.HonorReport2 ser() {
        CommonPb.HonorReport2.Builder builder = CommonPb.HonorReport2.newBuilder();
        builder.setCreateTime(createTime);
        builder.setCondId(condId);
        builder.setRole(role.ser());
        if (!CheckNull.isEmpty(this.param)) {
            builder.addAllParam(this.param);
        }
        return builder.build();
    }

    public ReportInfo getRole() {
        return role;
    }

    public void setRole(ReportInfo role) {
        this.role = role;
    }

    public int getCondId() {
        return condId;
    }

    public void setCondId(int condId) {
        this.condId = condId;
    }

    public List<String> getParam() {
        return param;
    }

    public void setParam(List<String> param) {
        this.param = param;
    }

    @Override
    public int getCreateTime() {
        return createTime;
    }

    public void setCreateTime(int createTime) {
        this.createTime = createTime;
    }

}
