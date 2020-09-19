package com.brandonfl.discordrolepersistence.db.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "server_role_blacklist")
public class ServerRoleBlacklistEntity {

  @ManyToOne
  @JoinColumn(name = "server_guid", nullable = false)
  private ServerEntity serverGuid;

  @Column(name = "role_guid", nullable = false, updatable = false)
  private Long roleGuid;

}
