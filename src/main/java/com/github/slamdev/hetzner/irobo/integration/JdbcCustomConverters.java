package com.github.slamdev.hetzner.irobo.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.slamdev.hetzner.irobo.business.entity.ServerModel;
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

    private final ObjectMapper objectMapper;

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

    @ReadingConverter
    @RequiredArgsConstructor
    public static class StringArrayToInetAddressArrayConverter implements Converter<PGobject[], InetAddress[]> {

        private final StringToInetAddressConverter converter;

        @Override
        public InetAddress[] convert(PGobject[] source) {
            return Arrays.stream(source).map(converter::convert).toArray(InetAddress[]::new);
        }
    }

    @WritingConverter
    @RequiredArgsConstructor
    public static class InetAddressArrayToStringArrayConverter implements Converter<InetAddress[], String[]> {

        private final InetAddressToStringConverter converter;

        @Override
        public String[] convert(InetAddress[] source) {
            return Arrays.stream(source).map(converter::convert).toArray(String[]::new);
        }
    }

    @ReadingConverter
    @RequiredArgsConstructor
    public static class StringToSubnetConverter implements Converter<PGobject, ServerModel.Subnet> {

        private final ObjectMapper objectMapper;

        @Override
        @SneakyThrows
        public ServerModel.Subnet convert(PGobject source) {
            return objectMapper.readValue(source.getValue(), ServerModel.Subnet.class);
        }
    }

    @WritingConverter
    @RequiredArgsConstructor
    public static class SubnetToStringConverter implements Converter<ServerModel.Subnet, String> {

        private final ObjectMapper objectMapper;

        @Override
        @SneakyThrows
        public String convert(ServerModel.Subnet source) {
            return objectMapper.writeValueAsString(source);
        }
    }


    @ReadingConverter
    @RequiredArgsConstructor
    public static class StringToSubnetArrayConverter implements Converter<PGobject, ServerModel.Subnet[]> {

        private final ObjectMapper objectMapper;

        @Override
        @SneakyThrows
        public ServerModel.Subnet[] convert(PGobject source) {
            return objectMapper.readValue(source.getValue(), ServerModel.Subnet[].class);
        }
    }

    @WritingConverter
    @RequiredArgsConstructor
    public static class SubnetArrayToStringConverter implements Converter<ServerModel.Subnet[], String> {

        private final ObjectMapper objectMapper;

        @Override
        @SneakyThrows
        public String convert(ServerModel.Subnet[] source) {
            return objectMapper.writeValueAsString(source);
        }
    }

    @Bean
    @Override
    public JdbcCustomConversions jdbcCustomConversions() {
        StringToInetAddressConverter stringToInetAddressConverter = new StringToInetAddressConverter();
        InetAddressToStringConverter inetAddressToStringConverter = new InetAddressToStringConverter();
        StringArrayToInetAddressArrayConverter stringArrayToInetAddressArrayConverter = new StringArrayToInetAddressArrayConverter(stringToInetAddressConverter);
        InetAddressArrayToStringArrayConverter inetAddressArrayToStringArrayConverter = new InetAddressArrayToStringArrayConverter(inetAddressToStringConverter);
        StringToSubnetConverter stringToSubnetConverter = new StringToSubnetConverter(objectMapper);
        SubnetToStringConverter subnetToStringConverter = new SubnetToStringConverter(objectMapper);
        StringToSubnetArrayConverter stringToSubnetArrayConverter = new StringToSubnetArrayConverter(objectMapper);
        SubnetArrayToStringConverter subnetArrayToStringConverter = new SubnetArrayToStringConverter(objectMapper);
        return new JdbcCustomConversions(Arrays.asList(
                stringToInetAddressConverter, inetAddressToStringConverter,
                stringArrayToInetAddressArrayConverter, inetAddressArrayToStringArrayConverter,
                stringToSubnetConverter, subnetToStringConverter,
                stringToSubnetArrayConverter, subnetArrayToStringConverter
        ));
    }
}
