package com.brandonfl.discordrolepersistence.db.repository;

import com.brandonfl.discordrolepersistence.db.entity.ServerRoleEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ServerRoleRepository extends JpaRepository<ServerRoleEntity, Long> {

  @Query("SELECT serverRoleEntity FROM ServerRoleEntity serverRoleEntity WHERE serverRoleEntity.roleGuid = :roleGuid AND serverRoleEntity.serverGuid.guid = :serverGuid")
  Optional<ServerRoleEntity> findByRoleGuidAndRoleGuid(@Param("roleGuid") Long roleGuid, @Param("serverGuid") Long serverGuid);

}
