"""
ai_provider.py -- AIProvider interface for Jay's cloud backend.
No Gemini, per project rules.
"""
from __future__ import annotations
import os
import time
import logging
from abc import ABC, abstractmethod
from typing import List, Dict, Optional

logger = logging.getLogger("jay.ai_provider")


class AIProviderError(Exception):
    pass


class AIProvider(ABC):
    name: str = "base"

    @abstractmethod
    def chat(self, messages: List[Dict[str, str]], system: Optional[str] = None) -> str:
        raise NotImplementedError


class LocalProvider(AIProvider):
    """Offline, deterministic. Used by /health and dev/testing only."""
    name = "local"

    def chat(self, messages: List[Dict[str, str]], system: Optional[str] = None) -> str:
        last_user = ""
        for m in reversed(messages):
            if m.get("role") == "user":
                last_user = m.get("content", "")
                break
        return f"[local test provider] I heard: {last_user!r}"


class AnthropicProvider(AIProvider):
    """Real cloud AI provider using the Anthropic Claude API."""
    name = "anthropic"

    def __init__(self, api_key: Optional[str] = None, model: Optional[str] = None):
        self.api_key = api_key or os.environ.get("AI_API_KEY")
        self.model = model or os.environ.get("AI_MODEL", "claude-sonnet-4-6")
        if not self.api_key:
            raise AIProviderError("AnthropicProvider requires AI_API_KEY to be set in the environment.")
        try:
            import anthropic
        except ImportError as e:
            raise AIProviderError("The 'anthropic' package is not installed. Run: pip install anthropic") from e
        self._client = anthropic.Anthropic(api_key=self.api_key)

    def chat(self, messages: List[Dict[str, str]], system: Optional[str] = None) -> str:
        max_retries = 3
        backoff_seconds = 1.5
        for attempt in range(1, max_retries + 1):
            try:
                kwargs = {"model": self.model, "max_tokens": 1024, "messages": messages}
                if system:
                    kwargs["system"] = system
                response = self._client.messages.create(**kwargs)
                text_parts = [b.text for b in response.content if getattr(b, "type", None) == "text"]
                return "".join(text_parts).strip()
            except Exception as e:
                is_rate_limit = "429" in str(e) or "rate_limit" in str(e).lower()
                is_last_attempt = attempt == max_retries
                logger.warning("AnthropicProvider.chat attempt %d/%d failed: %s", attempt, max_retries, e)
                if is_last_attempt:
                    raise AIProviderError(f"AI provider failed after {max_retries} attempts: {e}") from e
                sleep_time = backoff_seconds * (2 ** (attempt - 1))
                if is_rate_limit:
                    sleep_time *= 2
                time.sleep(sleep_time)
        raise AIProviderError("AI provider failed for an unknown reason.")


def get_provider() -> AIProvider:
    """Factory. AI_PROVIDER=local|anthropic. No gemini option, per project rules."""
    provider_name = os.environ.get("AI_PROVIDER")
    if provider_name is None:
        provider_name = "anthropic" if os.environ.get("AI_API_KEY") else "local"
    provider_name = provider_name.lower().strip()
    if provider_name == "local":
        return LocalProvider()
    elif provider_name == "anthropic":
        return AnthropicProvider()
    else:
        raise AIProviderError(f"Unknown AI_PROVIDER '{provider_name}'. Supported: local, anthropic.")
