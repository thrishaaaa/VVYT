// Sidebar Toggle Functionality
const sidebarToggle = document.getElementById('sidebarToggle');
const sidebar = document.getElementById('sidebar');
const mainContent = document.getElementById('mainContent');
const sidebarOverlay = document.getElementById('sidebarOverlay');

sidebarToggle.addEventListener('click', function () {
    if (window.innerWidth <= 768) {
        // Mobile behavior
        sidebar.classList.toggle('show');
        sidebarOverlay.classList.toggle('show');
    } else {
        // Desktop behavior
        sidebar.classList.toggle('collapsed');
        mainContent.classList.toggle('expanded');
    }
});

// Close sidebar when clicking overlay (mobile)
sidebarOverlay.addEventListener('click', function () {
    sidebar.classList.remove('show');
    sidebarOverlay.classList.remove('show');
});

// Dropdown functionality
document.querySelectorAll('.dropdown-toggle').forEach(function (element) {
    element.addEventListener('click', function (e) {
        e.preventDefault();
        const targetId = this.getAttribute('data-bs-target');
        const targetElement = document.querySelector(targetId);
        const arrow = this.querySelector('.dropdown-arrow');

        if (targetElement.classList.contains('show')) {
            targetElement.classList.remove('show');
            arrow.classList.remove('rotated');
        } else {
            // Close all other dropdowns
            document.querySelectorAll('.dropdown-menu-custom').forEach(function (dropdown) {
                dropdown.classList.remove('show');
            });
            document.querySelectorAll('.dropdown-arrow').forEach(function (arrow) {
                arrow.classList.remove('rotated');
            });

            // Open clicked dropdown
            targetElement.classList.add('show');
            arrow.classList.add('rotated');
        }
    });
});

// Menu item active state
document.querySelectorAll('.menu-link').forEach(function (link) {
    link.addEventListener('click', function (e) {
        if (!this.classList.contains('dropdown-toggle')) {
            document.querySelectorAll('.menu-link').forEach(function (l) {
                l.classList.remove('active');
            });
            this.classList.add('active');
        }
    });
});



// Responsive behavior
window.addEventListener('resize', function () {
    if (window.innerWidth > 768) {
        sidebar.classList.remove('show');
        sidebarOverlay.classList.remove('show');
    }
});

// Auto-hide sidebar on mobile when clicking menu items
document.querySelectorAll('.menu-link, .dropdown-item-custom').forEach(function (link) {
    link.addEventListener('click', function () {
        if (window.innerWidth <= 768) {
            sidebar.classList.remove('show');
            sidebarOverlay.classList.remove('show');
        }
    });
});