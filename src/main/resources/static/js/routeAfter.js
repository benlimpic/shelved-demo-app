const routeAfter = () => {
  if (typeof loader !== 'undefined' && typeof loader.start === 'function') {
    loader.start();

    // Force DOM paint before routing by chaining requestAnimationFrame
    requestAnimationFrame(() => {
      setTimeout(() => {
        window.location.href = '/index';
      }, 3000);
    });
  } else {
    window.location.href = '/index';
  }
};
