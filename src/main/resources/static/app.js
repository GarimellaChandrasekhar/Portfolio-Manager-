const form = document.getElementById("goalForm");
const responseMsg = document.getElementById("responseMsg");

form.addEventListener("submit", async (e) => {
    e.preventDefault();

    const goalData = {
        goalName: document.getElementById("goalName").value,
        targetAmount: document.getElementById("targetAmount").value,
        timeHorizon: document.getElementById("timeHorizon").value,
        riskLevel: document.getElementById("riskLevel").value,
        monthlyInvestment: document.getElementById("monthlyInvestment").value || null
    };

    try {
        const response = await fetch("http://localhost:5400/api/goals", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(goalData)
        });

        if (!response.ok) {
            throw new Error("Failed to create goal");
        }

        const data = await response.json();

        responseMsg.style.color = "green";
        responseMsg.innerText = "Goal created successfully 🎯";
        form.reset();

    } catch (error) {
        responseMsg.style.color = "red";
        responseMsg.innerText = "Error creating goal ❌";
    }
});
