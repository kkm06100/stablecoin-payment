const $ = (id) => document.getElementById(id);
const api = () => $('baseUrl').value.replace(/\/$/, '');
const state = {merchantToken:'', customerToken:'', merchantId:'', paymentId:'', qrToken:'', qrPayload:''};

function log(label, value) {
  $('log').textContent += `[${new Date().toLocaleTimeString()}] ${label}\n${JSON.stringify(value, null, 2)}\n\n`;
}
function mark(step, status = 'done') {
  const node = document.querySelector(`[data-step="${step}"]`);
  if (node) { node.dataset.status = status; node.textContent = `${status === 'done' ? '✓' : '…'} ${node.textContent.replace(/^[✓…] /, '')}`; }
}
function headers(token) { return {'Content-Type':'application/json', ...(token ? {Authorization:`Bearer ${token}`} : {})}; }
async function request(method, path, body, token) {
  const response = await fetch(api() + path, {method, headers:headers(token), body:body ? JSON.stringify(body) : undefined});
  const text = await response.text();
  let data; try { data = text ? JSON.parse(text) : null; } catch { data = text; }
  log(`${method} ${path} → ${response.status}`, data);
  if (!response.ok) throw new Error(`${response.status} ${path}: ${JSON.stringify(data)}`);
  return data;
}
async function signup(prefix, displayName) {
  const email = `${prefix}-${Date.now()}@example.com`;
  return request('POST', '/v1/user-auth/signup', {email, password:'password123', display_name:displayName});
}
function renderQr(payload) {
  state.qrPayload = payload;
  state.qrToken = payload.split('/').pop();
  $('qrPayload').textContent = payload;
  if (window.QRCode) QRCode.toCanvas($('qrCanvas'), `${api()}${payload}`);
}
async function runScenario() {
  $('log').textContent = ''; $('status').textContent = '실행 중…';
  document.querySelectorAll('#steps li').forEach((node) => { delete node.dataset.status; node.textContent = node.textContent.replace(/^[✓…] /, ''); });
  try {
    const merchantAuth = await signup('merchant', 'Demo Merchant Owner');
    state.merchantToken = merchantAuth.access_token; mark('merchant-signup');
    const merchant = await request('POST', '/v1/merchants', {merchant_name:'Demo Merchant', business_number:String(Date.now()).slice(-10)}, state.merchantToken);
    state.merchantId = merchant.merchant_id; mark('merchant-create');
    const payment = await request('POST', `/v1/merchants/${state.merchantId}/payments`, {order_id:`order-${Date.now()}`, token:'USDC-test', amount:1, description:'Scenario v2'}, state.merchantToken);
    state.paymentId = payment.payment_id; renderQr(payment.qr_payload); mark('payment-create');
    const customerAuth = await signup('customer', 'Demo Customer');
    state.customerToken = customerAuth.access_token; mark('customer-signup');
    await request('GET', `/v1/payment-qr/${state.qrToken}`); mark('qr-lookup');
    try {
      await request('POST', `/v1/payment-qr/${state.qrToken}/confirm`, null, state.customerToken);
      mark('payment-confirm'); $('status').textContent = '전체 결제 시나리오 성공';
    } catch (error) {
      mark('payment-confirm', 'failed'); $('status').textContent = `결제 확정 실패: ${error.message}`;
    }
  } catch (error) { $('status').textContent = `시나리오 중단: ${error.message}`; }
}
$('runScenario').onclick = runScenario;
$('clearLog').onclick = () => { $('log').textContent = ''; $('status').textContent = '대기 중'; };
