package stablecointransaction.api;

import stablecointransaction.merchant.MerchantAccessDeniedException;
import stablecointransaction.merchant.MerchantAlreadyExistsException;
import stablecointransaction.merchant.MerchantInactiveException;
import stablecointransaction.merchant.MerchantNotFoundException;
import stablecointransaction.payment.InvalidPaymentRequestException;
import stablecointransaction.payment.InvalidQrTokenException;
import stablecointransaction.payment.PaymentAlreadyProcessedException;
import stablecointransaction.payment.PaymentExpiredException;
import stablecointransaction.payment.PaymentNotFoundException;
import stablecointransaction.payment.PaymentRequestMismatchException;
import stablecointransaction.userauth.UserAuthException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(InvalidPaymentRequestException.class)
  public ResponseEntity<ErrorResponse> invalidPayment(InvalidPaymentRequestException ex) {
    return response(400, ApiErrorCodes.INVALID_REQUEST, ex.getMessage());
  }

  @ExceptionHandler(InvalidQrTokenException.class)
  public ResponseEntity<ErrorResponse> invalidQr(InvalidQrTokenException ex) {
    return response(404, ApiErrorCodes.QR_TOKEN_INVALID, ex.getMessage());
  }

  @ExceptionHandler(PaymentNotFoundException.class)
  public ResponseEntity<ErrorResponse> paymentNotFound(PaymentNotFoundException ex) {
    return response(404, ApiErrorCodes.PAYMENT_NOT_FOUND, ex.getMessage());
  }

  @ExceptionHandler(PaymentExpiredException.class)
  public ResponseEntity<ErrorResponse> paymentExpired(PaymentExpiredException ex) {
    return response(410, ApiErrorCodes.PAYMENT_EXPIRED, ex.getMessage());
  }

  @ExceptionHandler({PaymentAlreadyProcessedException.class, PaymentRequestMismatchException.class})
  public ResponseEntity<ErrorResponse> paymentConflict(RuntimeException ex) {
    return response(409, ApiErrorCodes.PAYMENT_ALREADY_PROCESSED, ex.getMessage());
  }

  @ExceptionHandler(MerchantNotFoundException.class)
  public ResponseEntity<ErrorResponse> merchantNotFound(MerchantNotFoundException ex) {
    return response(404, ApiErrorCodes.MERCHANT_NOT_FOUND, ex.getMessage());
  }

  @ExceptionHandler(MerchantAlreadyExistsException.class)
  public ResponseEntity<ErrorResponse> merchantAlreadyExists(MerchantAlreadyExistsException ex) {
    return response(409, ApiErrorCodes.MERCHANT_ALREADY_EXISTS, ex.getMessage());
  }

  @ExceptionHandler(MerchantInactiveException.class)
  public ResponseEntity<ErrorResponse> merchantInactive(MerchantInactiveException ex) {
    return response(400, ApiErrorCodes.MERCHANT_INACTIVE, ex.getMessage());
  }

  @ExceptionHandler(MerchantAccessDeniedException.class)
  public ResponseEntity<ErrorResponse> merchantDenied(MerchantAccessDeniedException ex) {
    return response(403, ApiErrorCodes.MERCHANT_ACCESS_DENIED, ex.getMessage());
  }

  @ExceptionHandler(UserAuthException.class)
  public ResponseEntity<ErrorResponse> userAuth(UserAuthException ex) {
    return switch (ex.getCode()) {
      case EMAIL_ALREADY_REGISTERED -> response(409, ApiErrorCodes.EMAIL_ALREADY_REGISTERED, ex.getMessage());
      case LOGIN_FAILED -> response(401, ApiErrorCodes.LOGIN_FAILED, ex.getMessage());
      case USER_SUSPENDED -> response(403, ApiErrorCodes.USER_SUSPENDED, ex.getMessage());
      case REFRESH_TOKEN_INVALID -> response(401, ApiErrorCodes.REFRESH_TOKEN_INVALID, ex.getMessage());
    };
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> validation(MethodArgumentNotValidException ex) {
    String message = ex.getBindingResult().getFieldErrors().stream()
        .map(error -> error.getField() + ": " + error.getDefaultMessage())
        .findFirst().orElse("validation failed");
    return response(400, ApiErrorCodes.INVALID_REQUEST, message);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> badRequest(IllegalArgumentException ex) {
    return response(400, ApiErrorCodes.BAD_REQUEST, ex.getMessage());
  }

  private ResponseEntity<ErrorResponse> response(int status, String code, String message) {
    return ResponseEntity.status(status).body(ErrorResponse.of(code, message));
  }
}
