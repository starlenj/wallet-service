package com.nasuh.walletservice.common.utils;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

public final class RequestHashUtil {
  private RequestHashUtil() {

  }

  public static String transferHash(
      Long sourceWalletId,
      Long targetWalletId,
      BigDecimal amount) {
    String canonicalRequest = sourceWalletId + ":" + targetWalletId + ":" + amount.stripTrailingZeros().toPlainString();

    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(
          canonicalRequest.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(hash);

    } catch (Exception e) {
      throw new IllegalStateException("SHA-256 is not available", e);
    }
  }
}
