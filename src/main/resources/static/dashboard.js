const PORTFOLIO_ID = 1;
const BASE_URL = "http://localhost:5400";

// ⚠️ REQUIRED: Add your Finnhub API key here
// Get free API key from: https://finnhub.io/register
const FINNHUB_API_KEY = "d61gfi1r01qufbsn69f0d61gfi1r01qufbsn69fg";

let allocationChart = null;
let plChart = null;
let priceUpdateInterval = null;
let currentInputMode = 'preset';

// Company names mapping (fallback)
const COMPANY_NAMES = {
    'AAPL': 'Apple Inc.',
    'MSFT': 'Microsoft Corporation',
    'GOOGL': 'Alphabet Inc.',
    'AMZN': 'Amazon.com Inc.',
    'META': 'Meta Platforms Inc.',
    'NVDA': 'NVIDIA Corporation',
    'TSLA': 'Tesla Inc.',
    'JPM': 'JPMorgan Chase & Co.',
    'V': 'Visa Inc.',
    'MA': 'Mastercard Inc.',
    'BAC': 'Bank of America Corp.',
    'JNJ': 'Johnson & Johnson',
    'UNH': 'UnitedHealth Group',
    'PFE': 'Pfizer Inc.',
    'WMT': 'Walmart Inc.',
    'PG': 'Procter & Gamble Co.',
    'KO': 'The Coca-Cola Company',
    'NKE': 'Nike Inc.'
};

document.addEventListener("DOMContentLoaded", () => {
    checkAPIKey();
    loadDashboard();
    loadNewsWidget();

    // Update prices every 60 seconds
    priceUpdateInterval = setInterval(() => refreshPrices(), 60000);
});

window.addEventListener('beforeunload', () => {
    if (priceUpdateInterval) clearInterval(priceUpdateInterval);
});

function checkAPIKey() {
    if (!FINNHUB_API_KEY || FINNHUB_API_KEY === "YOUR_FINNHUB_API_KEY") {
        showNotification("⚠️ Finnhub API key not configured! Add your key in dashboard.js line 7", "warning");
        console.error("═".repeat(60));
        console.error("⚠️  FINNHUB API KEY REQUIRED");
        console.error("═".repeat(60));
        console.error("Get your free API key:");
        console.error("1. Visit: https://finnhub.io/register");
        console.error("2. Sign up and copy your API key");
        console.error("3. Open dashboard.js and replace line 7");
        console.error("4. Refresh the page");
        console.error("═".repeat(60));
    }
}

/* ================= MODE SWITCHING ================= */

function switchToPreset() {
    currentInputMode = 'preset';
    document.getElementById('presetMode').style.display = 'block';
    document.getElementById('customMode').style.display = 'none';
    document.getElementById('presetBtn').classList.add('active');
    document.getElementById('customBtn').classList.remove('active');
    resetFormFields();
}

function switchToCustom() {
    currentInputMode = 'custom';
    document.getElementById('presetMode').style.display = 'none';
    document.getElementById('customMode').style.display = 'block';
    document.getElementById('presetBtn').classList.remove('active');
    document.getElementById('customBtn').classList.add('active');
    resetFormFields();
}

function resetFormFields() {
    document.getElementById('assetSelect').value = '';
    document.getElementById('customTicker').value = '';
    document.getElementById('symbol').value = '';
    document.getElementById('holdingName').value = '';
    document.getElementById('price').value = '';
    document.getElementById('quantity').value = '';
    document.getElementById('currentPriceDisplay').style.display = 'none';
    document.getElementById('tickerInfoDisplay').style.display = 'none';
    document.getElementById('loadingPrice').style.display = 'none';
}

/* ================= FINNHUB API FUNCTIONS ================= */

async function fetchStockQuote(symbol) {
    try {
        if (!FINNHUB_API_KEY || FINNHUB_API_KEY === "YOUR_FINNHUB_API_KEY") {
            throw new Error("Finnhub API key not configured");
        }

        console.log(`Fetching quote for ${symbol}...`);

        const url = `https://finnhub.io/api/v1/quote?symbol=${encodeURIComponent(symbol)}&token=${FINNHUB_API_KEY}`;
        const response = await fetch(url);

        if (!response.ok) {
            throw new Error(`Finnhub API returned ${response.status}`);
        }

        const data = await response.json();
        console.log('Finnhub Quote Response:', data);

        if (data.c && data.c > 0) {
            return {
                currentPrice: data.c,
                previousClose: data.pc,
                high: data.h,
                low: data.l,
                open: data.o
            };
        }

        throw new Error('No price data available');
    } catch (error) {
        console.error(`Error fetching quote for ${symbol}:`, error);
        throw error;
    }
}

async function fetchStockProfile(symbol) {
    try {
        if (!FINNHUB_API_KEY || FINNHUB_API_KEY === "YOUR_FINNHUB_API_KEY") {
            return {
                name: COMPANY_NAMES[symbol] || symbol,
                ticker: symbol
            };
        }

        console.log(`Fetching profile for ${symbol}...`);

        const url = `https://finnhub.io/api/v1/stock/profile2?symbol=${encodeURIComponent(symbol)}&token=${FINNHUB_API_KEY}`;
        const response = await fetch(url);

        if (!response.ok) {
            throw new Error(`Finnhub API returned ${response.status}`);
        }

        const data = await response.json();
        console.log('Finnhub Profile Response:', data);

        return {
            name: data.name || COMPANY_NAMES[symbol] || symbol,
            ticker: data.ticker || symbol,
            exchange: data.exchange || 'US'
        };
    } catch (error) {
        console.error(`Error fetching profile for ${symbol}:`, error);
        return {
            name: COMPANY_NAMES[symbol] || symbol,
            ticker: symbol
        };
    }
}

/* ================= CUSTOM TICKER FETCH ================= */

async function fetchTickerInfo() {
    const tickerInput = document.getElementById('customTicker');
    const ticker = tickerInput.value.trim().toUpperCase();

    if (!ticker) {
        showNotification("Please enter a ticker symbol", "error");
        return;
    }

    document.getElementById('loadingPrice').style.display = 'flex';
    document.getElementById('tickerInfoDisplay').style.display = 'none';
    document.getElementById('currentPriceDisplay').style.display = 'none';

    try {
        const [quote, profile] = await Promise.all([
            fetchStockQuote(ticker),
            fetchStockProfile(ticker)
        ]);

        document.getElementById('loadingPrice').style.display = 'none';

        // Display information
        document.getElementById('displaySymbol').textContent = ticker;
        document.getElementById('displayName').textContent = profile.name;
        document.getElementById('displayPrice').textContent = `$${quote.currentPrice.toFixed(2)}`;
        document.getElementById('tickerInfoDisplay').style.display = 'block';

        // Set hidden fields
        document.getElementById('symbol').value = ticker;
        document.getElementById('holdingName').value = profile.name;
        document.getElementById('price').value = quote.currentPrice;

        // Show price display
        document.getElementById('modalCurrentPrice').textContent = `$${quote.currentPrice.toFixed(2)}`;
        document.getElementById('currentPriceDisplay').style.display = 'block';

        showNotification(`✅ Successfully fetched ${ticker} data!`, "success");

    } catch (error) {
        document.getElementById('loadingPrice').style.display = 'none';
        showNotification(`❌ Unable to fetch data for ${ticker}. Verify the ticker symbol.`, "error");
        console.error('Fetch error:', error);
    }
}

/* ================= PRESET ASSET SELECTION ================= */

async function handleAssetSelection() {
    const select = document.getElementById('assetSelect');
    const symbol = select.value;

    if (!symbol) {
        document.getElementById('currentPriceDisplay').style.display = 'none';
        return;
    }

    const optionText = select.options[select.selectedIndex].text;
    const companyName = optionText.split(' - ')[1] || COMPANY_NAMES[symbol] || symbol;

    document.getElementById('symbol').value = symbol;
    document.getElementById('holdingName').value = companyName;

    document.getElementById('loadingPrice').style.display = 'flex';
    document.getElementById('currentPriceDisplay').style.display = 'none';

    try {
        const quote = await fetchStockQuote(symbol);

        document.getElementById('loadingPrice').style.display = 'none';
        document.getElementById('price').value = quote.currentPrice;
        document.getElementById('modalCurrentPrice').textContent = `$${quote.currentPrice.toFixed(2)}`;
        document.getElementById('currentPriceDisplay').style.display = 'block';

    } catch (error) {
        document.getElementById('loadingPrice').style.display = 'none';
        showNotification("❌ Unable to fetch price. Check your API key.", "error");
        console.error(error);
    }
}

/* ================= NEWS WIDGET ================= */

async function fetchNewsForWidget() {
    try {
        if (!FINNHUB_API_KEY || FINNHUB_API_KEY === "YOUR_FINNHUB_API_KEY") {
            return [];
        }

        const response = await fetch(
            `https://finnhub.io/api/v1/news?category=general&token=${FINNHUB_API_KEY}`
        );

        if (!response.ok) throw new Error('Failed to fetch news');

        const data = await response.json();
        return data.slice(0, 5);
    } catch (error) {
        console.error("Error fetching news:", error);
        return [];
    }
}

async function loadNewsWidget() {
    const newsContainer = document.getElementById('dashboardNewsWidget');
    if (!newsContainer) return;

    try {
        const news = await fetchNewsForWidget();

        if (news.length === 0) {
            newsContainer.innerHTML = `
                <div class="news-widget-empty">
                    <span class="empty-icon">📰</span>
                    <p>No news available. Configure your Finnhub API key to see market news.</p>
                </div>
            `;
            return;
        }

        let newsHTML = '';
        news.forEach((article, index) => {
            let dateDisplay = '';
            if (article.datetime) {
                const date = new Date(article.datetime * 1000);
                const diffHours = Math.floor((Date.now() - date) / (1000 * 60 * 60));

                if (diffHours < 1) dateDisplay = 'Just now';
                else if (diffHours < 24) dateDisplay = `${diffHours}h ago`;
                else dateDisplay = `${Math.floor(diffHours / 24)}d ago`;
            }

            newsHTML += `
                <div class="news-widget-item" style="animation-delay: ${index * 0.1}s">
                    <div class="news-widget-meta">
                        <span class="news-widget-time">${dateDisplay}</span>
                        <span class="news-widget-source">${article.source || 'Unknown'}</span>
                    </div>
                    <h4 class="news-widget-title">${article.headline}</h4>
                    <p class="news-widget-summary">${article.summary || "No description available"}</p>
                    <a href="${article.url}" target="_blank" rel="noopener noreferrer" class="news-widget-link">
                        Read more →
                    </a>
                </div>
            `;
        });

        newsContainer.innerHTML = newsHTML;

    } catch (error) {
        console.error("Error loading news widget:", error);
        newsContainer.innerHTML = `
            <div class="news-widget-error">
                <span class="error-icon">⚠️</span>
                <p>Failed to load news</p>
            </div>
        `;
    }
}

/* ================= LIVE PRICE UPDATES ================= */

async function updateLivePrices(holdings) {
    const updatedHoldings = [];

    for (const holding of holdings) {
        try {
            const quote = await fetchStockQuote(holding.symbol);
            updatedHoldings.push({
                ...holding,
                currentPrice: quote.currentPrice
            });
        } catch (error) {
            console.error(`Failed to update price for ${holding.symbol}`);
            updatedHoldings.push({
                ...holding,
                currentPrice: holding.purchasePrice
            });
        }
    }

    return updatedHoldings;
}

/* ================= DASHBOARD FUNCTIONS ================= */

async function loadDashboard() {
    try {
        updatePriceStatus('loading');

        const res = await fetch(`${BASE_URL}/api/holdings/${PORTFOLIO_ID}`);
        if (!res.ok) throw new Error("Failed to fetch holdings");

        let holdings = await res.json();
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
        holdings = await updateLivePrices(holdings);

        renderTable(holdings);
        renderCharts(holdings);
        updateSummary(holdings);
        updateLastUpdatedTime();
        updatePriceStatus('active');

        showNotification("✅ Prices updated successfully!", "success");
    } catch (err) {
        console.error("Refresh error:", err);
        updatePriceStatus('error');
        showNotification("❌ Failed to update prices", "error");
    }
}

function updatePriceStatus(status) {
    const statusIndicator = document.getElementById('priceStatus');
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
    const timeString = now.toLocaleTimeString('en-US', {
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
                <td colspan="9">
                    <div class="empty-message">
                        <span class="empty-icon">📊</span>
                        <p>No stocks yet. Add your first US stock to get started!</p>
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
                <td>${h.quantity.toLocaleString('en-US')}</td>
                <td>$${h.purchasePrice.toFixed(2)}</td>
                <td>$${price.toFixed(2)}</td>
                <td><strong>$${value.toLocaleString('en-US', {minimumFractionDigits: 2})}</strong></td>
                <td class="${plClass}">
                    <strong>${plSign}$${Math.abs(pl).toFixed(2)}</strong>
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
    if (!confirm("Are you sure you want to delete this stock?")) return;

    try {
        const res = await fetch(`${BASE_URL}/api/holdings/${holdingId}`, {
            method: "DELETE"
        });

        if (!res.ok) throw new Error(await res.text());

        showNotification("✅ Stock deleted successfully", "success");
        loadDashboard();
    } catch (err) {
        console.error("Delete failed:", err);
        showNotification("❌ Failed to delete stock", "error");
    }
}

/* ================= CHARTS ================= */

function renderCharts(holdings) {
    const labels = [];
    const values = [];
    const plValues = [];
    const plColors = [];
    const chartColors = ['#8b5cf6', '#3b82f6', '#10b981', '#f59e0b', '#ef4444', '#ec4899', '#14b8a6', '#f97316'];

    holdings.forEach((h, index) => {
        const price = h.currentPrice ?? h.purchasePrice;
        const value = h.quantity * price;
        const pl = value - (h.quantity * h.purchasePrice);

        labels.push(h.symbol);
        values.push(value);
        plValues.push(pl);
        plColors.push(pl >= 0 ? '#10b981' : '#ef4444');
    });

    if (allocationChart) allocationChart.destroy();
    if (plChart) plChart.destroy();

    const commonOptions = {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
            legend: {
                labels: {
                    font: {
                        size: 13,
                        family: "'Plus Jakarta Sans', sans-serif",
                        weight: 600
                    },
                    padding: 15,
                    usePointStyle: true
                }
            }
        }
    };

    allocationChart = new Chart(document.getElementById("allocationChart"), {
        type: "doughnut",
        data: {
            labels: labels,
            datasets: [{
                data: values,
                backgroundColor: chartColors,
                borderWidth: 0,
                hoverOffset: 10
            }]
        },
        options: {
            ...commonOptions,
            cutout: '65%',
            plugins: {
                ...commonOptions.plugins,
                legend: { ...commonOptions.plugins.legend, position: 'bottom' },
                tooltip: {
                    callbacks: {
                        label: function(context) {
                            const value = context.parsed || 0;
                            const total = context.dataset.data.reduce((a, b) => a + b, 0);
                            const percentage = ((value / total) * 100).toFixed(1);
                            return `${context.label}: $${value.toLocaleString('en-US', {minimumFractionDigits: 2})} (${percentage}%)`;
                        }
                    }
                }
            }
        }
    });

    plChart = new Chart(document.getElementById("performanceChart"), {
        type: "bar",
        data: {
            labels,
            datasets: [{
                label: 'Profit/Loss',
                data: plValues,
                backgroundColor: plColors,
                borderRadius: 8
            }]
        },
        options: {
            ...commonOptions,
            plugins: {
                ...commonOptions.plugins,
                legend: { display: false },
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
                    ticks: {
                        callback: v => '$' + v.toLocaleString('en-US'),
                        font: { family: "'Plus Jakarta Sans', sans-serif" }
                    }
                },
                x: {
                    ticks: { font: { family: "'Plus Jakarta Sans', sans-serif" } }
                }
            }
        }
    });
}

/* ================= SUMMARY ================= */

function updateSummary(holdings) {
    let totalValue = 0, totalCost = 0;

    holdings.forEach(h => {
        const price = h.currentPrice ?? h.purchasePrice;
        const value = h.quantity * price;
        const cost = h.quantity * h.purchasePrice;
        totalValue += value;
        totalCost += cost;
    });

    const totalPL = totalValue - totalCost;
    const totalReturn = totalCost > 0 ? ((totalValue - totalCost) / totalCost) * 100 : 0;

    document.getElementById("stocksValue").innerText = `$${totalValue.toLocaleString('en-US', {minimumFractionDigits: 2})}`;

    const plElement = document.getElementById("totalPL");
    const plSign = totalPL >= 0 ? '+' : '';
    plElement.innerText = `${plSign}$${Math.abs(totalPL).toLocaleString('en-US', {minimumFractionDigits: 2})}`;
    plElement.className = totalPL >= 0 ? 'summary-value positive' : 'summary-value negative';

    document.getElementById("holdingsCount").innerText = holdings.length;
    document.getElementById("totalValue").innerText = `$${totalValue.toLocaleString('en-US', {minimumFractionDigits: 2})}`;

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
    resetFormFields();
}

document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape') closeModal();
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

/* ================= SUBMIT ASSET ================= */

async function submitAsset() {
    try {
        const symbol = document.getElementById("symbol").value.trim();
        const name = document.getElementById("holdingName").value.trim();
        const quantity = Number(document.getElementById("quantity").value);
        const price = Number(document.getElementById("price").value);

        if (!symbol || !name) {
            showNotification("⚠️ Please select or fetch a stock first", "warning");
            return;
        }

        if (quantity <= 0 || !price || price <= 0) {
            showNotification("⚠️ Please enter valid number of shares", "warning");
            return;
        }

        const asset = {
            symbol,
            name,
            quantity,
            purchasePrice: price,
            assetType: 'STOCK'
        };

        const res = await fetch(`${BASE_URL}/api/holdings/${PORTFOLIO_ID}`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(asset)
        });

        if (!res.ok) throw new Error(await res.text());

        closeModal();
        loadDashboard();
        showNotification(`✅ ${symbol} added at $${price.toFixed(2)}!`, "success");
    } catch (err) {
        console.error("Add asset failed:", err);
        showNotification("❌ Failed to add stock. Please try again.", "error");
    }
}

/* ================= NOTIFICATIONS ================= */

function showNotification(message, type = "info") {
    const existingNotifications = document.querySelectorAll('.notification');
    existingNotifications.forEach(n => n.remove());

    const notification = document.createElement('div');
    notification.className = `notification notification-${type}`;
    notification.textContent = message;

    let bgColor = '#3b82f6', borderColor = '#2563eb';
    if (type === 'success') { bgColor = '#10b981'; borderColor = '#059669'; }
    if (type === 'error') { bgColor = '#ef4444'; borderColor = '#dc2626'; }
    if (type === 'warning') { bgColor = '#f59e0b'; borderColor = '#d97706'; }

    notification.style.cssText = `
        position: fixed; top: 24px; right: 24px; padding: 16px 24px;
        background: ${bgColor}; color: white; border-radius: 12px;
        border-left: 4px solid ${borderColor};
        box-shadow: 0 8px 24px rgba(0,0,0,0.15); z-index: 2000;
        animation: slideInRight 0.3s ease; max-width: 420px;
        font-size: 14px; font-weight: 600;
        font-family: 'Plus Jakarta Sans', sans-serif;
    `;

    document.body.appendChild(notification);
    setTimeout(() => {
        notification.style.animation = 'slideOutRight 0.3s ease';
        setTimeout(() => notification.remove(), 300);
    }, 5000);
}

/* ================= STYLES ================= */

const style = document.createElement('style');
style.textContent = `
    @keyframes slideInRight {
        from { transform: translateX(400px); opacity: 0; }
        to { transform: translateX(0); opacity: 1; }
    }
    @keyframes slideOutRight {
        from { transform: translateX(0); opacity: 1; }
        to { transform: translateX(400px); opacity: 0; }
    }

    /* Mode Toggle */
    .input-mode-toggle {
        display: grid;
        grid-template-columns: 1fr 1fr;
        gap: 0.5rem;
        background: var(--bg-tertiary, #f1f5f9);
        padding: 0.25rem;
        border-radius: 12px;
        margin-bottom: 1rem;
    }

    .toggle-btn {
        padding: 0.75rem 1rem;
        border: none;
        background: transparent;
        color: var(--text-secondary, #475569);
        font-weight: 600;
        font-size: 0.9rem;
        border-radius: 10px;
        cursor: pointer;
        transition: all 0.2s ease;
    }

    .toggle-btn:hover { background: rgba(59, 130, 246, 0.1); }

    .toggle-btn.active {
        background: var(--bg-secondary, #ffffff);
        color: var(--accent-primary, #3b82f6);
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    }

    /* Ticker Input */
    .ticker-input-group {
        display: flex;
        gap: 0.5rem;
    }

    .ticker-input-group input { flex: 1; }

    .btn-fetch {
        padding: 0.875rem 1.5rem;
        background: var(--accent-primary, #3b82f6);
        color: white;
        border: none;
        border-radius: 10px;
        font-weight: 600;
        cursor: pointer;
        transition: all 0.2s ease;
        white-space: nowrap;
    }

    .btn-fetch:hover {
        background: var(--accent-hover, #2563eb);
        transform: translateY(-1px);
    }

    .helper-text {
        display: block;
        margin-top: 0.5rem;
        color: var(--text-tertiary, #94a3b8);
        font-size: 0.8rem;
    }

    /* Ticker Info Card */
    .ticker-info-card {
        background: var(--bg-tertiary, #f1f5f9);
        padding: 1.25rem;
        border-radius: 12px;
        margin-top: 1rem;
        border: 2px solid var(--accent-primary, #3b82f6);
    }

    .info-row {
        display: flex;
        justify-content: space-between;
        padding: 0.75rem 0;
        border-bottom: 1px solid var(--border-color, #e2e8f0);
    }

    .info-row:last-child { border-bottom: none; }

    .info-label {
        font-size: 0.875rem;
        color: var(--text-secondary, #475569);
        font-weight: 600;
    }

    .info-value {
        font-size: 0.95rem;
        color: var(--text-primary, #0f172a);
        font-weight: 600;
    }

    .price-highlight {
        color: var(--success, #10b981);
        font-size: 1.1rem !important;
    }

    /* News Widget Styles */
    .news-widget-container {
        display: flex;
        flex-direction: column;
        gap: 1rem;
        max-height: 600px;
        overflow-y: auto;
    }

    .news-widget-item {
        padding: 1.25rem;
        border: 1px solid var(--border-color, #e2e8f0);
        border-radius: 10px;
        transition: all 0.3s ease;
        animation: fadeInUp 0.5s ease forwards;
        opacity: 0;
    }

    @keyframes fadeInUp {
        from { opacity: 0; transform: translateY(10px); }
        to { opacity: 1; transform: translateY(0); }
    }

    .news-widget-item:hover {
        border-color: var(--accent-primary, #3b82f6);
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
        transform: translateX(4px);
    }

    .loading-spinner-small {
        width: 40px;
        height: 40px;
        border: 3px solid var(--border-color, #e2e8f0);
        border-top-color: var(--accent-primary, #3b82f6);
        border-radius: 50%;
        animation: spin 1s linear infinite;
    }

    @keyframes spin { to { transform: rotate(360deg); } }
`;
document.head.appendChild(style);