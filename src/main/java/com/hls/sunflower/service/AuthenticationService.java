package com.hls.sunflower.service;

import java.text.ParseException;

import com.hls.sunflower.dto.request.AuthenticationRequest;
import com.hls.sunflower.dto.request.IntrospectRequest;
import com.hls.sunflower.dto.request.LogoutRequest;
import com.hls.sunflower.dto.request.RefreshRequest;
import com.hls.sunflower.dto.request.UserCreationRequest;
import com.hls.sunflower.dto.response.AuthenticationResponse;
import com.hls.sunflower.dto.response.IntrospectResponse;
import com.hls.sunflower.entity.Users;
import com.nimbusds.jose.JOSEException;

public interface AuthenticationService {

    // verify token
    IntrospectResponse introspect(IntrospectRequest request) throws JOSEException, ParseException;

    // check username, password -> generate token
    AuthenticationResponse authenticate(AuthenticationRequest request);

    // Accept an optional redirectUri so the frontend can supply the exact redirect URI used during authorization
    AuthenticationResponse outboundAuthenticate(String code, String redirectUri);

    String generateToken(Users user);

    void logout(LogoutRequest request) throws JOSEException, ParseException;

    AuthenticationResponse refreshToken(RefreshRequest request) throws ParseException, JOSEException;

    AuthenticationResponse register(UserCreationRequest request);

    String verifyEmail(String token);

    void resendVerificationEmail(String email);
}
