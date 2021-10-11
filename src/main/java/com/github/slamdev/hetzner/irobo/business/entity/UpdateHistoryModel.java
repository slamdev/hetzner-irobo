package com.github.slamdev.hetzner.irobo.business.entity;

import lombok.Builder;
import lombok.Value;
import lombok.With;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Value
@With
@Builder
@Table("update_history")
public class UpdateHistoryModel {

    @Id
    Long id;
    UpdateType updateType;
    Instant executedDate;

    public enum UpdateType {
        HETZNER_SERVER_LIST,
        ZABBIX_HOSTS_LIST,
        SERVER_SEARCH,
        SERVER_TAGS,
    }
}
