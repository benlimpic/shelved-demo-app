class ShelvedLoader {
  constructor() {
    this.injectHTML();
    this.injectCSS();
    this.wrapper = document.getElementById('logoWrapper');
    this.overlay = document.getElementById('overlay');
    this.body = document.body;
    this.centerX = window.innerWidth / 2;
    this.centerY = window.innerHeight / 2;
    this.logoAbsorbCount = 0;
    this.maxAbsorbCount = 400;
    this.exploded = false;
    this.running = false;
    this.animationFrame = null;
    this._resizeHandler = () => {
      this.centerX = window.innerWidth / 2;
      this.centerY = window.innerHeight / 2;
    };
    window.addEventListener('resize', this._resizeHandler);
  }

  injectHTML() {
    if (document.getElementById('logoWrapper')) return;
    const loaderHTML = `
      <div class="logo-wrapper" id="logoWrapper" style="display:none;">
        <img id="logo" src="/images/Shelved_Logo_White.png" alt="Logo">
      </div>
      <div id="overlay"></div>
    `;
    document.body.insertAdjacentHTML('beforeend', loaderHTML);
  }

  injectCSS() {
    if (document.getElementById('shelved-loader-style')) return;
    const style = document.createElement('style');
    style.id = 'shelved-loader-style';
    style.innerHTML = `
      html, body {
        margin: 0;
        padding: 0;
        background: #1a69e9;
        overflow: hidden;
        height: 100vh;
        width: 100vw;
        perspective: 1000px;
      }

      .logo-wrapper {
        position: fixed;
        top: 50%;
        left: 50%;
        transform: translate(-50%, -50%);
        z-index: 100000;
        display: none;
      }

      #logo {
        width: 200px;
        height: auto;
        display: block;
      }

      .blob, .explosion-particle {
        position: absolute;
        width: 6px;
        height: 6px;
        background: white;
        border-radius: 50%;
        pointer-events: none;
        opacity: 1;
        z-index: 100001;
      }

      #overlay {
        position: fixed;
        top: 0;
        left: 0;
        width: 100vw;
        height: 100vh;
        background: #1a69e9;
        opacity: 0;
        pointer-events: all;
        z-index: 99999;
        transition: opacity 0.3s ease-in-out;
        display: block; /* <- Always block, never none */
      }
    `;
    document.head.appendChild(style);
  }

  spawnMeteorBlobs(count = 400, totalDuration = 1750) {
    const spawnInterval = totalDuration / count;
    let spawned = 0;
    const spawn = () => {
      if (!this.running || spawned >= count) return;
      spawned++;
      const blob = document.createElement('div');
      blob.classList.add('blob');
      const angle = Math.random() * Math.PI * 2;
      const radius =
        Math.max(window.innerWidth, window.innerHeight) *
        (0.75 + Math.random() * 0.25);
      const startX = this.centerX + radius * Math.cos(angle);
      const startY = this.centerY + radius * Math.sin(angle);
      blob.style.left = `${startX}px`;
      blob.style.top = `${startY}px`;
      this.body.appendChild(blob);
      const travelTime = 1.3 + Math.random() * 0.5;
      gsap.to(blob, {
        x: this.centerX - startX,
        y: this.centerY - startY,
        duration: travelTime,
        ease: 'power2.in',
        onComplete: () => {
          this.logoAbsorbCount++;
          gsap.to(blob, {
            opacity: 0,
            scale: 0,
            duration: 0.25,
            onComplete: () => blob.remove(),
          });
        },
      });
      setTimeout(spawn, spawnInterval);
    };
    for (let i = 0; i < 10; i++) spawn();
  }

  explodeLogo() {
    this.wrapper.style.display = 'none';
    const count = 100;
    for (let i = 0; i < count; i++) {
      const p = document.createElement('div');
      p.classList.add('explosion-particle');
      p.style.left = `${this.centerX}px`;
      p.style.top = `${this.centerY}px`;
      this.body.appendChild(p);
      const angle = Math.random() * Math.PI * 2;
      const distance = 300 + Math.random() * 400;
      const dx = Math.cos(angle) * distance;
      const dy = Math.sin(angle) * distance;
      gsap.to(p, {
        x: dx,
        y: dy,
        opacity: 0,
        duration: 0.8,
        ease: 'power3.out',
        onComplete: () => p.remove(),
      });
    }
  }

  animateLogo() {
    this.logoAbsorbCount = 0;
    this.exploded = false;
    this.wrapper.style.display = 'block';
    this.overlay.style.opacity = 1;
    this.spawnMeteorBlobs(400, 1750);
    const startTime = performance.now();
    const updateLogo = (t) => {
      if (!this.running) return;
      const elapsed = t - startTime;
      const timeSec = elapsed / 1000;
      if (timeSec >= 3) {
        gsap.set(this.wrapper, {
          scale: 0,
          rotationX: 0,
          rotationY: 0,
          rotationZ: 0,
          skewX: 0,
          skewY: 0,
        });
        this.animateLogo(); // Loop
        return;
      }
      if (timeSec <= 2) {
        const progress = Math.min(
          this.logoAbsorbCount / this.maxAbsorbCount,
          1
        );
        const scale = progress * 2;
        const rx = Math.sin(timeSec * 6) * 5;
        const ry = Math.sin(timeSec * 5) * 5;
        const rz = Math.sin(timeSec * 7) * 3;
        const skewX = Math.sin(timeSec * 4) * 3;
        const skewY = Math.cos(timeSec * 3) * 2;
        gsap.set(this.wrapper, {
          scale,
          rotationX: rx,
          rotationY: ry,
          rotationZ: rz,
          skewX,
          skewY,
        });
      }
      if (timeSec >= 2 && !this.exploded) {
        this.exploded = true;
        gsap.to(this.wrapper, {
          scale: 0.4,
          duration: 0.15,
          ease: 'power3.inOut',
          onComplete: () => this.explodeLogo(),
        });
      }
      this.animationFrame = requestAnimationFrame(updateLogo);
    };
    this.animationFrame = requestAnimationFrame(updateLogo);
  }

  startLoop() {
    if (!this.running) return;
    this.animateLogo();
  }

  start() {
    if (this.running) return;
    this.running = true;
    this.wrapper.style.display = 'block';
    this.overlay.style.opacity = 0;
    // Force reflow to apply transition
    void this.overlay.offsetHeight;
    this.overlay.style.opacity = 1;
    this.startLoop();
  }

  stop() {
    this.running = false;
    cancelAnimationFrame(this.animationFrame);
    if (this.wrapper) this.wrapper.remove();
    if (this.overlay) this.overlay.remove();
  }
}
