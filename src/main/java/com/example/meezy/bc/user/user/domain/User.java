package com.example.meezy.bc.user.user.domain;

import com.example.meezy.bc.sharedkernel.domain.AbstractAggregateRoot;
import com.example.meezy.bc.user.user.domain.type.OauthProvider;
import com.example.meezy.bc.user.user.domain.vo.UserId;
import jakarta.persistence.*;
import lombok.*;


@Getter
@Entity
@Table(
        name = "tbl_user",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_email_provider",
                        columnNames = {"email", "oauth_provider"}
                )
        }
)
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends AbstractAggregateRoot {

    @EmbeddedId
    private UserId userId;

    @Column(nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OauthProvider oauthProvider;

    @Column(unique = true)
    private String accountId;

    private String name;

    private String password;

    private String profileImageUrl;

    //에미일 인증 필요
    public static User createLocal(String email, String accountId, String name, String password){
        return User.builder()
                .userId(UserId.newId())
                .email(email)
                .oauthProvider(OauthProvider.LOCAL)
                .accountId(accountId)
                .name(name)
                .password(password)
                .build();
    }

    //해당 단계에서는 이메일 검증을 건너뜀
    public static User createOauth(String email, OauthProvider oauthProvider){
        return User.builder()
                .userId(UserId.newId())
                .email(email)
                .oauthProvider(oauthProvider)
                .build();
    }

    //Oauth 유저일 때만 사용
    public void completeProfile(String accountId, String name, String password){
        this.accountId = accountId;
        this.name = name;
        this.password = password;
    }

    public boolean isProfileCompleted(){
        return this.accountId != null && this.name != null && this.password != null;
    }

    public void uploadProfileImage(String profileImageUrl){
        this.profileImageUrl = profileImageUrl;
    }

    public void changePassword(String newPassword){
        this.password = newPassword;
    }
}
