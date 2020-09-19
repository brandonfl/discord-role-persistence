package com.brandonfl.discordrolepersistence.db.entity;

import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "server_role")
public class ServerRoleEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false, updatable = false)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "server_guid", nullable = false, updatable = false)
  private ServerEntity serverGuid;

  @Column(name = "role_guid", nullable = false, updatable = false)
  private Long roleGuid;

  @Column(name = "blacklisted", nullable = false)
  private boolean blacklisted = false;

  @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
  @JoinTable(name = "server_user_has_server_role",
      joinColumns = @JoinColumn(name = "server_role_id", referencedColumnName = "id"),
      inverseJoinColumns = @JoinColumn(name = "server_user_id", referencedColumnName = "id"))
  private Set<ServerUserEntity> userEntities = new HashSet<>();


}
