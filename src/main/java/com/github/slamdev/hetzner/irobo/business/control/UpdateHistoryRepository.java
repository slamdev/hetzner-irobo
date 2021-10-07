package com.github.slamdev.hetzner.irobo.business.control;

import com.github.slamdev.hetzner.irobo.business.entity.UpdateHistoryModel;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Transactional
public interface UpdateHistoryRepository extends PagingAndSortingRepository<UpdateHistoryModel, Integer> {

    Optional<UpdateHistoryModel> findFirstByUpdateTypeOrderByExecutedDateDesc(UpdateHistoryModel.UpdateType updateType);
}
