const PORTFOLIO_ID = 1;
const BASE_URL = "http://localhost:5400";

let allocationChart = null;
let plChart = null;

document.addEventListener("DOMContentLoaded", loadDashboard);

/* ================= LOAD DASHBOARD ================= */

async function loadDashboard() {
    try {
        const res = await fetch(`${BASE_URL}/api/holdings/${PORTFOLIO_ID}`);
        if (!res.ok) throw new Error("Failed to fetch holdings");

        const holdings = await res.json();
        renderTable(holdings);
        renderCharts(holdings);
        updateSummary(holdings);
    } catch (err) {
        console.error("Dashboard error:", err);
    }
}

/* ================= TABLE ================= */

function renderTable(holdings) {
    const tbody = document.getElementById("holdingsTableBody");
    tbody.innerHTML = "";

    if (holdings.length === 0) {
        tbody.innerHTML = `
            <tr class="empty-state">
                <td colspan="7">
                    <div class="empty-message">
                        <span class="empty-icon">📊</span>
                        <p>No holdings yet. Add your first asset to get started!</p>
                    </div>
                </td>
            </tr>
        `;
        return;
    }

    holdings.forEach(h => {
        const price = h.currentPrice ?? h.purchasePrice;
        const value = h.quantity * price;
        const pl = value - (h.quantity * h.purchasePrice);
        const plClass = pl >= 0 ? "positive" : "negative";
        const plSign = pl >= 0 ? "+" : "";

        tbody.innerHTML += `
            <tr>
                <td><strong>${h.symbol}</strong></td>
                <td>${h.name}</td>
                <td>${h.quantity.toLocaleString()}</td>
                <td>$${h.purchasePrice.toFixed(2)}</td>
                <td>$${price.toFixed(2)}</td>
                <td><strong>$${value.toLocaleString('en-US', {minimumFractionDigits: 2, maximumFractionDigits: 2})}</strong></td>
                <td class="${plClass}">
                    <strong>${plSign}$${Math.abs(pl).toFixed(2)}</strong>
                </td>
                 <td>
                                    <button onclick="deleteHolding(${h.id})">🗑️</button>
                                </td>
            </tr>
        `;
    });
}

/* ================= CHARTS ================= */

function renderCharts(holdings) {
    let stock = 0, mf = 0, gold = 0;
    const labels = [];
    const plValues = [];
    const plColors = [];

    holdings.forEach(h => {
        const price = h.currentPrice ?? h.purchasePrice;
        const value = h.quantity * price;
        const pl = value - (h.quantity * h.purchasePrice);

        if (h.assetType === "STOCK") stock += value;
        if (h.assetType === "MUTUAL_FUND") mf += value;
        if (h.assetType === "GOLD") gold += value;

        labels.push(h.symbol);
        plValues.push(pl);
        plColors.push(pl >= 0 ? '#10b981' : '#ef4444');
    });

    // Destroy existing charts
    if (allocationChart) allocationChart.destroy();
    if (plChart) plChart.destroy();

    // Common chart options
    const commonOptions = {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
            legend: {
                labels: {
                    font: {
                        size: 12,
                        family: "-apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto"
                    },
                    padding: 15,
                    usePointStyle: true,
                    pointStyle: 'circle'
                }
            }
        }
    };

    // Allocation Chart (Pie)
    allocationChart = new Chart(
        document.getElementById("allocationChart"),
        {
            type: "doughnut",
            data: {
                labels: ["Stocks", "Mutual Funds", "Gold"],
                datasets: [{
                    data: [stock, mf, gold],
                    backgroundColor: [
                        '#8b5cf6',
                        '#3b82f6',
                        '#f59e0b'
                    ],
                    borderWidth: 0,
                    hoverOffset: 10
                }]
            },
            options: {
                ...commonOptions,
                cutout: '65%',
                plugins: {
                    ...commonOptions.plugins,
                    legend: {
                        ...commonOptions.plugins.legend,
                        position: 'bottom'
                    },
                    tooltip: {
                        callbacks: {
                            label: function(context) {
                                const label = context.label || '';
                                const value = context.parsed || 0;
                                const total = context.dataset.data.reduce((a, b) => a + b, 0);
                                const percentage = ((value / total) * 100).toFixed(1);
                                return `${label}: $${value.toLocaleString('en-US', {minimumFractionDigits: 2})} (${percentage}%)`;
                            }
                        }
                    }
                }
            }
        }
    );

    // Performance Chart (Bar)
    plChart = new Chart(
        document.getElementById("performanceChart"),
        {
            type: "bar",
            data: {
                labels,
                datasets: [{
                    label: 'Profit/Loss',
                    data: plValues,
                    backgroundColor: plColors,
                    borderRadius: 8,
                    borderSkipped: false
                }]
            },
            options: {
                ...commonOptions,
                plugins: {
                    ...commonOptions.plugins,
                    legend: {
                        display: false
                    },
                    tooltip: {
                        callbacks: {
                            label: function(context) {
                                const value = context.parsed.y;
                                const sign = value >= 0 ? '+' : '';
                                return `P/L: ${sign}$${value.toFixed(2)}`;
                            }
                        }
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        grid: {
                            color: 'rgba(0, 0, 0, 0.05)'
                        },
                        ticks: {
                            callback: function(value) {
                                return '$' + value.toLocaleString();
                            }
                        }
                    },
                    x: {
                        grid: {
                            display: false
                        }
                    }
                }
            }
        }
    );
}

/* ================= SUMMARY ================= */
async function deleteHolding(holdingId) {
    if (!confirm("Delete this holding?")) return;

    const res = await fetch(`${BASE_URL}/api/holdings/${holdingId}`, {
        method: "DELETE"
    });

    if (!res.ok) {
        alert("Failed to delete holding");
        return;
    }

    loadDashboard(); // refresh table
}

function updateSummary(holdings) {
    let stock = 0, mf = 0, gold = 0;
    let totalValue = 0;
    let totalCost = 0;

    holdings.forEach(h => {
        const price = h.currentPrice ?? h.purchasePrice;
        const value = h.quantity * price;
        const cost = h.quantity * h.purchasePrice;

        totalValue += value;
        totalCost += cost;

        if (h.assetType === "STOCK") stock += value;
        if (h.assetType === "MUTUAL_FUND") mf += value;
        if (h.assetType === "GOLD") gold += value;
    });

    // Update summary cards
    document.getElementById("stocksValue").innerText = `$${stock.toLocaleString('en-US', {minimumFractionDigits: 2, maximumFractionDigits: 2})}`;
    document.getElementById("mfValue").innerText = `$${mf.toLocaleString('en-US', {minimumFractionDigits: 2, maximumFractionDigits: 2})}`;
    document.getElementById("goldValue").innerText = `$${gold.toLocaleString('en-US', {minimumFractionDigits: 2, maximumFractionDigits: 2})}`;

    // Update header stats
    document.getElementById("totalValue").innerText = `$${totalValue.toLocaleString('en-US', {minimumFractionDigits: 2, maximumFractionDigits: 2})}`;
    
    const totalReturn = totalCost > 0 ? ((totalValue - totalCost) / totalCost) * 100 : 0;
    const returnElement = document.getElementById("totalReturn");
    const returnSign = totalReturn >= 0 ? '+' : '';
    returnElement.innerText = `${returnSign}${totalReturn.toFixed(2)}%`;
    returnElement.className = totalReturn >= 0 ? 'stat-value positive' : 'stat-value negative';
}

/* ================= MODAL ================= */

function openModal() {
    document.getElementById("addAssetModal").classList.remove("hidden");
    document.body.style.overflow = 'hidden';
}

function closeModal() {
    document.getElementById("addAssetModal").classList.add("hidden");
    document.body.style.overflow = '';
    // Reset form
    document.querySelectorAll('.modal-form input, .modal-form select').forEach(input => {
        if (input.type !== 'submit') input.value = '';
    });
}

// Close modal on ESC key
document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape') {
        closeModal();
    }
});

/* ================= THEME ================= */

function toggleTheme() {
    const body = document.body;
    const themeIcon = document.querySelector('.theme-icon');
    
    if (body.classList.contains('light')) {
        body.classList.remove('light');
        body.classList.add('dark');
        themeIcon.textContent = '☀️';
        localStorage.setItem('theme', 'dark');
    } else {
        body.classList.remove('dark');
        body.classList.add('light');
        themeIcon.textContent = '🌙';
        localStorage.setItem('theme', 'light');
    }
}

// Load saved theme
document.addEventListener('DOMContentLoaded', () => {
    const savedTheme = localStorage.getItem('theme') || 'light';
    const themeIcon = document.querySelector('.theme-icon');
    
    document.body.classList.add(savedTheme);
    themeIcon.textContent = savedTheme === 'dark' ? '☀️' : '🌙';
});

/* ================= ADD ASSET ================= */

async function submitAsset() {
    try {
        const asset = {
            symbol: document.getElementById("symbol").value.trim().toUpperCase(),
            name: document.getElementById("holdingName").value.trim(),
            quantity: Number(document.getElementById("quantity").value),
            purchasePrice: Number(document.getElementById("price").value),
            assetType: document.getElementById("assetType").value
        };

        // Basic validation
        if (!asset.symbol || !asset.name || asset.quantity <= 0 || asset.purchasePrice <= 0) {
            alert("Please fill in all fields with valid values");
            return;
        }

        const res = await fetch(`${BASE_URL}/api/holdings/${PORTFOLIO_ID}`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(asset)
        });

        if (!res.ok) throw new Error(await res.text());

        closeModal();
        loadDashboard();
        
        // Show success feedback
        showNotification("Asset added successfully!", "success");
    } catch (err) {
        console.error("Add asset failed:", err);
        showNotification("Failed to add asset. Please try again.", "error");
    }
}

/* ================= NOTIFICATIONS ================= */

function showNotification(message, type = "info") {
    // Create notification element
    const notification = document.createElement('div');
    notification.className = `notification notification-${type}`;
    notification.textContent = message;
    notification.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        padding: 1rem 1.5rem;
        background: ${type === 'success' ? '#10b981' : '#ef4444'};
        color: white;
        border-radius: 10px;
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
        z-index: 2000;
        animation: slideInRight 0.3s ease;
    `;
    
    document.body.appendChild(notification);
    
    // Remove after 3 seconds
    setTimeout(() => {
        notification.style.animation = 'slideOutRight 0.3s ease';
        setTimeout(() => notification.remove(), 300);
    }, 3000);
}

// Add animation styles
const style = document.createElement('style');
style.textContent = `
    @keyframes slideInRight {
        from {
            transform: translateX(400px);
            opacity: 0;
        }
        to {
            transform: translateX(0);
            opacity: 1;
        }
    }
    @keyframes slideOutRight {
        from {
            transform: translateX(0);
            opacity: 1;
        }
        to {
            transform: translateX(400px);
            opacity: 0;
        }
    }
`;
document.head.appendChild(style);