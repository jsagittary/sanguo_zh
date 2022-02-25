package com.gryphpoem.game.zw.gameplay.local.world;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticLordDataMgr;
import com.gryphpoem.game.zw.manager.TechDataManager;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.Effect;
import com.gryphpoem.game.zw.resource.domain.s.StaticCastleSkin;
import com.gryphpoem.game.zw.resource.domain.s.StaticCastleSkinStar;
import com.gryphpoem.game.zw.resource.domain.s.StaticWarFire;
import com.gryphpoem.game.zw.resource.pojo.world.BerlinWar;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.Turple;
import com.gryphpoem.game.zw.service.session.SeasonTalentService;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @ClassName BaseCalcWroldMap.java
 * @Description 主要用于计算坐标使用 ,cellId 从1开始; pos 是从0开始
 * @author QiuKun
 * @date 2019年3月20日
 */
public class BaseCalcWroldMap {

    /**
     * 地图id
     */
    private final int mapId;
    /**
     * 宽
     */
    private final int width;
    /**
     * 高
     */
    private final int height;
    private final int cellSize;
    @SuppressWarnings("unused")
    private final int cellHeightLength;
    private final int cellWidthLength;
    /** 最大的块id,块id是从1开始 */
    private final int maxCellId;
    /** 位置标号 从0开始 */
    private final int maxPos;
    /** 缓存用过的cell的坐标 <cellId,List<pos>> */
    private Map<Integer, List<Integer>> cacheCellPosListMap;

    public BaseCalcWroldMap(int mapId, int width, int height, int cellSize) {
        this.mapId = mapId;
        this.width = width;
        this.height = height;
        this.cellSize = cellSize;
        this.cacheCellPosListMap = new HashMap<>(100);
        this.cellWidthLength = width / cellSize;
        this.cellHeightLength = height / cellSize;
        int maxX = width - 1;
        int maxY = height - 1;
        this.maxCellId = xyToCell(maxX, maxY);
        this.maxPos = xyToPos(maxX, maxX);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getCellSize() {
        return cellSize;
    }

    /**
     * 获取块的开始的坐标
     * 
     * @param cellId
     * @return
     */
    public int cellToPos(int cellId) {
        if (!checkCellIsValid(cellId)) {
            return -1;
        }
        int[] cellToXy = cellToXy(cellId);
        if (cellToXy == null) {
            return -1;
        }
        return xyToPos(cellToXy[0], cellToXy[1]);
    }

    /**
     * 块转换成xy, 块的开始坐标
     * 
     * @param cellId
     * @return
     */
    public int[] cellToXy(int cellId) {
        if (!checkCellIsValid(cellId)) {
            return null;
        }
        int c = cellId - 1; // 减 1是因为 cell块id从1开始
        int cellX = c % cellWidthLength;
        int cellY = c / cellWidthLength;
        int startX = cellX * cellSize;
        int startY = cellY * cellSize;
        return new int[] { startX, startY };
    }

    /**
     * 获取这个块的所有点
     * 
     * @param cellId
     * @return
     */
    public List<Integer> getCellPosList(int cellId) {
        if (!checkCellIsValid(cellId)) {
            return null;
        }
        List<Integer> posList = cacheCellPosListMap.get(cellId);
        if (posList != null) {
            return posList;
        }
        int[] startXy = cellToXy(cellId);
        if (startXy == null) {
            return null;
        }
        int startX = startXy[0];
        int startY = startXy[1];
        posList = new ArrayList<>(100);
        for (int i = startX; i < startX + cellSize; i++) {
            for (int j = startY; j < startY + cellSize; j++) {
                int pos = xyToPos(i, j);
                if (pos != -1) {
                    posList.add(pos);
                }
            }
        }
        posList = Collections.unmodifiableList(posList);
        cacheCellPosListMap.put(cellId, posList);
        return posList;
    }

    /**
     * pos转成 cellId
     * 
     * @param pos
     * @return
     */
    public int posToCell(int pos) {
        if (!checkPoxIsValid(pos)) {
            return -1;
        }
        int x = pos % width;
        int y = pos / width;
        return xyToCell(x, y);
    }

    /**
     * 坐标转化为块
     * 
     * @param x
     * @param y
     * @return -1表示非法的块坐标
     */
    public int xyToCell(int x, int y) {
        if (!checkXyIsValid(x, y)) {
            return -1;
        }
        int cellX = x / cellSize;
        int cellY = y / cellSize;
        int cellId = cellY * cellWidthLength + cellX + 1; // 块id从1开始
        return cellId;
    }

    public int xyToPos(int x, int y) {
        if (!checkXyIsValid(x, y)) {
            return -1;
        }
        return y * width + x;
    }

    public int[] posToXy(int pos) {
        if (!checkPoxIsValid(pos)) {
            return null;
        }
        int x = pos % width;
        int y = pos / width;
        return new int[] { x, y };
    }

    public Turple<Integer, Integer> posToTurple(int pos) {
        int[] xy = posToXy(pos);
        if (xy == null) {// 返回默认的 0,0坐标
            return new Turple<Integer, Integer>(0, 0);
        }
        Turple<Integer, Integer> t = new Turple<Integer, Integer>(xy[0], xy[1]);
        return t;
    }

    /**
     * 检测x y坐标是否合法
     * 
     * @param x
     * @param y
     * @return
     */
    public boolean checkXyIsValid(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    /**
     * 检测块是否合法
     * 
     * @param cellId
     * @return
     */
    public boolean checkCellIsValid(int cellId) {
        return cellId >= 1 && cellId <= maxCellId;
    }

    /**
     * 检测 pos是否合法
     * 
     * @param pos
     * @return
     */
    public boolean checkPoxIsValid(int pos) {
        return pos >= 0 && pos <= maxPos;
    }

    public String posToStr(int pos) {
        int[] posToXy = posToXy(pos);
        if (posToXy == null) {
            return String.format("(非法点 pos:%d)", pos);
        }
        return String.format("(%d,%d)", posToXy[0], posToXy[1]);
    }

    /**
     * 获取以某个点为中心,周围的坐标点(包括本身),生成的坐标都是中心区域内的
     * 
     * @param pos 中心点坐标
     * @param radius 半径
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<Integer> getRoundPos(int pos, int radius) {
        if (radius < 1) {
            radius = 1;
        }
        if (radius >= width / 2 || radius >= height / 2) { // 半径最大值
            radius = Math.min(width / 2, height / 2) - 1;
        }
        int[] posToXy = posToXy(pos);
        if (posToXy == null) {
            return Collections.EMPTY_LIST;
        }
        List<Integer> posList = new ArrayList<>();
        int x = posToXy[0];
        int y = posToXy[1];

        // 边界
        int xMin = 0;
        int yMin = 0;
        int xMax = width - 1;
        int yMax = height - 1;

        int xPBegin = x - radius;
        int xPEnd = x + radius;
        int yPBegin = y - radius;
        int yPEnd = y + radius;

        // 不能超过范围,四个角落的情况
        if (xPBegin < xMin) { // 左边越界
            xPBegin = xMin;
            xPEnd = xPBegin + (2 * radius);
        }
        if (xPEnd > xMax) {// 右边越界
            xPEnd = xMax;
            xPBegin = xPEnd - (2 * radius);
        }
        if (yPBegin < yMin) {// 顶边越界
            yPBegin = yMin;
            yPEnd = yPBegin + (2 * radius);
        }
        if (yPEnd > yMax) {// 底边越界
            yPEnd = yMax;
            yPBegin = yPEnd - (2 * radius);
        }
        for (int posY = yPBegin; posY <= yPEnd; posY++) {
            for (int posX = xPBegin; posX <= xPEnd; posX++) {
                int p = xyToPos(posX, posY);
                if (p != -1) {
                    posList.add(p);
                }
            }
        }
        return posList;
    }

    /**
     * 计算两个点之间的距离
     * 
     * @param pos1
     * @param pos2
     * @return -1 非法值
     */
    public int calcDistance(int pos1, int pos2) {
        int[] xy1 = posToXy(pos1);
        int[] xy2 = posToXy(pos2);
        if (xy1 == null || xy2 == null) {
            return -1;
        }
        return Math.abs(xy1[0] - xy2[0]) + Math.abs(xy1[1] - xy2[1]);
    }

    /**
     * 计算玩家行军到目标坐标需要的时间 行军时间（秒）=8*（|X差|+|Y差|）*（1-行军加速_科技[%])*(1-行军加速_道具[%])/(1+军曹官加成[%]） 向上取整
     *
     * @param cMap 新地图数据, 可以取玩家个人buff, 和城池的buff
     * @param player 玩家
     * @param pos1 出发点
     * @param pos2 目标点
     * @return
     */
    public int marchTime(CrossWorldMap cMap, Player player, int pos1, int pos2) {
        int distance = calcDistance(pos1, pos2);

        int baseRatio = Constant.MARCH_TIME_RATIO;

        int time = distance * baseRatio;

        //********************增加行军速度****************************
        TechDataManager techDataManager = DataResource.ac.getBean(TechDataManager.class);
        // 科技加成
        double addRatio = techDataManager.getTechEffect4SingleVal(player, TechConstant.TYPE_6);
        // 柏林官员
        double berlinJobEffect = BerlinWar.getBerlinBuffVal(player.roleId, BerlinWarConstant.BUFF_TYPE_MARCH_TIME);
        // 赛季天赋:行军加速
        SeasonTalentService seasonTalentService = DataResource.ac.getBean(SeasonTalentService.class);
        double seasonTalentEffect = seasonTalentService.getSeasonTalentEffectValue(player, SeasonConst.TALENT_EFFECT_301);

        //********************减少行军时间****************************
        // buff加成
        Effect effect = player.getEffect().get(EffectConstant.WALK_SPEED);
        double addRatio1 = effect != null ? effect.getEffectVal() : 0;

        effect = player.getEffect().get(EffectConstant.WALK_SPEED_HIGHT);
        // 督战官(商店加速BUFF)
        double addRatio2 = effect != null ? effect.getEffectVal() : 0;

        effect = player.getEffect().get(EffectConstant.PREWAR_WALK_SPEED);
        // 柏林战前buff
        double addRatio3 = effect != null ? effect.getEffectVal() : 0;

        // 战火燎原城池加成
        Map<Integer, Integer> cityBuff = cMap.getCityBuff(player);
        double addRatio4 = !CheckNull.isEmpty(cityBuff) ? cityBuff.getOrDefault(StaticWarFire.BUFF_TYPE_5, 0) : 0;

        // 皮肤Buff加成
        List<StaticCastleSkin> staticCastleSkinList = player.getOwnCastleSkin().stream().map(StaticLordDataMgr::getCastleSkinMapById).filter(staticCastleSkin -> staticCastleSkin.getEffectType() == 4).collect(Collectors.toList());
        int skinAdd = 0;
        for(StaticCastleSkin o : staticCastleSkinList){
            int star = player.getCastleSkinStarById(o.getId());
            StaticCastleSkinStar staticCastleSkinStar = StaticLordDataMgr.getCastleSkinStarById(o.getId() * 100 + star);
            skinAdd += staticCastleSkinStar.getEffectVal();
        }
        int addRatio5 = skinAdd;
        // 行军时间（秒）=8*（|X差|+|Y差|）/（1+行军加速_科技[%]+官职加成[%])*(1-道具加成[%])*(1-军曹官加成[%])*(1-柏林战前加成[%])*(1-战火燎原城池加成[%])*(1-皮肤Buff加成[%])*战火燎原行军时间系数
        try {
            time = (int) Math.ceil((time / (1 + (addRatio + berlinJobEffect + seasonTalentEffect) / Constant.TEN_THROUSAND )
                    * (1 - addRatio1 / Constant.TEN_THROUSAND)
                    * (1 - addRatio2 / Constant.TEN_THROUSAND)
                    * (1 - addRatio3 / Constant.TEN_THROUSAND)
                    * (1 - addRatio4 / Constant.TEN_THROUSAND)
                    * (1 - addRatio5 / Constant.TEN_THROUSAND))
                    * (WorldConstant.WAR_FIRE_MARCH_TIME_COEF / Constant.TEN_THROUSAND)) ;
        } catch (Exception e) {
            LogUtil.error("行军时间计算出错", e);
        }
        if (time < 1) {
            time = 1;
        }
        LogUtil.debug("实际时间=", distance, ", 科技等加成=", addRatio, ", 风字令=", addRatio1, ", 军曹官=", addRatio2, ", 柏林战前buff=",
                addRatio3, ", 柏林官员=", berlinJobEffect, ", 最终时间=", time);
        return time;
    }

    public int getMapId() {
        return mapId;
    }

    // public static void main(String[] args) {
    // BaseCalcWroldMap bmap = new BaseCalcWroldMap(26, 100, 100, 10);
    // List<Integer> roundPos = bmap.getRoundPos(2565, 2);
    // System.out.println(roundPos);
    // int cellId = 1;
    // for (int i = 0; i < 10; i++) {
    // for (int j = 0; j < 10; j++) {
    // int cellToPos = bmap.cellToPos(cellId);
    // System.out.print(bmap.posToStr(cellToPos) + "," + cellId + ", ");
    // cellId++;
    // }
    // System.out.println();
    // }
    // int posToCell = bmap.posToCell(7189);
    // String posToStr = bmap.posToStr(7189);
    // List<Integer> cellPosList = bmap.getCellPosList(posToCell);
    // int xyToPos = bmap.xyToPos(81, 90);
    // int[] posToXy = bmap.posToXy(xyToPos);
    // System.out.println();
    // }
}
