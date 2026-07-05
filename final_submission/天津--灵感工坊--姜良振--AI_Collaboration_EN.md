# AI Collaboration

## Human Role

The human contributor made the topic choice, product direction, scope boundaries, real API configuration, real-device testing, and final trade-off decisions. Human feedback identified UI crowding, awkward button wrapping, incomplete prompt display, History/Create page performance issues, encryption boundaries, and crop interaction problems. The human also decided to defer accounts, cloud sync, image-to-image, inpainting, a professional image editor, and multi-model ranking.

## AI Role

GPT was used for task breakdown, planning, prompt design, risk review, and documentation drafts. Codex was used for implementation support, build checks, evidence organization, submission material generation, and Git operations. AI output was constrained by human goals and checked through human judgment; the project is not presented as “entirely built by AI.”

## Key Iterations

The project started with a Mock demo flow and then added real API integration. It expanded from text creation to image description, image generation, history, favorites, sharing, and gallery save. Image processing grew from rotation and watermark to black-and-white filter, center crop, and gesture box crop. Image generation moved from basic calls to prompt optimization and companion-image handoff from text results. Local security expanded from encrypted API keys/history to encrypted generated-image files. Performance work focused on lazy History thumbnails and asynchronous Create-page image preview.

## AI Mistakes And Human Correction

Human review prevented the project from being documented as a generic thesis-style system instead of an Android app. Human testing found layout crowding, wrapped buttons, incomplete prompt display, and page-switching stalls. The install workflow was corrected so the report does not pretend that a rejected install succeeded. The crop roadmap was also clarified: center crop was useful but not the final target, so gesture box crop was implemented later as a separate focused stage.

## Evaluation Abilities Reflected

Critical thinking appears in the decision not to blindly add features, plus clear statements about local encryption boundaries. Questioning ability appears in the staged breakdown from 14-1 to 14-7, with goals, boundaries, and checks for each stage. Integration ability appears in the combination of Compose UI, model APIs, image processing, FileProvider, MediaStore, Android Keystore, AES-GCM, history caching, and performance work. Learning ability appears in the rapid application of real-device debugging, API configuration, encryption, image handling, and performance diagnosis.
