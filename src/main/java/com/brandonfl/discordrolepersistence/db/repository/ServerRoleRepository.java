package com.brandonfl.discordrolepersistence.db.repository;

import com.brandonfl.discordrolepersistence.db.entity.ServerRoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServerRoleRepository extends JpaRepository<ServerRoleEntity, Long> {

}
