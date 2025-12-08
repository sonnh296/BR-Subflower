package com.hls.sunflower.controller;

import java.text.ParseException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.hls.sunflower.dto.request.AuthenticationRequest;
import com.hls.sunflower.dto.request.IntrospectRequest;
import com.hls.sunflower.dto.request.LogoutRequest;
import com.hls.sunflower.dto.request.RefreshRequest;
import com.hls.sunflower.dto.request.UserCreationRequest;
import com.hls.sunflower.dto.response.ApiResponse;
import com.hls.sunflower.dto.response.AuthenticationResponse;
import com.hls.sunflower.dto.response.IntrospectResponse;
import com.hls.sunflower.service.AuthenticationService;
import com.nimbusds.jose.JOSEException;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    @Autowired
    private AuthenticationService authenticationService;

    @PostMapping("/token")
    public ApiResponse<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
        var res = authenticationService.authenticate(request);

        ApiResponse<AuthenticationResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(res);
        return apiResponse;
    }

    @PostMapping("/outbound/authentication")
    ApiResponse<AuthenticationResponse> outboundAuthenticate(
            @RequestParam("code") String code,
            @RequestParam(value = "redirect_uri", required = false) String redirectUri) {
        var result = authenticationService.outboundAuthenticate(code, redirectUri);
        ApiResponse<AuthenticationResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(result);
        return apiResponse;
    }

    @PostMapping("/introspect")
    public ApiResponse<IntrospectResponse> authenticate(@RequestBody IntrospectRequest request)
            throws ParseException, JOSEException {
        var res = authenticationService.introspect(request);

        ApiResponse<IntrospectResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(res);
        return apiResponse;
    }

    @PostMapping("/refresh")
    public ApiResponse<AuthenticationResponse> authenticate(@RequestBody RefreshRequest request)
            throws ParseException, JOSEException {
        var res = authenticationService.refreshToken(request);

        ApiResponse<AuthenticationResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(res);
        return apiResponse;
    }

    @PostMapping("/logout")
    public ApiResponse<Void> authenticate(@RequestBody LogoutRequest request) throws ParseException, JOSEException {
        authenticationService.logout(request);

        return new ApiResponse<>();
    }

    @PostMapping("/register")
    public ApiResponse<AuthenticationResponse> register(@RequestBody UserCreationRequest request) {
        var res = authenticationService.register(request);

        ApiResponse<AuthenticationResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(res);
        return apiResponse;
    }

    @GetMapping("/verify-email")
    public ApiResponse<String> verifyEmail(@RequestParam("token") String token) {
        String message = authenticationService.verifyEmail(token);
        ApiResponse<String> apiResponse = new ApiResponse<>();
        apiResponse.setResult(message);
        return apiResponse;
    }

    @PostMapping("/resend-verification")
    public ApiResponse<String> resendVerification(@RequestParam("email") String email) {
        authenticationService.resendVerificationEmail(email);
        ApiResponse<String> apiResponse = new ApiResponse<>();
        apiResponse.setResult("Verification email sent successfully");
        return apiResponse;
    }
}
