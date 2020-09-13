package com.brandonfl.discordrolepersistence.db.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.brandonfl.discordrolepersistence.db.entity.DiscordBotConfigEntity;

public interface DiscordBotConfigRepository extends JpaRepository<DiscordBotConfigEntity, Long> {
  Optional<DiscordBotConfigEntity> findByTokenAndGuid(String key, String guid);
}
