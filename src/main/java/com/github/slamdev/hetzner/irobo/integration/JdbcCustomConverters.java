package com.github.slamdev.hetzner.irobo.integration;

import com.google.common.net.InetAddresses;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.postgresql.util.PGobject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.jdbc.core.convert.JdbcCustomConversions;
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration;

import java.net.InetAddress;
import java.util.Arrays;

@Configuration
@RequiredArgsConstructor
@SuppressWarnings("UnstableApiUsage")
public class JdbcCustomConverters extends AbstractJdbcConfiguration {

    @Bean
    @Override
    public JdbcCustomConversions jdbcCustomConversions() {
        StringToInetAddressConverter stringToInetAddressConverter = new StringToInetAddressConverter();
        InetAddressToStringConverter inetAddressToStringConverter = new InetAddressToStringConverter();
        return new JdbcCustomConversions(Arrays.asList(
                stringToInetAddressConverter, inetAddressToStringConverter
        ));
    }

    @ReadingConverter
    public static class StringToInetAddressConverter implements Converter<PGobject, InetAddress> {

        @Override
        @SneakyThrows
        public InetAddress convert(PGobject source) {
            return InetAddresses.forString(source.getValue());
        }
    }

    @WritingConverter
    public static class InetAddressToStringConverter implements Converter<InetAddress, String> {

        @Override
        public String convert(InetAddress source) {
            return InetAddresses.toAddrString(source);
        }
    }
}
