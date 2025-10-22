package com.example.shopmark2.global.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.example.shopmark2.global.infrastructure.config.security.auth.CustomUserDetails;
import com.example.shopmark2.global.infrastructure.constants.Constants;
import com.example.shopmark2.user.domain.model.User;
import com.example.shopmark2.user.domain.model.UserRole;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UtilFunction {

    public static Instant toInstantFrom(LocalDate localDate) {
        if (localDate == null) {
            return null;
        }
        return localDate.atStartOfDay().toInstant(ZoneOffset.UTC);
    }

    public static Instant toInstantFrom(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return localDateTime.toInstant(ZoneOffset.UTC);
    }

    public static Object getMapAttribute(Map<String, Object> attributes, String... fields) {
        Object value = attributes;
        for (String arg : fields) {
            if (value instanceof Map) {
                value = ((Map<?, ?>) value).get(arg);
            } else {
                return null;
//                throw new RuntimeException(arg + "와 관련된 정보가 없습니다.");
            }
        }
        return value;
    }

    public static String convertImageToBase64(MultipartFile multipartFile) {
        if (multipartFile.isEmpty()) {
            return null;
        }
        if (Objects.requireNonNull(multipartFile.getOriginalFilename()).contains(" ")) {
            throw new RuntimeException("파일 이름에 공백이 포함되어 있습니다.");
        }
        if (!isValidImageExtension(multipartFile.getOriginalFilename())) {
            throw new RuntimeException("jpeg, jpg, png, gif 파일만 업로드 가능합니다.");
        }
        String imageBase64;
        try {
            imageBase64 = "data:" + multipartFile.getContentType() + ";base64,"
                    + Base64.getEncoder().encodeToString(multipartFile.getBytes());
        } catch (Exception e) {
            throw new RuntimeException("프로필 이미지 변환에 실패했습니다." + e.getMessage());
        }
        return imageBase64;
    }

    public static boolean isValidImageExtension(String fileName) {
        return Pattern.compile(Constants.Regex.VALID_IMAGE_EXTENSION).matcher(fileName).matches();
    }

    public static List<String> getUrlListFromMarkdown(String markdown) {
        Pattern pattern = Pattern.compile(Constants.Regex.MARKDOWN_IMAGE);
        Matcher matcher = pattern.matcher(markdown);
        List<String> urlList = new ArrayList<>();
        while (matcher.find()) {
            urlList.add(matcher.group(1));
        }
        return urlList;
    }

    public static WebClient getWebClientAutoRedirect(String acceptType) {
        ExchangeStrategies exchangeStrategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(-1)) // to unlimited memory size
                .build();
        ConnectionProvider provider = ConnectionProvider.builder("newConnection")
                .maxConnections(10)  // 최대 연결 수
                .pendingAcquireMaxCount(-1)  // 대기 큐 무한
                .maxIdleTime(Duration.ZERO)  // 최대 유휴 시간 0
                .maxLifeTime(Duration.ZERO)  // 최대 수명 시간 0
                .build();
        HttpClient httpClient = HttpClient.create(provider)
                .followRedirect(true);
        WebClient.Builder weblClientBuilder = WebClient.builder()
                .exchangeStrategies(exchangeStrategies)
                .clientConnector(new ReactorClientHttpConnector(httpClient));
        if ("json".equals(acceptType)) {
            weblClientBuilder.defaultHeader(HttpHeaders.ACCEPT, "application/json");
        } else {
            weblClientBuilder.defaultHeader(HttpHeaders.ACCEPT, "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7");
        }
        weblClientBuilder.defaultHeader(HttpHeaders.ACCEPT_LANGUAGE, "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7")
                .defaultHeader(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36")
                .defaultHeader("Sec-Ch-Ua", "\"Google Chrome\";v=\"123\", \"Not:A-Brand\";v=\"8\", \"Chromium\";v=\"123\"");
        return weblClientBuilder.build();
    }

    public static String generateAccessJwtBy() {

        return JWT.create()
                .withSubject(Constants.Jwt.ACCESS)
                .withIssuedAt(Instant.now())
                .withExpiresAt(Instant.now().plusMillis(Constants.Jwt.ACCESS_EXPIRATION_TIME))
                .withClaim("id", 1L)
                .withClaim("username", "temp")
                .withClaim("nickname", "temp")
                .withClaim("email", "temp@test.com")
                .withClaim("roleList", List.of("ADMIN"))
                .sign(Algorithm.HMAC512(Constants.Jwt.SECRET));

    }

    public static String generateRefreshJwtBy() {

        return JWT.create()
                .withSubject(Constants.Jwt.REFRESH)
                .withIssuedAt(Instant.now())
                .withExpiresAt(Instant.now().plusMillis(Constants.Jwt.REFRESH_EXPIRATION_TIME))
                .withClaim("id", 1L)
                .sign(Algorithm.HMAC512(Constants.Jwt.SECRET));

    }

    public static String generateAccessJwtBy(Long id, String role) {

        return JWT.create()
                .withSubject(Constants.Jwt.ACCESS)
                .withIssuedAt(Instant.now())
                .withExpiresAt(Instant.now().plusMillis(Constants.Jwt.ACCESS_EXPIRATION_TIME))
                .withClaim("id", id)
                .withClaim("username", "temp")
                .withClaim("nickname", "temp")
                .withClaim("email", "temp@test.com")
                .withClaim("roleList", List.of(role))
                .sign(Algorithm.HMAC512(Constants.Jwt.SECRET));

    }

    public static String generateRefreshJwtBy(Long id) {

        return JWT.create()
                .withSubject(Constants.Jwt.REFRESH)
                .withIssuedAt(Instant.now())
                .withExpiresAt(Instant.now().plusMillis(Constants.Jwt.REFRESH_EXPIRATION_TIME))
                .withClaim("id", id)
                .sign(Algorithm.HMAC512(Constants.Jwt.SECRET));

    }

    public static String generateAccessJwtBy(User user) {

        return JWT.create()
                .withSubject(Constants.Jwt.ACCESS)
                .withIssuedAt(Instant.now())
                .withExpiresAt(Instant.now().plusMillis(Constants.Jwt.ACCESS_EXPIRATION_TIME))
                .withClaim("id", user.getId().toString())
                .withClaim("username", user.getUsername())
                .withClaim("nickname", user.getNickname())
                .withClaim("email", user.getEmail())
                .withClaim("roleList", user.getUserRoleList()
                        .stream()
                        .map(UserRole::getRole)
                        .map(Enum::toString)
                        .toList()
                )
                .sign(Algorithm.HMAC512(Constants.Jwt.SECRET));

    }

    public static String generateRefreshJwtBy(User user) {

        return JWT.create()
                .withSubject(Constants.Jwt.REFRESH)
                .withIssuedAt(Instant.now())
                .withExpiresAt(Instant.now().plusMillis(Constants.Jwt.REFRESH_EXPIRATION_TIME))
                .withClaim("id", user.getId().toString())
                .sign(Algorithm.HMAC512(Constants.Jwt.SECRET));

    }

    public static String generateAccessJwtBy(CustomUserDetails customUserDetails) {

        return JWT.create()
                .withSubject(Constants.Jwt.ACCESS)
                .withIssuedAt(Instant.now())
                .withExpiresAt(Instant.now().plusMillis(Constants.Jwt.ACCESS_EXPIRATION_TIME))
//                .withClaim("id", customUserDetails.getMember().getId())
//                .withClaim("username", customUserDetails.getUsername())
//                .withClaim("nickname", customUserDetails.getMember().getNickname())
//                .withClaim("email", customUserDetails.getMember().getEmail())
//                .withClaim("roleList", customUserDetails.getMember().getRoleList())
                .sign(Algorithm.HMAC512(Constants.Jwt.SECRET));

    }

    public static String generateRefreshJwtBy(CustomUserDetails customUserDetails) {

        return JWT.create()
                .withSubject(Constants.Jwt.REFRESH)
                .withIssuedAt(Instant.now())
                .withExpiresAt(Instant.now().plusMillis(Constants.Jwt.REFRESH_EXPIRATION_TIME))
//                .withClaim("id", customUserDetails.getMember().getId())
                .sign(Algorithm.HMAC512(Constants.Jwt.SECRET));

    }

    public static boolean isWindows() {
        String OS = System.getProperty("os.name").toLowerCase();
        return OS.toLowerCase().contains("win");
    }

    public static boolean isMac() {
        String OS = System.getProperty("os.name").toLowerCase();
        return OS.toLowerCase().contains("mac");
    }

    public static boolean isUnix() {
        String OS = System.getProperty("os.name").toLowerCase();
        return OS.contains("nix") || OS.contains("nux") || OS.contains("aix");
    }

    public static Map<String, String> parseQueryStringOf(String url) {
        Map<String, String> queryPairs = new HashMap<>();
        try {
            URL urlObj = new URL(url);
            String query = urlObj.getQuery();
            String[] pairs = query != null ? query.split("&") : new String[0];
            for (String pair : pairs) {
                int idx = pair.indexOf("=");
                queryPairs.put(URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8), URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return queryPairs;
    }

}
