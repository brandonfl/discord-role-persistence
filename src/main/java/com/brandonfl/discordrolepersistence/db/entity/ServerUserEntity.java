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
@Table(name = "server_user")
public class ServerUserEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false, updatable = false)
  private Long id;

  @Column(name = "user_guid", nullable = false, updatable = false)
  private Long userGuid;

  @ManyToOne
  @JoinColumn(name = "server_guid", nullable = false, updatable = false)
  private ServerEntity serverGuid;

  @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
  @JoinTable(name = "server_user_has_server_role",
      joinColumns = @JoinColumn(name = "server_user_id", referencedColumnName = "id"),
      inverseJoinColumns = @JoinColumn(name = "server_role_id", referencedColumnName = "id"))
  private Set<ServerRoleEntity> roleEntities = new HashSet<>();


}
