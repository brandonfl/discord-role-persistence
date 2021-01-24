package com.brandonfl.discordrolepersistence.db.entity;

import java.util.HashSet;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "server")
public class ServerEntity {

  @Id
  @Column(name = "guid", nullable = false, updatable = false)
  private Long guid;

  @Column(name = "log_channel")
  private Long logChannel;

  @Column(name = "welcome_back_channel")
  private Long welcomeBackChannel;

  @OneToMany(fetch = FetchType.EAGER, mappedBy = "serverGuid")
  private Set<ServerRoleEntity> roleEntities = new HashSet<>();

  @OneToMany(mappedBy = "serverGuid")
  private Set<ServerUserEntity> userEntities;

}
