package com.hls.sunflower.service.ServiceImpl;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.hls.sunflower.dao.InvalidatedTokenRepository;
import com.hls.sunflower.dao.RoleRepository;
import com.hls.sunflower.dao.UsersRepository;
import com.hls.sunflower.dao.httpClient.OutBoundUserClient;
import com.hls.sunflower.dao.httpClient.OutboundIdentityClient;
import com.hls.sunflower.dto.request.*;
import com.hls.sunflower.dto.response.AuthenticationResponse;
import com.hls.sunflower.dto.response.IntrospectResponse;
import com.hls.sunflower.entity.InvalidatedToken;
import com.hls.sunflower.entity.UserRole;
import com.hls.sunflower.entity.Users;
import com.hls.sunflower.exception.AppException;
import com.hls.sunflower.exception.ErrorCode;
import com.hls.sunflower.service.AuthenticationService;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {
    private final UsersRepository usersRepository;
    private final RoleRepository roleRepository;
    private final InvalidatedTokenRepository invalidatedTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final OutboundIdentityClient outboundIdentityClient;
    private final OutBoundUserClient outBoundUserClient;

    @NonFinal
    @Value("${jwt.signerKey}")
    protected String SIGN_KEY;

    @NonFinal
    @Value("${jwt.valid-duration}")
    protected long VALID_DURATION;

    @NonFinal
    @Value("${jwt.refreshable-duration}")
    protected long REFRESHABLE_DURATION;

    @NonFinal
    @Value("${outbound.identity.client-id}")
    private String CLIENT_ID;

    @NonFinal
    @Value("${outbound.identity.client-secret}")
    private String CLIENT_SECRET;

    @NonFinal
    @Value("${outbound.identity.redirect-uri}")
    private String REDIRECT_URI;

    @NonFinal
    private String GRANT_TYPE = "authorization_code";

    public AuthenticationServiceImpl(UsersRepository usersRepository, RoleRepository roleRepository, InvalidatedTokenRepository invalidatedTokenRepository, PasswordEncoder passwordEncoder, OutboundIdentityClient outboundIdentityClient, OutBoundUserClient outBoundUserClient, RestTemplate restTemplate) {
        this.usersRepository = usersRepository;
        this.roleRepository = roleRepository;
        this.invalidatedTokenRepository = invalidatedTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.outboundIdentityClient = outboundIdentityClient;
        this.outBoundUserClient = outBoundUserClient;
    }

    @Override
    public IntrospectResponse introspect(IntrospectRequest request) throws JOSEException, ParseException {
        var token = request.getToken();
        boolean isValid = true;

        try {
            verifyToken(token, false);
        } catch (AppException e) {
            isValid = false;
        }

        return IntrospectResponse.builder()
                .valid(isValid)
                .build();
    }

    @Override
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        var user = usersRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        boolean authenticated =  passwordEncoder.matches(request.getPassword(), user.getPassword());

        if(!authenticated || user.getOAuth2())
            throw new AppException(ErrorCode.UNAUTHENTICATED);

        // login success -> create token
        var token = generateToken(user);

        return AuthenticationResponse.builder()
                .token(token)
                .authenticated(true)
                .build();

    }

    @Override
    public AuthenticationResponse outboundAuthenticate(String code) {
        var response = outboundIdentityClient.exchangeToken(ExchangeTokenRequest.builder()
                .code(code)
                .clientId(CLIENT_ID)
                .clientSecret(CLIENT_SECRET)
                .redirectUri(REDIRECT_URI)
                .grantType(GRANT_TYPE)
                .build());

        log.info("TOKEN RESPONSE {}", response);

        var userInfo = outBoundUserClient.getUserInfo("json", response.getAccessToken());
        log.info("USER INFO {}", userInfo);

        var user = usersRepository.findByUsername(userInfo.getEmail()).orElseGet(
                () -> {
                    Users newUser = Users.builder()
                            .username(userInfo.getEmail())
                            .avatarUrl(userInfo.getPicture())
                            .email(userInfo.getEmail())
                            .fullName(userInfo.getName())
                            .oAuth2(true)
                            .build();
                    //add role User
                    Set<UserRole> userRoles = new HashSet<>();
                    UserRole userRole = new UserRole();
                    userRole.setRole(roleRepository.findByRoleName("USER"));
                    userRole.setUser(newUser);
                    userRoles.add(userRole);
                    newUser.setUser_roles(userRoles);
                    return usersRepository.save(newUser);
                }
        );

        var token = generateToken(user);

        return AuthenticationResponse.builder()
                .token(token)
                .authenticated(true)
                .build();

    }


    @Override
    public String generateToken(Users user){
        // header : xac dinh thuat toan ma hoa cho jwt
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        //tao body cho payload JWTClaimsSet
        JWTClaimsSet jwtClaimsSet =  new JWTClaimsSet.Builder()
                .subject(user.getUsername())
                .issuer("oppo.com")
                .issueTime(new Date())
                .expirationTime(new Date(
                        Instant.now().plus(VALID_DURATION, ChronoUnit.SECONDS).toEpochMilli() // het han sau VALID_DURATION giây
                ))
                .jwtID(UUID.randomUUID().toString())  //token ID
//                .claim("customClaim", "custom") //custom claim
                .claim("scope", buildScope(user)) // để spring security biết role thì cần claim có scope trong jwt
                .claim("name", user.getFullName())
                .claim("avatarUrl", user.getAvatarUrl())
                .build();

        //tao payload
        Payload payload = new Payload(jwtClaimsSet.toJSONObject());

        JWSObject jwsObject = new JWSObject(header,payload);

        //signature
        try {
            jwsObject.sign(new MACSigner(SIGN_KEY.getBytes()));
            return jwsObject.serialize();
        }catch (JOSEException e){
            log.error("Cannot create token",e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void logout(LogoutRequest request) throws JOSEException, ParseException {
        try {
            var signToken = verifyToken(request.getToken(), true);

            String jit = signToken.getJWTClaimsSet().getJWTID();
            Date expiryTime = signToken.getJWTClaimsSet().getExpirationTime();

            InvalidatedToken invalidatedToken =
                    InvalidatedToken.builder().id(jit).expiryTime(expiryTime).build();

            invalidatedTokenRepository.save(invalidatedToken);
        } catch (AppException exception){
            log.info("Token already expired");
        }
    }

    public AuthenticationResponse refreshToken(RefreshRequest request) throws ParseException, JOSEException {
        var signedJWT = verifyToken(request.getToken(), true);

        var jit = signedJWT.getJWTClaimsSet().getJWTID();
        var expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        InvalidatedToken invalidatedToken =
                InvalidatedToken.builder()
                        .id(jit)
                        .expiryTime(expiryTime).build();

        invalidatedTokenRepository.save(invalidatedToken);

        var username = signedJWT.getJWTClaimsSet().getSubject();

        var user =
                usersRepository.findByUsername(username).orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));

        var token = generateToken(user);

        return AuthenticationResponse.builder().token(token).authenticated(true).build();
    }

    private SignedJWT verifyToken(String token, boolean isRefresh) throws JOSEException, ParseException {

        JWSVerifier verifier = new MACVerifier(SIGN_KEY.getBytes());

        SignedJWT signedJWT = SignedJWT.parse(token);

        var verified = signedJWT.verify(verifier);

        //check thoi han cua token
        Date expiryTime = (isRefresh)
                ? new Date(signedJWT.getJWTClaimsSet().getIssueTime()
                .toInstant().plus(REFRESHABLE_DURATION, ChronoUnit.SECONDS).toEpochMilli())
                : signedJWT.getJWTClaimsSet().getExpirationTime();

        if (!(verified && expiryTime.after(new Date()))) throw new AppException(ErrorCode.UNAUTHENTICATED);

        if (invalidatedTokenRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID()))
            throw new AppException(ErrorCode.UNAUTHENTICATED);

        return signedJWT;
    }

    private String buildScope(Users user) {
        StringJoiner stringJoiner = new StringJoiner(" ");

        if (!CollectionUtils.isEmpty(user.getUser_roles()))
            user.getUser_roles().forEach(role -> {
                stringJoiner.add("ROLE_" + role.getRole().getRoleName());
            });

        return stringJoiner.toString();
    }
}
