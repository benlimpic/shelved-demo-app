function routeAfter() {
  if (
    typeof loader !== 'undefined' &&
    typeof loader.start === 'function' &&
    typeof loader.stop === 'function'
  ) {
    loader.start();
    setTimeout(() => {
      window.location.href = '/index';
    }, 3000); // Only navigate after loader finishes
  } else {
    window.location.href = '/index';
  }
}
