import json
import os
import sys
import unittest

sys.path.insert(0, os.path.dirname(__file__))
from server import app


class JayApiTests(unittest.TestCase):
    def setUp(self):
        self.client = app.test_client()

    def test_health(self):
        response = self.client.get("/health")
        self.assertEqual(response.status_code, 200)
        body = response.get_json()
        self.assertEqual(body["status"], "ok")
        self.assertIn("external_ai_required", body)
        self.assertEqual(
            body["external_ai_required"],
            bool(os.environ.get("GEMINI_API_KEY")),
        )

    def test_local_chat(self):
        response = self.client.post("/api/chat", json={"message": "Hello Jay"})
        self.assertEqual(response.status_code, 200)
        body = response.get_json()
        self.assertEqual(body["status"], "ok")
        self.assertIn("Sir", body["reply"])

    def test_math_is_safe(self):
        response = self.client.post("/api/chat", json={"message": "calculate 12 * (3 + 2)"})
        self.assertEqual(response.status_code, 200)
        self.assertIn("60", response.get_json()["reply"])

    def test_invalid_payload(self):
        response = self.client.post("/api/chat", data=json.dumps({"message": 123}), content_type="application/json")
        self.assertEqual(response.status_code, 400)

    def test_oversized_payload(self):
        response = self.client.post("/api/chat", json={"message": "x" * 4001})
        self.assertEqual(response.status_code, 413)


if __name__ == "__main__":
    unittest.main()
