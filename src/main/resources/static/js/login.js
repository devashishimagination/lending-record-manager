if (location.search.includes('error')) {
  document.getElementById('msg').hidden = false;
}


if ('serviceWorker' in navigator) {
  window.addEventListener('load', () => {
    navigator.serviceWorker.register('/sw.js').catch(() => { /* app still works without it */ });
  });
}