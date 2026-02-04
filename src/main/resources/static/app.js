const form = document.getElementById("goalForm");
const responseMsg = document.getElementById("responseMsg");
const resultCard = document.getElementById("resultCard");

// Form submission handler
form.addEventListener("submit", async (e) => {
    e.preventDefault();

    // Get form values
    const goalData = {
        goalName: document.getElementById("goalName").value,
        targetAmount: document.getElementById("targetAmount").value,
        timeHorizon: document.getElementById("timeHorizon").value,
        riskLevel: document.getElementById("riskLevel").value,
        monthlyInvestment: document.getElementById("monthlyInvestment").value
    };

    // Show loading state
    const submitButton = form.querySelector('button[type="submit"]');
    const originalButtonText = submitButton.innerHTML;
    submitButton.innerHTML = '<span>Creating Plan...</span>';
    submitButton.disabled = true;

    try {
        const response = await fetch("http://localhost:5400/api/goals", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(goalData)
        });

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const data = await response.json();

        // Populate result card
        document.getElementById("resGoal").innerText = data.goalName;
        document.getElementById("resMonthly").innerText = formatNumber(data.monthlyInvestment);

        // Render allocations
        const allocationsDiv = document.getElementById("allocations");
        allocationsDiv.innerHTML = "";

        if (data.allocations && data.allocations.length > 0) {
            data.allocations.forEach(allocation => {
                const allocationEl = createAllocationElement(allocation);
                allocationsDiv.appendChild(allocationEl);
            });
        }

        // Show result card with animation
        resultCard.classList.remove("hidden");
        resultCard.scrollIntoView({ behavior: 'smooth', block: 'nearest' });

        // Show success message
        showMessage("Investment plan created successfully! 🎯", "success");

        // Reset form
        form.reset();

    } catch (error) {
        console.error("Error creating goal:", error);
        showMessage("Error creating investment plan. Please try again.", "error");
    } finally {
        // Reset button state
        submitButton.innerHTML = originalButtonText;
        submitButton.disabled = false;
    }
});

// Helper function to create allocation element
function createAllocationElement(allocation) {
    const div = document.createElement("div");
    div.className = "allocation";

    div.innerHTML = `
        <div class="allocation-info">
            <span class="allocation-type">${getAssetIcon(allocation.assetType)} ${allocation.assetType}</span>
            <span class="allocation-percentage">${allocation.percentage}% allocation</span>
        </div>
        <span class="allocation-amount">₹${formatNumber(allocation.sipAmount)}</span>
    `;

    return div;
}

// Helper function to get asset icon
function getAssetIcon(assetType) {
    const icons = {
        'EQUITY': '📈',
        'DEBT': '💰',
        'GOLD': '🪙',
        'STOCKS': '📊',
        'BONDS': '📄',
        'MUTUAL_FUNDS': '🏦',
        'SIP': '💵'
    };

    return icons[assetType.toUpperCase()] || '💼';
}

// Helper function to format numbers with commas
function formatNumber(num) {
    return Number(num).toLocaleString('en-IN');
}

// Helper function to show messages
function showMessage(message, type) {
    responseMsg.textContent = message;
    responseMsg.className = `response-message ${type}`;

    // Auto-hide message after 5 seconds
    setTimeout(() => {
        responseMsg.className = 'response-message';
    }, 5000);
}

// Add input formatting for currency fields
const currencyInputs = ['targetAmount', 'monthlyInvestment'];
currencyInputs.forEach(id => {
    const input = document.getElementById(id);
    if (input) {
        input.addEventListener('blur', (e) => {
            if (e.target.value) {
                // You can add additional formatting here if needed
                e.target.value = Math.abs(parseInt(e.target.value) || 0);
            }
        });
    }
});

// Add input validation for time horizon
const timeHorizonInput = document.getElementById('timeHorizon');
if (timeHorizonInput) {
    timeHorizonInput.addEventListener('input', (e) => {
        if (e.target.value < 0) {
            e.target.value = 0;
        }
    });
}