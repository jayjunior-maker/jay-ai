"""
ai_provider.py -- AI provider interface for Jay's cloud backend.

Gemini is Jay's current cloud AI provider. The API key is read only from
GEMINI_API_KEY / AI_API_KEY on the backend and is never embedded in Android.
"""
from __future__ import annotations

import logging
import os
import time
from abc import ABC, abstractmethod
from typing import Dict, List, Optional

logger = logging.getLogger("jay.ai_provider")


class AIProviderError(Exception):
    pass


class AIProvider(ABC):
    name: str = "base"

    @abstractmethod
    def chat(self, messages: List[Dict[str, str]], system: Optional[str] = None) -> str:
        raise NotImplementedError


class LocalProvider(AIProvider):
    name = "local"

    def chat(self, messages: List[Dict[str, str]], system: Optional[str] = None) -> str:
        last_user = ""
        for message in reversed(messages):
            if message.get("role") == "user":
                last_user = message.get("content", "")
                break
        return f"[local test provider] I heard: {last_user!r}"


class GeminiProvider(AIProvider):
    """Gemini cloud provider using Google's current GenAI Python SDK."""

    name = "gemini"

    def __init__(self, api_key: Optional[str] = None, model: Optional[str] = None):
        self.api_key = api_key or os.environ.get("GEMINI_API_KEY") or os.environ.get("AI_API_KEY")
        self.model = model or os.environ.get("GEMINI_MODEL", "gemini-3.7-flash")
        if not self.api_key:
            raise AIProviderError("Gemini is not configured: set GEMINI_API_KEY on the backend.")

        try:
            from google import genai
        except ImportError as error:
            raise AIProviderError(
                "The google-genai package is not installed. Run: pip install -U google-genai"
            ) from error

        try:
            self._client = genai.Client(api_key=self.api_key)
        except Exception as error:
            raise AIProviderError(f"Could not initialize Gemini: {error}") from error

    def chat(self, messages: List[Dict[str, str]], system: Optional[str] = None) -> str:
        user_text = "\n".join(
            message.get("content", "")
            for message in messages
            if message.get("role") == "user"
        ).strip()
        if not user_text:
            raise AIProviderError("Gemini received an empty user message.")

        try:
            from google.genai import types
        except ImportError as error:
            raise AIProviderError("The google-genai types module is unavailable.") from error

        for attempt in range(1, 4):
            try:
                config = types.GenerateContentConfig(
                    system_instruction=system or (
                        "You are Jay, a helpful personal AI assistant. Address the user as Sir."
                    ),
                    thinking_config=types.ThinkingConfig(
                        thinking_level=os.environ.get("GEMINI_THINKING_LEVEL", "low")
                    ),
                )
                response = self._client.models.generate_content(
                    model=self.model,
                    contents=user_text,
                    config=config,
                )
                reply = (getattr(response, "text", "") or "").strip()
                if not reply:
                    raise AIProviderError("Gemini returned an empty response.")
                return reply
            except AIProviderError:
                raise
            except Exception as error:
                is_last_attempt = attempt == 3
                is_rate_limit = "429" in str(error) or "rate" in str(error).lower()
                logger.warning("Gemini request %d/3 failed: %s", attempt, error)
                if is_last_attempt:
                    raise AIProviderError(f"Gemini request failed: {error}") from error
                time.sleep((2 ** (attempt - 1)) * (2 if is_rate_limit else 1))

        raise AIProviderError("Gemini request failed for an unknown reason.")


def get_provider() -> AIProvider:
    """Factory: AI_PROVIDER=gemini|local. Gemini is the default when configured."""
    provider_name = os.environ.get("AI_PROVIDER", "").lower().strip()
    if not provider_name:
        provider_name = "gemini" if (
            os.environ.get("GEMINI_API_KEY") or os.environ.get("AI_API_KEY")
        ) else "local"

    if provider_name == "local":
        return LocalProvider()
    if provider_name == "gemini":
        return GeminiProvider()

    raise AIProviderError(
        f"Unknown AI_PROVIDER '{provider_name}'. Supported providers: gemini, local."
    )
