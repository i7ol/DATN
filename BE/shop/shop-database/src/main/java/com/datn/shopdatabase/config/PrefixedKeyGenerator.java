package com.datn.shopdatabase.config;

import lombok.Getter;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.RandomStringUtils;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.info.GitProperties;
import org.springframework.cache.interceptor.KeyGenerator;

import java.lang.reflect.Method;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@Getter
public class PrefixedKeyGenerator implements KeyGenerator {

    private final String prefix;

    public PrefixedKeyGenerator(GitProperties gitProperties, BuildProperties buildProperties) {
        this.prefix = generatePrefix(gitProperties, buildProperties);
    }

    private String generatePrefix(GitProperties gitProperties, BuildProperties buildProperties) {
        String shortCommitId = gitProperties != null ? gitProperties.getShortCommitId() : null;
        Instant time = buildProperties != null ? buildProperties.getTime() : null;
        String version = buildProperties != null ? buildProperties.getVersion() : null;

        Object p = ObjectUtils.firstNonNull(shortCommitId, time, version, RandomStringUtils.randomAlphanumeric(12));
        return p instanceof Instant ? DateTimeFormatter.ISO_INSTANT.format((Instant) p) : p.toString();
    }

    @NotNull
    @Override
    public Object generate(Object target, Method method, Object... params) {
        return new PrefixedSimpleKey(this.prefix, method.getName(), params);
    }
}
