package stablecointransaction.api;

import stablecointransaction.exception.ApplicationException;
import stablecointransaction.external.exception.StablecoinTransactionRemoteException;
import stablecointransaction.merchant.exception.*;
import stablecointransaction.payment.exception.*;
import stablecointransaction.user.exception.*;
import stablecointransaction.userauth.exception.UserAuthException;
import stablecointransaction.payment.exception.*;
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

  @ExceptionHandler({CustomerNotFoundException.class, CustomerWalletNotFoundException.class})
  public ResponseEntity<ErrorResponse> customerNotFound(ApplicationException ex) {
    return response(404, ApiErrorCodes.NOT_FOUND, ex.getMessage());
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

  @ExceptionHandler(ApplicationException.class)
  public ResponseEntity<ErrorResponse> applicationError(ApplicationException ex) {
    return response(500, ApiErrorCodes.INTERNAL_ERROR, ex.getMessage());
  }

  @ExceptionHandler(StablecoinTransactionRemoteException.class)
  public ResponseEntity<ErrorResponse> transactionRemote(StablecoinTransactionRemoteException ex) {
    String code = ex.getRemoteCode();
    if (code != null && isKnownRemoteCode(code)) {
      return response(ex.getStatus(), code, ex.getMessage());
    }
    int status = ex.getStatus() >= 400 && ex.getStatus() < 500 ? ex.getStatus() : 502;
    return response(status, ApiErrorCodes.STABLECOIN_TRANSACTION_FAILED, ex.getMessage());
  }

  private boolean isKnownRemoteCode(String code) {
    return switch (code) {
      case ApiErrorCodes.ACCOUNT_FROZEN, ApiErrorCodes.INSUFFICIENT_BALANCE,
          ApiErrorCodes.INVALID_ADDRESS, ApiErrorCodes.POLICY_DENIED,
          ApiErrorCodes.SELF_TRANSFER_NOT_ALLOWED, ApiErrorCodes.UNSUPPORTED_TOKEN,
          ApiErrorCodes.TRANSFER_NOT_FOUND, ApiErrorCodes.WALLET_NOT_FOUND,
          ApiErrorCodes.WALLET_NOT_USER, ApiErrorCodes.BAD_REQUEST -> true;
      default -> false;
    };
  }

  private ResponseEntity<ErrorResponse> response(int status, String code, String message) {
    return ResponseEntity.status(status).body(ErrorResponse.of(code, message));
  }
}
