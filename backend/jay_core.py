"""Core orchestration for Jay's self-owned backend API."""
from __future__ import annotations

import ast
import operator
import re
from datetime import datetime
from typing import Callable, Optional

from ai_provider import AIProviderError, get_provider

SYSTEM_IDENTITY = (
    "You are Jay, a helpful personal AI assistant. Address the user as Sir. "
    "Be intelligent, calm, warm, concise, and honest. Never claim to have "
    "performed a device action unless the Android app actually reports that "
    "action succeeded. If a request is about the user's phone, prefer the "
    "specific device information supplied by Jay rather than inventing values."
)

_OPERATORS = {
    ast.Add: operator.add,
    ast.Sub: operator.sub,
    ast.Mult: operator.mul,
    ast.Div: operator.truediv,
    ast.Mod: operator.mod,
    ast.Pow: operator.pow,
    ast.USub: operator.neg,
    ast.UAdd: operator.pos,
}


def _safe_math(expression: str):
    if len(expression) > 100 or not re.fullmatch(r"[0-9+\-*/().%\s]+", expression):
        return None
    try:
        tree = ast.parse(expression, mode="eval")

        def evaluate(node):
            if isinstance(node, ast.Expression):
                return evaluate(node.body)
            if isinstance(node, ast.Constant) and isinstance(node.value, (int, float)):
                return node.value
            if isinstance(node, ast.UnaryOp) and type(node.op) in _OPERATORS:
                return _OPERATORS[type(node.op)](evaluate(node.operand))
            if isinstance(node, ast.BinOp) and type(node.op) in _OPERATORS:
                left, right = evaluate(node.left), evaluate(node.right)
                if isinstance(node.op, ast.Pow) and abs(right) > 100:
                    raise ValueError("power too large")
                return _OPERATORS[type(node.op)](left, right)
            raise ValueError("unsupported expression")

        value = evaluate(tree)
        return round(value, 10) if isinstance(value, float) and not value.is_integer() else value
    except Exception:
        return None


def _time_greeting() -> str:
    hour = datetime.now().astimezone().hour
    if 5 <= hour < 12:
        return "Good morning, Sir. Jay is online and ready."
    if 12 <= hour < 18:
        return "Good afternoon, Sir. Jay is online and ready."
    if 18 <= hour < 22:
        return "Good evening, Sir. Jay is online and ready."
    return "Good night, Sir. Jay is online and ready."


def local_answer(message: str) -> Optional[str]:
    """Handle useful requests without consuming cloud AI quota."""
    q = re.sub(r"\s+", " ", message or "").strip()
    lower = q.lower()

    if re.search(r"\b(hello|hi|hey|habari|mambo|niaje|sasa|good morning|good afternoon|good evening|good night)\b", lower):
        return _time_greeting()
    if "who are you" in lower:
        return "I'm Jay, Sir — your personal AI assistant and the core of the Jay project."
    if "are you online" in lower or lower in {"ping", "test"}:
        return "Yes, Sir. Jay's backend is reachable."
    if "what time" in lower or lower in {"time", "current time"}:
        now = datetime.now().astimezone()
        return f"It is {now.strftime('%H:%M:%S %Z')}, Sir."
    if lower in {"help", "what can you do"} or "what can you do" in lower:
        return (
            "I can chat with you, use the internet for current information, "
            "and work with Jay's memory, security and device tools as those "
            "capabilities are connected, Sir."
        )

    expression = None
    if lower.startswith("calculate "):
        expression = q[10:].strip()
    elif lower.startswith("what is ") and re.fullmatch(r"[0-9+\-*/().%\s]+", q[8:].strip()):
        expression = q[8:].strip()
    if expression:
        value = _safe_math(expression)
        if value is not None:
            return f"The answer is {value}, Sir."
    return None


def build_online_answer(message: str, web_search: Callable, article: Callable) -> tuple[str, str]:
    """Use Gemini when configured, otherwise use public web retrieval."""
    try:
        provider = get_provider()
        if provider.name == "gemini":
            answer = provider.chat(
                [{"role": "user", "content": message}],
                system=SYSTEM_IDENTITY,
            )
            return answer, "gemini"
    except AIProviderError:
        # Fall back to the public web path so Jay remains useful when Gemini
        # is not configured, temporarily unavailable, or rate limited.
        pass

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
            extract = re.sub(r"\s+", " ", data["extract"]).strip()
            if len(extract) > 3500:
                extract = extract[:3500].rsplit(" ", 1)[0] + "..."
            return (
                f"Here is what I found online, Sir.\n\n{data.get('title', best_title)}\n\n{extract}",
                "web",
            )

    parts = []
    for result in results[:5]:
        title = re.sub(r"\s+", " ", result.get("title", "")).strip()
        snippet = re.sub(r"\s+", " ", result.get("snippet", "")).strip()
        if title and snippet:
            parts.append(f"{title}\n{snippet}")
    if parts:
        return "I found these online results, Sir.\n\n" + "\n\n".join(parts), "web"
    return "I found the topic online, but couldn't retrieve enough reliable detail, Sir.", "web"
