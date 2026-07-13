package stablecointransaction.userauth;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.proc.SecurityContext;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

  @Bean
  SecurityFilterChain userSecurityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable())
        .sessionManagement(session -> session
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(authorize -> authorize
            .requestMatchers(UserAuthPaths.USER_AUTH_PREFIX + "/**").permitAll()
            .requestMatchers(HttpMethod.GET, UserAuthPaths.PAYMENT_QR_PREFIX + "/**").permitAll()
            .requestMatchers(UserAuthPaths.MERCHANT_PREFIX + "/**").authenticated()
            .requestMatchers(HttpMethod.POST, UserAuthPaths.PAYMENT_QR_PREFIX + "/**")
                .authenticated()
            .anyRequest().permitAll())
        .oauth2ResourceServer(resourceServer -> resourceServer.jwt(Customizer.withDefaults()));
    return http.build();
  }

  @Bean
  PasswordEncoder passwordEncoder() {
    return PasswordEncoderFactories.createDelegatingPasswordEncoder();
  }

  @Bean
  SecretKey userJwtSecretKey(UserAuthProperties properties) {
    byte[] secret = properties.getJwtSecret().getBytes(StandardCharsets.UTF_8);
    if (secret.length < 32) {
      throw new IllegalStateException("user-auth.jwt-secret must be at least 32 bytes");
    }
    return new SecretKeySpec(secret, "HmacSHA256");
  }

  @Bean
  JwtEncoder jwtEncoder(SecretKey userJwtSecretKey) {
    return new NimbusJwtEncoder(new ImmutableSecret<SecurityContext>(userJwtSecretKey));
  }

  @Bean
  JwtDecoder jwtDecoder(SecretKey userJwtSecretKey, UserAuthProperties properties) {
    NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(userJwtSecretKey)
        .macAlgorithm(MacAlgorithm.HS256)
        .build();
    decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(properties.getIssuer()));
    return decoder;
  }
}
