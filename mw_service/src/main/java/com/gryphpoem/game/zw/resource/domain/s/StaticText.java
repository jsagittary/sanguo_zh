package com.gryphpoem.game.zw.resource.domain.s;

/**
 * @Description 配置文字描述信息
 * @author TanDonghai
 * @date 创建时间：2017年9月7日 下午2:01:06
 *
 */
public class StaticText {
    private String id;// 文本表ID, 格式:s_表名_表内ID,例如s_prop_1001
    private String name;// 对应的文本名字,例如装备名,将领名,建筑名等
    private String dec1;// 对应的文本描述,例如装备属性描述,将领描述,建筑名等,包含通配符

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDec1() {
        return dec1;
    }

    public void setDec1(String dec1) {
        this.dec1 = dec1;
    }

    @Override
    public String toString() {
        return "StaticText [id=" + id + ", name=" + name + ", dec1=" + dec1 + "]";
    }

}
