let allocationChart, performanceChart;

function renderCharts(isDark) {
    const textColor = isDark ? '#e5e7eb' : '#1f2937';

    if (allocationChart) allocationChart.destroy();
    if (performanceChart) performanceChart.destroy();

    allocationChart = new Chart(document.getElementById('allocationChart'), {
        type: 'doughnut',
        data: {
            labels: ['Stocks', 'Bonds', 'Crypto', 'Cash'],
            datasets: [{
                data: [55, 15, 20, 10],
                backgroundColor: [
                    '#2563eb',
                    '#16a34a',
                    '#dc2626',
                    '#facc15'
                ],
                borderWidth: 4,
                hoverOffset: 12
            }]
        },
        options: {
            cutout: '65%',
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    position: 'right',
                    labels: { color: textColor }
                }
            }
        }
    });

    performanceChart = new Chart(document.getElementById('performanceChart'), {
        type: 'line',
        data: {
            labels: ['Apr', 'May', 'Jun', 'Jul', 'Aug'],
            datasets: [{
                label: 'Portfolio Value',
                data: [120000, 126000, 134000, 142000, 148000],
                borderColor: '#2563eb',
                backgroundColor: 'rgba(37,99,235,0.15)',
                tension: 0.4,
                fill: true
            }]
        },
        options: {
            maintainAspectRatio: false,
            plugins: {
                legend: { labels: { color: textColor } }
            },
            scales: {
                x: { ticks: { color: textColor } },
                y: { ticks: { color: textColor } }
            }
        }
    });
}

function toggleTheme() {
    document.body.classList.toggle('dark');
    renderCharts(document.body.classList.contains('dark'));
}

renderCharts(false);
