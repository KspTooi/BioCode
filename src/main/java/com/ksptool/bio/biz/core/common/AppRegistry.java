package com.ksptool.bio.biz.core.common;

import lombok.Getter;

/**
 * 系统注册表
 *
 * @author KspTooi
 * @since 1.6.13(M).107
 */
public enum AppRegistry {

    // ==================== 系统配置 ====================
    CM_VERSION("config.main", "version", "1.0A", NvalueKind.STRING, "系统版本", "系统内部版本号,不要修改这个值,它由系统自动更新."),

    CC_USER_SESSION_EXPIRE("config.cache.userSession", "expire", "5", NvalueKind.INTEGER, "用户Session缓存过期时间(分钟)"),
    CC_USER_SESSION_MAX_SIZE("config.cache.userSession", "max_size", "1000", NvalueKind.INTEGER, "用户Session缓存最大条目数"),

    CC_USER_PROFILE_EXPIRE("config.cache.userProfile", "expire", "5", NvalueKind.INTEGER, "用户个人信息缓存过期时间(分钟)"),
    CC_USER_PROFILE_MAX_SIZE("config.cache.userProfile", "max_size", "1000", NvalueKind.INTEGER, "用户个人信息缓存最大条目数"),

    CC_MENU_TREE_EXPIRE("config.cache.menuTree", "expire", "30", NvalueKind.INTEGER, "菜单与按钮树缓存过期时间(分钟)"),
    CC_MENU_TREE_MAX_SIZE("config.cache.menuTree", "max_size", "100", NvalueKind.INTEGER, "菜单与按钮树缓存最大条目数"),

    CC_ENDPOINT_EXPIRE("config.cache.endpoint", "expire", "30", NvalueKind.INTEGER, "端点接口动态权限配置缓存过期时间(分钟)"),
    CC_ENDPOINT_MAX_SIZE("config.cache.endpoint", "max_size", "2000", NvalueKind.INTEGER, "端点接口动态权限配置缓存最大条目数"),

    CIW_ENABLED("config.install_wizard", "enabled", "0", NvalueKind.INTEGER, "启用向导模式", "当前是否位于向导模式 0:否 1:是"),
    CIW_ALLOW_UPGRADE("config.install_wizard", "allow_upgrade", "0", NvalueKind.INTEGER, "版本落后时自动触发安装向导", "0:否 1:是"),

    //==================== 域配置 ====================
    FA_ALLOW_USER_REGISTER("field.auth", "allow_user_register", "0", NvalueKind.INTEGER, "允许用户注册", "0:否 1:是"),
    FA_CAPTCHA_ENABLED_REGISTER("field.auth", "captcha_enabled_register", "0", NvalueKind.INTEGER, "注册验证码启用", "0:否 1:是"),
    FA_CAPTCHA_ENABLED_LOGIN("field.auth", "captcha_enabled_login", "0", NvalueKind.INTEGER, "登录验证码启用", "0:否 1:是"),
    FA_COOKIE_ALLOWED("field.auth", "cookie_allowed", "0", NvalueKind.INTEGER, "允许使用Cookie鉴权", "0:否 1:是"),
    FA_COOKIE_NAME("field.auth", "cookie_name", "bio-session-id", NvalueKind.STRING, "Cookie名称"),

    FA_ASP_MAX_ATTEMPTS_USER("field.auth", "asp_max_attempts_user", "5", NvalueKind.INTEGER, "同一用户最大尝试登录次数", "为-1时表示不限制"),
    FA_ASP_LOCK_TIME_USER("field.auth", "asp_lock_time_user", "10", NvalueKind.INTEGER, "达到最大尝试次数后的锁定时间(分钟)", "为-1时表示不限制"),
    FA_ASP_ALLOW_WEAK_PASSWORD("field.auth", "asp_allow_weak_password", "1", NvalueKind.INTEGER, "是否允许弱密码", "0:否 1:是"),
    FA_ASP_ALLOW_USERNAME_IN_PASSWORD("field.auth", "asp_allow_username_in_password", "1", NvalueKind.INTEGER, "是否允许密码包含用户名", "0:否 1:是"),
    FA_ASP_REQUIRE_SPECIAL("field.auth", "asp_require_special", "0", NvalueKind.INTEGER, "是否要求特殊字符", "0:否 1:是"),
    FA_ASP_MIN_LENGTH("field.auth", "asp_min_length", "8", NvalueKind.INTEGER, "密码最小长度", "为-1时表示不限制"),

    FA_SESSION_EXPIRE("field.auth", "session_expire", "30", NvalueKind.INTEGER, "用户Session过期时间(分钟)", "为-1时表示不限制"),


    //==================== GEN域配置 ====================
    FG_PROXY_ENABLE("field.gen", "proxy_enable", "0", NvalueKind.INTEGER, "是否启用代理", "0:否 1:是"),
    FG_PROXY_TYPE("field.gen", "proxy_type", "HTTP", NvalueKind.STRING, "代理类型", "只支持HTTP代理"),
    FG_PROXY_HOST("field.gen", "proxy_host", "127.0.0.1", NvalueKind.STRING, "代理主机"),
    FG_PROXY_PORT("field.gen", "proxy_port", "8080", NvalueKind.INTEGER, "代理端口"),
    FG_PROXY_USERNAME("field.gen", "proxy_username", "?", NvalueKind.STRING, "代理用户名(为?时表示不使用代理用户名)"),
    FG_PROXY_PASSWORD("field.gen", "proxy_password", "?", NvalueKind.STRING, "代理密码(为?时表示不使用代理密码)"),
    ;


    //父节点全路径
    private final String nodeKeyPath;

    //条目键名
    private final String nkey;

    //默认值
    private final String value;

    //数据类型
    private final NvalueKind nvalueKind;

    //标签
    private final String label;

    //备注
    private final String remark;


    AppRegistry(String nodeKeyPath, String nkey, String value, NvalueKind nvalueKind, String label) {
        this(nodeKeyPath, nkey, value, nvalueKind, label, null);
    }

    AppRegistry(String nodeKeyPath, String nkey, String value, NvalueKind nvalueKind, String label, String remark) {
        this.nodeKeyPath = nodeKeyPath;
        this.nkey = nkey;
        this.value = value;
        this.nvalueKind = nvalueKind;
        this.label = label;
        this.remark = remark;
    }

    public String getNodeKeyPath() {
        return nodeKeyPath;
    }

    public String getNkey() {
        return nkey;
    }

    public String getValue() {
        return value;
    }

    public NvalueKind getNvalueKind() {
        return nvalueKind;
    }

    public String getLabel() {
        return label;
    }

    public String getRemark() {
        return remark;
    }

    /**
     * 拼接完整 keyPath,供 SDK 读取条目时使用
     */
    public String getFullKey() {
        return nodeKeyPath + "." + nkey;
    }

    @Getter
    public enum NvalueKind {

        //字串
        STRING(0),

        //整数
        INTEGER(1),

        //浮点
        DOUBLE(2),

        //日期时间
        DATETIME(3);

        private final int value;

        NvalueKind(int value) {
            this.value = value;
        }
    }

}
