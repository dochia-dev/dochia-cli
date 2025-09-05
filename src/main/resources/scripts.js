// Dochiar API Security Analysis Report - JavaScript Functions
// Consolidated script file for both landing page and test detail pages

// ==========================================
// THEME MANAGEMENT
// ==========================================

/**
 * Toggle between light and dark themes
 */
function toggleTheme() {
    const body = document.body;
    const isDark = body.classList.contains('dark-theme');
    
    if (isDark) {
        body.classList.remove('dark-theme');
        localStorage.setItem('theme', 'light');
    } else {
        body.classList.add('dark-theme');
        localStorage.setItem('theme', 'dark');
    }
}

/**
 * Initialize theme on page load
 */
function initializeTheme() {
    const savedTheme = localStorage.getItem('theme');
    if (savedTheme === 'dark') {
        document.body.classList.add('dark-theme');
    }
}

// ==========================================
// CHART INITIALIZATION (Landing Page)
// ==========================================

/**
 * Initialize the doughnut chart on the landing page
 */
function initializeChart() {
    const chartElement = document.getElementById('heroChart');
    if (!chartElement) return; // Chart not present on this page
    
    const ctx = chartElement.getContext('2d');
    new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: ['Success', 'Warnings', 'Errors'],
            datasets: [{
                data: [totalScs, totalWrn, totalErr],
                backgroundColor: ['#10b981', '#f59e0b', '#ef4444'],
                borderWidth: 0,
                cutout: '70%'
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: { legend: { display: false } }
        }
    });
}

// ==========================================
// FILTERING FUNCTIONALITY (Landing Page)
// ==========================================

/**
 * Initialize filtering functionality for test results table
 */
function initializeFiltering() {
    const filterButtons = document.querySelectorAll('.filter-btn');
    const tableRows = document.querySelectorAll('.results-table tbody tr');
    
    if (filterButtons.length === 0) return; // Not on landing page
    
    filterButtons.forEach(button => {
        button.addEventListener('click', function() {
            const filterType = this.getAttribute('data-filter');
            
            // Update active button
            filterButtons.forEach(btn => btn.classList.remove('active'));
            this.classList.add('active');
            
            // Filter table rows
            filterTableRows(filterType, tableRows);
        });
    });
}

/**
 * Filter table rows based on status and search term
 * @param {string} filterType - The status filter type ('all', 'passed', 'warning', 'failed')
 * @param {NodeList} rows - The table rows to filter
 */
function filterTableRows(filterType, rows) {
    const searchInput = document.querySelector('.search-input');
    const searchTerm = searchInput ? searchInput.value.toLowerCase() : '';
    
    rows.forEach(row => {
        const rowStatus = row.getAttribute('data-status');
        const rowText = row.textContent.toLowerCase();
        
        // Check if row matches status filter
        const matchesStatus = filterType === 'all' || rowStatus === filterType;
        
        // Check if row matches search term
        const matchesSearch = searchTerm === '' || rowText.includes(searchTerm);
        
        // Show row only if it matches both filters
        if (matchesStatus && matchesSearch) {
            row.style.display = '';
        } else {
            row.style.display = 'none';
        }
    });
}

// ==========================================
// SEARCH FUNCTIONALITY (Landing Page)
// ==========================================

/**
 * Initialize search functionality for test results table
 */
function initializeSearch() {
    const searchInput = document.querySelector('.search-input');
    const clearButton = document.querySelector('.search-clear');
    const tableRows = document.querySelectorAll('.results-table tbody tr');
    
    if (!searchInput) return; // Not on landing page
    
    searchInput.addEventListener('input', function() {
        const activeFilter = document.querySelector('.filter-btn.active');
        const filterType = activeFilter ? activeFilter.getAttribute('data-filter') : 'all';
        
        // Apply both search and status filtering
        filterTableRows(filterType, tableRows);
    });
    
    if (clearButton) {
        clearButton.addEventListener('click', function() {
            searchInput.value = '';
            const activeFilter = document.querySelector('.filter-btn.active');
            const filterType = activeFilter ? activeFilter.getAttribute('data-filter') : 'all';
            
            // Reapply filtering after clearing search
            filterTableRows(filterType, tableRows);
        });
    }
}

// ==========================================
// TERMINAL TAB FUNCTIONALITY (Test Detail Page)
// ==========================================

/**
 * Show specific request tab in terminal view
 * @param {string} tabName - The tab name to show ('payload', 'headers', 'curl')
 */
function showRequestTab(tabName) {
    // Hide all terminal sections
    document.querySelectorAll('#term-payload, #term-headers, #term-curl').forEach(section => {
        section.classList.remove('active');
    });
    
    // Remove active class from all tab buttons
    document.querySelectorAll('.terminal-tab').forEach(btn => {
        btn.classList.remove('active');
    });
    
    // Show selected section and activate button
    const targetSection = document.getElementById(`term-${tabName}`);
    if (targetSection) {
        targetSection.classList.add('active');
    }
    
    // Activate the clicked button
    if (event && event.target) {
        event.target.classList.add('active');
    }
}

// ==========================================
// COPY FUNCTIONALITY (Test Detail Page)
// ==========================================

/**
 * Copy trace ID to clipboard
 * @param {Event} event - The click event
 */
function copyTraceId(event) {
    const traceId = 'd3ba2bfa-31b3-4a4f-87dc-4d8dd9f41376';
    navigator.clipboard.writeText(traceId).then(() => {
        const btn = event.target;
        const originalText = btn.textContent;
        btn.textContent = 'Copied!';
        setTimeout(() => { btn.textContent = originalText; }, 2000);
    }).catch(err => {
        console.error('Failed to copy trace ID:', err);
    });
}

/**
 * Copy request code from active terminal tab
 * @param {Event} event - The click event
 */
function copyRequestCode(event) {
    const activeSection = document.querySelector('#term-payload.active, #term-headers.active, #term-curl.active');
    if (!activeSection) return;
    
    const code = activeSection.querySelector('pre').textContent;
    navigator.clipboard.writeText(code).then(() => {
        event.target.textContent = 'Copied!';
        setTimeout(() => { event.target.textContent = 'Copy'; }, 2000);
    }).catch(err => {
        console.error('Failed to copy request code:', err);
    });
}

/**
 * Copy response code from terminal
 * @param {Event} event - The click event
 */
function copyResponseCode(event) {
    const responseBlock = document.querySelector('.terminal-block:last-of-type .terminal-content pre');
    if (!responseBlock) return;
    
    const code = responseBlock.textContent;
    navigator.clipboard.writeText(code).then(() => {
        event.target.textContent = 'Copied!';
        setTimeout(() => { event.target.textContent = 'Copy'; }, 2000);
    }).catch(err => {
        console.error('Failed to copy response code:', err);
    });
}

/**
 * Copy replay command to clipboard
 * @param {Event} event - The click event
 */
function copyReplayCommand(event) {
    const code = 'dochia replay Test001';
    navigator.clipboard.writeText(code).then(() => {
        event.target.textContent = 'Copied!';
        setTimeout(() => { event.target.textContent = 'Copy'; }, 2000);
    }).catch(err => {
        console.error('Failed to copy replay command:', err);
    });
}

// ==========================================
// SUCCESS RATE STYLING
// ==========================================

/**
 * Update success rate styling based on percentage value
 */
function updateSuccessRateStyles() {
    document.querySelectorAll('.success-rate-compact').forEach(element => {
        const percentage = parseFloat(element.getAttribute('data-percentage'));
        
        // Remove all existing state classes
        element.classList.remove('warning', 'error');
        
        // Add appropriate class based on percentage
        if (percentage < 50) {
            element.classList.add('error');
        } else if (percentage < 95) {
            element.classList.add('warning');
        }
    });
}

// Buckets/Clusters related javascript functions
function toggleCluster(element) {
    // Toggle the 'collapsed' class on the cluster header
    element.classList.toggle('collapsed');
    
    // Get the content div that follows the header
    const content = element.nextElementSibling;
    if (content) {
        content.classList.toggle('collapsed');
    }
    
    // Toggle the arrow icon
    const toggle = element.querySelector('.cluster-item-toggle');
    if (toggle) {
        toggle.textContent = element.classList.contains('collapsed') ? '▶' : '▼';
    }
}

function toggleErrorCluster(element) {
    // Toggle the 'collapsed' class on the header
    element.classList.toggle('collapsed');
    
    // Get the content div that follows the header
    const content = element.nextElementSibling;
    if (content) {
        content.classList.toggle('collapsed');
    }
    
    // Toggle the arrow icon
    const toggle = element.querySelector('.cluster-toggle');
    if (toggle) {
        toggle.textContent = element.classList.contains('collapsed') ? '▶' : '▼';
    }
}

// Optional: Add function to expand or collapse all clusters
function expandAllClusters() {
    document.querySelectorAll('.cluster-content.collapsed').forEach(content => {
        content.classList.remove('collapsed');
        content.previousElementSibling.classList.remove('collapsed');
    });
}

function collapseAllClusters() {
    document.querySelectorAll('.cluster-content:not(.collapsed)').forEach(content => {
        content.classList.add('collapsed');
        content.previousElementSibling.classList.add('collapsed');
    });
}

// ==========================================
// INITIALIZATION
// ==========================================

/**
 * Initialize all functionality when DOM is loaded
 */
document.addEventListener('DOMContentLoaded', function() {
    // Initialize theme on page load
    initializeTheme();

    // Initialize success rate styling
    updateSuccessRateStyles();
    
    // Initialize chart (landing page only)
    initializeChart();
    
    // Initialize filtering functionality (landing page only)
    initializeFiltering();
    
    // Initialize search functionality (landing page only)
    initializeSearch();
});
