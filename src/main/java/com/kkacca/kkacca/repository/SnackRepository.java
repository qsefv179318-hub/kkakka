package com.kkacca.kkacca.repository;

import com.kkacca.kkacca.entity.Snack;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SnackRepository extends JpaRepository<Snack, Long> {

    /** 검색: 과자 이름 부분일치 (대소문자 무시) */
    List<Snack> findByNameContainingIgnoreCase(String keyword);
}
