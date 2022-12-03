package com.dailycodebuffer.cloudgateway.controller;

import com.dailycodebuffer.cloudgateway.model.AuthenticationResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;


@RestController
@RequestMapping("/authenticate")
public class AuthController {

    @GetMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(
            @AuthenticationPrincipal OidcUser oidcUser,
            Model model,
            @RegisteredOAuth2AuthorizedClient("okta") OAuth2AuthorizedClient client
    ) {
        AuthenticationResponse authenticationResponse = AuthenticationResponse.builder()
                .accessToken(client.getAccessToken().getTokenValue())
                .refreshToken(client.getRefreshToken().getTokenValue())
                .expiresAt(client.getAccessToken().getExpiresAt().toEpochMilli())
                .userId(oidcUser.getEmail())
                .authorityList(oidcUser.getAuthorities()
                        .stream()
                        .map(GrantedAuthority::getAuthority)
                        .toList())
                .build();

        return new ResponseEntity<>(authenticationResponse, HttpStatus.OK);

    }

    @RequestMapping("/oauthinfo")
    @ResponseBody
    public String oauthUserInfo(@RegisteredOAuth2AuthorizedClient OAuth2AuthorizedClient authorizedClient,
                                @AuthenticationPrincipal OAuth2User oauth2User) {
        return
                "User Name: " + oauth2User.getName() + "<br/>" +
                        "User Authorities: " + oauth2User.getAuthorities() + "<br/>" +
                        "Client Name: " + authorizedClient.getClientRegistration().getClientName() + "<br/>" +
                        this.prettyPrintAttributes(oauth2User.getAttributes());
    }

    private String prettyPrintAttributes(Map<String, Object> attributes) {
        String acc = "User Attributes: <br/><div style='padding-left:20px'>";
        for (String key : attributes.keySet()){
            Object value = attributes.get(key);
            acc += "<div>"+key + ":&nbsp" + value.toString() + "</div>";
        }
        return acc + "</div>";
    }

}
