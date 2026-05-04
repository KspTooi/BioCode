package com.ksptool.bio.biz.core.common;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 标准化应用程序版本号类
 * 格式要求: {主版本}.{次版本}{修订字母}{可选构建号}
 * 示例: 1.0A, 1.0A1, 1.0A1815
 *
 * @author KspTooi
 * @since 1.6.14(N).45
 */
@Getter
public class AppVersion {

    /**
     * 正则表达式解释：
     * ^(\d+)       : 主版本号 (数字)
     * \.           : 分隔点
     * (\d+)        : 次版本号 (数字)
     * ([a-zA-Z]+)  : 修订号 (至少一个字母，必选)
     * (\d*)        : 构建号 (紧跟字母后的数字，可选)
     * $            : 结束
     */
    private static final String VERSION_REGEX = "^(\\d+)\\.(\\d+)([a-zA-Z]+)(\\d*)$";
    private static final Pattern PATTERN = Pattern.compile(VERSION_REGEX);

    //主版本号 major version
    private Integer mv;

    //次版本号 sub version
    private Integer sv;

    //修订号 revision version
    private String rv;

    //构建版本号 build version
    private Integer bv;

    /**
     * 将版本号字符串转换为AppVersion对象
     *
     * @param version 版本号字符串 格式必须为 1.0A
     * @return AppVersion对象
     */
    public static AppVersion of(String version) {

        Matcher matcher = PATTERN.matcher(version);

        if (!matcher.matches()) {
            throw new IllegalArgumentException("无效的版本格式[" + version + "]。版本必须符合格式: {主版本}.{次版本}{修订字母}{可选构建号}");
        }

        AppVersion appVersion = new AppVersion();
        appVersion.mv = Integer.parseInt(matcher.group(1));
        appVersion.sv = Integer.parseInt(matcher.group(2));
        appVersion.rv = matcher.group(3); //提取字母部分

        String buildStr = matcher.group(4); //提取紧跟其后的数字部分
        if (StringUtils.isNotBlank(buildStr)) {
            appVersion.bv = Integer.parseInt(buildStr);
        }
        if (StringUtils.isBlank(buildStr)) {
            appVersion.bv = 0;
        }

        return appVersion;
    }

    /**
     * 转换为三段式纯数字版本号 X.X.X
     * 这是为了兼容那些不支持字母修订号的版本号系统和框架，例如flyway
     * 规则：第三段 = [修订号数字][构建号数字] 直接拼接而成
     */
    public String toNumericVersion() {

        //修订号转数字 (取首字母, A=1, B=2...)
        int rvNum = 0;

        if (StringUtils.isNotBlank(rv)) {
            char firstChar = rv.toUpperCase().charAt(0);
            rvNum = firstChar - 'A' + 1;
        }

        //直接拼接第三段
        String thirdPart = String.valueOf(rvNum) + bv;
        return String.format("%d.%d.%s", mv, sv, thirdPart);
    }

    /**
     * 判断给定的版本号是否大于目标版本号
     */
    public boolean isGreaterThan(AppVersion otherVersion) {
        if (otherVersion == null) {
            return true; // 与空值比较，当前对象始终更大（或者根据业务需求抛出异常）
        }

        //比较主版本
        if (!this.mv.equals(otherVersion.mv)) {
            return this.mv > otherVersion.mv;
        }

        //比较次版本
        if (!this.sv.equals(otherVersion.sv)) {
            return this.sv > otherVersion.sv;
        }

        //比较修订号 (字母字典序)
        int rvCompare = this.rv.compareTo(otherVersion.rv);
        if (rvCompare != 0) {
            return rvCompare > 0;
        }

        //比较构建号
        return this.bv > otherVersion.bv;
    }

    /**
     * 判断给定的版本号是否小于目标版本号
     */
    public boolean isLessThan(AppVersion otherVersion) {
        if (otherVersion == null) {
            return false;
        }
        //利用反向调用，完美避开"相等"时的逻辑错误
        return otherVersion.isGreaterThan(this);
    }

    /**
     * 判断当前版本是否等于目标版本
     * (强烈建议顺手补上这个方法，逻辑就彻底闭环了)
     */
    public boolean isEqualTo(AppVersion otherVersion) {
        if (otherVersion == null) {
            return false;
        }
        return this.mv.equals(otherVersion.mv) &&
                this.sv.equals(otherVersion.sv) &&
                this.rv.equals(otherVersion.rv) &&
                this.bv.equals(otherVersion.bv);
    }

    /**
     * 判断给定的版本号是否大于当前版本号
     *
     * @param version 版本号
     * @return 是否大于
     */
    public boolean isGreaterThan(String version) {
        return isGreaterThan(AppVersion.of(version));
    }

    /**
     * 判断给定的版本号是否小于当前版本号
     *
     * @param version 版本号
     * @return 是否小于
     */
    public boolean isLessThan(String version) {
        return isLessThan(AppVersion.of(version));
    }

    /**
     * 判断给定的版本号是否等于当前版本号
     *
     * @param version 版本号
     * @return 是否等于
     */
    public boolean isEqualTo(String version) {
        return isEqualTo(AppVersion.of(version));
    }

    /**
     * 将AppVersion对象转换为字符串
     *
     * @return 字符串
     */
    @Override
    public String toString() {
        return String.format("%d.%d%s%d", mv, sv, rv, bv);
    }


}
