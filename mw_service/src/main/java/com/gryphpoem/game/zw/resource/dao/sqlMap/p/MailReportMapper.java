package com.gryphpoem.game.zw.resource.dao.sqlMap.p;

import com.gryphpoem.game.zw.resource.domain.p.DbMailReport;
import org.apache.ibatis.annotations.*;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-09-15 14:37
 */
public interface MailReportMapper {
    @Select("select lord_id, key_id, report from p_mail_report where lord_id = #{lordId} and key_id = #{keyId}")
    DbMailReport selectByLordId(@Param("lordId") long lordId, @Param("keyId") int keyId);

    @Insert({"replace into p_mail_report(lord_id, key_id, report) values(#{lordId},#{keyId},#{report})"})
    int insert(DbMailReport dbMailReport);

    @Update({"update p_mail_report set report=#{report} where lord_id = #{lordId} and key_id = #{keyId}"})
    int update(DbMailReport dbMailReport);

    @Delete({"delete from p_mail_report where lord_id = #{lordId} and key_id = #{keyId}"})
    int delete(DbMailReport dbMailReport);
}
