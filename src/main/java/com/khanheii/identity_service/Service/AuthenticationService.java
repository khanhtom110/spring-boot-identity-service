package com.khanheii.identity_service.Service;

import com.khanheii.identity_service.dto.request.AuthenticationRequest;
import com.khanheii.identity_service.dto.request.IntrospectRequest;
import com.khanheii.identity_service.dto.request.LogoutRequest;
import com.khanheii.identity_service.dto.request.RefreshRequest;
import com.khanheii.identity_service.dto.response.AuthenticationResponse;
import com.khanheii.identity_service.dto.response.IntrospectResponse;
import com.khanheii.identity_service.entity.InvalidatedToken;
import com.khanheii.identity_service.entity.User;
import com.khanheii.identity_service.exception.AppException;
import com.khanheii.identity_service.exception.ErrorCode;
import com.khanheii.identity_service.repository.InvalidatedTokenRepository;
import com.khanheii.identity_service.repository.UserRepository;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.StringJoiner;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationService {
    UserRepository userRepository;
    InvalidatedTokenRepository invalidatedTokenRepository;

    @NonFinal
    @Value("${jwt.signerKey}")
    String SIGNER_KEY;

    @NonFinal
    @Value("${jwt.valid-duration}")
    protected long VALID_DURATION;

    @NonFinal
    @Value("${jwt.refreshable-duration}")
    protected long REFRESHABLE_DURATION;

    //Api verify token
    public IntrospectResponse introspect(IntrospectRequest request) throws JOSEException, ParseException { //2 Exception nay de kiem tra token co hop le khong hay bi loi
        String token = request.getToken(); //get set mot bien co ten la token nhu bthg chu khong phai method getToken nhu nghia den

        boolean isValid = true;
        try {
            verifyToken(token,false);
        } catch (AppException e) {
            isValid=false;
        }
        return IntrospectResponse.builder()
                .valid(isValid)
                .build();
    }

    //Api login
    public AuthenticationResponse authenticate(AuthenticationRequest request){
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(()-> new AppException(ErrorCode.USER_NONEXISTED));

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);

        boolean authenticated = passwordEncoder.matches(request.getPassword(), user.getPassword());
        if(!authenticated)
            throw new AppException(ErrorCode.UNAUTHENTICATED);

        String token = generateToken(user);

        return AuthenticationResponse.builder()
                .token(token)
                .authenticated(true)
                .build();
    }
    public void logout(LogoutRequest request) throws ParseException, JOSEException {
        //Co try catch de phong verify token fail thi bi loi 401-UNAUTHENTICATED
        try{
            var signToken = verifyToken(request.getToken(),true);

            String jwi = signToken.getJWTClaimsSet().getJWTID(); //Can cu vao id cua tung token chu khong de lo token vi van de bao mat
            Date expiryTime = signToken.getJWTClaimsSet().getExpirationTime();

            InvalidatedToken invalidatedToken = InvalidatedToken.builder()
                    .id((jwi))
                    .expiryTime(expiryTime)
                    .build();

            invalidatedTokenRepository.save(invalidatedToken);
        } catch (AppException exception) {
            log.info("Token has been expired");
        }
    }

    public AuthenticationResponse refreshToken(RefreshRequest request) throws ParseException, JOSEException {
        //Kiem tra hieu luc token
        var signJWT = verifyToken(request.getToken(),true);
        //Them token cu vao InvalidatedToken
        String jwi = signJWT.getJWTClaimsSet().getJWTID();
        Date expiryTime = signJWT.getJWTClaimsSet().getExpirationTime();

        InvalidatedToken invalidatedToken = InvalidatedToken.builder()
                .expiryTime(expiryTime)
                .id(jwi)
                .build();
        invalidatedTokenRepository.save(invalidatedToken);
        //generate token moi
        String username = signJWT.getJWTClaimsSet().getSubject();
        User user = userRepository.findByUsername(username).orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));
        var token = generateToken(user);
        return AuthenticationResponse.builder()
                .token(token)
                .authenticated(true)
                .build();

    }

    //Them para boolean de phan biet verify giua authenticate va generate Token Refresh do 2 cai nay can chu y den VALID_DURATION va REFRESHABLE_DURATION
    private SignedJWT verifyToken(String token, boolean isRefresh) throws JOSEException, ParseException { //Qua dang cap:))))))))))
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes()); //MACVerifier duoc nimbus ho tro de kiem tra chu ky

        SignedJWT signedJWT = SignedJWT.parse(token); //Phan tich token vao signedJWT

        Date expiryTime = (isRefresh) ? new Date(signedJWT.getJWTClaimsSet().getExpirationTime().toInstant().plus(REFRESHABLE_DURATION, ChronoUnit.SECONDS).toEpochMilli())
                : signedJWT.getJWTClaimsSet().getExpirationTime(); //Ap dung toan tu 3 ngoi thay if else cho tien:))

        var verified = signedJWT.verify(verifier); //Token bi chinh sua thi se tra ve false

        if(!(verified && expiryTime.after(new Date()))) //Neu chu ky khong xac thuc va het han su dung thi nem ra loi khong xac thuc
            throw new AppException(ErrorCode.UNAUTHENTICATED);

        if(invalidatedTokenRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID())) //Kiem tra id cua token co o trong danh sach logout chua??
            throw new AppException(ErrorCode.UNAUTHENTICATED);

        return signedJWT;
    }
    private String generateToken(User user){
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getUsername())
                .issuer("khanheii.com")
                .issueTime(new Date())
                .expirationTime(new Date(
                        Instant.now().plus(VALID_DURATION, ChronoUnit.SECONDS).toEpochMilli()
                ))
                .jwtID(UUID.randomUUID().toString())//UUID là chuỗi 32 ký tự random ngẫu nhiên, khoong trungf
                .claim("scope",buildScope(user))
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject()); //Chuyển đổi đối tượng JWTClaimsSet sang định dạng JSON Object tiêu chuẩn.

        //JWSObject dai dien token truoc khi ky
        JWSObject jwsObject = new JWSObject(header,payload); //Ghep header va payload

        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize(); //Tuan tu hoa thanh dung dinh dang cua token
        } catch (JOSEException e) {
            log.error("Cannot create token", e);
            throw new RuntimeException(e);
        }

    }

    private String buildScope(User user){
        StringJoiner stringJoiner = new StringJoiner(" "); //stringJoiner de luu tru tam thoi
        if(!CollectionUtils.isEmpty(user.getRoles())) //Neu role cua user khong rong
            user.getRoles().forEach(role -> {
                stringJoiner.add("ROLE_"+role.getName());
                if(!CollectionUtils.isEmpty(role.getPermissions()))
                    role.getPermissions().forEach(permission -> stringJoiner.add(permission.getName()));
            });
        return stringJoiner.toString();
    }
}
