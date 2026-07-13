package stablecointransaction.api;

import stablecointransaction.auth.AuthException;
import stablecointransaction.ledger.LedgerInvalidRequestException;
import stablecointransaction.merchant.MerchantAccessDeniedException;
import stablecointransaction.merchant.MerchantInactiveException;
import stablecointransaction.merchant.MerchantNotFoundException;
import stablecointransaction.payment.InvalidPaymentRequestException;
import stablecointransaction.payment.InvalidQrTokenException;
import stablecointransaction.payment.PaymentAlreadyProcessedException;
import stablecointransaction.payment.PaymentExpiredException;
import stablecointransaction.payment.PaymentNotFoundException;
import stablecointransaction.sweep.exception.AlreadyInFlightException;
import stablecointransaction.sweep.exception.BlockhashNotFoundException;
import stablecointransaction.sweep.exception.SweepNotFoundException;
import stablecointransaction.sweep.exception.SweepSendFailedException;
import stablecointransaction.sweep.exception.WalletNoUnsweptException;
import stablecointransaction.transfer.AccountFrozenException;
import stablecointransaction.transfer.DuplicateRequestMismatchException;
import stablecointransaction.transfer.InsufficientBalanceException;
import stablecointransaction.transfer.InvalidTransferRequestException;
import stablecointransaction.transfer.SelfTransferNotAllowedException;
import stablecointransaction.transfer.TransferNotFoundException;
import stablecointransaction.transfer.WalletNotFoundException;
import stablecointransaction.transfer.WalletNotUserException;
import stablecointransaction.withdrawal.exception.PolicyDeniedException;
import stablecointransaction.withdrawal.exception.WithdrawalNotFoundException;
import stablecointransaction.withdrawal.exception.WithdrawalSendFailedException;
import stablecointransaction.userauth.UserAuthException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(InvalidPaymentRequestException.class)
  public ResponseEntity<ErrorResponse> handleInvalidPaymentRequest(
      InvalidPaymentRequestException ex) {
    return ResponseEntity.status(400)
        .body(ErrorResponse.of(ApiErrorCodes.INVALID_REQUEST, ex.getMessage()));
  }

  @ExceptionHandler(InvalidQrTokenException.class)
  public ResponseEntity<ErrorResponse> handleInvalidQrToken(InvalidQrTokenException ex) {
    return ResponseEntity.status(404)
        .body(ErrorResponse.of(ApiErrorCodes.QR_TOKEN_INVALID, ex.getMessage()));
  }

  @ExceptionHandler(PaymentNotFoundException.class)
  public ResponseEntity<ErrorResponse> handlePaymentNotFound(PaymentNotFoundException ex) {
    return ResponseEntity.status(404)
        .body(ErrorResponse.of(ApiErrorCodes.PAYMENT_NOT_FOUND, ex.getMessage()));
  }

  @ExceptionHandler(PaymentExpiredException.class)
  public ResponseEntity<ErrorResponse> handlePaymentExpired(PaymentExpiredException ex) {
    return ResponseEntity.status(410)
        .body(ErrorResponse.of(ApiErrorCodes.PAYMENT_EXPIRED, ex.getMessage()));
  }

  @ExceptionHandler(PaymentAlreadyProcessedException.class)
  public ResponseEntity<ErrorResponse> handlePaymentAlreadyProcessed(
      PaymentAlreadyProcessedException ex) {
    return ResponseEntity.status(409)
        .body(ErrorResponse.of(ApiErrorCodes.PAYMENT_ALREADY_PROCESSED, ex.getMessage()));
  }

  @ExceptionHandler(MerchantNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleMerchantNotFound(MerchantNotFoundException ex) {
    return ResponseEntity.status(404)
        .body(ErrorResponse.of(ApiErrorCodes.MERCHANT_NOT_FOUND, ex.getMessage()));
  }

  @ExceptionHandler(MerchantInactiveException.class)
  public ResponseEntity<ErrorResponse> handleMerchantInactive(MerchantInactiveException ex) {
    return ResponseEntity.status(400)
        .body(ErrorResponse.of(ApiErrorCodes.MERCHANT_INACTIVE, ex.getMessage()));
  }

  @ExceptionHandler(MerchantAccessDeniedException.class)
  public ResponseEntity<ErrorResponse> handleMerchantAccessDenied(
      MerchantAccessDeniedException ex) {
    return ResponseEntity.status(403)
        .body(ErrorResponse.of(ApiErrorCodes.MERCHANT_ACCESS_DENIED, ex.getMessage()));
  }

  @ExceptionHandler(UserAuthException.class)
  public ResponseEntity<ErrorResponse> handleUserAuth(UserAuthException ex) {
    return switch (ex.getCode()) {
      case EMAIL_ALREADY_REGISTERED -> ResponseEntity.status(409)
          .body(ErrorResponse.of(ApiErrorCodes.EMAIL_ALREADY_REGISTERED, ex.getMessage()));
      case LOGIN_FAILED -> ResponseEntity.status(401)
          .body(ErrorResponse.of(ApiErrorCodes.LOGIN_FAILED, ex.getMessage()));
      case USER_SUSPENDED -> ResponseEntity.status(403)
          .body(ErrorResponse.of(ApiErrorCodes.USER_SUSPENDED, ex.getMessage()));
      case REFRESH_TOKEN_INVALID -> ResponseEntity.status(401)
          .body(ErrorResponse.of(ApiErrorCodes.REFRESH_TOKEN_INVALID, ex.getMessage()));
    };
  }

  @ExceptionHandler(AuthException.class)
  public ResponseEntity<ErrorResponse> handleAuth(AuthException ex) {
    return ResponseEntity.status(401).body(ErrorResponse.of(ex.getCode().name(), ex.getMessage()));
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> handleBadRequest(IllegalArgumentException ex) {
    return ResponseEntity.status(400).body(ErrorResponse.of(ApiErrorCodes.BAD_REQUEST, ex.getMessage()));
  }

  @ExceptionHandler(LedgerInvalidRequestException.class)
  public ResponseEntity<ErrorResponse> handleLedgerInvalidRequest(LedgerInvalidRequestException ex) {
    return ResponseEntity.status(400).body(ErrorResponse.of(ApiErrorCodes.BAD_REQUEST, ex.getMessage()));
  }

  @ExceptionHandler(InvalidTransferRequestException.class)
  public ResponseEntity<ErrorResponse> handleInvalidTransferRequest(InvalidTransferRequestException ex) {
    return ResponseEntity.status(400).body(ErrorResponse.of(ApiErrorCodes.BAD_REQUEST, ex.getMessage()));
  }

  @ExceptionHandler(InvalidAddressException.class)
  public ResponseEntity<ErrorResponse> handleInvalidAddress(InvalidAddressException ex) {
    return ResponseEntity.status(400).body(ErrorResponse.of(ApiErrorCodes.INVALID_ADDRESS, ex.getMessage()));
  }

  @ExceptionHandler(UnsupportedTokenException.class)
  public ResponseEntity<ErrorResponse> handleUnsupportedToken(UnsupportedTokenException ex) {
    return ResponseEntity.status(400).body(ErrorResponse.of(ApiErrorCodes.UNSUPPORTED_TOKEN, ex.getMessage()));
  }

  @ExceptionHandler(SendFailedException.class)
  public ResponseEntity<ErrorResponse> handleSendFailed(SendFailedException ex) {
    return ResponseEntity.status(500).body(ErrorResponse.of(ApiErrorCodes.SEND_FAILED, ex.getMessage()));
  }

  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException ex) {
    return ResponseEntity.status(404).body(ErrorResponse.of(ApiErrorCodes.NOT_FOUND, ex.getMessage()));
  }

  @ExceptionHandler(SelfTransferNotAllowedException.class)
  public ResponseEntity<ErrorResponse> handleSelfTransfer(SelfTransferNotAllowedException ex) {
    return ResponseEntity.status(400).body(ErrorResponse.of(ApiErrorCodes.SELF_TRANSFER_NOT_ALLOWED, ex.getMessage()));
  }

  @ExceptionHandler(WalletNotUserException.class)
  public ResponseEntity<ErrorResponse> handleWalletNotUser(WalletNotUserException ex) {
    return ResponseEntity.status(400).body(ErrorResponse.of(ApiErrorCodes.WALLET_NOT_USER, ex.getMessage()));
  }

  @ExceptionHandler(AccountFrozenException.class)
  public ResponseEntity<ErrorResponse> handleAccountFrozen(AccountFrozenException ex) {
    return ResponseEntity.status(400).body(ErrorResponse.of(ApiErrorCodes.ACCOUNT_FROZEN, ex.getMessage()));
  }

  @ExceptionHandler(InsufficientBalanceException.class)
  public ResponseEntity<ErrorResponse> handleInsufficientBalance(InsufficientBalanceException ex) {
    return ResponseEntity.status(400).body(ErrorResponse.of(ApiErrorCodes.INSUFFICIENT_BALANCE, ex.getMessage()));
  }

  @ExceptionHandler(WalletNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleWalletNotFound(WalletNotFoundException ex) {
    return ResponseEntity.status(404).body(ErrorResponse.of(ApiErrorCodes.WALLET_NOT_FOUND, ex.getMessage()));
  }

  @ExceptionHandler(TransferNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleTransferNotFound(TransferNotFoundException ex) {
    return ResponseEntity.status(404).body(ErrorResponse.of(ApiErrorCodes.TRANSFER_NOT_FOUND, ex.getMessage()));
  }

  @ExceptionHandler(DuplicateRequestMismatchException.class)
  public ResponseEntity<ErrorResponse> handleDuplicateMismatch(DuplicateRequestMismatchException ex) {
    return ResponseEntity.status(409).body(ErrorResponse.of(ApiErrorCodes.DUPLICATE_REQUEST_MISMATCH, ex.getMessage()));
  }

  @ExceptionHandler(WalletNoUnsweptException.class)
  public ResponseEntity<ErrorResponse> handleWalletNoUnswept(WalletNoUnsweptException ex) {
    return ResponseEntity.status(400).body(ErrorResponse.of(ApiErrorCodes.WALLET_NO_UNSWEPT, ex.getMessage()));
  }

  @ExceptionHandler(AlreadyInFlightException.class)
  public ResponseEntity<ErrorResponse> handleAlreadyInFlight(AlreadyInFlightException ex) {
    return ResponseEntity.status(409).body(ErrorResponse.of(ApiErrorCodes.ALREADY_IN_FLIGHT, ex.getMessage()));
  }

  @ExceptionHandler(SweepNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleSweepNotFound(SweepNotFoundException ex) {
    return ResponseEntity.status(404).body(ErrorResponse.of(ApiErrorCodes.SWEEP_NOT_FOUND, ex.getMessage()));
  }

  @ExceptionHandler(SweepSendFailedException.class)
  public ResponseEntity<ErrorResponse> handleSweepSendFailed(SweepSendFailedException ex) {
    return ResponseEntity.status(500).body(ErrorResponse.of(ApiErrorCodes.SEND_FAILED, ex.getMessage()));
  }

  @ExceptionHandler(BlockhashNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleBlockhashNotFound(BlockhashNotFoundException ex) {
    return ResponseEntity.status(500).body(ErrorResponse.of(ApiErrorCodes.BLOCKHASH_NOT_FOUND, ex.getMessage()));
  }

  @ExceptionHandler(stablecointransaction.withdrawal.exception.BlockhashNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleWithdrawalBlockhashNotFound(
      stablecointransaction.withdrawal.exception.BlockhashNotFoundException ex) {
    return ResponseEntity.status(500).body(ErrorResponse.of(ApiErrorCodes.BLOCKHASH_NOT_FOUND, ex.getMessage()));
  }

  @ExceptionHandler(PolicyDeniedException.class)
  public ResponseEntity<ErrorResponse> handlePolicyDenied(PolicyDeniedException ex) {
    return ResponseEntity.status(403).body(ErrorResponse.of(ApiErrorCodes.POLICY_DENIED, ex.getMessage()));
  }

  @ExceptionHandler(WithdrawalNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleWithdrawalNotFound(WithdrawalNotFoundException ex) {
    return ResponseEntity.status(404).body(ErrorResponse.of(ApiErrorCodes.WITHDRAWAL_NOT_FOUND, ex.getMessage()));
  }

  @ExceptionHandler(WithdrawalSendFailedException.class)
  public ResponseEntity<ErrorResponse> handleWithdrawalSendFailed(WithdrawalSendFailedException ex) {
    return ResponseEntity.status(500).body(ErrorResponse.of(ApiErrorCodes.SEND_FAILED, ex.getMessage()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
    String msg = ex.getBindingResult().getFieldErrors().stream()
        .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
        .findFirst().orElse("validation failed");
    return ResponseEntity.status(400).body(ErrorResponse.of(ApiErrorCodes.INVALID_REQUEST, msg));
  }
}
