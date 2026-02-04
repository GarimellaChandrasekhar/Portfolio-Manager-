const PORTFOLIO_ID = 1;
const BASE_URL = "http://localhost:5400";

// ⚠️ IMPORTANT: Replace this with your actual Finnhub API key
// Get free API key from: https://finnhub.io/register
const FINNHUB_API_KEY = "";

let allocationChart = null;
let plChart = null;
let priceUpdateInterval = null;

// Mapping for Indian stocks to NSE symbols
const STOCK_SYMBOL_MAP = {
    'TCS': 'TCS.NS',
    'RELIANCE': 'RELIANCE.NS',
    'HDFCBANK': 'HDFCBANK.NS',
    'ICICIBANK': 'ICICIBANK.NS',
    'INFY': 'INFY',
    'SBIN': 'SBIN'
};

// Default prices (fallback if API fails)
const DEFAULT_PRICES = {
    'TCS': 4250.00,
    'RELIANCE': 2850.00,
    'HDFCBANK': 1650.00,
    'ICICIBANK': 1180.00,
    'INFY': 1820.00,
    'SBIN': 785.00,
    'SBI-TAX': 150.00,
    'ICICI-PRUD': 450.00,
    'GOLD': 6500.00
};

document.addEventListener("DOMContentLoaded", () => {
    // Check if API key is configured
    if (FINNHUB_API_KEY === "YOUR_FINNHUB_API_KEY" || !FINNHUB_API_KEY) {
        showNotification("⚠️ Finnhub API key not configured. Using default prices. Check console for instructions.", "warning");
        console.warn("═══════════════════════════════════════════════════════");
        console.warn("⚠️  FINNHUB API KEY NOT CONFIGURED");
        console.warn("═══════════════════════════════════════════════════════");
        console.warn("To get live prices:");
        console.warn("1. Visit: https://finnhub.io/register");
        console.warn("2. Sign up for a free account");
        console.warn("3. Copy your API key");
        console.warn("4. Replace 'YOUR_FINNHUB_API_KEY' in dashboard.js (line 5)");
        console.warn("5. Refresh the page");
        console.warn("═══════════════════════════════════════════════════════");
    }

    loadDashboard();
    // Update prices every 60 seconds
    priceUpdateInterval = setInterval(() => refreshPrices(), 60000);
});

// Clean up interval on page unload
window.addEventListener('beforeunload', () => {
    if (priceUpdateInterval) clearInterval(priceUpdateInterval);
});

/* ================= FINNHUB API FUNCTIONS ================= */

async function fetchLivePrice(symbol, assetType) {
    try {
        // Check if API key is configured
        if (FINNHUB_API_KEY === "YOUR_FINNHUB_API_KEY" || !FINNHUB_API_KEY) {
            console.warn("Finnhub API key not configured. Using default prices.");
            return DEFAULT_PRICES[symbol] || null;
        }

        if (assetType === 'GOLD') {
            // For gold, use default price or integrate gold API
            return DEFAULT_PRICES['GOLD'];
        }

        if (assetType === 'MUTUAL_FUND') {
            // Mutual funds use default prices
            return DEFAULT_PRICES[symbol] || null;
        }

        // Map to NSE symbol
        const nseSymbol = STOCK_SYMBOL_MAP[symbol] || symbol;

        console.log(`Fetching price for ${nseSymbol}...`);

        const response = await fetch(
            `https://finnhub.io/api/v1/quote?symbol=${nseSymbol}&token=${FINNHUB_API_KEY}`
        );

        if (!response.ok) {
            console.error(`API request failed with status: ${response.status}`);
            throw new Error('Failed to fetch price from Finnhub');
        }

        const data = await response.json();

        console.log(`API Response for ${symbol}:`, data);

        // Finnhub returns current price in 'c' field
        if (data.c && data.c > 0) {
            return data.c;
        } else {
            console.warn(`No valid price returned for ${symbol}. Using default price.`);
            return DEFAULT_PRICES[symbol] || null;
        }
    } catch (error) {
        console.error(`Error fetching price for ${symbol}:`, error);
        // Return default price as fallback
        return DEFAULT_PRICES[symbol] || null;
    }
}

async function updateLivePrices(holdings) {
    const updatedHoldings = [];

    for (const holding of holdings) {
        const livePrice = await fetchLivePrice(holding.symbol, holding.assetType);

        updatedHoldings.push({
            ...holding,
            currentPrice: livePrice || holding.purchasePrice
        });
    }

    return updatedHoldings;
}

/* ================= LOAD DASHBOARD ================= */

async function loadDashboard() {
    try {
        updatePriceStatus('loading');

        const res = await fetch(`${BASE_URL}/api/holdings/${PORTFOLIO_ID}`);
        if (!res.ok) throw new Error("Failed to fetch holdings");

        let holdings = await res.json();

        // Fetch live prices
        holdings = await updateLivePrices(holdings);

        renderTable(holdings);
        renderCharts(holdings);
        updateSummary(holdings);
        updateLastUpdatedTime();
        updatePriceStatus('active');
    } catch (err) {
        console.error("Dashboard error:", err);
        updatePriceStatus('error');
    }
}

async function refreshPrices() {
    try {
        updatePriceStatus('loading');

        const res = await fetch(`${BASE_URL}/api/holdings/${PORTFOLIO_ID}`);
        if (!res.ok) throw new Error("Failed to fetch holdings");

        let holdings = await res.json();

        // Fetch live prices
        holdings = await updateLivePrices(holdings);

        renderTable(holdings);
        renderCharts(holdings);
        updateSummary(holdings);
        updateLastUpdatedTime();
        updatePriceStatus('active');

        showNotification("Prices updated successfully!", "success");
    } catch (err) {
        console.error("Refresh error:", err);
        updatePriceStatus('error');
        showNotification("Failed to update prices", "error");
    }
}

function updatePriceStatus(status) {
    const statusIndicator = document.getElementById('priceStatus');
    const statusDot = statusIndicator.querySelector('.status-dot');
    const statusText = statusIndicator.querySelector('span:last-child');

    statusIndicator.className = 'status-indicator';

    switch(status) {
        case 'loading':
            statusIndicator.classList.add('status-loading');
            statusText.textContent = 'Updating...';
            break;
        case 'active':
            statusIndicator.classList.add('status-active');
            statusText.textContent = 'Live Prices';
            break;
        case 'error':
            statusIndicator.classList.add('status-error');
            statusText.textContent = 'Update Failed';
            break;
    }
}

function updateLastUpdatedTime() {
    const now = new Date();
    const timeString = now.toLocaleTimeString('en-IN', {
        hour: '2-digit',
        minute: '2-digit'
    });
    document.getElementById('lastUpdated').textContent = timeString;
}

/* ================= TABLE ================= */

function renderTable(holdings) {
    const tbody = document.getElementById("holdingsTableBody");
    tbody.innerHTML = "";

    if (holdings.length === 0) {
        tbody.innerHTML = `
            <tr class="empty-state">
                <td colspan="8">
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
        const plPercent = ((pl / (h.quantity * h.purchasePrice)) * 100);
        const plClass = pl >= 0 ? "positive" : "negative";
        const plSign = pl >= 0 ? "+" : "";

        tbody.innerHTML += `
            <tr>
                <td><strong>${h.symbol}</strong></td>
                <td>${h.name}</td>
                <td>${h.quantity.toLocaleString('en-IN')}</td>
                <td>₹${h.purchasePrice.toFixed(2)}</td>
                <td>₹${price.toFixed(2)}</td>
                <td><strong>₹${value.toLocaleString('en-IN', {minimumFractionDigits: 2, maximumFractionDigits: 2})}</strong></td>
                <td class="${plClass}">
                    <strong>${plSign}₹${Math.abs(pl).toFixed(2)}</strong>
                </td>
                <td class="${plClass}">
                    <strong>${plSign}${plPercent.toFixed(2)}%</strong>
                </td>
                  <td>
                            <button class="btn-delete" onclick="deleteHolding(${h.id})" title="Delete">
                                🗑️
                            </button>
                        </td>
            </tr>
        `;
    });
}
async function deleteHolding(holdingId) {
    const confirmDelete = confirm(
        "Are you sure you want to delete this asset?\nThis action cannot be undone."
    );

    if (!confirmDelete) return;

    try {
        const res = await fetch(
            `${BASE_URL}/api/holdings/${holdingId}`,
            { method: "DELETE" }
        );

        if (!res.ok) {
            throw new Error(await res.text());
        }

        showNotification("Asset deleted successfully", "success");
        loadDashboard(); // refresh UI
    } catch (err) {
        console.error("Delete failed:", err);
        showNotification("Failed to delete asset", "error");
    }
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

    // Allocation Chart (Doughnut)
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
                                return `${label}: ₹${value.toLocaleString('en-IN', {minimumFractionDigits: 2})} (${percentage}%)`;
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
                                return `P/L: ${sign}₹${value.toFixed(2)}`;
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
                                return '₹' + value.toLocaleString('en-IN');
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
    document.getElementById("stocksValue").innerText = `₹${stock.toLocaleString('en-IN', {minimumFractionDigits: 2, maximumFractionDigits: 2})}`;
    document.getElementById("mfValue").innerText = `₹${mf.toLocaleString('en-IN', {minimumFractionDigits: 2, maximumFractionDigits: 2})}`;
    document.getElementById("goldValue").innerText = `₹${gold.toLocaleString('en-IN', {minimumFractionDigits: 2, maximumFractionDigits: 2})}`;

    // Update header stats
    document.getElementById("totalValue").innerText = `₹${totalValue.toLocaleString('en-IN', {minimumFractionDigits: 2, maximumFractionDigits: 2})}`;

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
    document.getElementById('assetSelect').value = '';
    document.getElementById('quantity').value = '';
    document.getElementById('symbol').value = '';
    document.getElementById('holdingName').value = '';
    document.getElementById('assetType').value = '';
    document.getElementById('price').value = '';
    document.getElementById('currentPriceDisplay').style.display = 'none';
    document.getElementById('loadingPrice').style.display = 'none';
}

// Close modal on ESC key
document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape') {
        closeModal();
    }
});

/* ================= ASSET SELECTION ================= */

async function handleAssetSelection() {
    const select = document.getElementById('assetSelect');
    const value = select.value;

    if (!value) {
        document.getElementById('currentPriceDisplay').style.display = 'none';
        document.getElementById('loadingPrice').style.display = 'none';
        document.getElementById('price').value = '';
        return;
    }

    const [symbol, name, assetType] = value.split('|');

    // Set hidden fields
    document.getElementById('symbol').value = symbol;
    document.getElementById('holdingName').value = name;
    document.getElementById('assetType').value = assetType;

    // Show loading state
    document.getElementById('currentPriceDisplay').style.display = 'none';
    document.getElementById('loadingPrice').style.display = 'flex';

    // Fetch live price
    const currentPrice = await fetchLivePrice(symbol, assetType);

    // Hide loading state
    document.getElementById('loadingPrice').style.display = 'none';

    if (currentPrice && currentPrice > 0) {
        // Set the price in the hidden field
        document.getElementById('price').value = currentPrice;

        // Display the current price
        document.getElementById('modalCurrentPrice').textContent = `₹${currentPrice.toFixed(2)}`;
        document.getElementById('currentPriceDisplay').style.display = 'block';

        // Check if using API or default price
        if (FINNHUB_API_KEY === "YOUR_FINNHUB_API_KEY" || !FINNHUB_API_KEY) {
            // Show note about default price
            const noteElement = document.querySelector('.price-note small');
            if (noteElement) {
                noteElement.innerHTML = '⚠️ Using default price (API key not configured)';
                noteElement.style.color = '#f59e0b';
            }
        }
    } else {
        // If price fetch fails completely, show error
        showNotification("Unable to fetch price for this asset. Please try again or check your API configuration.", "error");
        document.getElementById('assetSelect').value = '';
        console.error(`Failed to get price for ${symbol}`);
    }
}

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
        const symbol = document.getElementById("symbol").value.trim();
        const name = document.getElementById("holdingName").value.trim();
        const assetType = document.getElementById("assetType").value;
        const quantity = Number(document.getElementById("quantity").value);
        const price = Number(document.getElementById("price").value);

        // Basic validation
        if (!symbol || !name || !assetType) {
            showNotification("Please select an asset from the dropdown", "error");
            return;
        }

        if (quantity <= 0) {
            showNotification("Please enter a valid quantity", "error");
            return;
        }

        if (!price || price <= 0) {
            showNotification("Unable to fetch price. Please try selecting the asset again.", "error");
            return;
        }

        const asset = {
            symbol: symbol,
            name: name,
            quantity: quantity,
            purchasePrice: price,
            assetType: assetType
        };

        const res = await fetch(`${BASE_URL}/api/holdings/${PORTFOLIO_ID}`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(asset)
        });

        if (!res.ok) throw new Error(await res.text());

        closeModal();
        loadDashboard();

        showNotification(`${symbol} added successfully at ₹${price.toFixed(2)}!`, "success");
    } catch (err) {
        console.error("Add asset failed:", err);
        showNotification("Failed to add asset. Please try again.", "error");
    }
}

/* ================= NOTIFICATIONS ================= */

function showNotification(message, type = "info") {
    const notification = document.createElement('div');
    notification.className = `notification notification-${type}`;
    notification.textContent = message;

    let bgColor = '#3b82f6'; // info
    if (type === 'success') bgColor = '#10b981';
    if (type === 'error') bgColor = '#ef4444';
    if (type === 'warning') bgColor = '#f59e0b';

    notification.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        padding: 1rem 1.5rem;
        background: ${bgColor};
        color: white;
        border-radius: 10px;
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
        z-index: 2000;
        animation: slideInRight 0.3s ease;
        max-width: 400px;
    `;

    document.body.appendChild(notification);

    setTimeout(() => {
        notification.style.animation = 'slideOutRight 0.3s ease';
        setTimeout(() => notification.remove(), 300);
    }, 5000);
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
    @keyframes spin {
        from { transform: rotate(0deg); }
        to { transform: rotate(360deg); }
    }
`;
document.head.appendChild(style);