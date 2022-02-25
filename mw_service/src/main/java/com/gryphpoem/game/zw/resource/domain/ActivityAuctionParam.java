package com.gryphpoem.game.zw.resource.domain;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.resource.constant.ActParamConstant;
import com.gryphpoem.game.zw.resource.constant.GameError;
import com.gryphpoem.game.zw.resource.domain.p.ActivityAuctionConst;
import com.gryphpoem.game.zw.resource.util.ActParamTabLoader;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class ActivityAuctionParam {

//    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private int round;

    private Date startTime;

    private Date aboutToEndTime;

    private Date endTime;

    public int getRound() {
        return round;
    }

    public void setRound(int round) {
        this.round = round;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Date getAboutToEndTime() {
        return aboutToEndTime;
    }

    public void setAboutToEndTime(Date aboutToEndTime) {
        this.aboutToEndTime = aboutToEndTime;
    }

    public static List<ActivityAuctionParam> sort(List<ActivityAuctionParam> list) {
        if (ObjectUtils.isEmpty(list)) {
            return null;
        }

        return list.stream().sorted(Comparator.comparingInt(ActivityAuctionParam::getRound)).collect(Collectors.toList());
    }
}
