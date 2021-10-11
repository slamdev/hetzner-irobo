package com.github.slamdev.hetzner.irobo.business.control;

import com.github.slamdev.hetzner.irobo.business.entity.ServerModel;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ServerRepository extends PagingAndSortingRepository<ServerModel, Integer> {

    @Override
    @SuppressWarnings("NullableProblems")
    List<ServerModel> findAll(Sort sort);

    List<ServerModel> findAllBySearchKeywordsLike(@Param("searchKeywords") String searchKeywords, Sort sort);
}
