// Format a number as Indian rupees
const money = n => new Intl.NumberFormat('en-IN', {
  style: 'currency', currency: 'INR', maximumFractionDigits: 2
}).format(n ?? 0);

const daysBetween = (fromText, to = new Date()) =>
  Math.floor((to.getTime() - new Date(fromText).getTime()) / 86400000);

const formatDate = text => text
  ? new Date(text).toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' })
  : '-';

const initials = name => name.trim().split(/\s+/).slice(0, 2)
  .map(w => w[0].toUpperCase()).join('');

// Small helper: make <div class="cls">text</div>
function div(cls, text) {
  const d = document.createElement('div');
  d.className = cls;
  if (text !== undefined) d.textContent = text;
  return d;
}

// One place to call the API and handle errors
async function api(path, options = {}) {
  const res = await fetch(path, { headers: { 'Content-Type': 'application/json' }, ...options });
  if (res.status === 401) {
    location.href = 'login.html';
    throw new Error('Please sign in again');
  }
  if (!res.ok) {
    let message = 'Server returned ' + res.status;
    try {
      const body = await res.json();
      if (body.message) message = body.message;
    } catch (e) { /* no JSON body */ }
    throw new Error(message);
  }
  const text = await res.text();
  return text ? JSON.parse(text) : null;
}

function toast(message, isError = false) {
  let box = document.getElementById('toast');
  if (!box) {
    box = document.createElement('div');
    box.id = 'toast';
    document.body.appendChild(box);
  }
  box.textContent = message;
  box.className = 'show' + (isError ? ' bad' : '');
  clearTimeout(toast.timer);
  toast.timer = setTimeout(() => { box.className = ''; }, 3000);
}

// Draws the top bar and the bottom bar (phones) on every page
function renderShell(active) {
  const links = [
    { href: 'index.html', label: 'Dashboard', icon: '&#128202;' },
    { href: 'people.html', label: 'People', icon: '&#128101;' }
  ];
  document.getElementById('app-header').innerHTML =
    '<div class="brand">&#128213; Bahi Khata</div><nav class="top-nav">' +
    links.map(l => '<a href="' + l.href + '" class="' + (l.href === active ? 'active' : '') + '">' + l.label + '</a>').join('') +
    '<a href="/logout" class="logout">Log out</a></nav>';
  document.getElementById('app-bottom').innerHTML =
    links.map(l => '<a href="' + l.href + '" class="' + (l.href === active ? 'active' : '') + '"><span>' + l.icon + '</span>' + l.label + '</a>').join('') +
    '<a href="/logout"><span>&#128274;</span>Log out</a>';
}
// Today's date as yyyy-mm-dd in the device's own time zone
function todayText() {
  const d = new Date();
  return d.getFullYear() + '-' + String(d.getMonth() + 1).padStart(2, '0') + '-' +
         String(d.getDate()).padStart(2, '0');
}


if ('serviceWorker' in navigator) {
  window.addEventListener('load', () => {
    navigator.serviceWorker.register('/sw.js').catch(() => { /* app still works without it */ });
  });
}