package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

import com.gryphpoem.game.zw.resource.pojo.world.CityHero;

/**
 * 都城升级
 * 
 * @author tyler
 *
 */
public class StaticCityDev {
    private int lv;
    private int exp;
    private List<Integer> form;
    private List<CityHero> formList;
    private int supermineNum;

    public int getLv() {
        return lv;
    }

    public void setLv(int lv) {
        this.lv = lv;
    }

    public int getExp() {
        return exp;
    }

    public void setExp(int exp) {
        this.exp = exp;
    }

    public List<Integer> getForm() {
        return form;
    }

    public void setForm(List<Integer> form) {
        this.form = form;
    }

    public List<CityHero> getFormList() {
        return formList;
    }

    public void setFormList(List<CityHero> formList) {
        this.formList = formList;
    }

    public int getSupermineNum() {
        return supermineNum;
    }

    public void setSupermineNum(int supermineNum) {
        this.supermineNum = supermineNum;
    }

    @Override
    public String toString() {
        return "StaticCityDev [lv=" + lv + ", exp=" + exp + ", form=" + form + ", formList=" + formList
                + ", supermineNum=" + supermineNum + "]";
    }
}
