package com.brandonfl.discordrolepersistence.db.repository;

import com.brandonfl.discordrolepersistence.db.entity.ServerUserEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ServerUserRepository extends JpaRepository<ServerUserEntity, Long> {

  @Query("SELECT serverUserEntity FROM ServerUserEntity serverUserEntity WHERE serverUserEntity.userGuid = :userGuid AND serverUserEntity.serverGuid.guid = :serverGuid")
  Optional<ServerUserEntity> findByUserGuidAndServerGuid(@Param("userGuid") Long userGuid, @Param("serverGuid") Long serverGuid);

}
