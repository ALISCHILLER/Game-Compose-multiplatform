#!/usr/bin/env python3
"""Reproducibly generate all MSA Bee-owned PNG icons and PCM audio assets.

Only the Python standard library is used so asset regeneration works in clean CI
and developer environments without Pillow or other third-party packages.
"""
from __future__ import annotations

from pathlib import Path
import math
import os
import struct
import wave
import zlib

ROOT = Path(os.environ.get("MSA_BEE_ASSET_ROOT", Path(__file__).resolve().parents[1])).resolve()
RATE = 22_050

Color = tuple[int, int, int, int]


class Raster:
    def __init__(self, width: int, height: int, background: Color) -> None:
        self.width = width
        self.height = height
        self.pixels = bytearray(background * (width * height))

    def blend(self, x: int, y: int, color: Color) -> None:
        if x < 0 or y < 0 or x >= self.width or y >= self.height:
            return
        index = (y * self.width + x) * 4
        source_alpha = color[3] / 255.0
        inverse_alpha = 1.0 - source_alpha
        for channel in range(3):
            destination = self.pixels[index + channel]
            self.pixels[index + channel] = round(color[channel] * source_alpha + destination * inverse_alpha)
        destination_alpha = self.pixels[index + 3]
        self.pixels[index + 3] = round(color[3] + destination_alpha * inverse_alpha)

    def ellipse(self, box: tuple[float, float, float, float], color: Color) -> None:
        left, top, right, bottom = box
        center_x = (left + right) / 2.0
        center_y = (top + bottom) / 2.0
        radius_x = max((right - left) / 2.0, 0.5)
        radius_y = max((bottom - top) / 2.0, 0.5)
        min_x = max(0, math.floor(left))
        max_x = min(self.width - 1, math.ceil(right))
        min_y = max(0, math.floor(top))
        max_y = min(self.height - 1, math.ceil(bottom))
        for y in range(min_y, max_y + 1):
            normalized_y = ((y + 0.5) - center_y) / radius_y
            for x in range(min_x, max_x + 1):
                normalized_x = ((x + 0.5) - center_x) / radius_x
                if normalized_x * normalized_x + normalized_y * normalized_y <= 1.0:
                    self.blend(x, y, color)

    def rounded_rectangle(
        self,
        box: tuple[float, float, float, float],
        radius: float,
        color: Color,
    ) -> None:
        left, top, right, bottom = box
        radius = max(0.0, min(radius, (right - left) / 2.0, (bottom - top) / 2.0))
        min_x = max(0, math.floor(left))
        max_x = min(self.width - 1, math.ceil(right))
        min_y = max(0, math.floor(top))
        max_y = min(self.height - 1, math.ceil(bottom))
        inner_left = left + radius
        inner_right = right - radius
        inner_top = top + radius
        inner_bottom = bottom - radius
        radius_squared = radius * radius

        for y in range(min_y, max_y + 1):
            py = y + 0.5
            for x in range(min_x, max_x + 1):
                px = x + 0.5
                inside = inner_left <= px <= inner_right or inner_top <= py <= inner_bottom
                if not inside and radius > 0:
                    corner_x = inner_left if px < inner_left else inner_right
                    corner_y = inner_top if py < inner_top else inner_bottom
                    dx = px - corner_x
                    dy = py - corner_y
                    inside = dx * dx + dy * dy <= radius_squared
                if inside:
                    self.blend(x, y, color)

    def triangle(
        self,
        first: tuple[float, float],
        second: tuple[float, float],
        third: tuple[float, float],
        color: Color,
    ) -> None:
        min_x = max(0, math.floor(min(first[0], second[0], third[0])))
        max_x = min(self.width - 1, math.ceil(max(first[0], second[0], third[0])))
        min_y = max(0, math.floor(min(first[1], second[1], third[1])))
        max_y = min(self.height - 1, math.ceil(max(first[1], second[1], third[1])))

        def edge(a: tuple[float, float], b: tuple[float, float], point: tuple[float, float]) -> float:
            return (point[0] - a[0]) * (b[1] - a[1]) - (point[1] - a[1]) * (b[0] - a[0])

        for y in range(min_y, max_y + 1):
            for x in range(min_x, max_x + 1):
                point = (x + 0.5, y + 0.5)
                first_edge = edge(first, second, point)
                second_edge = edge(second, third, point)
                third_edge = edge(third, first, point)
                if (first_edge >= 0 and second_edge >= 0 and third_edge >= 0) or (
                    first_edge <= 0 and second_edge <= 0 and third_edge <= 0
                ):
                    self.blend(x, y, color)

    def png_bytes(self, rgb: bool = False) -> bytes:
        color_type = 2 if rgb else 6
        rows = bytearray()
        for y in range(self.height):
            rows.append(0)  # PNG filter type: None
            start = y * self.width * 4
            row = self.pixels[start:start + self.width * 4]
            if rgb:
                for x in range(self.width):
                    pixel = x * 4
                    rows.extend(row[pixel:pixel + 3])
            else:
                rows.extend(row)

        signature = b"\x89PNG\r\n\x1a\n"
        header = struct.pack(">IIBBBBB", self.width, self.height, 8, color_type, 0, 0, 0)
        payload = signature + png_chunk(b"IHDR", header)
        payload += png_chunk(b"IDAT", zlib.compress(bytes(rows), level=9))
        payload += png_chunk(b"IEND", b"")
        return payload

    def write_png(self, path: Path, rgb: bool = False) -> None:
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_bytes(self.png_bytes(rgb=rgb))


def png_chunk(chunk_type: bytes, data: bytes) -> bytes:
    checksum = zlib.crc32(chunk_type)
    checksum = zlib.crc32(data, checksum) & 0xFFFFFFFF
    return struct.pack(">I", len(data)) + chunk_type + data + struct.pack(">I", checksum)


def make_icon(size: int) -> Raster:
    image = Raster(size, size, (47, 28, 15, 255))
    scale = size / 1024.0

    def box(left: float, top: float, right: float, bottom: float) -> tuple[float, float, float, float]:
        return left * scale, top * scale, right * scale, bottom * scale

    image.ellipse(box(120, 120, 904, 904), (255, 201, 40, 255))
    image.ellipse(box(180, 180, 844, 844), (255, 224, 118, 255))
    image.ellipse(box(245, 180, 525, 500), (255, 255, 255, 210))
    image.ellipse(box(500, 180, 780, 500), (255, 255, 255, 210))
    image.ellipse(box(250, 360, 790, 720), (255, 190, 20, 255))
    for x in (390, 520, 650):
        image.rounded_rectangle(box(x, 380, x + 65, 700), 24 * scale, (74, 42, 18, 255))
    image.ellipse(box(700, 405, 900, 650), (74, 42, 18, 255))
    image.ellipse(box(785, 455, 855, 525), (255, 255, 255, 255))
    image.ellipse(box(815, 475, 845, 505), (0, 0, 0, 255))
    image.triangle(
        (250 * scale, 500 * scale),
        (150 * scale, 550 * scale),
        (250 * scale, 600 * scale),
        (74, 42, 18, 255),
    )
    return image


def write_ico(path: Path, sizes: tuple[int, ...]) -> None:
    images = [(size, make_icon(size).png_bytes()) for size in sizes]
    header_size = 6 + 16 * len(images)
    entries = bytearray()
    payload = bytearray()
    offset = header_size
    for size, image in images:
        dimension = 0 if size >= 256 else size
        entries.extend(struct.pack(
            "<BBBBHHII",
            dimension,
            dimension,
            0,
            0,
            1,
            32,
            len(image),
            offset,
        ))
        payload.extend(image)
        offset += len(image)
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_bytes(struct.pack("<HHH", 0, 1, len(images)) + entries + payload)


def write_icns(path: Path) -> None:
    icon_types = (
        (b"icp4", 16),
        (b"icp5", 32),
        (b"icp6", 64),
        (b"ic07", 128),
        (b"ic08", 256),
        (b"ic09", 512),
        (b"ic10", 1024),
    )
    chunks = bytearray()
    for icon_type, size in icon_types:
        image = make_icon(size).png_bytes()
        chunks.extend(icon_type)
        chunks.extend(struct.pack(">I", 8 + len(image)))
        chunks.extend(image)
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_bytes(b"icns" + struct.pack(">I", 8 + len(chunks)) + chunks)


def envelope(time: float, duration: float, attack: float = 0.02, release: float = 0.08) -> float:
    attack_level = min(1.0, time / max(attack, 1e-6))
    release_level = min(1.0, (duration - time) / max(release, 1e-6))
    return max(0.0, min(attack_level, release_level))


def write_wav(path: Path, samples: list[float]) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    with wave.open(str(path), "wb") as output:
        output.setnchannels(1)
        output.setsampwidth(2)
        output.setframerate(RATE)
        frames = bytearray()
        for sample in samples:
            frames.extend(struct.pack("<h", max(-32767, min(32767, int(sample * 32767)))))
        output.writeframes(bytes(frames))


def generate_audio() -> dict[str, list[float]]:
    duration = 8.0
    notes = [261.63, 329.63, 392.0, 329.63, 293.66, 349.23, 440.0, 349.23]
    music: list[float] = []
    for index in range(int(RATE * duration)):
        time = index / RATE
        note = notes[int(time) % len(notes)]
        local_time = time % 1.0
        volume = envelope(local_time, 1.0, 0.03, 0.18)
        music.append((
            math.sin(2 * math.pi * note * time) * 0.17
            + math.sin(2 * math.pi * (note / 2) * time) * 0.08
        ) * volume)

    jump_duration = 0.16
    jump: list[float] = []
    for index in range(int(RATE * jump_duration)):
        time = index / RATE
        frequency = 520 + 900 * (time / jump_duration)
        jump.append(
            math.sin(2 * math.pi * frequency * time)
            * 0.34
            * envelope(time, jump_duration, 0.005, 0.06)
        )

    score_duration = 0.24
    score: list[float] = []
    for index in range(int(RATE * score_duration)):
        time = index / RATE
        split = score_duration * 0.48
        frequency = 740.0 if time < split else 988.0
        score.append((
            math.sin(2 * math.pi * frequency * time) * 0.30
            + math.sin(2 * math.pi * frequency * 2.0 * time) * 0.07
        ) * envelope(time, score_duration, 0.004, 0.07))

    over_duration = 0.9
    game_over: list[float] = []
    for index in range(int(RATE * over_duration)):
        time = index / RATE
        frequency = 440 - 310 * (time / over_duration)
        game_over.append((
            math.sin(2 * math.pi * frequency * time) * 0.28
            + math.sin(2 * math.pi * frequency * 0.5 * time) * 0.12
        ) * envelope(time, over_duration, 0.01, 0.2))

    return {
        "game_sound.wav": music,
        "jump.wav": jump,
        "score.wav": score,
        "game_over.wav": game_over,
    }


def main() -> None:
    ios_icon = ROOT / "iosApp/iosApp/Assets.xcassets/AppIcon.appiconset/app-icon-1024.png"
    make_icon(1024).write_png(ios_icon, rgb=True)

    desktop_icons = ROOT / "composeApp/src/jvmMain/resources/icons"
    make_icon(512).write_png(desktop_icons / "msa-bee.png")
    write_ico(desktop_icons / "msa-bee.ico", (16, 32, 48, 64, 128, 256))
    write_icns(desktop_icons / "msa-bee.icns")

    for density, size in {"mdpi": 48, "hdpi": 72, "xhdpi": 96, "xxhdpi": 144, "xxxhdpi": 192}.items():
        icon = make_icon(size)
        for filename in ("ic_launcher.png", "ic_launcher_round.png"):
            destination = ROOT / f"composeApp/src/androidMain/res/mipmap-{density}/{filename}"
            icon.write_png(destination)

    for filename, samples in generate_audio().items():
        write_wav(ROOT / "composeApp/src/commonMain/composeResources/files" / filename, samples)
        write_wav(ROOT / "composeApp/src/androidMain/res/raw" / filename, samples)

    print("MSA Bee assets generated successfully with the Python standard library.")


if __name__ == "__main__":
    main()
