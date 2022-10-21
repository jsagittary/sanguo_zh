package com.gryphpoem.game.zw.resource.util;

import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.core.util.RandomHelper;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author TanDonghai
 * @ClassName MapHelper.java
 * @Description 世界地图、坐标相关工具类
 * @date 创建时间：2017年3月29日 下午5:12:11
 */
public class MapHelper {

    /**
     * 区域的上下左右
     */
    public static final int UP_HALF_IN_AREA = 1;
    public static final int DOWN_HALF_IN_AREA = 2;
    public static final int LEFT_HALF_IN_AREA = 3;
    public static final int RIGHT_HALF_IN_AREA = 4;

    /**
     * 最大格子数
     */
    public static final int MAX_GRID = 500;
    public static final int[] AREA_MAP = {21, 22, 23, 24, 25, 16, 17, 18, 19, 20, 11, 12, 13, 14, 15, 6, 7, 8, 9, 10,
            1, 2, 3, 4, 5};
    private static Map<Integer, Set<Integer>> areaBlockMap;

    /**
     * 在行政分区内随机坐标，不考虑坐标是否被占 坐标从1开始， 没有0的坐标
     *
     * @param areaId
     * @return
     */
    public static int randomPosInArea(int areaId) {
        int xBegin = ((areaId - 1) % 5) * 100;
        int yBegin = (4 - (areaId - 1) / 5) * 100;

        return (RandomHelper.randomInSize(100) + xBegin) + (RandomHelper.randomInSize(100) + yBegin) * 500;
    }

    public static Turple<Integer, Integer> reduceXYInArea(int areaId) {
        Turple<Integer, Integer> turple = new Turple<Integer, Integer>(((areaId - 1) % 5) * 100,
                (4 - (areaId - 1) / 5) * 100);
        return turple;
    }

    /**
     * 从起始的坐标, 取区域的随机坐标
     *
     * @param areaId 区域 1-25
     * @param xBegin x起始
     * @param yBegin y起始
     * @return 随机的坐标
     */
    public static int randomPosInArea(int areaId, int xBegin, int yBegin) {
        return (RandomHelper.randomInSize(100) + xBegin) + (RandomHelper.randomInSize(100) + yBegin) * 500;
    }

    /**
     * 从起始的坐标, 取区域的一半的随机坐标
     *
     * @param areaId 区域 1-25
     * @param xBegin x起始
     * @param yBegin y起始
     * @param half   一半
     * @return 随机的坐标
     */
    public static int randomPosHalfInArea(int areaId, int xBegin, int yBegin, int half) {
        int x = 0;
        int y = 0;
        if (half == UP_HALF_IN_AREA) {
            x = RandomHelper.randomInSize(100) + xBegin;
            y = RandomHelper.randomInArea(0, 51) + yBegin;
        } else if (half == DOWN_HALF_IN_AREA) {
            x = RandomHelper.randomInSize(100) + xBegin;
            y = RandomHelper.randomInArea(51, 100) + yBegin;
        } else if (half == LEFT_HALF_IN_AREA) {
            x = RandomHelper.randomInArea(0, 51) + xBegin;
            y = RandomHelper.randomInSize(100) + yBegin;
        } else if (half == RIGHT_HALF_IN_AREA) {
            x = RandomHelper.randomInArea(51, 100) + xBegin;
            y = RandomHelper.randomInSize(100) + yBegin;
        }
        return x + y * MAX_GRID;
    }

    public static Turple<Integer, Integer> reducePos(int pos) {
        Turple<Integer, Integer> turple = new Turple<Integer, Integer>(pos % 500, pos / 500);
        return turple;
    }

    /**
     * 计算坐标所属的行政分区
     *
     * @param pos
     * @return
     */
    public static int getAreaIdByPos(int pos) {
        if (pos < 0) {
            return -1;
        }
        Turple<Integer, Integer> xy = reducePos(pos);

        // 原来的算法
        // int xArea = (xy.getA() - 1) / 100 + 1;
        // int yArea = (xy.getB() - 1) / 100;
        //// return xArea + yArea * 5;
        // return AREA_MAP[(xArea + yArea * 5) - 1];

        int xArea = (xy.getA()) / 100 + 1;
        int yArea = (xy.getB()) / 100;
        // return xArea + yArea * 5;
        return AREA_MAP[(xArea + yArea * 5) - 1];

        // int x = xy.getA() +1;
        // int y = xy.getB() +1;
        // int xArea = x%100==0?x/100:x/100+1;
        // int yArea = y%100==0?y/100:y/100+1;
        // return 5* yArea-(5-xArea);
        // return AREA_MAP[(5* yArea-(5-xArea))-1];
    }

    /**
     * 计算坐标所属的客户端10*10的拉区分块
     *
     * @param pos
     * @return
     */
    public static int block(int pos) {
        Turple<Integer, Integer> xy = reducePos(pos);
        // int block = (xy.getA() - 1) / 10 + 1 + (xy.getB() - 1) / 10 * 50;
        int block = (xy.getA()) / 10 + 1 + (xy.getB()) / 10 * 50;
        return block;
    }

    /**
     * 块转换成区域
     *
     * @param block
     * @return
     */
    public static int blockToArea(int block) {
        int x = (block - 1) % 50 / 10 + 1;
        int y = (block - 1) / 50 / 10;
        int areaId = x + y * 5;
        return convertArea(areaId);
    }

    /**
     * 获取分区中的所有分块
     *
     * @param area
     * @return
     */
    public static Set<Integer> getBlockInArea(int area) {
        if (null == areaBlockMap) {
            areaBlockMap = new HashMap<>();
            for (int block = 1; block <= 2500; block++) {
                int areaId = blockToArea(block);
                Set<Integer> set = areaBlockMap.get(areaId);
                if (null == set) {
                    set = new HashSet<>();
                    areaBlockMap.put(areaId, set);
                }
                set.add(block);
            }
        }
        return areaBlockMap.get(area);
    }

    /**
     * 从某个行政分区的多个分块（10*10）中随机一个坐标，这里的分块不是大地图中的分块id，而是相对本分区从1开始计算的分块，范围1-100
     *
     * @param area
     * @param blockList
     * @return
     */
    public static int randomPosInBlockList(int area, List<Integer> blockList) {
        if (CheckNull.isEmpty(blockList)) {
            return 0;
        }

        int block = blockList.get(RandomHelper.randomInSize(blockList.size()));
        LogUtil.error("randBlock =" + block);
        return randomPosInBlock(area, block);
    }

    /**
     * 从分块中随机一个坐标
     *
     * @param area
     * @param block 这里的分块不是大地图中的分块id，而是相对本分区从1开始计算的分块，范围1-100
     * @return
     */
    public static int randomPosInBlock(int area, int block) {
        area = AREA_MAP[area - 1];
        Turple<Integer, Integer> minTurPle = getMinPosInBlock(area, block);
        LogUtil.error("xyBlock =" + minTurPle.getA() + ",y=" + minTurPle.getB());
        int x = minTurPle.getA() + RandomHelper.randomInSize(10);
        int y = minTurPle.getB() + RandomHelper.randomInSize(10);
        return x + y * 500;
    }

    /**
     * 计算并返回分块的起始X,Y轴坐标
     *
     * @param area
     * @param block 这里的分块不是大地图中的分块id，而是相对本分区从1开始计算的分块，范围1-100
     * @return
     */
    public static Turple<Integer, Integer> getMinPosInBlock(int area, int block) {
        // int beginX = (area - 1) % 5 * 100;
        // int beginY = (area - 1) / 5 * 100;
        Turple<Integer, Integer> startXy = reduceXYInArea(area);
        int beginX = startXy.getA();
        int beginY = startXy.getB();
        int minX = beginX + ((block - 1) % 10) * 10;
        int minY = beginY + (block - 1) / 10 * 10;
        return new Turple<Integer, Integer>(minX, minY);
    }

    /**
     * 将区域的块转换成大地图的块
     *
     * @param area
     * @param block 这里的分块不是大地图中的分块id，而是相对本分区从1开始计算的分块，范围1-100
     * @return 返回大地图的block
     */
    public static int areaBlockToMapBlock(int area, int block) {
        Turple<Integer, Integer> xy = getMinPosInBlock(area, block);
        int pos = xy.getA() + xy.getB() * 500;
        return block(pos);
    }

    /**
     * 获取某个块的所有点
     *
     * @param area
     * @param block 这里的分块不是大地图中的分块id，而是相对本分区从1开始计算的分块，范围1-100
     * @return
     */
    public static List<Integer> getPosListByAreaBlock(int area, int block) {
        List<Integer> posList = new ArrayList<>();
        Turple<Integer, Integer> xy = getMinPosInBlock(area, block);
        int minX = xy.getA();
        int minY = xy.getB();
        for (int x = minX; x < minX + 10; x++) {
            for (int y = minY; y < minY + 10; y++) {
                int pos = x + y * 500;
                posList.add(pos);
            }
        }
        return posList;
    }

    /**
     * 根据坐标，计算该坐标在该分区中的从1开始计算的分块id
     *
     * @param pos
     * @return
     */
    public static int getRangeBlockByPos(int pos) {
        Turple<Integer, Integer> turple = reducePos(pos);
        int blockX = (turple.getA() % 100 - 1) / 10 + 1;
        int blockY = (turple.getB() % 100 - 1) / 10;
        return blockX + blockY * 10;
    }

    /**
     * 计算两个坐标点的距离，这里的距离不是直线距离，而是X、Y轴距离的和
     *
     * @param pos1
     * @param pos2
     * @return
     */
    public static int calcDistance(int pos1, int pos2) {
        Turple<Integer, Integer> turple1 = reducePos(pos1);
        Turple<Integer, Integer> turple2 = reducePos(pos2);

        return Math.abs(turple1.getA() - turple2.getA()) + Math.abs(turple1.getB() - turple2.getB());
    }

    /**
     * 获取以某个点为中心,周围的坐标点(包括本身),生成的坐标都是中心区域内的
     *
     * @param pos    中心点坐标
     * @param radius 半径
     * @return
     */
    public static List<Integer> getRoundPos(int pos, int radius) {
        List<Integer> posList = new ArrayList<>();
        if (radius < 1) {
            return posList;
        }
        Turple<Integer, Integer> xy = reducePos(pos);
        int x = xy.getA();
        int y = xy.getB();
        pos = x + y * 500;
        int areaId = getAreaIdByPos(pos);

        // 计算区域范围
        Turple<Integer, Integer> beginXy = MapHelper.reduceXYInArea(areaId);
        int xMin = beginXy.getA();
        int yMin = beginXy.getB();
        int xMax = xMin + 99;
        int yMax = yMin + 99;

        int xPBegin = x - radius;
        int xPEnd = x + radius;
        int yPBegin = y - radius;
        int yPEnd = y + radius;

        // 不能超过范围,四个角落的情况
        if (xPBegin < xMin) { // 左边越界
            xPBegin = xMin;
            xPEnd = xPBegin + (2 * radius + 1);
        }
        if (xPEnd > xMax) {// 右边越界
            xPEnd = xMax;
            xPBegin = xPEnd - (2 * radius + 1);
        }
        if (yPBegin < yMin) {// 定边越界
            yPBegin = yMin;
            yPEnd = yPBegin + (2 * radius + 1);
        }
        if (yPEnd > yMax) {// 底边越界
            yPEnd = yMax;
            yPBegin = yPEnd - (2 * radius + 1);
        }
        for (int posY = yPBegin; posY <= yPEnd; posY++) {
            for (int posX = xPBegin; posX <= xPEnd; posX++) {
                int newPos = posX + posY * 500;
                posList.add(newPos);
            }
        }
        return posList;
    }

    /**
     * 获取过滤掉的格子
     *
     * @param pos
     * @param radius
     * @param excludedRadius
     * @return
     */
    public static List<Integer> getExcludedRoundPos(int pos, int radius, int excludedRadius) {
        List<Integer> posList = new ArrayList<>();
        if (radius < 1) {
            return posList;
        }
        Turple<Integer, Integer> xy = reducePos(pos);
        int x = xy.getA();
        int y = xy.getB();
        pos = x + y * 500;
        int areaId = getAreaIdByPos(pos);

        // 计算区域范围
        Turple<Integer, Integer> beginXy = MapHelper.reduceXYInArea(areaId);
        int xMin = beginXy.getA();
        int yMin = beginXy.getB();
        int xMax = xMin + 99;
        int yMax = yMin + 99;

        int xPBegin = x - radius;
        int xPEnd = x + radius;
        int yPBegin = y - radius;
        int yPEnd = y + radius;

        // 不能超过范围,四个角落的情况
        if (xPBegin < xMin) { // 左边越界
            xPBegin = xMin;
            xPEnd = xPBegin + (2 * radius + 1);
        }
        if (xPEnd > xMax) {// 右边越界
            xPEnd = xMax;
            xPBegin = xPEnd - (2 * radius + 1);
        }
        if (yPBegin < yMin) {// 定边越界
            yPBegin = yMin;
            yPEnd = yPBegin + (2 * radius + 1);
        }
        if (yPEnd > yMax) {// 底边越界
            yPEnd = yMax;
            yPBegin = yPEnd - (2 * radius + 1);
        }

        int xPBegin_ = x - excludedRadius;
        int xPEnd_ = x + excludedRadius;
        int yPBegin_ = y - excludedRadius;
        int yPEnd_ = y + excludedRadius;
        if (excludedRadius >= 1) {
            // 不能超过范围,四个角落的情况
            if (xPBegin_ < xMin) { // 左边越界
                xPBegin_ = xMin;
                xPEnd_ = xPBegin_ + (2 * excludedRadius + 1);
            }
            if (xPEnd_ > xMax) {// 右边越界
                xPEnd_ = xMax;
                xPBegin_ = xPEnd_ - (2 * excludedRadius + 1);
            }
            if (yPBegin_ < yMin) {// 定边越界
                yPBegin_ = yMin;
                yPEnd_ = yPBegin_ + (2 * excludedRadius + 1);
            }
            if (yPEnd_ > yMax) {// 底边越界
                yPEnd_ = yMax;
                yPBegin_ = yPEnd_ - (2 * excludedRadius + 1);
            }
        }

        for (int posY = yPBegin; posY <= yPEnd; posY++) {
            for (int posX = xPBegin; posX <= xPEnd; posX++) {
                if (excludedRadius >= 1 && posY >= yPBegin_ &&
                        posY <= yPEnd_ && posX >= xPBegin_ && posX <= xPEnd_)
                    continue;
                int newPos = posX + posY * 500;
                posList.add(newPos);
            }
        }
        return posList;
    }

    /**
     * 皇城中某个城,的区域随机坐标
     *
     * @param kingCityPos 皇城中某个城坐标
     * @return
     */
    public static int randomPosInKingCity(int kingCityPos) {
        Turple<Integer, Integer> reducePos = reducePos(kingCityPos);
        int x = reducePos.getA();
        int y = reducePos.getB();
        // 范围是 [x-10,x+9], [y-10,y+9]
        int randomX = RandomHelper.randomInArea(x - 10, x + 10);
        int randomY = RandomHelper.randomInArea(y - 10, y + 10);
        int ranomPos = randomX + randomY * 500;
        return ranomPos;
    }

    /**
     * 判断某个点是否在城池范围类(仅限于皇城区域)
     *
     * @param kingCityPos
     * @param pos
     * @return
     */
    public static boolean isInKingCityAreaPos(int kingCityPos, int pos) {
        Turple<Integer, Integer> reducePos = reducePos(kingCityPos);
        int x = reducePos.getA();
        int y = reducePos.getB();
        // 250, 250
        Turple<Integer, Integer> tXy = reducePos(pos);
        int tx = tXy.getA();
        int ty = tXy.getB();
        // 252, 254
        // 范围是 [x-10,x+9], [y-10,y+9]
        // 240 <= 252 && 252 < 260 && 240 <= 254 && 254 < 260
        return x - 10 <= tx && tx < x + 10 && y - 10 <= ty && ty < y + 10;
    }

    /**
     * 皇城中某个城,的区域所有坐标
     *
     * @param kingCityPos
     * @return
     */
    public static Collection<Integer> posInKingCityList(int kingCityPos) {
        List<Integer> list = new ArrayList<>();
        Turple<Integer, Integer> reducePos = reducePos(kingCityPos);
        int x = reducePos.getA();
        int y = reducePos.getB();
        // 范围是 [x-10,x+9], [y-10,y+9]
        for (int rx = x - 10; rx < x + 10; rx++) {
            for (int ry = y - 10; ry < y + 10; ry++) {
                int ranomPos = rx + ry * 500;
                list.add(ranomPos);
            }
        }
        return list;
    }

    /**
     * 将x,y转换成,需要在外调用部分确定x y的范围在[0,500)之间
     *
     * @param x
     * @param y
     * @return
     */
    public static int xy2Pos(int x, int y) {
        return x + y * MAX_GRID;
    }

    /**
     * 判断x y坐标是否满足规范
     *
     * @param x
     * @param y
     * @return
     */
    public static boolean isUsablePos(int x, int y) {
        return x >= 0 && x < MAX_GRID && y >= 0 && y < MAX_GRID;
    }

    /**
     * 获取反向的区域
     *
     * @param pos
     * @return
     */
    private static int getReverseAreaIdByPos(int pos) {
        if (pos < 0) {
            return -1;
        }
        Turple<Integer, Integer> xy = reducePos(pos);
        int xArea = (xy.getA()) / 100 + 1;
        int yArea = (xy.getB()) / 100;
        return xArea + yArea * 5;
    }

    /**
     * @param area
     * @return
     */
    private static int convertArea(int area) {
        return AREA_MAP[area - 1];
        // return area;
    }

    /**
     * 每个区域开始的坐标
     *
     * @param areaList
     * @return
     */
    public static List<Integer> getAreaStartPos(List<Integer> areaList) {
        List<Integer> posList = new ArrayList<>();
        for (int areaId : areaList) {
            Turple<Integer, Integer> beginXy = MapHelper.reduceXYInArea(areaId);
            int x = beginXy.getA();
            int y = beginXy.getB();
            int pos = x + y * 500;
            posList.add(pos);
        }
        return posList;
    }

    /**
     * 计算两个点经过的区域
     *
     * @param pos1
     * @param pos2
     * @return
     */
    public static List<Integer> getLineAcorss(int pos1, int pos2) {
        // 非跨区的情况
        List<Integer> areaList = new ArrayList<>();
        int area1 = getReverseAreaIdByPos(pos1);
        int area2 = getReverseAreaIdByPos(pos2);
        if (area1 == area2) {
            areaList.add(area1);
        } else {
            // 跨区的情况
            areaList.add(area1);
            areaList.add(area2);
            Turple<Integer, Integer> xy1 = reducePos(pos1);
            Turple<Integer, Integer> xy2 = reducePos(pos2);
            // 斜率的计算
            int dx = xy2.getA() - xy1.getA();
            int dy = xy2.getB() - xy1.getB();
            if (dx == 0) { // 斜率不存在
                int minArea = Math.min(area1, area2);
                int maxArea = Math.max(area1, area2);
                for (int i = minArea; i < maxArea; i += 5) {
                    areaList.add(i);
                }
            } else if (dy == 0) {// 斜率为0
                int minArea = Math.min(area1, area2);
                int maxArea = Math.max(area1, area2);
                for (int i = minArea; i < maxArea; i++) {
                    areaList.add(i);
                }
            } else {
                // y = kx + b;
                double k = (dy * 1.0) / (dx * 1.0);
                double b = (xy1.getB() + xy2.getB() - k * (xy1.getA() + xy2.getA())) / 2;
                // x轴范围
                int minX = Math.min(xy1.getA(), xy2.getA());
                int maxX = Math.max(xy1.getA(), xy2.getA());
                int startX = minX / 100 * 100 + 100; // +100为了跳过本区域
                int endX = maxX / 100 * 100;
                for (int x = startX; x <= endX; x += 100) {
                    int y = (int) (k * x + b);
                    if (y < 0 || y > 499) {// 处理边界问题
                        continue;
                    }
                    // System.out.println("(" + x + "," + y + ")");
                    int pos = x + y * 500;
                    int areaId = getReverseAreaIdByPos(pos);
                    areaList.add(areaId);

                }
                for (int x = startX + 99; x <= endX + 99; x += 100) {
                    int y = (int) (k * x + b);
                    if (y < 0 || y > 499) {// 处理边界问题
                        continue;
                    }
                    int pos = x + y * 500;
                    int areaId = getReverseAreaIdByPos(pos);
                    areaList.add(areaId);

                }

                // y轴范围
                int minY = Math.min(xy1.getB(), xy2.getB());
                int maxY = Math.max(xy1.getB(), xy2.getB());
                int startY = minY / 100 * 100 + 100;
                int endY = maxY / 100 * 100;
                for (int y = startY; y <= endY; y += 100) {//
                    // x = (y-b)/k
                    int x = (int) ((y - b) / k);
                    if (x < 0 || x > 499) {// 处理边界问题
                        continue;
                    }
                    int pos = x + y * 500;
                    int areaId = getReverseAreaIdByPos(pos);
                    areaList.add(areaId);
                }

                for (int y = startY + 99; y <= endY + 99; y += 100) {//
                    // x = (y-b)/k
                    int x = (int) ((y - b) / k);
                    if (x < 0 || x > 499) {// 处理边界问题
                        continue;
                    }
                    int pos = x + y * 500;
                    int areaId = getReverseAreaIdByPos(pos);
                    areaList.add(areaId);
                }
            }
        }
        // 去重
        return areaList.stream().distinct().map(MapHelper::convertArea).collect(Collectors.toList());
    }

    /**
     * 获取某个区域的所有点
     *
     * @param areaId
     * @return
     */
    public static List<Integer> getAreaAllPos(int areaId) {
        int xBegin = ((areaId - 1) % 5) * 100;
        int yBegin = (4 - (areaId - 1) / 5) * 100;
        int xEnd = xBegin + 100;
        int yEnd = yBegin + 100;
        List<Integer> posList = new ArrayList<>(100 * 100);
        for (int x = xBegin; x < xEnd; x++) {
            for (int y = yBegin; y < yEnd; y++) {
                int pos = x + y * 500;
                posList.add(pos);
            }
        }

        return posList;
    }

    /**
     * 以x坐标为中心在指定范围内随机一个坐标
     *
     * @param pos
     * @param radius
     * @return
     */
    public static int randomPosByCentre(int pos, int radius) {
        Turple<Integer, Integer> xy = MapHelper.pos2Xy(pos);
        int x = xy.getA(), y = xy.getB();
        int minX = x - radius, maxX = x + radius;
        int minY = y - radius, maxY = y + radius;
        int rdmX = RandomUtil.randomIntIncludeEnd(minX, maxX);
        int rdmY = RandomUtil.randomIntIncludeEnd(minY, maxY);
        return xy2Pos(rdmX, rdmY);
    }

    public static Turple<Integer, Integer> pos2Xy(int pos) {
        return reducePos(pos);
    }

    public static List<Integer> getRoundPos0(int pos, int radius) {
        List<Integer> posList = new ArrayList<>();
        if (radius < 1) {
            return posList;
        }
        Turple<Integer, Integer> xy = reducePos(pos);
        int x = xy.getA();
        int y = xy.getB();
        pos = x + y * 500;
        int areaId = getAreaIdByPos(pos);

        // 计算区域范围
        Turple<Integer, Integer> beginXy = MapHelper.reduceXYInArea(areaId);
        int xMin = beginXy.getA();
        int yMin = beginXy.getB();
        int xMax = xMin + 99;
        int yMax = yMin + 99;

        int xPBegin = x - radius;
        int xPEnd = x + radius;
        int yPBegin = y - radius;
        int yPEnd = y + radius;

        // 不能超过范围,四个角落的情况
        if (xPBegin < xMin) { // 左边越界
            xPBegin = xMin;
            xPEnd = xPBegin + radius;
        }
        if (xPEnd > xMax) {// 右边越界
            xPEnd = xMax;
            xPBegin = xPEnd - (radius + 1);
        }
        if (yPBegin < yMin) {// 定边越界
            yPBegin = yMin;
            yPEnd = yPBegin + (radius + 1);
        }
        if (yPEnd > yMax) {// 底边越界
            yPEnd = yMax;
            yPBegin = yPEnd - (radius + 1);
        }
        for (int posY = yPBegin; posY <= yPEnd; posY++) {
            for (int posX = xPBegin; posX <= xPEnd; posX++) {
                int newPos = posX + posY * 500;
                posList.add(newPos);
            }
        }
        return posList;
    }
    // ============================= TEST =====================

}
