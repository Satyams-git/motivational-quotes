import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.FileReader;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class Main {

    private static List<String> quotes;

    private static final String HTML_PAGE = """
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>Daily Wisdom</title>
  <link rel="preconnect" href="https://fonts.googleapis.com" />
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin />
  <link href="https://fonts.googleapis.com/css2?family=Playfair+Display:ital,wght@0,400;0,700;1,400&family=Jost:wght@300;400&display=swap" rel="stylesheet" />
  <style>
    *, *::before, *::after { box-sizing: border-box; margin: 0; padding: 0; }

    :root {
      --bg:        #0f0f0f;
      --surface:   #171717;
      --border:    #2a2a2a;
      --gold:      #c9a84c;
      --gold-dim:  #7a6230;
      --text:      #e8e2d4;
      --muted:     #7a7068;
    }

    html, body {
      height: 100%;
    }

    body {
      background: var(--bg);
      color: var(--text);
      font-family: 'Jost', sans-serif;
      display: flex;
      align-items: center;
      justify-content: center;
      min-height: 100vh;
      overflow: hidden;
    }

    /* Subtle radial glow behind card */
    body::before {
      content: '';
      position: fixed;
      inset: 0;
      background: radial-gradient(ellipse 60% 50% at 50% 50%, rgba(201,168,76,0.06) 0%, transparent 70%);
      pointer-events: none;
    }

    .card {
      position: relative;
      background: var(--surface);
      border: 1px solid var(--border);
      border-radius: 2px;
      padding: 3.5rem 4rem;
      max-width: 680px;
      width: 90%;
      text-align: center;
      box-shadow: 0 40px 100px rgba(0,0,0,0.6);
    }

    /* top gold bar */
    .card::before {
      content: '';
      position: absolute;
      top: 0; left: 10%; right: 10%;
      height: 2px;
      background: linear-gradient(90deg, transparent, var(--gold), transparent);
    }

    .ornament {
      color: var(--gold-dim);
      font-size: 4rem;
      line-height: 1;
      font-family: 'Playfair Display', serif;
      margin-bottom: 1.2rem;
      user-select: none;
    }

    #quote-text {
      font-family: 'Playfair Display', serif;
      font-size: clamp(1.2rem, 2.5vw, 1.55rem);
      font-style: italic;
      line-height: 1.7;
      color: var(--text);
      min-height: 4em;
      transition: opacity 0.4s ease, transform 0.4s ease;
    }

    #quote-text.fade {
      opacity: 0;
      transform: translateY(8px);
    }

    #quote-author {
      margin-top: 1.6rem;
      font-size: 0.8rem;
      font-weight: 300;
      letter-spacing: 0.18em;
      text-transform: uppercase;
      color: var(--gold);
      min-height: 1.2em;
      transition: opacity 0.4s ease;
    }

    #quote-author.fade { opacity: 0; }

    .divider {
      width: 40px;
      height: 1px;
      background: var(--gold-dim);
      margin: 2.4rem auto;
    }

    button {
      display: inline-flex;
      align-items: center;
      gap: 0.5rem;
      background: transparent;
      border: 1px solid var(--gold-dim);
      color: var(--gold);
      font-family: 'Jost', sans-serif;
      font-size: 0.78rem;
      font-weight: 400;
      letter-spacing: 0.2em;
      text-transform: uppercase;
      padding: 0.75rem 2rem;
      border-radius: 1px;
      cursor: pointer;
      transition: background 0.2s, border-color 0.2s, color 0.2s, transform 0.15s;
      outline: none;
    }

    button:hover {
      background: rgba(201,168,76,0.08);
      border-color: var(--gold);
      color: #e8c96a;
    }

    button:active {
      transform: scale(0.97);
    }

    button svg {
      transition: transform 0.4s ease;
    }

    button.spinning svg {
      transform: rotate(360deg);
    }

    .footer {
      margin-top: 2rem;
      font-size: 0.68rem;
      color: var(--muted);
      letter-spacing: 0.08em;
    }
  </style>
</head>
<body>
  <div class="card">
    <div class="ornament">&#8220;</div>
    <p id="quote-text">Loading wisdom&hellip;</p>
    <p id="quote-author"></p>
    <div class="divider"></div>
    <button id="refresh-btn" onclick="loadQuote()">
      <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <polyline points="23 4 23 10 17 10"/><polyline points="1 20 1 14 7 14"/>
        <path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"/>
      </svg>
      Next Quote
    </button>
    <p class="footer">Daily Wisdom &mdash; Motivational Quotes</p>
  </div>

  <script>
    async function loadQuote() {
      const textEl   = document.getElementById('quote-text');
      const authorEl = document.getElementById('quote-author');
      const btn      = document.getElementById('refresh-btn');

      // fade out
      textEl.classList.add('fade');
      authorEl.classList.add('fade');
      btn.classList.add('spinning');

      await new Promise(r => setTimeout(r, 380));

      try {
        const res  = await fetch('/quote');
        const data = await res.json();
        const raw  = data.quote || '';

        // Split "Quote text – Author" on em-dash or hyphen
        const match = raw.match(/^(.+?)\\s*[\\u2013\\u2014\\-]{1,2}\\s*(.+)$/);
        if (match) {
          textEl.textContent  = match[1].trim();
          authorEl.textContent = '— ' + match[2].trim();
        } else {
          textEl.textContent  = raw;
          authorEl.textContent = '';
        }
      } catch (e) {
        textEl.textContent = 'Could not fetch quote. Please try again.';
        authorEl.textContent = '';
      }

      // fade in
      textEl.classList.remove('fade');
      authorEl.classList.remove('fade');
      btn.classList.remove('spinning');
    }

    loadQuote();
  </script>
</body>
</html>
""";

    public static void main(String[] args) throws IOException {
        quotes = loadQuotesFromFile("quotes.txt");

        if (quotes.isEmpty()) {
            System.err.println("No quotes found in quotes.txt. Please ensure the file has content.");
            return;
        }

        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);

        // UI route — serves the HTML page
        server.createContext("/", exchange -> {
            if (!exchange.getRequestURI().getPath().equals("/")) {
                exchange.sendResponseHeaders(404, -1);
                return;
            }
            byte[] bytes = HTML_PAGE.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) { os.write(bytes); }
        });

        // API route — serves a random quote as JSON
        server.createContext("/quote", exchange -> {
            String json = String.format("{\"quote\": \"%s\"}", escapeJson(getRandomQuote()));
            byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) { os.write(bytes); }
        });

        server.start();
        System.out.println("Server running → http://localhost:8000/");
    }

    private static String getRandomQuote() {
        return quotes.get(new Random().nextInt(quotes.size()));
    }

    private static String escapeJson(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    private static List<String> loadQuotesFromFile(String filename) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            return reader.lines()
                         .map(String::trim)
                         .filter(l -> !l.isEmpty())
                         .collect(Collectors.toList());
        } catch (IOException e) {
            System.err.println("Error reading quotes file: " + e.getMessage());
            return List.of();
        }
    }
}
