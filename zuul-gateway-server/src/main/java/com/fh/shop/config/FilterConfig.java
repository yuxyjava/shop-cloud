package com.fh.shop.config;

import com.fh.shop.filter.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

//    @Bean
//    public Filter1 filter1() {
//        return new Filter1();
//    }
//
//    @Bean
//    public Filter2 filter2() {
//        return new Filter2();
//    }
//
//    @Bean
//    public Filter3 filter3() {
//        return new Filter3();
//    }
//
//    @Bean
//    public Filter4 filter4() {
//        return new Filter4();
//    }

    @Bean
    public TokenFilter tokenFilter() {
        return new TokenFilter();
    }

}
