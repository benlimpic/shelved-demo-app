document.addEventListener('DOMContentLoaded', function () {
  const loader = new ShelvedLoader();

  document.querySelectorAll('form').forEach((form) => {
    form.addEventListener('submit', function (e) {
      e.preventDefault();
      loader.start();

      setTimeout(() => {
        form.submit();
      }, 3000); // Give time for the animation before submission
    });
  });
});
