package com.brandonfl.demobot.db.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.brandonfl.demobot.db.entity.DiscordBotConfigEntity;

public interface DiscordBotConfigRepository extends JpaRepository<DiscordBotConfigEntity, Long> {
  Optional<DiscordBotConfigEntity> findByTokenAndGuid(String key, String guid);
}
