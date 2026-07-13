let accessToken = '';
let merchantId = '';

const $ = (id) => document.getElementById(id);
const api = () => $('baseUrl').value.replace(/\/$/, '');
const headers = () => ({'Content-Type': 'application/json', ...(accessToken ? {Authorization: `Bearer ${accessToken}`} : {})});
const show = (message) => { $('status').textContent = message; };
const print = (id, value) => { $(id).textContent = JSON.stringify(value, null, 2); };

async function request(path, options = {}) {
  const response = await fetch(api() + path, { ...options, headers: {...headers(), ...(options.headers || {})} });
  const text = await response.text();
  let body; try { body = text ? JSON.parse(text) : null; } catch { body = text; }
  if (!response.ok) throw new Error(`${response.status}: ${JSON.stringify(body)}`);
  return body;
}

async function authenticate(path) {
  try {
    const body = await request(path, {method:'POST', body: JSON.stringify({
      email: $('email').value, password: $('password').value, display_name: $('displayName').value
    })});
    accessToken = body.access_token;
    show('인증 성공');
  } catch (error) { show(error.message); }
}

$('signup').onclick = () => authenticate('/v1/user-auth/signup');
$('login').onclick = () => authenticate('/v1/user-auth/login');
$('createMerchant').onclick = async () => {
  try {
    const body = await request('/v1/merchants', {method:'POST', body: JSON.stringify({merchant_name:'Demo Merchant', business_number:String(Date.now()).slice(-10)})});
    merchantId = body.merchant_id; print('merchantResult', body); show('가맹점 생성 성공');
  } catch (error) { show(error.message); }
};
$('createPayment').onclick = async () => {
  try {
    if (!merchantId) throw new Error('먼저 가맹점을 생성하세요.');
    const body = await request(`/v1/merchants/${merchantId}/payments`, {method:'POST', body: JSON.stringify({
      order_id:$('orderId').value, token:$('token').value, amount:Number($('amount').value), description:'API demo'
    })});
    print('paymentResult', body);
    if (body.qr_payload) {
      $('qrToken').value = body.qr_payload.split('/').pop();
      $('qrPayload').textContent = body.qr_payload;
      if (window.QRCode) QRCode.toCanvas($('qrCanvas'), location.origin + body.qr_payload);
    }
    show('결제 생성 성공');
  } catch (error) { show(error.message); }
};
$('lookupQr').onclick = async () => {
  try { const body = await request(`/v1/payment-qr/${encodeURIComponent($('qrToken').value)}`); print('qrResult', body); show('QR 조회 성공'); }
  catch (error) { show(error.message); }
};
$('confirmPayment').onclick = async () => {
  try { const body = await request(`/v1/payment-qr/${encodeURIComponent($('qrToken').value)}/confirm`, {method:'POST'}); print('qrResult', body); show('결제 확정 성공'); }
  catch (error) { show(error.message); }
};
