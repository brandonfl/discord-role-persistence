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

package com.brandonfl.discordrolepersistence.db.repository.role;

import com.brandonfl.discordrolepersistence.db.entity.role.ServerRoleBlacklistEntity;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ServerRoleBlacklistRepository extends JpaRepository<ServerRoleBlacklistEntity, Long> {
  Optional<ServerRoleBlacklistEntity> findByServerGuidAndRoleGuid(Long serverGuid, Long roleGuid);

  @Modifying
  @Transactional
  void deleteAllByServerGuidAndRoleGuid(Long serverGuid, Long roleGuid);

  @Query("""
    SELECT serverRoleBlacklistEntity.roleGuid
    FROM ServerRoleBlacklistEntity serverRoleBlacklistEntity
    WHERE serverRoleBlacklistEntity.serverGuid = :serverGuid
  """)
  List<Long> getBlacklistedRolesByServerGuid(@Param("serverGuid") Long serverGuid);

  @Modifying
  @Transactional
  void deleteAllByServerGuid(Long serverGuid);
}
