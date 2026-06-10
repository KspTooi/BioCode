package com.ksptool.bio;

import com.ksptool.bio.biz.core.common.AppRegistry;
import com.ksptool.bio.biz.core.common.AppVersion;
import com.ksptool.bio.biz.core.service.RegistrySdk;
import com.ksptool.bio.biz.relay.service.RelayServerService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;


@MapperScan(basePackages = "com.ksptool.bio.biz", annotationClass = Mapper.class)
@EnableTransactionManagement
@SpringBootApplication(
        exclude = {
                org.springframework.boot.actuate.autoconfigure.endpoint.jmx.JmxEndpointAutoConfiguration.class,
        }
)
@EnableScheduling
@EnableCaching
@EnableAsync
@Slf4j
public class BioRunner {

    //应用版本号
    private static final AppVersion appVersion = AppVersion.of("1.7C35");

    @Autowired
    private RelayServerService relayServerService;

    @Autowired
    private RegistrySdk reg;

    public static void main(String[] args) {
        System.setProperty("jdk.httpclient.allowRestrictedHeaders", "content-length,host,connection");
        SpringApplication.run(BioRunner.class, args);
    }

    /**
     * 获取应用版本号
     *
     * @return 应用版本号
     */
    public static AppVersion getVersion() {
        return appVersion;
    }


    @PostConstruct
    public void init() {
        relayServerService.initRelayServer();
    }

    /**
     * 应用启动时检查全局配置
     */
    @Bean
    public ApplicationRunner configInitializer() {
        return args -> {

            //如果在注册表里面禁用了"自动升级向导"功能,则直接返回
            if (reg.getInt(AppRegistry.CIW_ALLOW_UPGRADE.getFullKey(), 1) == 0) {
                log.info("自动升级向导功能已禁用,跳过检查升级。");
                return;
            }

            //查询注册表中的应用版本
            var regVersionStr = reg.getString(AppRegistry.CM_VERSION.getFullKey(), "1.0A");

            //如果当前版本大于注册表中的版本,则触发升级向导
            if (appVersion.isGreaterThan(regVersionStr)) {

                log.info("应用程序版本落后 当前:{} 最新:{} 已激活升级向导。", regVersionStr, appVersion);

                //设置注册表以启用安装向导
                reg.setInt(AppRegistry.CIW_ENABLED.getFullKey(), 1);
            }

        };
    }
}
