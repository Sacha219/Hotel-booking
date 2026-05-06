# Deployment

## Local Docker Compose

1. Copy `.env.example` to `.env` and change `POSTGRES_PASSWORD`.
2. Start the stack:

```bash
docker compose up --build
```

The local URLs are:

- Frontend: `http://localhost:3000`
- Backend API: `http://localhost:8080/api`
- Backend healthcheck: `http://localhost:8080/actuator/health`

## Render PaaS

This repository includes `render.yaml` for a Render Blueprint:

- `royalstay-backend`: Docker web service
- `royalstay-frontend`: static web service
- `royalstay-db`: Render Postgres

Create the Blueprint in Render from the repository root. If Render changes the generated `onrender.com` hostnames, update these values in `render.yaml`:

- Backend `CORS_ALLOWED_ORIGINS`
- Frontend `VITE_API_URL`

Render Free is suitable for demo and lab deployments. Free Postgres is limited and expires after 30 days, so do not use it for production data.

## GitHub Actions Secrets And Variables

Create these repository secrets:

- `RENDER_BACKEND_DEPLOY_HOOK_URL`: deploy hook URL from the backend service settings.
- `RENDER_FRONTEND_DEPLOY_HOOK_URL`: deploy hook URL from the frontend service settings.

Create these repository variables:

- `APP_HEALTH_URL`: `https://royalstay-backend.onrender.com/actuator/health`
- `APP_URL`: `https://royalstay-frontend.onrender.com`

The workflow `.github/workflows/ci-cd.yml` runs backend tests, frontend lint/build, Docker image builds, deploys on pushes to `main`, and then checks the backend health endpoint.
