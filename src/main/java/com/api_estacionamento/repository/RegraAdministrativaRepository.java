package com.api_estacionamento.repository;

import com.api_estacionamento.model.RegraAdministrativa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RegraAdministrativaRepository extends JpaRepository<RegraAdministrativa, Long> {
}