package com.github.slamdev.hetzner.irobo.business.entity;

import lombok.Builder;
import lombok.Value;
import lombok.With;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Value
@With
@Builder
@Table("server_search")
public class ServerSearchModel {

    @Id
    int serverNumber;

    String searchData;

    @Version
    Long version;

    @CreatedDate
    Instant createdDate;

    @LastModifiedDate
    Instant lastModifiedDate;
}
