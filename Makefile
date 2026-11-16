.PHONY: help backend apk run test clean

help:
	@echo "Novvy targets:"
	@echo "  make backend   Install backend deps into backend/.venv"
	@echo "  make run       Run the FastAPI server on :8000"
	@echo "  make apk       Build the Android debug APK into dist/novvy.apk"
	@echo "  make test      Smoke-test the backend multi-agent run"

backend:
	cd backend && python3 -m venv .venv && . .venv/bin/activate && pip install -q -r requirements.txt

run: backend
	./scripts/run_backend.sh

apk:
	./scripts/build_apk.sh

test: backend
	cd backend && . .venv/bin/activate && python -m novvy "Plan a launch; calculate 12 * 8 + 4"

clean:
	rm -rf backend/.venv
	cd android && ./gradlew clean || true