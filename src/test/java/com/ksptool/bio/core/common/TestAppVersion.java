package com.ksptool.bio.core.common;

import com.ksptool.bio.biz.core.common.AppVersion;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class TestAppVersion {

    @Test
    public void testAppVersion() {
        AppVersion appVersion = AppVersion.of("1.0A1024");
        System.out.println(appVersion);
    }

    @Test
    public void testMultiPartVersion() {
        AppVersion appVersion = AppVersion.of("1.0M1");
        System.out.println("主版本号: " + appVersion.getMv());
        System.out.println("次版本号: " + appVersion.getSv());
        System.out.println("修订号: " + appVersion.getRv());
        System.out.println("构建版本号: " + appVersion.getBv());
    }

    @Test
    public void testNumericVersion() {
        AppVersion appVersion = AppVersion.of("1.6S32");
        System.out.println("数字版本号: " + appVersion.toNumericVersion());
    }

    @Test
    public void testIsGreaterThan() {
        AppVersion appVersion = AppVersion.of("1.0M26");
        System.out.println("1.0M26 大于 1.0M25: " + appVersion.isGreaterThan("1.0M25"));
        System.out.println("1.0M26 小于 1.0M25: " + appVersion.isLessThan("1.0M25"));

        appVersion = AppVersion.of("1.0A1500");
        System.out.println("1.0A1500 大于 1.0B1024: " + appVersion.isGreaterThan("1.0B1024"));
        System.out.println("1.0A1500 小于 1.0B1024: " + appVersion.isLessThan("1.0B1024"));

        appVersion = AppVersion.of("1.0Z");
        System.out.println("1.0Z 大于 1.1A: " + appVersion.isGreaterThan("1.1A"));
        System.out.println("1.0Z 小于 1.1A: " + appVersion.isLessThan("1.1A"));

        appVersion = AppVersion.of("1.12E");
        System.out.println("1.12E 大于 1.13A: " + appVersion.isGreaterThan("1.13A"));
        System.out.println("1.12E 小于 1.13A: " + appVersion.isLessThan("1.13A"));
        System.out.println("1.12E 等于 1.12E: " + appVersion.isEqualTo("1.12E"));
    }

}
