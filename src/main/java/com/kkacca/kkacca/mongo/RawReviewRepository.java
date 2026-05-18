package com.kkacca.kkacca.mongo;

import com.kkacca.kkacca.entity.RawReview;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface RawReviewRepository extends MongoRepository<RawReview, Long> {
    List<RawReview> findBySnackId(Long snackId);

     boolean existsBySnackIdAndContent(Long snackId, String content);
}
