"use client";

import { FormEvent, useState } from "react";

type ShortenResponse = {
  shortUrl?: string;
  message?: string;
};

export default function Home() {
  const [url, setUrl] = useState("");
  const [result, setResult] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  async function shortenUrl(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setLoading(true);
    setResult("");
    setError("");

    try {
      const response = await fetch("/api/shorten", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ url }),
      });
      const body = (await response.json()) as ShortenResponse;

      if (!response.ok || !body.shortUrl) {
        throw new Error(body.message || "Unable to shorten this URL.");
      }
      setResult(body.shortUrl);
    } catch (requestError) {
      setError(requestError instanceof Error ? requestError.message : "Unable to shorten this URL.");
    } finally {
      setLoading(false);
    }
  }

  return (
    <main>
      <section aria-labelledby="page-title">
        <h1 id="page-title">URL Shortener</h1>
        <form onSubmit={shortenUrl}>
          <label htmlFor="url">Enter URL</label>
          <input
            id="url"
            name="url"
            type="url"
            placeholder="https://example.com/long-url"
            value={url}
            onChange={(event) => setUrl(event.target.value)}
            required
          />
          <button type="submit" disabled={loading}>
            {loading ? "Shortening..." : "Shorten URL"}
          </button>
        </form>

        <div className="result" aria-live="polite">
          <span>Result</span>
          {result && (
            <a href={result} target="_blank" rel="noreferrer">
              {result}
            </a>
          )}
          {error && <p role="alert">{error}</p>}
        </div>
      </section>
    </main>
  );
}
