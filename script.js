async function loadHistory() {
    const res = await fetch('/api/history');
    const files = await res.json();
    const historyList = document.getElementById('historyList');
    historyList.innerHTML = '';
    files.forEach(f => {
        const li = document.createElement('li');
        li.innerHTML = `<a href="#" onclick="loadScript('${f}')">${f}</a>`;
        historyList.appendChild(li);
    });
}

async function loadScript(filename) {
    const res = await fetch('/api/load-script?filename=' + filename);
    const text = await res.text();
    document.getElementById('scriptOutput').innerText = text;
}

document.getElementById("generateBtn").addEventListener("click", async () => {

    const prompt = document.getElementById("scenePrompt").value;
    const characters = document.getElementById("characterNames").value;

    const response = await fetch("/api/generate-script", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            prompt: prompt,
            characters: characters
        })
    });

    const data = await response.json();
    document.getElementById("scriptOutput").innerText = data.script;
});

document.getElementById('downloadBtn').addEventListener('click', async () => {
    const scriptContent = document.getElementById('scriptOutput').innerText;
    const res = await fetch(`/api/download-pdf?scriptContent=${encodeURIComponent(scriptContent)}`);
    const blob = await res.blob();
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = 'script.pdf';
    a.click();
});

document.getElementById('suggestBtn').addEventListener('click', async () => {
    const script = document.getElementById('scriptOutput').innerText;
    const res = await fetch(`/api/generate-suggestions?script=${encodeURIComponent(script)}`, { method: 'POST' });
    const suggestions = await res.text();
    document.getElementById('suggestOutput').innerText = suggestions;
});

loadHistory();
