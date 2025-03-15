package com.hls.sunflower.controller;

import com.nimbusds.jose.JOSEException;
import com.hls.sunflower.dto.request.AuthenticationRequest;
import com.hls.sunflower.dto.request.IntrospectRequest;
import com.hls.sunflower.dto.request.LogoutRequest;
import com.hls.sunflower.dto.request.RefreshRequest;
import com.hls.sunflower.dto.response.ApiResponse;
import com.hls.sunflower.dto.response.AuthenticationResponse;
import com.hls.sunflower.dto.response.IntrospectResponse;
import com.hls.sunflower.service.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    @Autowired
    private AuthenticationService authenticationService;

    @PostMapping("/token")
    public ApiResponse<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request){
        var res = authenticationService.authenticate(request);

        return ApiResponse.<AuthenticationResponse>builder()
                .result(res)
                .build();
    }

    @PostMapping("/outbound/authentication")
    ApiResponse<AuthenticationResponse> outboundAuthenticate(
            @RequestParam("code") String code
    ){
        var result = authenticationService.outboundAuthenticate(code);
        return ApiResponse.<AuthenticationResponse>builder().result(result).build();
    }

    @PostMapping("/introspect")
    public ApiResponse<IntrospectResponse> authenticate(@RequestBody IntrospectRequest request)
            throws ParseException, JOSEException {
        var res = authenticationService.introspect(request);

        return ApiResponse.<IntrospectResponse>builder()
                .result(res)
                .build();
    }

    @PostMapping("/refresh")
    public ApiResponse<AuthenticationResponse> authenticate(@RequestBody RefreshRequest request)
            throws ParseException, JOSEException {
        var res = authenticationService.refreshToken(request);

        return ApiResponse.<AuthenticationResponse>builder()
                .result(res)
                .build();
    }

    @PostMapping("/logout")
    public ApiResponse<Void> authenticate(@RequestBody LogoutRequest request)
            throws ParseException, JOSEException {
        authenticationService.logout(request);

        return ApiResponse.<Void>builder()
                .build();
    }
}
