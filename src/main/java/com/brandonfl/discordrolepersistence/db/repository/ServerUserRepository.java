package com.brandonfl.discordrolepersistence.db.repository;

import com.brandonfl.discordrolepersistence.db.entity.ServerUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServerUserRepository extends JpaRepository<ServerUserEntity, Long> {

}
