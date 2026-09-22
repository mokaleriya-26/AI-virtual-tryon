package com.virtualfit.ai.repository;

import com.virtualfit.ai.model.TryOnResult;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface TryOnResultRepository extends MongoRepository<TryOnResult, String> {
    List<TryOnResult> findByUserIdOrderByCreatedAtDesc(String userId);
}
