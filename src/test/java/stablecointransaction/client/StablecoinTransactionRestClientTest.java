package stablecointransaction.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

class StablecoinTransactionRestClientTest {
  private HttpServer server;

  @BeforeEach
  void setUp() throws Exception {
    server = HttpServer.create(new InetSocketAddress(0), 0);
    server.start();
  }

  @AfterEach
  void tearDown() {
    server.stop(0);
  }

  @Test
  void createsTransferWithSignedRestContract() throws Exception {
    UUID src = UUID.randomUUID();
    UUID dst = UUID.randomUUID();
    UUID transfer = UUID.randomUUID();
    String[] observed = new String[5];
    server.createContext("/v1/transfers", exchange -> {
      observed[0] = exchange.getRequestMethod();
      observed[1] = exchange.getRequestHeaders().getFirst("x-nw-operator-id");
      observed[2] = exchange.getRequestHeaders().getFirst("x-nw-timestamp");
      observed[3] = exchange.getRequestHeaders().getFirst("x-nw-signature");
      observed[4] = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
      byte[] response = ("{\"transfer_id\":\"" + transfer
          + "\",\"src_wallet_id\":\"" + src + "\",\"dst_wallet_id\":\"" + dst
          + "\",\"token\":\"USDC\",\"amount\":12,\"reference_id\":\"ref\",\"status\":\"PENDING\"}")
          .getBytes(StandardCharsets.UTF_8);
      exchange.getResponseHeaders().set("Content-Type", "application/json");
      exchange.sendResponseHeaders(200, response.length);
      exchange.getResponseBody().write(response);
      exchange.close();
    });

    StablecoinTransactionClientProperties properties = new StablecoinTransactionClientProperties();
    properties.setBaseUrl("http://localhost:" + server.getAddress().getPort());
    StablecoinTransactionClient client = new StablecoinTransactionRestClient(
        RestClient.builder(), new ObjectMapper(), properties,
        (method, path, body, timestamp) -> new StablecoinTransactionRequestSigner.SignedHeaders(
            "operator-1", "abcd", "timestamp-1"));

    StablecoinTransactionClient.RemoteTransfer result = client.createTransfer(
        src, dst, "USDC", java.math.BigInteger.valueOf(12), "ref", null);
    assertNotNull(result);
    assertEquals("POST", observed[0]);
    assertEquals("operator-1", observed[1]);
    assertEquals("timestamp-1", observed[2]);
    assertEquals("abcd", observed[3]);
    assertEquals(true, observed[4].contains(src.toString()));
    assertEquals(true, observed[4].contains(dst.toString()));
    assertEquals(transfer, result.transferId());
    assertEquals("USDC", result.token());
  }
}
