package com.accesscontroll.main.repositories;

import com.accesscontroll.main.entities.ProtectedResource;
import com.accesscontroll.main.entities.UserResourceAccess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserResourceAccessRepository extends JpaRepository<UserResourceAccess, Long> {
    List<UserResourceAccess> findByUsername(String username);
    @Query("SELECT a.resource FROM UserResourceAccess a WHERE a.username = :username")
    List<ProtectedResource> findResourcesByUsername(@Param("username") String username);

}
