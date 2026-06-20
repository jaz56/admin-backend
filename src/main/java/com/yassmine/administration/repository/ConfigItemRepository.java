// ConfigItemRepository.java
package com.yassmine.administration.repository;

import com.yassmine.administration.model.ConfigItem;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface ConfigItemRepository extends MongoRepository<ConfigItem, String> {
    List<ConfigItem> findByCategoryAndActiveTrueOrderByOrderAsc(String category);
}