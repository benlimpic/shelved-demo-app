document.addEventListener('DOMContentLoaded', function () {
  const loader = new ShelvedLoader();

  // FORM SUBMISSIONS
  document.querySelectorAll('form').forEach((form) => {
    form.addEventListener('submit', function (e) {
      e.preventDefault(); // Block default form submission
      loader.start();

      const action = form.getAttribute('action') || window.location.href;
      const method = form.getAttribute('method')?.toLowerCase() || 'get';

      if (method === 'get') {
        const params = new URLSearchParams(new FormData(form)).toString();
        setTimeout(() => {
          window.location.href = `${action}?${params}`;
        }, 3000); // Trigger route AFTER animation
      } else {
        // POST fallback — create hidden form and submit after delay
        const hiddenForm = document.createElement('form');
        hiddenForm.method = 'post';
        hiddenForm.action = action;
        hiddenForm.style.display = 'none';

        Array.from(new FormData(form).entries()).forEach(([key, value]) => {
          const input = document.createElement('input');
          input.type = 'hidden';
          input.name = key;
          input.value = value;
          hiddenForm.appendChild(input);
        });

        document.body.appendChild(hiddenForm);
        setTimeout(() => {
          hiddenForm.submit();
        }, 3000);
      }
    });
  });
});
