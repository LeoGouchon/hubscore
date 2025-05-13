package com.leogouchon.squashapp.repository;

import com.leogouchon.squashapp.model.InvitationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvitationTokenRepository extends JpaRepository<InvitationToken, Long> {
    InvitationToken findByToken(String invitationToken);
}
