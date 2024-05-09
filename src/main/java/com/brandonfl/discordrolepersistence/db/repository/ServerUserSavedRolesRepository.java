/*
 * MIT License
 *
 * Copyright (c) 2021 Fontany--Legall Brandon
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.brandonfl.discordrolepersistence.db.repository;

import com.brandonfl.discordrolepersistence.db.entity.ServerUserSavedRolesEntity;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ServerUserSavedRolesRepository extends JpaRepository<ServerUserSavedRolesEntity, Long> {

  @Modifying
  @Transactional
  void deleteAllByServerGuidAndUserGuid(Long serverGuid, Long userGuid);

  @Modifying
  @Transactional
  @Query(value = """
      INSERT IGNORE 
      INTO server_user_saved_roles (server_guid, role_guid, user_guid) 
      VALUES (:serverGuid, :roleGuid, :userGuid)
      """, nativeQuery = true)
  void insertIgnore(@Param("serverGuid") Long serverGuid, @Param("roleGuid") Long roleGuid, @Param("userGuid") Long userGuid);

  @Query(value = """
  SELECT server_user_saved_roles.role_guid
  FROM server_user_saved_roles
  WHERE server_user_saved_roles.server_guid = :serverGuid 
    AND server_user_saved_roles.user_guid = :userGuid
    AND server_user_saved_roles.role_guid NOT IN (
      SELECT server_role_blacklist.role_guid
      FROM server_role_blacklist
      WHERE server_role_blacklist.server_guid = :serverGuid
    )
  """, nativeQuery = true)
  List<Long> findAllNonBacklistedRolesByServerGuidAndUserGuid(@Param("serverGuid") Long serverGuid, @Param("userGuid") Long userGuid);

  @Modifying
  @Transactional
  void deleteAllByServerGuidAndRoleGuid(Long serverGuid, Long roleGuid);

  @Modifying
  @Transactional
  void deleteAllByServerGuidAndRoleGuidAndUserGuid(Long serverGuid, Long roleGuid, Long userGuid);

  @Modifying
  @Transactional
  void deleteAllByServerGuid(Long serverGuid);

  /**
   * @deprecated Use insertIgnore instead
   */
  @Override
  @Deprecated(since = "1.11.0")
  default <S extends ServerUserSavedRolesEntity> S save(S entity) {
    throw new UnsupportedOperationException("Use insertIgnore instead");
  }

  /**
   * @deprecated Use insertIgnore instead
   */
  @Override
  @Deprecated(since = "1.11.0")
  default <S extends ServerUserSavedRolesEntity> List<S> saveAll(Iterable<S> entities) {
    throw new UnsupportedOperationException("Use insertIgnore instead");
  }

  /**
   * @deprecated Use insertIgnore instead
   */
  @Override
  @Deprecated(since = "1.11.0")
  default <S extends ServerUserSavedRolesEntity> S saveAndFlush(S entity) {
    throw new UnsupportedOperationException("Use insertIgnore instead");
  }
}
