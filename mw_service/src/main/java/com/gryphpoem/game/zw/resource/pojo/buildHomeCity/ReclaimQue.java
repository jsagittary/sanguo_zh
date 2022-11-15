// package com.gryphpoem.game.zw.resource.pojo.buildHomeCity;
//
// import com.gryphpoem.game.zw.pb.CommonPb;
//
// /**
//  * @Author: GeYuanpeng
//  * @Date: 2022/11/7 18:35
//  */
// public class ReclaimQue {
//
//     private int keyId;
//
//     private int index;
//
//     private int farmerCnt;
//
//     private int cellId;
//
//     private int period;
//
//     private int endTime;
//
//     private int freeTime;
//
//     public ReclaimQue(int keyId, int index, int farmerCnt, int cellId, int period, int endTime) {
//         this.keyId = keyId;
//         this.index = index;
//         this.farmerCnt = farmerCnt;
//         this.cellId = cellId;
//         this.period = period;
//         this.endTime = endTime;
//     }
//
//     public ReclaimQue(CommonPb.ReclaimQue pb) {
//         this.keyId = pb.getKeyId();
//         this.index = pb.getIndex();
//         this.farmerCnt = pb.getFarmerCnt();
//         this.cellId = pb.getCellId();
//         this.period = pb.getPeriod();
//         this.endTime = pb.getEndTime();
//         if (pb.hasFreeTime()) {
//             this.freeTime = pb.getFreeTime();
//         }
//     }
//
//     public int getKeyId() {
//         return keyId;
//     }
//
//     public void setKeyId(int keyId) {
//         this.keyId = keyId;
//     }
//
//     public int getIndex() {
//         return index;
//     }
//
//     public void setIndex(int index) {
//         this.index = index;
//     }
//
//     public int getFarmerCnt() {
//         return farmerCnt;
//     }
//
//     public void setFarmerCnt(int farmerCnt) {
//         this.farmerCnt = farmerCnt;
//     }
//
//     public int getCellId() {
//         return cellId;
//     }
//
//     public void setCellId(int cellId) {
//         this.cellId = cellId;
//     }
//
//     public int getPeriod() {
//         return period;
//     }
//
//     public void setPeriod(int period) {
//         this.period = period;
//     }
//
//     public int getEndTime() {
//         return endTime;
//     }
//
//     public void setEndTime(int endTime) {
//         this.endTime = endTime;
//     }
//
//     public int getFreeTime() {
//         return freeTime;
//     }
//
//     public void setFreeTime(int freeTime) {
//         this.freeTime = freeTime;
//     }
//
//     public CommonPb.ReclaimQue creatReclaimQuePb() {
//         CommonPb.ReclaimQue.Builder builder = CommonPb.ReclaimQue.newBuilder();
//         builder.setKeyId(this.getKeyId());
//         builder.setFarmerCnt(this.getFarmerCnt());
//         builder.setCellId(this.getCellId());
//         builder.setPeriod(this.getPeriod());
//         builder.setEndTime(this.getEndTime());
//         builder.setFreeTime(this.getFreeTime());
//         return builder.build();
//     }
//
//     @Override
//     public String toString() {
//         return "ReclaimQue{" +
//                 "keyId=" + keyId +
//                 ", index=" + index +
//                 ", farmerCnt=" + farmerCnt +
//                 ", cellId=" + cellId +
//                 ", period=" + period +
//                 ", endTime=" + endTime +
//                 ", freeTime=" + freeTime +
//                 '}';
//     }
// }
