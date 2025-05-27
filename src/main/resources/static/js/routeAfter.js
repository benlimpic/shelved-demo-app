const routeAfter = () => {
  loader.start();
  setTimeout(() => {
    loader.stop(); // Ensure loader hides before navigating
    window.location.href = '/index';
  }, 3000);
};
