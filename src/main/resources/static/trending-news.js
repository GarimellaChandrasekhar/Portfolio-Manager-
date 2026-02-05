const FINNHUB_API_KEY = "d61ksk1r01qgcobptr8gd61ksk1r01qgcobptr90"; // Replace with your actual API key

document.addEventListener("DOMContentLoaded", () => {
    // Load saved theme
    loadTheme();

    // Fetch trending news when the page loads
    loadTrendingNews();

    // Close sidebar when clicking outside on mobile
    document.addEventListener('click', (e) => {
        const sidebar = document.getElementById('sidebar');
        const sidebarToggle = document.getElementById('sidebarToggle');

        if (window.innerWidth <= 768 &&
            sidebar.classList.contains('active') &&
            !sidebar.contains(e.target) &&
            e.target !== sidebarToggle) {
            closeSidebar();
        }
    });
});

// Fetch live trending stock news from Finnhub
async function fetchTrendingStockNews() {
    try {
        const response = await fetch(
            `https://finnhub.io/api/v1/news?category=general&token=${FINNHUB_API_KEY}`
        );

        if (!response.ok) {
            console.error(`API request failed with status: ${response.status}`);
            throw new Error('Failed to fetch stock news from Finnhub');
        }

        const data = await response.json();
        console.log(`Fetched ${data.length} news articles`);
        return data;
    } catch (error) {
        console.error("Error fetching trending stock news:", error);
        return []; // return an empty array if there's an error
    }
}

// Load trending news into the page
async function loadTrendingNews() {
    const newsContainer = document.getElementById("trendingNewsList");
    newsContainer.innerHTML = '<div class="loading-state">Loading trending news...</div>';

    try {
        // Fetch the trending news
        const news = await fetchTrendingStockNews();

        if (news.length === 0) {
            newsContainer.innerHTML = `
                <div class="empty-state">
                    <div style="font-size: 3rem; margin-bottom: 1rem;">📰</div>
                    <h3>No news available at the moment</h3>
                    <p>Please check back later or refresh the page.</p>
                    <button class="btn-retry" onclick="loadTrendingNews()">Refresh News</button>
                </div>
            `;
            return;
        }

        // Clear the loading text
        newsContainer.innerHTML = "";

        // Render each news article with animation delay
        news.forEach((article, index) => {
            const newsItem = document.createElement("div");
            newsItem.classList.add("news-item");
            newsItem.style.animationDelay = `${index * 0.05}s`;

            // Format the date if available
            let dateDisplay = '';
            if (article.datetime) {
                const date = new Date(article.datetime * 1000);
                const now = new Date();
                const diffHours = Math.floor((now - date) / (1000 * 60 * 60));

                if (diffHours < 1) {
                    dateDisplay = 'Just now';
                } else if (diffHours < 24) {
                    dateDisplay = `${diffHours}h ago`;
                } else {
                    const diffDays = Math.floor(diffHours / 24);
                    dateDisplay = `${diffDays}d ago`;
                }
            }

            // Get the source name
            const source = article.source || 'Unknown Source';

            // Build the news item HTML
            let newsHTML = '';

            // Add metadata if available
            if (dateDisplay) {
                newsHTML += `<div class="news-meta">${dateDisplay} • ${source}</div>`;
            }

            newsHTML += `
                <h4 class="news-title">${article.headline}</h4>
                <p class="news-description">${article.summary || "No description available"}</p>
                <a href="${article.url}" target="_blank" class="news-link" rel="noopener noreferrer">Read full article</a>
            `;

            newsItem.innerHTML = newsHTML;
            newsContainer.appendChild(newsItem);
        });

    } catch (error) {
        console.error("Error loading news:", error);
        newsContainer.innerHTML = `
            <div class="error-state">
                <div style="font-size: 3rem; margin-bottom: 1rem;">⚠️</div>
                <h3>Failed to load news</h3>
                <p>Please check your internet connection and try again.</p>
                <button class="btn-retry" onclick="loadTrendingNews()">Retry</button>
            </div>
        `;
    }
}

// Theme toggle functionality
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

// Load saved theme on page load
function loadTheme() {
    const savedTheme = localStorage.getItem('theme') || 'light';
    const themeIcon = document.querySelector('.theme-icon');

    document.body.classList.remove('light', 'dark');
    document.body.classList.add(savedTheme);

    if (themeIcon) {
        themeIcon.textContent = savedTheme === 'dark' ? '☀️' : '🌙';
    }
}

// Sidebar toggle functionality (for mobile)
function toggleSidebar() {
    const sidebar = document.getElementById('sidebar');
    const backdrop = document.getElementById('sidebarBackdrop');

    sidebar.classList.toggle('active');
    backdrop.classList.toggle('active');

    // Prevent body scroll when sidebar is open
    if (sidebar.classList.contains('active')) {
        document.body.style.overflow = 'hidden';
    } else {
        document.body.style.overflow = '';
    }
}

function closeSidebar() {
    const sidebar = document.getElementById('sidebar');
    const backdrop = document.getElementById('sidebarBackdrop');

    sidebar.classList.remove('active');
    backdrop.classList.remove('active');
    document.body.style.overflow = '';
}

// Close sidebar on ESC key
document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape') {
        closeSidebar();
    }
});

// Handle window resize
let resizeTimer;
window.addEventListener('resize', () => {
    clearTimeout(resizeTimer);
    resizeTimer = setTimeout(() => {
        if (window.innerWidth > 768) {
            closeSidebar();
        }
    }, 250);
});