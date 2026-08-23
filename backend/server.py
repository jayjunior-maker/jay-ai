from flask import Flask, request, jsonify
import json
import urllib.parse
import urllib.request
import re

app = Flask(__name__)

APP_NAME = "JAY BACKEND"
VERSION = "3.0"


# =========================================================
# HEALTH CHECK
# =========================================================

@app.route("/health", methods=["GET"])
def health():

    return jsonify({
        "status": "ok",
        "service": APP_NAME,
        "version": VERSION,
        "online": True
    })


# =========================================================
# INTERNET REQUEST
# =========================================================

def fetch_url(url, timeout=15):

    request = urllib.request.Request(
        url,
        headers={
            "User-Agent": "JayAI/3.0"
        }
    )

    with urllib.request.urlopen(
        request,
        timeout=timeout
    ) as response:

        return response.read().decode(
            "utf-8",
            errors="replace"
        )


# =========================================================
# WIKIPEDIA SEARCH
# =========================================================

def wikipedia_search(query):

    encoded_query = urllib.parse.quote(
        query
    )

    url = (
        "https://en.wikipedia.org/w/api.php"
        "?action=query"
        "&format=json"
        "&list=search"
        "&utf8=1"
        "&srlimit=5"
        "&srsearch="
        + encoded_query
    )

    try:

        raw = fetch_url(url)

        data = json.loads(raw)

        return data.get(
            "query",
            {}
        ).get(
            "search",
            []
        )

    except Exception as error:

        print(
            "WEB SEARCH ERROR:",
            error
        )

        return []


# =========================================================
# CLEAN SEARCH RESULT
# =========================================================

def clean_snippet(text):

    if not text:
        return ""

    return re.sub(
        r"<[^>]+>",
        "",
        text
    ).strip()
    # =========================================================
# WIKIPEDIA ARTICLE
# =========================================================

def wikipedia_article(title):

    encoded_title = urllib.parse.quote(
        title
    )

    url = (
        "https://en.wikipedia.org/w/api.php"
        "?action=query"
        "&format=json"
        "&prop=extracts"
        "&explaintext=1"
        "&exintro=0"
        "&redirects=1"
        "&titles="
        + encoded_title
    )

    try:

        raw = fetch_url(url)

        data = json.loads(raw)

        pages = data.get(
            "query",
            {}
        ).get(
            "pages",
            {}
        )

        for page in pages.values():

            extract = page.get(
                "extract",
                ""
            )

            if extract:

                return {
                    "title": page.get(
                        "title",
                        title
                    ),
                    "extract": extract
                }

        return None

    except Exception as error:

        print(
            "ARTICLE ERROR:",
            error
        )

        return None


# =========================================================
# BUILD FULL ONLINE ANSWER
# =========================================================

def build_answer(
        question,
        results):

    if not results:

        return (
            "I couldn't find reliable information "
            "about that online, Sir."
        )

    # Try the best search result first.

    best_title = results[0].get(
        "title",
        ""
    )

    article = wikipedia_article(
        best_title
    )

    if article:

        title = article.get(
            "title",
            best_title
        )

        extract = article.get(
            "extract",
            ""
        ).strip()

        if extract:

            # Keep the answer useful without
            # returning an enormous article.

            if len(extract) > 6000:

                extract = (
                    extract[:6000]
                    + "..."
                )

            return (
                "Here is what I found online, Sir.\n\n"
                + title
                + "\n\n"
                + extract
            )

    # Fallback to multiple search results.

    answer_parts = []

    for result in results:

        title = result.get(
            "title",
            ""
        )

        snippet = clean_snippet(
            result.get(
                "snippet",
                ""
            )
        )

        if title and snippet:

            answer_parts.append(
                title
                + "\n"
                + snippet
            )

    if answer_parts:

        return (
            "I found the following information "
            "online, Sir.\n\n"
            + "\n\n".join(
                answer_parts
            )
        )

    return (
        "I found the topic online, but "
        "couldn't retrieve enough information "
        "to give you a reliable answer, Sir."
    )


# =========================================================
# CHAT REQUEST
# =========================================================

@app.route(
    "/api/chat",
    methods=["POST"]
)
def chat():

    try:

        data = request.get_json(
            silent=True
        )

        if not data:

            return jsonify({
                "status": "error",
                "reply":
                    "No message was received, Sir."
            }), 400

        message = data.get(
            "message",
            ""
        )

        if not isinstance(
                message,
                str
        ):

            return jsonify({
                "status": "error",
                "reply":
                    "The message must be text, Sir."
            }), 400

        message = message.strip()

        if not message:

            return jsonify({
                "status": "error",
                "reply":
                    "Please give me a question, Sir."
            }), 400

        print(
            "ONLINE QUESTION:",
            message
        )

        results = wikipedia_search(
            message
        )

        answer = build_answer(
            message,
            results
        )

        return jsonify({
            "status": "ok",
            "online": True,
            "provider": "web",
            "reply": answer
        })

    except Exception as error:

        print(
            "CHAT ERROR:",
            error
        )

        return jsonify({
            "status": "error",
            "online": True,
            "provider": "web",
            "reply":
                "The online brain encountered "
                "an error, Sir: "
                + str(error)
        }), 500
        # =========================================================
# ROOT ENDPOINT
# =========================================================

@app.route("/", methods=["GET"])
def home():

    return jsonify({
        "service": APP_NAME,
        "version": VERSION,
        "status": "ok",
        "online": True,
        "endpoints": {
            "health": "/health",
            "chat": "/api/chat"
        }
    })


# =========================================================
# START JAY BACKEND
# =========================================================

if __name__ == "__main__":

    print()
    print("========================================")
    print("        JAY ONLINE BACKEND 3.0")
    print("========================================")
    print("Health:  http://127.0.0.1:5000/health")
    print("Chat:    http://127.0.0.1:5000/api/chat")
    print("Port:    5000")
    print("Online:  YES")
    print("========================================")
    print()

    app.run(
        host="0.0.0.0",
        port=5000,
        debug=False
    )
