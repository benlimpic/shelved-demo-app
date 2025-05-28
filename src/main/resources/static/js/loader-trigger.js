document.addEventListener('DOMContentLoaded', function () {
  const loader = new ShelvedLoader();

  document.querySelectorAll('form').forEach((form) => {
    form.addEventListener('submit', function (e) {
      e.preventDefault(); // Prevent immediate navigation
      loader.start();
      setTimeout(() => {
        form.submit();
        loader.stop(); // Submit the form after 3 seconds
      }, 3000);
    });
  });
});
