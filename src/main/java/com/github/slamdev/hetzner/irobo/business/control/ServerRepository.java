package com.github.slamdev.hetzner.irobo.business.control;

import com.github.slamdev.hetzner.irobo.business.entity.ServerModel;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ServerRepository extends PagingAndSortingRepository<ServerModel, Integer> {

    List<ServerModel> findAllByServerNumberIsIn(List<Integer> serverNumbers, Sort sort);

    @Override
    List<ServerModel> findAll();

    List<ServerModel> findAllByZabbixIpIsNotNull();

    @Transactional
    @Override
    void deleteAllById(Iterable ids);
}
