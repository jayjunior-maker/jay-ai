"""Core orchestration for Jay's self-owned backend API.

The API is provider-free by default: it can answer common requests locally and
use public web sources for current information. Optional external model
providers can be added later without changing the Android client.
"""
from __future__ import annotations

import re
from datetime import datetime, timezone
from typing import Callable, Dict, List, Optional


SYSTEM_IDENTITY = (
    "You are Jay, a helpful personal AI assistant. Address the user as Sir. "
    "Be clear, honest, concise, and never claim to have performed an action "
    "you did not actually perform."
)


def _clean(text: str) -> str:
    return re.sub(r"\s+", " ", text or "").strip()


def local_answer(message: str) -> Optional[str]:
    """Handle useful, deterministic requests without a paid AI API."""
    q = _clean(message)
    lower = q.lower()

    if re.search(r"\b(hello|hi|hey|good morning|good afternoon|good evening)\b", lower):
        return "Hello, Sir. Jay is online and ready."

    if "who are you" in lower or "what are you" in lower:
        return "I'm Jay, Sir — your personal AI assistant and the core of the Jay project."

    if "what time" in lower or lower in {"time", "current time"}:
        now = datetime.now(timezone.utc).astimezone()
        return f"It is {now.strftime('%H:%M:%S %Z')}, Sir."

    if lower in {"help", "what can you do"} or "what can you do" in lower:
        return (
            "I can chat with you, use the internet for current information, "
            "remember approved information through Jay's memory system, and "
            "eventually control supported device functions through the security layer, Sir."
        )

    if lower in {"ping", "test"} or "are you online" in lower:
        return "Yes, Sir. Jay's backend is reachable."

    if "calculate " in lower or lower.startswith("what is "):
        expression = q[lower.find("calculate ") + 10:] if "calculate " in lower else ""
        if expression:
            safe = re.fullmatch(r"[0-9+\-*/().%\s]+", expression)
            if safe:
                try:
                    value = eval(expression, {"__builtins__": {}}, {})
                    return f"The answer is {value}, Sir."
                except Exception:
                    pass

    return None


def build_online_answer(
    message: str,
    web_search: Callable[[str], List[Dict[str, str]]],
    article: Callable[[str], Optional[Dict[str, str]]],
) -> tuple[str, str]:
    """Produce a useful answer from public web data without an AI API."""
    results = web_search(message)
    if not results:
        return (
            "I couldn't find reliable information about that online, Sir. "
            "I also don't want to invent an answer.",
            "web",
        )

    best_title = results[0].get("title", "")
    if best_title:
        data = article(best_title)
        if data and data.get("extract"):
            extract = _clean(data["extract"])
            if len(extract) > 3500:
                extract = extract[:3500].rsplit(" ", 1)[0] + "..."
            return (
                f"Here is what I found online, Sir.\n\n{data.get('title', best_title)}\n\n{extract}",
                "web",
            )

    parts = []
    for result in results[:5]:
        title = _clean(result.get("title", ""))
        snippet = _clean(result.get("snippet", ""))
        if title and snippet:
            parts.append(f"{title}\n{snippet}")

    if parts:
        return "I found these online results, Sir.\n\n" + "\n\n".join(parts), "web"
    return "I found the topic online, but couldn't retrieve enough reliable detail, Sir.", "web"
