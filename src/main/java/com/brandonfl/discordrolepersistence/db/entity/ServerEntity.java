package com.brandonfl.discordrolepersistence.db.entity;

import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
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
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "guid", nullable = false, updatable = false)
  private Long guid;

  @Column(name = "command_prefix", nullable = false)
  private String commandPrefix;

  @Column(name = "log_channel")
  private Long logChannel;

  @OneToMany(mappedBy = "serverGuid")
  private Set<ServerRoleBlacklistEntity> roleBlacklist;

  public Set<Long> getRoleBlacklistIds() {
    return roleBlacklist
        .stream()
        .map(ServerRoleBlacklistEntity::getRoleGuid)
        .collect(Collectors.toSet());
  }

  @OneToMany(mappedBy = "serverGuid")
  private Set<ServerUserRoleEntity> serverUserRoles;

}
