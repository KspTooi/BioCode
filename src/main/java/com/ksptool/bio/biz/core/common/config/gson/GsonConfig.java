package com.ksptool.bio.biz.core.common.config.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ksptool.bio.commons.config.LocalDateTimeAdapter;
import com.ksptool.bio.commons.config.OffsetDateTimeAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

/**
 * @since 1.6.21(U).90
 */
@Configuration
public class GsonConfig {

    @Bean
    public Gson gson() {
        return new GsonBuilder()
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(OffsetDateTime.class, new OffsetDateTimeAdapter())
                .create();
    }


}
