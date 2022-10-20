package com.gryphpoem.game.zw.resource.dao.impl.p;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.gryphpoem.game.zw.resource.dao.BaseDao;
import com.gryphpoem.game.zw.resource.domain.p.Pay;
import com.gryphpoem.game.zw.resource.domain.p.PaySum;

public class PayDao extends BaseDao {
    public Pay selectPay(int platNo, String orderId) {
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("platNo", platNo);
        map.put("orderId", orderId);
        return this.getSqlSession().selectOne("PayDao.selectPay", map);
    }

    public List<PaySum> selectPaySum() {
        return this.getSqlSession().selectList("PayDao.selectPaySum");
    }

    public List<Pay> selectRolePay(long roleId) {
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("roleId", roleId);
        return this.getSqlSession().selectList("PayDao.selectRolePay", map);
    }

    // public void updateState(Pay pay) {
    // this.getSqlSession().update("PayDao.updateState", pay);
    // }

    // public Pay selectPayType(String serialId, long roleId) {
    // Map<String, Object> map = new HashMap<String, Object>();
    // map.put("serialId", serialId);
    // map.put("roleId", roleId);
    // return this.getSqlSession().selectOne("PayDao.selectPayType", map);
    // }

    // public int updatePayCallback(Pay pay){
    // return this.getSqlSession().update("PayDao.updatePayCallback", pay);
    // }

    public void createPay(Pay pay) {
        this.getSqlSession().insert("PayDao.createPay", pay);
    }

    // public int createSerialId(String serialId, long roleId, int payType) {
    // Pay pay = new Pay();
    // pay.setSerialId(serialId);
    // pay.setRoleId(roleId);
    // pay.setPayType(payType);
    // pay.setState(0);
    // pay.setOrderTime(new Date());
    // return this.getSqlSession().insert("PayDao.createSerialId", pay);
    // }

}
