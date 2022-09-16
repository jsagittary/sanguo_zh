package com.gryphpoem.game.zw.resource.dao.sqlMap.p;

import com.gryphpoem.game.zw.resource.domain.p.DbMailReport;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-09-15 14:37
 */
public interface MailReportMapper {
    @Select("select * from p_mail_report where lord_id = #{lordId} and key_id = #{keyId}")
    DbMailReport selectByLordId(@Param("lordId") long lordId, @Param("keyId") int keyId);

    @Insert({"insert into p_mail_report(lord_id, key_id, report) values(#{lordId},#{keyId},#{report})"})
    int insert(DbMailReport playerHero);

    @Update({"update p_mail_report set report=#{report} where lord_id = #{lordId} and key_id = #{keyId}"})
    int update(DbMailReport playerHero);
}
