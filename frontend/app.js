const $ = (id) => document.getElementById(id);
const state = { token: '', merchantId: '', paymentId: '', qrToken: '', qrExpiresAt: null, qrTimer: null, wallet: null, historyCursor: null };
const api = () => $('baseUrl').value.replace(/\/$/, '');
const headers = () => ({'Content-Type':'application/json', ...(state.token ? {Authorization:`Bearer ${state.token}`} : {})});

function output(id, value) { $(id).textContent = JSON.stringify(value, null, 2); }
const sleep = (ms) => new Promise(resolve => setTimeout(resolve, ms));
async function waitForPayment(paymentId) {
  for (let attempt = 0; attempt < 15; attempt++) {
    await sleep(1000);
    const result = await request('GET', `/v1/payments/${paymentId}`);
    output('payment', result.data);
    if (result.data.status === 'PAID') return result.data;
    if (result.data.status === 'FAILED' || result.data.status === 'CANCELED') {
      throw new Error(`결제 처리 실패: ${result.data.status}`);
    }
  }
  throw new Error('결제 처리 시간이 초과되었습니다');
}
function log(label, value) { $('log').textContent += `[${new Date().toLocaleTimeString()}] ${label}\n${JSON.stringify(value, null, 2)}\n\n`; }
async function request(method, path, body, options = {}) {
  const response = await fetch(api() + path, {method, headers: headers(), body: body == null ? undefined : JSON.stringify(body)});
  const text = await response.text(); let data;
  try { data = text ? JSON.parse(text) : null; } catch { data = text; }
  log(`${method} ${path} -> ${response.status}`, data);
  if (data?.qr_payload) state.qrExpiresAt = data.expires_at || null;
  if (!response.ok && !options.allowError) throw new Error(`${response.status} ${JSON.stringify(data)}`);
  return {ok: response.ok, status: response.status, data};
}
function requireLogin() { if (!state.token) throw new Error('먼저 로그인하세요'); }
function showError(error) { $('status').textContent = error.message; }

$('signup').onclick = async () => { try {
  const result = await request('POST', '/v1/user-auth/signup', {email:$('email').value, password:$('password').value, display_name:$('email').value});
  output('session', result.data); $('status').textContent = '회원가입 완료';
} catch (e) { showError(e); } };
$('login').onclick = async () => { try {
  const result = await request('POST', '/v1/user-auth/login', {email:$('email').value, password:$('password').value});
  state.token = result.data.access_token; output('session', {user: result.data.user_id, authenticated:true}); $('status').textContent = '로그인 완료';
} catch (e) { showError(e); } };

$('createMerchant').onclick = async () => { try {
  requireLogin();
  const businessNumber = $('businessNumber').value || `demo-${Date.now()}`;
  const result = await request('POST', '/v1/merchants', {
    merchant_name: $('merchantName').value,
    business_number: businessNumber
  });
  state.merchantId = result.data.merchant_id;
  $('merchantId').value = state.merchantId;
  output('payment', result.data);
  $('status').textContent = '가맹점 생성 완료';
} catch (e) { showError(e); } };

$('createPayment').onclick = async () => { try {
  requireLogin(); if (!$('merchantId').value) throw new Error('merchant id가 필요합니다');
  state.merchantId = $('merchantId').value;
  const result = await request('POST', `/v1/merchants/${state.merchantId}/payments`, {order_id:$('orderId').value, token:'USDC-test', amount:Number($('amount').value), description:$('description').value});
  state.paymentId = result.data.payment_id; state.qrToken = (result.data.qr_payload || '').split('/').pop(); output('payment', result.data); renderQr(result.data.qr_payload); $('status').textContent = '결제와 QR 생성 완료';
} catch (e) { showError(e); } };
$('lookupQr').onclick = async () => { try { requireLogin(); const result = await request('GET', `/v1/payment-qr/${state.qrToken}`); output('payment', result.data); } catch (e) { showError(e); } };
$('confirmQr').onclick = async () => { try { requireLogin(); const result = await request('POST', `/v1/payment-qr/${state.qrToken}/confirm`); output('payment', result.data); $('status').textContent = '결제 승인 완료'; } catch (e) { showError(e); } };
$('cancelPayment').onclick = async () => { try { requireLogin(); const result = await request('POST', `/v1/merchants/${state.merchantId}/payments/${state.paymentId}/cancel`, {}); output('payment', result.data); } catch (e) { showError(e); } };
$('confirmQr').onclick = async () => { try {
  requireLogin();
  const result = await request('POST', `/v1/payment-qr/${state.qrToken}/confirm`);
  output('payment', result.data);
  $('status').textContent = '결제 승인 처리 중';
  await waitForPayment(result.data.payment_id);
  $('status').textContent = '결제 승인 완료';
} catch (e) { showError(e); } };

$('myPayments').onclick = async () => { try { requireLogin(); state.historyCursor = null; await loadPaymentHistory(); } catch (e) { showError(e); } };
$('nextPayments').onclick = async () => { try { requireLogin(); if (!state.historyCursor) throw new Error('다음 페이지가 없습니다'); await loadPaymentHistory(); } catch (e) { showError(e); } };
$('paymentDetail').onclick = async () => { try { requireLogin(); const result = await request('GET', `/v1/payments/${$('paymentId').value || state.paymentId}`); output('history', result.data); } catch (e) { showError(e); } };
$('wallet').onclick = async () => { try { requireLogin(); const result = await request('GET', '/v1/me/wallet', null, {allowError:true}); state.wallet = result.data; output('walletResult', result.data); } catch (e) { showError(e); } };
$('copyAddress').onclick = async () => { try { const address = state.wallet?.deposit_address; if (!address) throw new Error('먼저 지갑을 조회하세요'); await navigator.clipboard.writeText(address); $('status').textContent = '지갑 주소 복사 완료'; } catch (e) { showError(e); } };

$('sendTransfer').onclick = async () => { try { requireLogin(); const key = crypto.randomUUID(); const result = await request('POST', '/v1/transfers', {destination_wallet_id:$('destinationWalletId').value, token:$('transferToken').value, amount:Number($('transferAmount').value), memo:$('memo').value, idempotency_key:key}); state.transferId = result.data.transfer_id; output('transferResult', result.data); } catch (e) { showError(e); } };
$('transferDetail').onclick = async () => { try { requireLogin(); const id = $('transferId').value || state.transferId; const result = await request('GET', `/v1/transfers/${id}`); output('transferResult', result.data); } catch (e) { showError(e); } };

function extractQrToken(payload) {
  if (!payload) return '';
  try {
    const path = new URL(payload, api()).pathname;
    return decodeURIComponent(path.split('/').filter(Boolean).pop() || '');
  } catch {
    return payload.split('/').filter(Boolean).pop() || '';
  }
}

function clearQr() {
  if (state.qrTimer) clearInterval(state.qrTimer);
  state.qrTimer = null;
  $('qrCanvas').getContext('2d').clearRect(0, 0, 220, 220);
  $('qrStatus').textContent = 'QR 없음';
}

function renderQr(payload, expiresAt = state.qrExpiresAt) {
  clearQr();
  state.qrToken = extractQrToken(payload);
  state.qrExpiresAt = expiresAt || null;
  if (!payload) return;
  if (!window.QRCode) {
    $('qrStatus').textContent = 'QR 라이브러리를 불러오지 못했습니다.';
    return;
  }

  const qrUrl = new URL(payload, api()).href;
  QRCode.toCanvas($('qrCanvas'), qrUrl, {width:220, margin:2}, (error) => {
    if (error) {
      $('qrStatus').textContent = 'QR 생성 실패';
      showError(error);
      return;
    }
    $('qrStatus').textContent = `QR 생성 완료 · ${qrUrl}`;
  });

  if (expiresAt) {
    const updateExpiry = () => {
      const remaining = new Date(expiresAt).getTime() - Date.now();
      if (remaining <= 0) {
        clearQr();
        $('qrStatus').textContent = 'QR 만료됨';
        return;
      }
      $('qrStatus').textContent = `QR 생성 완료 · ${Math.ceil(remaining / 1000)}초 남음`;
    };
    updateExpiry();
    state.qrTimer = setInterval(updateExpiry, 1000);
  }
}
async function loadPaymentHistory() {
  const limit = Math.min(Math.max(Number($('historyLimit').value || 20), 1), 200);
  const query = new URLSearchParams({limit:String(limit)});
  if (state.historyCursor) query.set('before', state.historyCursor);
  const result = await request('GET', `/v1/payments?${query}`);
  state.historyCursor = result.data.next_cursor || null;
  output('history', result.data);
  $('historyCursor').textContent = state.historyCursor ? '다음 페이지 있음' : '마지막 페이지';
}
$('clearLog').onclick = () => { $('log').textContent = ''; $('status').textContent = '대기 중'; };
