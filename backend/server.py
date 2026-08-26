from flask import Flask, request, jsonify
import json
import os
import urllib.parse
import urllib.request

from jay_core import SYSTEM_IDENTITY, build_online_answer, local_answer

app = Flask(__name__)
APP_NAME = "JAY BACKEND"
VERSION = "5.0-gemini"
MAX_MESSAGE_LENGTH = 4000


def fetch_url(url, timeout=15):
    req = urllib.request.Request(url, headers={"User-Agent": "JayAI/5.0"})
    with urllib.request.urlopen(req, timeout=timeout) as response:
        return response.read().decode("utf-8", errors="replace")


def wikipedia_search(query):
    url = ("https://en.wikipedia.org/w/api.php?action=query&format=json&list=search"
           "&utf8=1&srlimit=5&srsearch=" + urllib.parse.quote(query))
    try:
        data = json.loads(fetch_url(url))
        return data.get("query", {}).get("search", [])
    except Exception as error:
        app.logger.warning("WEB SEARCH ERROR: %s", error)
        return []


def wikipedia_article(title):
    url = ("https://en.wikipedia.org/w/api.php?action=query&format=json&prop=extracts"
           "&explaintext=1&exintro=0&redirects=1&titles=" + urllib.parse.quote(title))
    try:
        data = json.loads(fetch_url(url))
        for page in data.get("query", {}).get("pages", {}).values():
            extract = page.get("extract", "")
            if extract:
                return {"title": page.get("title", title), "extract": extract}
    except Exception as error:
        app.logger.warning("ARTICLE ERROR: %s", error)
    return None


def configured_provider():
    if os.environ.get("AI_PROVIDER", "").strip().lower() == "local":
        return "local"
    if os.environ.get("GEMINI_API_KEY") or os.environ.get("AI_API_KEY"):
        return "gemini"
    return "web-fallback"


@app.route("/health", methods=["GET"])
def health():
    provider = configured_provider()
    return jsonify({
        "status": "ok",
        "service": APP_NAME,
        "version": VERSION,
        "online": True,
        "provider": provider,
        "external_ai_required": provider == "gemini",
        "gemini_configured": provider == "gemini",
    })


@app.route("/", methods=["GET"])
def home():
    return jsonify({
        "service": APP_NAME,
        "version": VERSION,
        "status": "ok",
        "online": True,
        "provider": configured_provider(),
        "endpoints": {"health": "/health", "chat": "/api/chat"},
    })


@app.route("/api/chat", methods=["POST"])
def chat():
    try:
        data = request.get_json(silent=True)
        if not isinstance(data, dict):
            return jsonify({"status": "error", "reply": "No valid message was received, Sir."}), 400

        message = data.get("message", "")
        if not isinstance(message, str):
            return jsonify({"status": "error", "reply": "The message must be text, Sir."}), 400
        message = message.strip()
        if not message:
            return jsonify({"status": "error", "reply": "Please give me a question, Sir."}), 400
        if len(message) > MAX_MESSAGE_LENGTH:
            return jsonify({"status": "error", "reply": "That message is too long, Sir. Please shorten it."}), 413

        app.logger.info("JAY REQUEST: %s", message[:200])

        local = local_answer(message)
        if local:
            return jsonify({
                "status": "ok", "online": True, "provider": "jay-native",
                "mode": "local", "reply": local,
            })

        answer, provider = build_online_answer(message, wikipedia_search, wikipedia_article)
        return jsonify({
            "status": "ok", "online": True, "provider": provider,
            "mode": provider, "reply": answer,
        })

    except Exception:
        app.logger.exception("CHAT ERROR")
        return jsonify({
            "status": "error", "online": False, "provider": configured_provider(),
            "reply": "Jay's online brain encountered a temporary error, Sir. Please try again.",
        }), 500


if __name__ == "__main__":
    print("========================================")
    print("        JAY SELF-OWNED API 5.0")
    print("========================================")
    print("Health:  http://127.0.0.1:5000/health")
    print("Chat:    http://127.0.0.1:5000/api/chat")
    print("AI:      Gemini when GEMINI_API_KEY is configured")
    print("========================================")
    app.run(host="0.0.0.0", port=int(os.environ.get("PORT", "5000")), debug=False)
