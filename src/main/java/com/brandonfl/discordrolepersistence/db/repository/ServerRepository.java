package com.brandonfl.discordrolepersistence.db.repository;

import com.brandonfl.discordrolepersistence.db.entity.ServerEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServerRepository extends JpaRepository<ServerEntity, Long> {

  Optional<ServerEntity> findByGuid(Long guid);

}
