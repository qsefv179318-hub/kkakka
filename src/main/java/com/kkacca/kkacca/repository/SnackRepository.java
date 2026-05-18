package com.kkacca.kkacca.repository;

import com.kkacca.kkacca.entity.Snack;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SnackRepository extends JpaRepository<Snack, Long> {
    List<Snack> findByNameContainingIgnoreCase(String keyword);
}
