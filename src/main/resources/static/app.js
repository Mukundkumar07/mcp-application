// API Base URL
const API_BASE = 'http://localhost:8080/api/rag';

// State
let currentTab = 'chat';
let chatHistory = [];

// Initialize
document.addEventListener('DOMContentLoaded', () => {
    initializeTabs();
    initializeChat();
    initializeStats();
    setupEventListeners();
});

// Tab Management
function initializeTabs() {
    const tabBtns = document.querySelectorAll('.tab-btn');
    tabBtns.forEach(btn => {
        btn.addEventListener('click', () => {
            const tabName = btn.dataset.tab;
            switchTab(tabName);
        });
    });
}

function switchTab(tabName) {
    // Update buttons
    document.querySelectorAll('.tab-btn').forEach(btn => {
        btn.classList.toggle('active', btn.dataset.tab === tabName);
    });
    
    // Update content
    document.querySelectorAll('.tab-content').forEach(content => {
        content.classList.toggle('active', content.id === `${tabName}-tab`);
    });
    
    currentTab = tabName;
    
    // Load data for specific tabs
    if (tabName === 'manage') {
        refreshStats();
    }
}

// Chat Functions
function initializeChat() {
    const chatInput = document.getElementById('chatInput');
    const sendBtn = document.getElementById('sendBtn');
    
    chatInput.addEventListener('keypress', (e) => {
        if (e.key === 'Enter' && !e.shiftKey) {
            e.preventDefault();
            sendMessage();
        }
    });
    
    sendBtn.addEventListener('click', sendMessage);
}

function askExample(question) {
    document.getElementById('chatInput').value = question;
    sendMessage();
}

async function sendMessage() {
    const input = document.getElementById('chatInput');
    const message = input.value.trim();
    const topK = parseInt(document.getElementById('topKInput').value) || 5;
    
    if (!message) return;
    
    // Clear input
    input.value = '';
    
    // Add user message to chat
    addMessageToChat('user', message);
    
    // Show loading
    const loadingId = addMessageToChat('assistant', 'Thinking...', true);
    
    try {
        const response = await fetch(`${API_BASE}/chat`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ message, topK })
        });
        
        if (!response.ok) throw new Error('Failed to get response');
        
        const data = await response.json();
        
        // Remove loading message
        document.getElementById(loadingId)?.remove();
        
        // Add assistant response
        addMessageToChat('assistant', data.response, false, data.metadata);
        
    } catch (error) {
        document.getElementById(loadingId)?.remove();
        addMessageToChat('assistant', '❌ Error: ' + error.message);
        showToast('Failed to send message', 'error');
    }
}

function addMessageToChat(role, text, isLoading = false, metadata = null) {
    const messagesContainer = document.getElementById('chatMessages');
    const messageId = `msg-${Date.now()}`;
    
    // Remove welcome message if exists
    const welcome = messagesContainer.querySelector('.welcome-message');
    if (welcome) welcome.remove();
    
    const messageDiv = document.createElement('div');
    messageDiv.className = `message ${role}`;
    messageDiv.id = messageId;
    
    const avatar = role === 'user' ? '👤' : '🤖';
    
    let metadataHTML = '';
    if (metadata) {
        metadataHTML = `
            <div class="message-meta">
                <span class="chunk-badge">${metadata.retrievedChunks} chunks retrieved</span>
                <span>Context: ${metadata.contextLength} chars</span>
            </div>
        `;
    }
    
    messageDiv.innerHTML = `
        <div class="message-avatar">${avatar}</div>
        <div class="message-content">
            <div class="message-text">${isLoading ? '<span class="loading">●●●</span>' : escapeHtml(text)}</div>
            ${metadataHTML}
        </div>
    `;
    
    messagesContainer.appendChild(messageDiv);
    messagesContainer.scrollTop = messagesContainer.scrollHeight;
    
    return messageId;
}

// Ingestion Functions
async function ingestFilesystem() {
    const path = document.getElementById('fsPath').value.trim();
    const filePattern = document.getElementById('fsPattern').value.trim();
    
    if (!path) {
        showToast('Please enter a directory path', 'warning');
        return;
    }
    
    const payload = { path };
    if (filePattern) payload.filePattern = filePattern;
    
    try {
        const response = await fetch(`${API_BASE}/ingest/filesystem`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        
        if (!response.ok) throw new Error('Ingestion failed');
        
        const result = await response.json();
        displayIngestionResult(result);
        showToast(`Successfully ingested ${result.processedFiles} files`, 'success');
        updateHeaderStats();
        
    } catch (error) {
        showToast('Filesystem ingestion failed: ' + error.message, 'error');
    }
}

async function ingestWeb() {
    const url = document.getElementById('webUrl').value.trim();
    
    if (!url) {
        showToast('Please enter a URL', 'warning');
        return;
    }
    
    try {
        const response = await fetch(`${API_BASE}/ingest/web`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ url })
        });
        
        if (!response.ok) throw new Error('Ingestion failed');
        
        const result = await response.json();
        displayIngestionResult(result);
        showToast('Successfully ingested web page', 'success');
        updateHeaderStats();
        
    } catch (error) {
        showToast('Web ingestion failed: ' + error.message, 'error');
    }
}

async function ingestDatabase() {
    const query = document.getElementById('dbQuery').value.trim();
    const connectionString = document.getElementById('dbConnection').value.trim();
    
    if (!query || !connectionString) {
        showToast('Please enter both query and connection string', 'warning');
        return;
    }
    
    try {
        const response = await fetch(`${API_BASE}/ingest/database`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ query, connectionString })
        });
        
        if (!response.ok) throw new Error('Ingestion failed');
        
        const result = await response.json();
        displayIngestionResult(result);
        showToast('Successfully ingested database records', 'success');
        updateHeaderStats();
        
    } catch (error) {
        showToast('Database ingestion failed: ' + error.message, 'error');
    }
}

function displayIngestionResult(result) {
    const container = document.getElementById('ingestResult');
    
    const successRate = result.totalFiles > 0 
        ? ((result.processedFiles / result.totalFiles) * 100).toFixed(1)
        : 0;
    
    container.innerHTML = `
        <h4>✅ Ingestion Complete</h4>
        <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(150px, 1fr)); gap: 1rem; margin-top: 1rem;">
            <div>
                <div style="color: var(--text-muted); font-size: 0.875rem;">Total Files</div>
                <div style="font-size: 1.5rem; font-weight: 700; color: var(--primary-light);">${result.totalFiles}</div>
            </div>
            <div>
                <div style="color: var(--text-muted); font-size: 0.875rem;">Processed</div>
                <div style="font-size: 1.5rem; font-weight: 700; color: var(--success);">${result.processedFiles}</div>
            </div>
            <div>
                <div style="color: var(--text-muted); font-size: 0.875rem;">Chunks</div>
                <div style="font-size: 1.5rem; font-weight: 700; color: var(--info);">${result.totalChunks}</div>
            </div>
            <div>
                <div style="color: var(--text-muted); font-size: 0.875rem;">Duration</div>
                <div style="font-size: 1.5rem; font-weight: 700; color: var(--secondary);">${result.duration}ms</div>
            </div>
        </div>
        ${result.errors && result.errors.length > 0 ? `
            <div style="margin-top: 1rem; padding: 1rem; background: rgba(239, 68, 68, 0.1); border-radius: 0.5rem;">
                <strong>Errors:</strong>
                <ul style="margin-top: 0.5rem;">
                    ${result.errors.map(err => `<li>${escapeHtml(err)}</li>`).join('')}
                </ul>
            </div>
        ` : ''}
    `;
}

// Search Functions
async function searchDocuments() {
    const query = document.getElementById('searchQuery').value.trim();
    const topK = parseInt(document.getElementById('searchTopK').value) || 10;
    
    if (!query) {
        showToast('Please enter a search query', 'warning');
        return;
    }
    
    try {
        const response = await fetch(`${API_BASE}/search?query=${encodeURIComponent(query)}&topK=${topK}`);
        
        if (!response.ok) throw new Error('Search failed');
        
        const results = await response.json();
        displaySearchResults(results);
        
    } catch (error) {
        showToast('Search failed: ' + error.message, 'error');
    }
}

function displaySearchResults(results) {
    const container = document.getElementById('searchResults');
    
    if (!results || results.length === 0) {
        container.innerHTML = '<div class="loading">No results found</div>';
        return;
    }
    
    container.innerHTML = results.map((result, index) => `
        <div class="search-result-item">
            <div class="result-score">Score: ${result.score.toFixed(4)}</div>
            <div style="margin-bottom: 0.5rem;">
                <strong style="color: var(--primary-light);">Chunk ${index + 1}</strong>
                ${result.metadata ? `<span style="color: var(--text-muted); margin-left: 0.5rem;">• ${result.metadata.fileName || 'Unknown'}</span>` : ''}
            </div>
            <div style="color: var(--text-secondary); line-height: 1.6;">${escapeHtml(result.content)}</div>
            ${result.metadata ? `
                <div style="margin-top: 0.5rem; font-size: 0.875rem; color: var(--text-muted);">
                    ${result.metadata.source ? `Source: ${result.metadata.source}` : ''}
                    ${result.metadata.filePath ? ` • Path: ${result.metadata.filePath}` : ''}
                </div>
            ` : ''}
        </div>
    `).join('');
}

// Stats Functions
async function initializeStats() {
    updateHeaderStats();
}

async function updateHeaderStats() {
    try {
        const response = await fetch(`${API_BASE}/stats/vector-db`);
        if (!response.ok) throw new Error('Failed to fetch stats');
        
        const stats = await response.json();
        
        document.getElementById('docCount').textContent = stats.totalDocuments || 0;
        document.getElementById('chunkCount').textContent = stats.totalChunks || 0;
        
    } catch (error) {
        console.error('Failed to update stats:', error);
    }
}

async function refreshStats() {
    // Vector DB Stats
    try {
        const response = await fetch(`${API_BASE}/stats/vector-db`);
        const stats = await response.json();
        
        document.getElementById('vectorStats').innerHTML = `
            <div style="display: grid; gap: 0.75rem;">
                <div style="display: flex; justify-content: space-between;">
                    <span>Total Documents:</span>
                    <strong style="color: var(--primary-light);">${stats.totalDocuments || 0}</strong>
                </div>
                <div style="display: flex; justify-content: space-between;">
                    <span>Total Chunks:</span>
                    <strong style="color: var(--success);">${stats.totalChunks || 0}</strong>
                </div>
                <div style="display: flex; justify-content: space-between;">
                    <span>Storage Type:</span>
                    <strong style="color: var(--info);">${stats.storageType || 'In-Memory'}</strong>
                </div>
                <div style="display: flex; justify-content: space-between;">
                    <span>Dimension:</span>
                    <strong style="color: var(--secondary);">${stats.dimension || 384}</strong>
                </div>
            </div>
        `;
    } catch (error) {
        document.getElementById('vectorStats').innerHTML = '<div class="loading">Failed to load</div>';
    }
    
    // MCP Sources
    try {
        const response = await fetch(`${API_BASE}/mcp/sources`);
        const sources = await response.json();
        
        document.getElementById('mcpSources').innerHTML = `
            <div style="display: grid; gap: 0.5rem;">
                ${sources.map(source => `
                    <div style="display: flex; align-items: center; gap: 0.5rem;">
                        <span style="color: ${source.enabled ? 'var(--success)' : 'var(--text-muted)'};">●</span>
                        <span>${source.name}</span>
                    </div>
                `).join('')}
            </div>
        `;
    } catch (error) {
        document.getElementById('mcpSources').innerHTML = '<div class="loading">Failed to load</div>';
    }
    
    updateHeaderStats();
    showToast('Stats refreshed', 'success');
}

async function clearDatabase() {
    if (!confirm('Are you sure you want to clear all documents from the database? This action cannot be undone.')) {
        return;
    }
    
    try {
        const response = await fetch(`${API_BASE}/clear`, { method: 'DELETE' });
        
        if (!response.ok) throw new Error('Failed to clear database');
        
        showToast('Database cleared successfully', 'success');
        updateHeaderStats();
        refreshStats();
        
        // Clear chat
        document.getElementById('chatMessages').innerHTML = `
            <div class="welcome-message">
                <div class="welcome-icon">🤖</div>
                <h2>Welcome to RAG-MCP Intelligence</h2>
                <p>Ask questions about your ingested documents and get AI-powered answers with context retrieval.</p>
            </div>
        `;
        
    } catch (error) {
        showToast('Failed to clear database: ' + error.message, 'error');
    }
}

// Event Listeners
function setupEventListeners() {
    // Check health on load
    checkHealth();
    
    // Periodic health check
    setInterval(checkHealth, 30000);
}

async function checkHealth() {
    try {
        const response = await fetch(`${API_BASE}/health`);
        const health = await response.json();
        
        const indicator = document.getElementById('statusIndicator');
        const statusText = document.getElementById('statusText');
        
        if (health.status === 'UP') {
            indicator.style.background = 'rgba(16, 185, 129, 0.1)';
            indicator.style.borderColor = 'rgba(16, 185, 129, 0.2)';
            statusText.textContent = 'Online';
        } else {
            indicator.style.background = 'rgba(239, 68, 68, 0.1)';
            indicator.style.borderColor = 'rgba(239, 68, 68, 0.2)';
            statusText.textContent = 'Offline';
        }
    } catch (error) {
        const indicator = document.getElementById('statusIndicator');
        const statusText = document.getElementById('statusText');
        indicator.style.background = 'rgba(239, 68, 68, 0.1)';
        indicator.style.borderColor = 'rgba(239, 68, 68, 0.2)';
        statusText.textContent = 'Offline';
    }
}

// Toast Notifications
function showToast(message, type = 'info') {
    const container = document.getElementById('toastContainer');
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.textContent = message;
    
    container.appendChild(toast);
    
    setTimeout(() => {
        toast.style.animation = 'slideInRight 250ms ease reverse';
        setTimeout(() => toast.remove(), 250);
    }, 3000);
}

// Utility Functions
function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}
