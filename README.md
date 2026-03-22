# AI Video Generator API

Spring Boot application to generate AI narrated videos from uploaded images and script.

## Generate video

The API accepts both of these routes for compatibility:

- `POST /generate-video`
- `POST /videos/generate`

Use multipart form data with real files (recommended via `-F`), for example:

```bash
curl -X POST 'http://localhost:8080/videos/generate' \
  -F 'images=@1.webp' \
  -F 'images=@2.webp' \
  -F 'script=Your narration script here' \
  -F 'voiceType=female' \
  -F 'duration=20' \
  -F 'format=mp4'
```
