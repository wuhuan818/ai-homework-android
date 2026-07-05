# Constraints And Decisions

The project prioritized a complete loop: input, generation, result display, editing, favorite/share, and history. It did not expand into accounts, cloud sync, model rankings, image-to-image, inpainting, or a professional editor.

The model integration follows a Qwen / Chat Completions compatible direction because the same configuration model can cover text generation, vision description, and text-to-image workflows while keeping provider replacement manageable. Demo mode is kept intentionally so the review flow can still be reproduced without network access or a configured key; demo output is clearly labeled and is not presented as a real model result.

Image processing is limited to useful basic operations: rotation, watermark, black-and-white filter, center crop, and gesture box crop. API keys, history, and newly generated image files use Android Keystore plus AES-GCM with random IVs and versioned payloads. Generated images stay encrypted in app-private storage and are decrypted only for preview, sharing, or user-initiated gallery export.

Performance work focused on lazy thumbnail loading in History and asynchronous generated-image preview on the Create page, avoiding UI blocking during encrypted image decode.
