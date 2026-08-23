from flask import Flask, request, jsonify
import json
import urllib.parse
import urllib.request
import urllib.error
import re

app = Flask(__name__)

# =========================================================
# JAY ONLINE BACKEND
# =========================================================

APP_NAME = "JAY BACKEND"
VERSION = "2.0"


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
# SIMPLE WEB REQUEST
# =========================================================

def fetch_url(url, timeout=15):

    request = urllib.request.Request(
        url,
        headers={
            "User-Agent":
                "JayAI/2.0"
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
        "&srlimit=3"
        "&srsearch="
        + encoded_query
    )

    try:

        raw = fetch_url(url)

        data = json.loads(raw)

        results =
                results = data.get(
            "query",
            {}
        ).get(
            "search",
            []
        )

        if not results:
            return None

        articles = []

        for item in results:

            title = item.get(
                "title",
                ""
            )

            snippet = item.get(
                "snippet",
                ""
            )

            snippet = re.sub(
                r"<[^>]+>",
                "",
                snippet
            )

            if title:
                articles.append({
                    "title": title,
                    "snippet": snippet
                })

        if not articles:
            return None

        return articles

    except Exception as error:

        print(
            "Wikipedia search error:",
            error
        )

        return None


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
        "&exintro=1"
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
            "Wikipedia article error:",
            error
        )

        return None


# =========================================================
# CLEAN TEXT
# =========================================================

def clean_text(text):

    if not text:
        return ""

    text = re.sub(
        r"\s+",
        " ",
        text
    )

    return text.strip()


# =========================================================
# CREATE ONLINE ANSWER
# =========================================================

def create_online_answer(
        question,
        search_results):

    if not search_results:
        return None

    best = search_results[0]

    article = wikipedia_article(
        best["title"]
    )

    if article:

        title = article["title"]

        extract = clean_text(
            article["extract"]
        )

        if extract:

            return (
                "Here is what I found online, Sir.\n\n"
                + title
                + "\n\n"
                + extract
            )

    lines = []

    for result in search_results:

        title = result.get(
            "title",
            ""
        )

        snippet = clean_text(
            result.get(
                "snippet",
                ""
            )
        )

        if title and snippet:

            lines.append(
                title
                + ": "
                + snippet
            )

    if lines:

        return (
            "I found these online results, Sir:\n\n"
            + "\n\n".join(lines)
        )

    return None
            # =========================================================
# ONLINE CHAT
# =========================================================

@app.route("/api/chat", methods=["POST"])
def chat():

    try:

        data = request.get_json(
            silent=True
        )

        if not data:

            return jsonify({
                "status": "error",
                "reply": "No JSON request was received."
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
                "reply": "The message must be text."
            }), 400

        message = message.strip()

        if not message:

            return jsonify({
                "status": "error",
                "reply": "Please provide a message, Sir."
            }), 400

        print(
            "Jay online question:",
            message
        )

        # -------------------------------------------------
        # SEARCH THE INTERNET
        # -------------------------------------------------

        results = wikipedia_search(
            message
        )

        answer = create_online_answer(
            message,
            results
        )

        if answer:

            return jsonify({
                "status": "ok",
                "online": True,
                "reply": answer
            })

        return jsonify({
            "status": "ok",
            "online": True,
            "reply":
                "I couldn't find reliable information "
                "for that question online, Sir."
        })

    except Exception as error:

        print(
            "CHAT ERROR:",
            error
        )

        return jsonify({
            "status": "error",
            "online": True,
            "reply":
                "Jay's online brain encountered an error: "
                + str(error)
        }), 500


# =========================================================
# ROOT
# =========================================================

@app.route("/", methods=["GET"])
def home():

    return jsonify({
        "service": APP_NAME,
        "version": VERSION,
        "status": "ok",
        "message":
            "Jay online backend is running."
    })


# =========================================================
# SERVER START
# =========================================================

if __name__ == "__main__":

    print()
    print("========================================")
    print("       JAY ONLINE BACKEND 2.0")
    print("========================================")
    print("Health:  /health")
    print("Chat:    /api/chat")
    print("Root:    /")
    print("Port:    5000")
    print("========================================")
    print()

    app.run(
        host="0.0.0.0",
        port=5000,
        debug=False
        )
