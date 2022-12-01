package com.gryphpoem.game.zw.resource.dao.sqlMap.p;

import com.gryphpoem.game.zw.resource.domain.p.DbFightRecord;
import org.apache.ibatis.annotations.*;

import java.util.Date;

/**
 * @author zhou jie
 * @time 2022/9/22 18:06
 */
public interface FightRecordMapper {

    @Insert({"replace into p_mail_report(key_id, create_Time, record) values(#{keyId}, #{createTime} ,#{record})"})
    int replace(DbFightRecord dbFightRecord);

    @Delete({"delete from p_mail_report where key_id = #{keyId}"})
    int delete(DbFightRecord dbFightRecord);

    @Update({"update p_mail_report set record=#{record} where key_id = #{keyId}"})
    int update(DbFightRecord dbFightRecord);

    @Select("select key_id, record from p_mail_report where key_id = #{keyId}")
    DbFightRecord selectByReportId(long reportId);

    @Delete({"delete from p_mail_report where create_time < #{expiredTime}"})
    void deleteExpired(@Param("expiredTime") Date expiredTime);

}
