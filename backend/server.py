"""
server.py -- Jay Cloud Backend, PART 1 milestone.
Exposes /health and POST /api/chat only. Later parts add memory,
inventory, repairs, web, voice, and admin auth.
"""
from __future__ import annotations
import logging
import os
from flask import Flask, request, jsonify
from ai_provider import get_provider, LocalProvider, AIProviderError

logging.basicConfig(level=logging.INFO, format="%(asctime)s %(levelname)s %(name)s: %(message)s")
logger = logging.getLogger("jay.server")

app = Flask(__name__)

JAY_SYSTEM_PROMPT = (
    "You are Jay, a helpful personal AI assistant running for a single user "
    "on their Android phone. Be concise, friendly, and clear."
)

try:
    _provider = get_provider()
    logger.info("AI provider initialized: %s", _provider.name)
except AIProviderError as e:
    logger.error("Failed to initialize AI provider: %s", e)
    _provider = None

_health_provider = LocalProvider()


@app.route("/health", methods=["GET"])
def health():
    reply = _health_provider.chat([{"role": "user", "content": "ping"}])
    return jsonify({
        "status": "ok",
        "configured_provider": _provider.name if _provider else None,
        "test_reply": reply,
    }), 200


@app.route("/api/chat", methods=["POST"])
def api_chat():
    if _provider is None:
        return jsonify({
            "error": "AI provider is not configured. Set AI_API_KEY (and optionally AI_PROVIDER), then restart."
        }), 500

    body = request.get_json(silent=True)
    if not body or "message" not in body:
        return jsonify({"error": "Request body must be JSON with a 'message' field."}), 400

    user_message = body.get("message", "")
    if not isinstance(user_message, str) or not user_message.strip():
        return jsonify({"error": "'message' must be a non-empty string."}), 400

    history = body.get("history", [])
    if not isinstance(history, list):
        return jsonify({"error": "'history' must be a list if provided."}), 400

    messages = history + [{"role": "user", "content": user_message}]

    try:
        reply_text = _provider.chat(messages, system=JAY_SYSTEM_PROMPT)
    except AIProviderError as e:
        logger.error("AI provider error on /api/chat: %s", e)
        return jsonify({"error": "Jay's AI provider is temporarily unavailable. Please try again."}), 502

    return jsonify({"reply": reply_text, "provider": _provider.name}), 200


if __name__ == "__main__":
    port = int(os.environ.get("PORT", 5000))
    debug = os.environ.get("FLASK_DEBUG", "false").lower() == "true"
    app.run(host="0.0.0.0", port=port, debug=debug)
