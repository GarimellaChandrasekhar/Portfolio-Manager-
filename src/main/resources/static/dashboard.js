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

    holdings.forEach(h => {
        const price = h.currentPrice ?? h.purchasePrice;
        const value = h.quantity * price;
        const pl = value - (h.quantity * h.purchasePrice);

        tbody.innerHTML += `
            <tr>
                <td>${h.symbol}</td>
                <td>${h.name}</td>
                <td>${h.quantity}</td>
                <td>$${h.purchasePrice}</td>
                <td>$${price}</td>
                <td>$${value.toFixed(2)}</td>
                <td class="${pl >= 0 ? "positive" : "negative"}">
                    ${pl.toFixed(2)}
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

    holdings.forEach(h => {
        const price = h.currentPrice ?? h.purchasePrice;
        const value = h.quantity * price;
        const pl = value - (h.quantity * h.purchasePrice);

        if (h.assetType === "STOCK") stock += value;
        if (h.assetType === "MUTUAL_FUND") mf += value;
        if (h.assetType === "GOLD") gold += value;

        labels.push(h.symbol);
        plValues.push(pl);
    });

    if (allocationChart) allocationChart.destroy();
    if (plChart) plChart.destroy();

    allocationChart = new Chart(
        document.getElementById("allocationChart"),
        {
            type: "pie",
            data: {
                labels: ["Stocks", "Mutual Funds", "Gold"],
                datasets: [{ data: [stock, mf, gold] }]
            }
        }
    );

    plChart = new Chart(
        document.getElementById("performanceChart"),
        {
            type: "bar",
            data: {
                labels,
                datasets: [{ data: plValues }]
            }
        }
    );
}

/* ================= SUMMARY ================= */

function updateSummary(holdings) {
    let stock = 0, mf = 0, gold = 0;

    holdings.forEach(h => {
        const price = h.currentPrice ?? h.purchasePrice;
        const value = h.quantity * price;

        if (h.assetType === "STOCK") stock += value;
        if (h.assetType === "MUTUAL_FUND") mf += value;
        if (h.assetType === "GOLD") gold += value;
    });

    document.getElementById("stocksValue").innerText = `$${stock.toFixed(2)}`;
    document.getElementById("mfValue").innerText = `$${mf.toFixed(2)}`;
    document.getElementById("goldValue").innerText = `$${gold.toFixed(2)}`;
}

/* ================= MODAL ================= */

function openModal() {
    document.getElementById("addAssetModal").classList.remove("hidden");
}

function closeModal() {
    document.getElementById("addAssetModal").classList.add("hidden");
}

/* ================= THEME ================= */

function toggleTheme() {
    document.body.classList.toggle("dark");
    document.body.classList.toggle("light");
}

/* ================= ADD ASSET ================= */

async function submitAsset() {
    try {
        const asset = {
            symbol: document.getElementById("symbol").value.trim(),
            name: document.getElementById("holdingName").value.trim(),
            quantity: Number(document.getElementById("quantity").value),
            purchasePrice: Number(document.getElementById("price").value),
            assetType: document.getElementById("assetType").value
        };

        const res = await fetch(`${BASE_URL}/api/holdings/${PORTFOLIO_ID}`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(asset)
        });

        if (!res.ok) throw new Error(await res.text());

        closeModal();
        loadDashboard();
    } catch (err) {
        console.error("Add asset failed:", err);
        alert("Failed to add asset");
    }
}
