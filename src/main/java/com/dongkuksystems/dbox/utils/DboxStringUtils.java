package com.dongkuksystems.dbox.utils;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import com.dongkuksystems.dbox.constants.DboxObjectType;
import com.dongkuksystems.dbox.models.table.etc.VUser;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DboxStringUtils {

  /**
   * 파일명에 복사본 추가
   * @param String prType, String objectId
   * @return object Id 뒤 5글자 추출  
   */
  public static String getPrCodeFromObjId(String prType, String objId) {
    if (DboxObjectType.PROJECT.getValue().equals(prType)) return "p".concat(objId.substring(objId.length()-5));
    else return "r".concat(objId.substring(objId.length()-5)); 
  }

  /**
   * 특수문자 제거
   * @param String fileName
   * @return 특수문자제거된 
   */
  public static String replaceSymbols(String str) {
    String match = "[&]";
    str =str.replaceAll(match, "");
    return str;
  }
  
  /**
   * 문서 Naming Rule
   * 파일명_팀명_버전_날짜_작성자
   * @param String fileName
   * @return fileName [복사본].extension  
   */
  public static String getDboxFileNameing(String fileName, String extension, VUser user, String version) { 
    return String.format("%s_%s_%s_%s_%s.%s", fileName, user.getOrgNm().replace("/", "-") , version, DateTimeUtils.now("yyyy-MM-dd"), user.getDisplayName(), extension);
  }
  
  public static String replaceLast(String str, String regex, String replacement) {
    int regexIndexOf = str.lastIndexOf(regex);
    if (regexIndexOf == -1) {
      return str;
    } else {
      return str.substring(0, regexIndexOf) + replacement + str.substring(regexIndexOf + regex.length());
    }
  }

  /**
   * 파일명에 복사본 추가
   * @param String fileName
   * @return fileName [복사본].extension  
   */
  public static String addCopyFileName(String fileName, String extension) {
    String originalFileName = fileName.trim();
    return originalFileName.concat(" [복사본].").concat(extension);
  }

  /**
   * 동일 경로 & 동일 이름 & 편집/삭제 권한이 없는 문서가 있을 경우 숫자를 붙여 등록한다.
   * @param String fileName
   * @return fileName (1++).extension  
   */
  public static String addFileNameNumber(String fileName, String extension) {
//    String extension = fileName.substring(fileName.lastIndexOf("."));
    String originalFileName = fileName.trim();
    String number = originalFileName.substring(originalFileName.lastIndexOf("(") + 1, originalFileName.length() - 1).trim();
    String newNumber = null;
    try {
      newNumber = String.valueOf(Integer.valueOf(number) + 1);
    } catch (Exception e) {
      return originalFileName.concat(" (1).").concat(extension);
    }
    String rst = replaceLast(originalFileName,
        originalFileName.substring(originalFileName.lastIndexOf("("), originalFileName.length()),
        "(".concat(newNumber).concat(").")).concat(extension);
    return rst;
  }

  /**
   * 동일 경로 & 동일 이름 & 편집/삭제 권한이 없는 폴더가 있을 경우 숫자를 붙여 등록한다.
   * @param String folderName
   * @return fileName (1++).extension  
   */
  public static String addFolderNameNumber(String folderName) {
    String originalfolderName = folderName.trim();
    String number = originalfolderName.substring(originalfolderName.lastIndexOf("(") + 1, originalfolderName.length() - 1).trim();
    String newNumber = null;
    try {
      newNumber = String.valueOf(Integer.valueOf(number) + 1);
    } catch (Exception e) {
      return originalfolderName.concat(" (1)");
    }
    String rst = replaceLast(originalfolderName,
        originalfolderName.substring(originalfolderName.lastIndexOf("("), originalfolderName.length()),
        "(".concat(newNumber).concat(")"));
    return rst;
  }
  
  public static String convertFileNameToNaming(String originalName, String userName, String orgNm, String version) {
    checkNotNull(originalName, "originalName must be provided.");
    checkNotNull(userName, "userName must be provided.");
    checkNotNull(orgNm, "orgNm must be provided.");
    checkNotNull(version, "version must be provided.");
    // 파일명_팅명_버전_날짜_작성자
    String newFileName = "";
    newFileName = MessageFormat.format("{0}_{1}_{2}_{3}_{4}.{5}", originalName, orgNm, version, LocalDate.now(),
        userName, originalName.substring(originalName.lastIndexOf(".") + 1));
    checkArgument(newFileName.length() <= 255, "문서/폴더 이름은 전체 80자를 넘지 않도록 함 (한글 80자, 영문 255자)");
    return newFileName;
  }

  public static String convertFileNameToNaming(String originalName, String userName, String orgNm, String version, String extension) {
    checkNotNull(originalName, "originalName must be provided.");
    checkNotNull(userName, "userName must be provided.");
    checkNotNull(orgNm, "orgNm must be provided.");
    checkNotNull(version, "version must be provided.");
    checkArgument(originalName.length() <= 255, "문서/폴더 이름은 전체 80자를 넘지 않도록 함 (한글 80자, 영문 255자)");
    // 파일명_팅명_버전_날짜_작성자
    String newFileName = "";
    newFileName = MessageFormat.format("{0}_{1}_{2}_{3}_{4}.{5}", originalName, orgNm, version, LocalDate.now(),
        userName, extension);
    return newFileName;
  }

  @Deprecated
  public static String extractFileName(String targetName, String uploadName, String orgNm, String version) {
    checkNotNull(targetName, "targetName must be provided.");
    checkNotNull(uploadName, "uploadName must be provided.");
    checkNotNull(orgNm, "orgNm must be provided.");
    // 파일명_팅명_버전_날짜_작성자
    String[] splitNames = uploadName.split("_");
    if (splitNames.length < 5) {
      return uploadName;
    }
    if (!(orgNm.equals(splitNames[1]) && targetName.equals(splitNames[0]))) {
      return uploadName;
    }
    boolean isNumeric = splitNames[2].matches("[+-]?\\d*(\\.\\d+)?");
    boolean isDate = splitNames[3].matches("\\d{4}-(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01])");
    if (isNumeric && isDate) {
      return splitNames[0].concat(uploadName.substring(uploadName.lastIndexOf(".")));
    } else {
      return uploadName;
    }
  }

  public static String extractFileName(String uploadName, String extension) {
    checkNotNull(uploadName, "uploadName must be provided.");
    checkNotNull(extension, "extension must be provided.");
    // 파일명_팅명_버전_날짜_작성자
    String[] splitNames = uploadName.split("_");
    if (splitNames.length < 5) {
      return uploadName;
    }
    try {
      boolean isNumeric = splitNames[splitNames.length-3].matches("[+-]?\\d*(\\.\\d+)?");
      boolean isDate = splitNames[splitNames.length-2].matches("\\d{4}-(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01])");
      if (isNumeric && isDate) {
        String rst = "";
        for (int i=0; i<splitNames.length-4; i++) {
          rst = i==0?rst.concat(splitNames[i]):rst.concat("_").concat(splitNames[i]);
        }
        return rst.concat(".").concat(extension);
      } else {
        return uploadName;
      }
    } catch (Exception e) {
      return uploadName;
    }
  }

  public static Map<String, Object> strToMap(String str) throws JsonParseException, JsonMappingException, IOException {
    if (str.isEmpty())
      return new HashMap<String, Object>();
    ObjectMapper mapper = new ObjectMapper();
    Map<String, Object> map = mapper.readValue(str, Map.class);
    return map;
  }

  public static String camelCaseToUnderscores(String camel) {
    String underscore;
    underscore = String.valueOf(Character.toLowerCase(camel.charAt(0)));
    for (int i = 1; i < camel.length(); i++) {
      underscore += Character.isLowerCase(camel.charAt(i)) ? String.valueOf(camel.charAt(i))
          : "_" + String.valueOf(Character.toLowerCase(camel.charAt(i)));
    }
    return underscore;
  }

  /**
   * @param camelCase a camel case string : only letters without consecutive
   *                  uppercase letters.
   * @return the transformed string or the same if not camel case.
   */
  public static String camelCaseToUnderScoreUpperCase(String camelCase) {
    String result = "";
    boolean prevUpperCase = false;
    for (int i = 0; i < camelCase.length(); i++) {
      char c = camelCase.charAt(i);
      if (!Character.isLetter(c))
        return camelCase;
      if (Character.isUpperCase(c)) {
        if (prevUpperCase)
          return camelCase;
        result += "_" + c;
        prevUpperCase = true;
      } else {
        result += Character.toUpperCase(c);
        prevUpperCase = false;
      }
    }
    return result;
  }

  /**
   * underscore
   * 
   * @param value
   * @return
   */
  public static String camelToUnder(String value) {
    String regex = "([a-z])([A-Z])";
    String replacement = "$1_$2";

    return value.replaceAll(regex, replacement).toLowerCase();
  }

  public static String camelToUnderline(String param) {
    if (param == null || param.length() == 0) {
      return param;
    }
    int len = param.length();
    StringBuilder sb = new StringBuilder(len);
    for (int i = 0; i < len; i++) {
      char c = param.charAt(i);
      if (Character.isUpperCase(c)) {
        sb.append('_');
        sb.append(Character.toLowerCase(c));
      } else {
        sb.append(c);
      }
    }
    return sb.toString();
  }
  
  public static boolean CheckString(String str) {
    return str == null || str.isEmpty();
  }
}
