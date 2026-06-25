/**
 * Hero Banner Slider — Homepage (UC_01 Field #3: Promotional Banner)
 */
(function () {
    'use strict';

    let currentSlide = 0;
    const AUTO_INTERVAL = 5000;
    let autoTimer;

    function getSlides() { return document.querySelectorAll('.slide'); }
    function getDots()   { return document.querySelectorAll('.dot'); }

    function showSlide(index) {
        const slides = getSlides();
        const dots   = getDots();
        if (!slides.length) return;

        currentSlide = ((index % slides.length) + slides.length) % slides.length;

        slides.forEach((s, i) => {
            s.classList.toggle('slide-active', i === currentSlide);
        });
        dots.forEach((d, i) => {
            d.classList.toggle('active', i === currentSlide);
        });
    }

    function startAuto() {
        clearInterval(autoTimer);
        autoTimer = setInterval(() => showSlide(currentSlide + 1), AUTO_INTERVAL);
    }

    /* Exposed to inline onclick handlers on the HTML */
    window.changeSlide = function (direction) {
        showSlide(currentSlide + direction);
        startAuto();
    };

    window.goToSlide = function (index) {
        showSlide(index);
        startAuto();
    };

    document.addEventListener('DOMContentLoaded', function () {
        showSlide(0);
        startAuto();

        /* Pause on hover */
        const slider = document.getElementById('heroSlider');
        if (slider) {
            slider.addEventListener('mouseenter', () => clearInterval(autoTimer));
            slider.addEventListener('mouseleave', startAuto);
        }
    });
}());