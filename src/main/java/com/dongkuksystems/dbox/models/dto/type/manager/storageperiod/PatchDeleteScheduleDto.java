package com.dongkuksystems.dbox.models.dto.type.manager.storageperiod;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import com.documentum.fc.client.IDfSession;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PatchDeleteScheduleDto {
  @ApiModelProperty(value = "회사코드")
  private String comCode;
  @ApiModelProperty(value = "수정한 시간값")
  private String hourtime;
  @ApiModelProperty(value = "휴지통, 폐기 구분")
  private String methodName;

  public static String PatchDeleteSchedule(IDfSession idfSession, PatchDeleteScheduleDto dto) throws Exception {    
    LocalDateTime nowDay = LocalDateTime.now();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");    
    LocalDateTime nextDay = nowDay.plusDays(1);
    LocalTime nowTime = LocalTime.now();
    int hour = nowTime.getHour();   
 
    if(dto.getHourtime().equals("23") && hour < 23) {
      String a =  "update dm_job object set a_next_invocation = date('";        
      String b = nowDay.format(formatter);
      String c = " ";
      String d = dto.getHourtime();
      String e = ":00:00', 'yyyy-MM-dd hh:mi:ss') where object_name='";
      String f = dto.getMethodName();
      String g = dto.getComCode();
      String h = "'";
      
      String dql = a+b+c+d+e+f+g+h;
    
      return dql;
    
    }
    else {
      String a =  "update dm_job object set a_next_invocation = date('";        
      String b = nextDay.format(formatter);
      String c = " ";
      String d = dto.getHourtime();
      String e = ":00:00', 'yyyy-MM-dd hh:mi:ss') where object_name='";
      String f = dto.getMethodName();
      String g = dto.getComCode();
      String h = "'";
      
      String dql = a+b+c+d+e+f+g+h;
    
      return dql;
   
    }       
      
  }
}

