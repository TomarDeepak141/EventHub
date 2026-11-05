# EventHub frontend

React 19, TypeScript, Vite, Tailwind CSS, React Router, Axios, TanStack Query, React Hook Form, Zod, and Lucide React.

## Local development

```powershell
Copy-Item .env.example .env
npm install
npm run dev
```

Open `http://localhost:5173`. Vite proxies `/api` to `http://localhost:8080`, so the frontend works with the backend's current API without requiring a CORS contract change. Start the backend with `mvn spring-boot:run -Dspring-boot.run.profiles=demo` to enable the mock-payment checkout flow. Set `VITE_API_PROXY_TARGET` to a different backend origin. `VITE_API_BASE_URL=/api` is a public URL setting, not a secret. Backend integration needs the Spring application and its configured PostgreSQL/Redis services; form/unit tests do not.

For the complete local Docker deployment, see the root [Docker local demo guide](../README.md#docker-local-demo-d1d2). The production frontend image compiles the Vite app and serves it from unprivileged Nginx on container port 8080. Nginx serves SPA routes and hashed assets, limits request bodies to 2 MB, and proxies `/api/*` to the Compose backend after removing the `/api` prefix.

## Checks

```powershell
npm test
npm run build
```

## Implemented pages

- Home page with editorial hero, event-type shortcuts, and featured upcoming events.
- Browse events with city, event type, start/end date, venue ID, status, category price bounds, sorting, and page controls.
- Event details with event-specific seat availability, category prices, public paginated reviews, and customer review eligibility/editing after a completed confirmed booking.
- Customer registration, login, protected routing, role-aware navigation, and editable profile.
- Customer seat selection at `/events/:eventId/seats`, with live `AVAILABLE`, `LOCKED`, and `CONFIRMED_BOOKED` states, live server pricing, a ten-seat limit, temporary reservation countdown, release, and conflict retry.
- Checkout at `/checkout/:bookingId`, with a server booking summary, demo-profile mock payment initiation, sandbox success/failure, retry, payment cancellation, booking cancellation, and status refresh.
- Booking confirmation at `/bookings/:bookingId/confirmation`, guarded by the backend `CONFIRMED` status and showing issued ticket references and QR-safe payloads.
- Customer dashboard at `/dashboard/customer` with profile summary, status-filtered paginated booking history, saved-event wishlist, booking detail, payment attempts, ticket states, and QR codes rendered from backend ticket payloads.
- Organizer workspace at `/dashboard/organizer` for owned event creation, editing, status controls, ticket admission verification, and database-backed event booking/ticket/review aggregates.
- Admin workspace at `/dashboard/admin` for database-backed platform totals, account roles/activation, venue/screen/category/seat inventory, and event status moderation.

The API client reads the Spring `Page` result directly. In development, the Axios base URL is `/api` and the Vite proxy strips that prefix before forwarding. The existing EventHub API does not return a currency code, so prices are shown as raw amounts. Mock payment actions require the backend's explicit `demo` profile; the browser never declares a booking paid, and tickets appear only after the server reports `CONFIRMED`. Ticket QR codes are generated locally from the opaque backend QR payload. Statistics come from the secured organizer/admin aggregate endpoints and reflect persisted database records; detailed organizer attendee lists remain unavailable.
