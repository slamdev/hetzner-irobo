package com.github.slamdev.hetzner.irobo.business.control;

import com.github.slamdev.hetzner.irobo.business.entity.ServerSearchModel;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
public interface ServerSearchRepository extends PagingAndSortingRepository<ServerSearchModel, Integer> {

    @Query("SELECT server_number from server_search WHERE search_data LIKE :searchData")
    List<Integer> findAllBySearchDataLike(@Param("searchData") String searchData);

    @Override
    @Transactional
    void deleteAllById(Iterable ids);
}
