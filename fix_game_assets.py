# fix_game_assets.py
# Run from project root:
# python fix_game_assets.py
#
# نیاز دارد:
# pip install pillow

from pathlib import Path
from PIL import Image

DRAWABLE_DIR = Path("composeApp/src/commonMain/composeResources/drawable")

BEE_SPRITE = DRAWABLE_DIR / "bee_sprite.png"
IMAGE_BEE = DRAWABLE_DIR / "image_bee.png"
MOVING_BACKGROUND = DRAWABLE_DIR / "moving_background.png"


def remove_light_checker_background(image: Image.Image) -> Image.Image:
    """
    بک‌گراند سفید/طوسی شطرنجی را شفاف می‌کند.
    برای assetهای فعلی پروژه مناسب است.
    """
    image = image.convert("RGBA")
    pixels = image.load()

    for y in range(image.height):
        for x in range(image.width):
            r, g, b, a = pixels[x, y]

            is_near_white = r > 238 and g > 238 and b > 238
            is_light_gray_checker = abs(r - g) < 10 and abs(g - b) < 10 and r > 205

            if is_near_white or is_light_gray_checker:
                pixels[x, y] = (r, g, b, 0)

    return image


def crop_to_visible(image: Image.Image, padding: int = 0) -> Image.Image:
    image = image.convert("RGBA")
    bbox = image.getbbox()

    if bbox is None:
        return image

    left, top, right, bottom = bbox

    left = max(0, left - padding)
    top = max(0, top - padding)
    right = min(image.width, right + padding)
    bottom = min(image.height, bottom + padding)

    return image.crop((left, top, right, bottom))


def center_on_canvas(image: Image.Image, size: tuple[int, int]) -> Image.Image:
    canvas = Image.new("RGBA", size, (0, 0, 0, 0))
    x = (size[0] - image.width) // 2
    y = (size[1] - image.height) // 2
    canvas.alpha_composite(image, (x, y))
    return canvas


def create_bee_sprite() -> None:
    """
    Sprite اصلی پروژه خراب بود:
    عکس 1254x1254 بود ولی کد آن را 80x80 می‌خواند.
    این تابع آن را به Sprite Sheet واقعی 3x3 با فریم‌های 80x80 تبدیل می‌کند.
    """
    source_path = IMAGE_BEE if IMAGE_BEE.exists() else BEE_SPRITE

    bee = Image.open(source_path)
    bee = remove_light_checker_background(bee)
    bee = crop_to_visible(bee, padding=16)

    frame_size = 80
    sheet_size = 240

    sprite_sheet = Image.new("RGBA", (sheet_size, sheet_size), (0, 0, 0, 0))

    rotations = [-10, -6, -2, 2, 6, 10, 6, 2, -4]
    offsets_y = [3, 0, -3, -5, -2, 2, 4, 1, -2]

    base = bee.copy()
    base.thumbnail((68, 68), Image.Resampling.LANCZOS)

    for index in range(9):
        frame = Image.new("RGBA", (frame_size, frame_size), (0, 0, 0, 0))

        rotated = base.rotate(
            rotations[index],
            resample=Image.Resampling.BICUBIC,
            expand=True
        )

        x = (frame_size - rotated.width) // 2
        y = (frame_size - rotated.height) // 2 + offsets_y[index]

        frame.alpha_composite(rotated, (x, y))

        col = index % 3
        row = index // 3

        sprite_sheet.alpha_composite(
            frame,
            (col * frame_size, row * frame_size)
        )

    sprite_sheet.save(BEE_SPRITE)


def fix_image_bee() -> None:
    if not IMAGE_BEE.exists():
        return

    bee = Image.open(IMAGE_BEE)
    bee = remove_light_checker_background(bee)
    bee.save(IMAGE_BEE)


def fix_moving_background() -> None:
    """
    moving_background قبلاً تصویر بزرگ با فضای سفید داشت.
    این تابع فقط زمین واقعی را نگه می‌دارد و بک‌گراند سفید را شفاف می‌کند.
    """
    if not MOVING_BACKGROUND.exists():
        return

    ground = Image.open(MOVING_BACKGROUND)
    ground = remove_light_checker_background(ground)

    bbox = ground.getbbox()
    if bbox is None:
        return

    left, top, right, bottom = bbox

    # کمی بالاتر از زمین را هم نگه می‌داریم تا کات تیز نخورد.
    top = max(0, top - 10)

    ground = ground.crop((left, top, right, bottom))
    ground.save(MOVING_BACKGROUND)


def main() -> None:
    if not DRAWABLE_DIR.exists():
        raise FileNotFoundError(f"Drawable directory not found: {DRAWABLE_DIR}")

    fix_image_bee()
    create_bee_sprite()
    fix_moving_background()

    print("Assets fixed successfully:")
    print(f"- {BEE_SPRITE}")
    print(f"- {IMAGE_BEE}")
    print(f"- {MOVING_BACKGROUND}")


if __name__ == "__main__":
    main()