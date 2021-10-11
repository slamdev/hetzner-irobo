package com.github.slamdev.hetzner.irobo.business.control;

import com.github.slamdev.hetzner.irobo.business.entity.UpdateHistoryModel;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Optional;

public interface UpdateHistoryRepository extends PagingAndSortingRepository<UpdateHistoryModel, Integer> {

    Optional<UpdateHistoryModel> findFirstByUpdateTypeOrderByExecutedDateDesc(UpdateHistoryModel.UpdateType updateType);
}
