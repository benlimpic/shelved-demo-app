const routeAfter = () => {
  if (
    typeof loader !== 'undefined' &&
    typeof loader.start === 'function' &&
    typeof loader.stop === 'function'
  ) {
    loader.start();
    setTimeout(() => {
      window.location.href = '/index';
    }, 3000);
  } else {
    window.location.href = '/index';
  }
};
