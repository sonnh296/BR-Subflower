package com.hls.sunflower.service;

import com.nimbusds.jose.JOSEException;
import com.hls.sunflower.dto.request.AuthenticationRequest;
import com.hls.sunflower.dto.request.IntrospectRequest;
import com.hls.sunflower.dto.request.LogoutRequest;
import com.hls.sunflower.dto.request.RefreshRequest;
import com.hls.sunflower.dto.response.AuthenticationResponse;
import com.hls.sunflower.dto.response.IntrospectResponse;
import com.hls.sunflower.entity.Users;

import java.text.ParseException;

public interface AuthenticationService {

    //verify token
    IntrospectResponse introspect(IntrospectRequest request) throws JOSEException, ParseException;

    //check username, password -> generate token
    AuthenticationResponse authenticate(AuthenticationRequest request);

    AuthenticationResponse outboundAuthenticate(String code);

    String generateToken(Users user);

    void logout(LogoutRequest request) throws JOSEException, ParseException;

    AuthenticationResponse refreshToken(RefreshRequest request) throws ParseException, JOSEException;
}
