// Service worker: keeps the app shell (pages, styles, scripts) available offline.
// Money data is NEVER stored: every /api/ request goes straight to the server.
const CACHE = 'lending-shell-v1';
const SHELL = [
  '/login.html', '/offline.html',
  '/css/style.css', '/js/common.js', '/js/login.js',
  '/icons/icon-192.png', '/icons/icon-512.png'
];

self.addEventListener('install', event => {
  event.waitUntil(caches.open(CACHE).then(cache => cache.addAll(SHELL)));
  self.skipWaiting();
});

self.addEventListener('activate', event => {
  event.waitUntil(
    caches.keys()
      .then(keys => Promise.all(keys.filter(k => k !== CACHE).map(k => caches.delete(k))))
      .then(() => self.clients.claim())
  );
});

self.addEventListener('fetch', event => {
  const req = event.request;
  const url = new URL(req.url);

  // Only handle our own GET requests; leave everything else alone
  if (req.method !== 'GET' || url.origin !== location.origin) return;

  // Private data and account actions always go to the network
  if (url.pathname.startsWith('/api/') || url.pathname === '/login' || url.pathname === '/logout') return;

  // Network first, so updates show at once; fall back to the cache when offline
  event.respondWith(
    fetch(req)
      .then(res => {
        if (res.ok && !res.redirected && res.type === 'basic') {
          const copy = res.clone();
          caches.open(CACHE).then(cache => cache.put(req, copy));
        }
        return res;
      })
      .catch(() =>
        caches.match(req).then(hit =>
          hit || (req.mode === 'navigate' ? caches.match('/offline.html') : Response.error())
        )
      )
  );
});
