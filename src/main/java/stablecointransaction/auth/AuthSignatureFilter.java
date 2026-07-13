package stablecointransaction.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import stablecointransaction.api.ErrorResponse;
import stablecointransaction.userauth.UserAuthPaths;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class AuthSignatureFilter extends OncePerRequestFilter {

  static final long ALLOWED_SKEW_MS = 5 * 60_000L;

  private final OperatorPublicKeyRepository operatorKeys;
  private final ObjectMapper objectMapper;

  public AuthSignatureFilter(OperatorPublicKeyRepository operatorKeys, ObjectMapper objectMapper) {
    this.operatorKeys = operatorKeys;
    this.objectMapper = objectMapper;
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    return UserAuthPaths.isUserOwned(request.getRequestURI());
  }

  @Override
  protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
      throws ServletException, IOException {

    if (!req.getRequestURI().startsWith("/v1/")) {
      chain.doFilter(req, res);
      return;
    }

    try {
      verifyAndContinue(req, res, chain);
    } catch (AuthException e) {
      writeAuthError(res, e);
    }
  }

  private void verifyAndContinue(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
      throws ServletException, IOException {

    CachedBodyHttpServletRequest wrapped = new CachedBodyHttpServletRequest(req);

    String operatorId = req.getHeader(AuthHeaders.OPERATOR_ID);
    String timestamp = req.getHeader(AuthHeaders.TIMESTAMP);
    String signatureHex = req.getHeader(AuthHeaders.SIGNATURE);

    if (operatorId == null || timestamp == null || signatureHex == null) {
      throw new AuthException(AuthException.Code.AUTH_MISSING, "missing x-nw-* headers");
    }

    long ts;
    try {
      ts = Long.parseLong(timestamp);
    } catch (NumberFormatException e) {
      throw new AuthException(AuthException.Code.SIGNATURE_EXPIRED, "timestamp not numeric");
    }
    if (Math.abs(System.currentTimeMillis() - ts) > ALLOWED_SKEW_MS) {
      throw new AuthException(AuthException.Code.SIGNATURE_EXPIRED, "timestamp outside ±5min");
    }

    Optional<OperatorPublicKey> opKey = operatorKeys.findById(operatorId);
    if (opKey.isEmpty()) {
      throw new AuthException(AuthException.Code.AUTH_INVALID, "unknown operator");
    }

    String pathAndQuery = req.getRequestURI() + (req.getQueryString() == null ? "" : "?" + req.getQueryString());
    byte[] body = wrapped.body();
    byte[] canonical = CanonicalMessage.bytes(operatorId, req.getMethod(), pathAndQuery, timestamp, body);

    byte[] signature;
    try {
      signature = Ed25519Verifier.decodeHex(signatureHex);
    } catch (IllegalArgumentException e) {
      throw new AuthException(AuthException.Code.SIGNATURE_INVALID, "signature not hex");
    }
    byte[] publicKey = Ed25519Verifier.decodeHex(opKey.get().getPublicKeyHex());

    if (!Ed25519Verifier.verify(publicKey, canonical, signature)) {
      throw new AuthException(AuthException.Code.SIGNATURE_INVALID, "ed25519 verify failed");
    }

    req.setAttribute(AuthHeaders.OPERATOR_ID_ATTRIBUTE, operatorId);
    chain.doFilter(wrapped, res);
  }

  private void writeAuthError(HttpServletResponse res, AuthException e) throws IOException {
    res.setStatus(401);
    res.setContentType(MediaType.APPLICATION_JSON_VALUE);
    ErrorResponse err = ErrorResponse.of(e.getCode().name(), e.getMessage());
    res.getWriter().write(objectMapper.writeValueAsString(err));
  }
}
