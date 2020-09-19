package com.brandonfl.discordrolepersistence.db.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "server_user_role")
public class ServerUserRoleEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false, updatable = false)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "server_guid", nullable = false, updatable = false)
  private ServerEntity serverGuid;

  @Column(name = "user_guid", nullable = false, updatable = false)
  private Long userGuid;

  @Column(name = "role_guid", nullable = false, updatable = false)
  private Long roleGuid;
}
