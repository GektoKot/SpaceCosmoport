package com.space.repository;

import com.space.model.Ship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * TODO: add documentation
 */
@Repository
public interface ShipRepository extends JpaRepository<Ship, Long> {
}
