/* ================= CONFIG ================= */

const PORTFOLIO_ID = 1;
const BASE_URL = "http://localhost:5400";

let allocationChart = null;
let plChart = null;

document.addEventListener("DOMContentLoaded", loadDashboard);

/* ================= LOAD DASHBOARD ================= */

async function loadDashboard() {
    try {
        const res = await fetch(`${BASE_URL}/api/holdings/${PORTFOLIO_ID}`);

        if (!res.ok) {
            throw new Error("Failed to fetch holdings");
        }

        const holdings = await res.json();

        renderTable(holdings);
        renderCharts(holdings);
        updateSummary(holdings);

    } catch (err) {
        console.error("Dashboard load error:", err);
    }
}

/* ================= TABLE ================= */

function renderTable(holdings) {
    const tbody = document.getElementById("holdingsTableBody");
    tbody.innerHTML = "";

    holdings.forEach(h => {
        const price = h.currentPrice ?? h.purchasePrice;
        const marketValue = h.quantity * price;
        const investment = h.quantity * h.purchasePrice;
        const pl = marketValue - investment;

        tbody.innerHTML += `
            <tr>
                <td>${h.symbol}</td>
                <td>${h.name}</td>
                <td>${h.quantity}</td>
                <td>$${h.purchasePrice.toFixed(2)}</td>
                <td>$${price.toFixed(2)}</td>
                <td>$${marketValue.toFixed(2)}</td>
                <td class="${pl >= 0 ? "positive" : "negative"}">
                    ${pl >= 0 ? "+" : ""}${pl.toFixed(2)}
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
                datasets: [{
                    data: [stock, mf, gold],
                    backgroundColor: ["#3498db", "#2ecc71", "#f1c40f"]
                }]
            }
        }
    );

    plChart = new Chart(
        document.getElementById("performanceChart"),
        {
            type: "bar",
            data: {
                labels,
                datasets: [{
                    label: "Profit / Loss",
                    data: plValues,
                    backgroundColor: plValues.map(v =>
                        v >= 0 ? "#2ecc71" : "#e74c3c"
                    )
                }]
            },
            options: {
                scales: {
                    y: { beginAtZero: true }
                }
            }
        }
    );
}

/* ================= SUMMARY ================= */

function updateSummary(holdings) {
    let totalValue = 0;
    let stock = 0, mf = 0, gold = 0;

    holdings.forEach(h => {
        const price = h.currentPrice ?? h.purchasePrice;
        const value = h.quantity * price;
        totalValue += value;

        if (h.assetType === "STOCK") stock += value;
        if (h.assetType === "MUTUAL_FUND") mf += value;
        if (h.assetType === "GOLD") gold += value;
    });

    document.getElementById("totalValue").innerText =
        `$${totalValue.toFixed(2)}`;

    document.getElementById("stocksValue").innerText =
        `$${stock.toFixed(2)}`;

    document.getElementById("mfValue").innerText =
        `$${mf.toFixed(2)}`;

    document.getElementById("goldValue").innerText =
        `$${gold.toFixed(2)}`;
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
    const symbol = document.getElementById("symbol").value.trim();
    const name = document.getElementById("holdingName").value.trim();
    const quantity = Number(document.getElementById("quantity").value);
    const purchasePrice = Number(document.getElementById("price").value);
    const assetType = document.getElementById("assetType").value;

    if (!symbol || !name || quantity <= 0 || purchasePrice <= 0) {
        alert("Please fill all fields correctly.");
        return;
    }

    const asset = {
        symbol,
        name,
        quantity,
        purchasePrice,
        assetType
    };

    const res = await fetch(
        `${BASE_URL}/api/holdings/${PORTFOLIO_ID}`,
        {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(asset)
        }
    );

    if (!res.ok) {
        console.error("Backend error:", await res.text());
        alert("Failed to add asset");
        return;
    }

    closeModal();
    loadDashboard();
}


    } catch (err) {
        console.error("Add asset failed:", err);
        alert("Failed to add asset. Check console.");
    }
}

}
