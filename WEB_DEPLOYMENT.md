# Web Deployment

Compose Multiplatform Web is delivered as two artifacts because browser capabilities differ:

- `composeApp/build/dist/wasmJs/productionExecutable` — preferred Wasm build for browsers with WasmGC support
- `composeApp/build/dist/js/productionExecutable` — JavaScript compatibility fallback

Do not merge the generated files from the two directories into one directory because names can collide.

## Recommended layout

```text
/releases/1.7.1/wasm/
/releases/1.7.1/js/
```

Deploy each directory as an immutable artifact and select the public URL according to the browser-support policy of the product. The JS artifact is the conservative fallback when WasmGC support is not guaranteed.

## Server requirements

- Serve `.wasm` with `application/wasm`.
- Serve JavaScript modules with an appropriate JavaScript MIME type.
- Serve `index.html` with short or no caching.
- Serve content-hashed generated assets with long immutable caching.
- Use HTTPS in production.
- Do not inject analytics or third-party scripts without updating `PRIVACY.md` and the security review.

## Required browser verification

Before publishing, verify both artifacts on current Chrome, Firefox, Safari, Android browsers, and iOS Safari. Test:

- initial loading and error handling
- pointer, touch, Space, Enter, and Arrow Up input
- audio playback after a user gesture
- game-state restoration after refresh
- Persian RTL and English LTR
- large text and browser zoom
- `prefers-reduced-motion`
- storage-disabled/private-browsing behavior

## Content Security Policy

The checked-in `index.html` uses an external bootstrap script and a restrictive baseline CSP. Preserve `object-src 'none'`, `base-uri 'none'`, and `frame-ancestors 'none'`. A hosting platform may deliver the equivalent or stricter policy as an HTTP response header. Test the selected Wasm runtime with the final policy before publishing.
