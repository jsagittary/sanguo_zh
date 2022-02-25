package com.gryphpoem.game.zw.resource.dao.impl.p;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.gryphpoem.game.zw.resource.dao.BaseDao;
import com.gryphpoem.game.zw.resource.domain.p.MailData;
import com.gryphpoem.game.zw.resource.util.DbDataCheckSum;

public class MailDao extends BaseDao {
    private DbDataCheckSum checkSum = new DbDataCheckSum(MailData.class, "lordId");

    public MailData selectData(Long lordId) {
        return this.getSqlSession().selectOne("MailDao.selectData", lordId);
    }

    public void insertData(MailData data) {
        this.getSqlSession().insert("MailDao.insertData", data);
    }

    public int insertFullData(MailData data) {
        return this.getSqlSession().insert("MailDao.insertFullData", data);
    }

    public void updateOptimizeData(MailData data) {
        Map<String, Object> saveParam = checkSum.saveParam(data);
        if (saveParam != null && saveParam.size() > 1) { // >1只有主键,就不进行保存
            this.getSqlSession().update("MailDao.updateOptimizeData", saveParam);
        }
    }

    public int updateData(MailData data) {
        return this.getSqlSession().update("MailDao.updateData", data);
    }

    public int replaceMail(MailData data) {
        return this.getSqlSession().insert("MailDao.replaceMail", data);
    }

    public void save(MailData data) {
        if (updateData(data) == 0) {
            replaceMail(data);
        }
    }

    public Map<Long, MailData> loadData() {
        Map<Long, MailData> map = new HashMap<>();
        long curIndex = 0;
        int count = 2000;
        int pageSize = 0;
        while (true) {
            List<MailData> page = loadData(curIndex, count);
            pageSize = page.size();
            if (pageSize > 0) {
                for (MailData mail : page) {
                    map.put(mail.getLordId(), mail);
                }
                curIndex = page.get(pageSize - 1).getLordId();
            } else {
                break;
            }

            if (page.size() < count) {
                break;
            }
        }
        return map;
    }

    private List<MailData> loadData(long curIndex, int count) {
        Map<String, Object> params = paramsMap();
        params.put("curIndex", curIndex);
        params.put("count", count);
        return this.getSqlSession().selectList("MailDao.loadData", params);
    }
}
