document.addEventListener('DOMContentLoaded', function() {
    // Sidebar toggle functionality
    const sidebarToggle = document.getElementById('sidebar-toggle');
    const sidebarClose = document.getElementById('sidebar-close');
    const sidebar = document.querySelector('.sidebar');

    if (sidebarToggle) {
        sidebarToggle.addEventListener('click', function() {
            sidebar.classList.toggle('show');
        });
    }

    if (sidebarClose) {
        sidebarClose.addEventListener('click', function() {
            sidebar.classList.remove('show');
        });
    }

    // Chart.js Configurations
    // Task Completion Pie Chart
    const taskPieCtx = document.getElementById('taskPie').getContext('2d');
    const taskPieChart = new Chart(taskPieCtx, {
        type: 'doughnut',
        data: {
            labels: ['Completed', 'Pending', 'Overdue'],
            datasets: [{
                data: [65, 25, 10],
                backgroundColor: [
                    '#7DC142',  // Green
                    '#FFC20E',  // Yellow
                    '#FF6B6B'   // Red
                ],
                borderWidth: 0
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            cutout: '70%',
            plugins: {
                legend: {
                    position: 'bottom',
                    labels: {
                        boxWidth: 12,
                        padding: 20
                    }
                },
                tooltip: {
                    callbacks: {
                        label: function(context) {
                            return `${context.label}: ${context.parsed}%`;
                        }
                    }
                }
            },
            animation: {
                animateScale: true,
                animateRotate: true
            }
        }
    });

    // Projects by Status Bar Chart
    const projectBarCtx = document.getElementById('projectBar').getContext('2d');
    const projectBarChart = new Chart(projectBarCtx, {
        type: 'bar',
        data: {
            labels: ['Active', 'Completed', 'On Hold', 'Canceled'],
            datasets: [{
                label: 'Number of Projects',
                data: [42, 28, 8, 4],
                backgroundColor: '#00549F', // BBOXX Blue
                borderRadius: 8,
                maxBarThickness: 50
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    display: false
                },
                tooltip: {
                    backgroundColor: '#00549F',
                    titleColor: 'white',
                    bodyColor: 'white'
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: {
                        precision: 0,
                        color: '#666'
                    },
                    grid: {
                        color: 'rgba(0,0,0,0.05)',
                        drawBorder: false
                    }
                },
                x: {
                    ticks: {
                        color: '#666'
                    },
                    grid: {
                        display: false
                    }
                }
            }
        }
    });

    // Inventory Levels Horizontal Bar Chart
    const inventoryBarCtx = document.getElementById('inventoryBar').getContext('2d');
    const inventoryBarChart = new Chart(inventoryBarCtx, {
        type: 'bar',
        data: {
            labels: ['Solar Panels', 'Batteries', 'Inverters', 'LED Lights', 'Cables'],
            datasets: [{
                label: 'Current Stock',
                data: [124, 85, 56, 210, 160],
                backgroundColor: '#2E9FD2', // BBOXX Light Blue
                borderRadius: 8,
                maxBarThickness: 50
            }]
        },
        options: {
            indexAxis: 'y',
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    display: false
                },
                tooltip: {
                    backgroundColor: '#2E9FD2',
                    titleColor: 'white',
                    bodyColor: 'white'
                }
            },
            scales: {
                x: {
                    beginAtZero: true,
                    ticks: {
                        precision: 0,
                        color: '#666'
                    },
                    grid: {
                        color: 'rgba(0,0,0,0.05)',
                        drawBorder: false
                    }
                },
                y: {
                    ticks: {
                        color: '#666'
                    },
                    grid: {
                        display: false
                    }
                }
            }
        }
    });

    // Search functionality (basic example)
    const searchInput = document.querySelector('.search-input');
    if (searchInput) {
        searchInput.addEventListener('keyup', function(e) {
            const searchTerm = e.target.value.toLowerCase();
            // Implement search logic here
            console.log('Searching for:', searchTerm);
        });
    }

    // Notification and User Dropdown Interactions
    const notificationDropdown = document.querySelector('.notification-dropdown');
    if (notificationDropdown) {
        const markAllReadBtn = notificationDropdown.querySelector('.mark-all');
        if (markAllReadBtn) {
            markAllReadBtn.addEventListener('click', function() {
                const unreadItems = notificationDropdown.querySelectorAll('.notification-item.unread');
                unreadItems.forEach(item => {
                    item.classList.remove('unread');
                });
            });
        }
    }
});