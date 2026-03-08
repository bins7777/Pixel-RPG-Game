# UI Add-ons

Use this folder for new GUI modules that are separate from the core HUD.

Recommended structure:

- `ui/addons/<feature-name>/` for each add-on.
- Keep raw source previews and final runtime textures together in that feature folder.
- Reference add-on assets from code using the same subfolder path so edits are isolated.

Current add-ons:

- `message_notification_02/`:
  - `Panel + Border.png` (runtime border/panel texture)
  - `Panel.png`
  - `Preview.png`
