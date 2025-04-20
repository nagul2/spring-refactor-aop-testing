package org.example.expert.domain.user.service;

import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.user.dto.request.UserRoleChangeRequest;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserAdminServiceTest {

    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private UserAdminService userAdminService;


    @Test
    void 어드민_유저는_다른사용자의_유저롤을_변경할_수_있다() {
        // given
        AuthUser authUser = new AuthUser(1L, "test@a.com", UserRole.ADMIN);
        User adminUser = User.fromAuthUser(authUser);   // 관리자 유저

        long targetUserId = 2;
        User targetUser = new User("user@a.com", "qwer1234!@#$", UserRole.USER);
        UserRoleChangeRequest request = new UserRoleChangeRequest("admin");

        given(userRepository.findById(targetUserId)).willReturn(Optional.of(targetUser));

        // when
        userAdminService.changeUserRole(targetUserId, request);

        // then
        verify(userRepository).findById(targetUserId);  // 호출 확인
        Assertions.assertEquals(UserRole.ADMIN, targetUser.getUserRole());  // 어드민으로 바뀌었음
    }

}