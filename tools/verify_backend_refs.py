#!/usr/bin/env python3
"""Static consistency checks for the Cryonix + Pojav Android integration.

The launcher mixes a Kotlin application layer with a vendored Pojav backend
(``net.kdt.pojavlaunch``). Both share one Android resource namespace, so a
typo or a resource that was never ported from the backend source tree only
shows up when Gradle runs on a machine with the Android SDK installed.

This script catches those problems without an Android SDK:

* every ``R.<type>.<name>`` used from Java/Kotlin resolves against
  ``src/main/res`` (plus the resources that build dependencies and
  ``resValue`` declarations provide)
* every ``@type/name`` used from resource XML resolves too
* resource XML and the manifest are well formed and their attribute/attribute
  definitions stay consistent with the sources
* classes referenced by the manifest exist as source files
* Kotlin code that calls into the vendored backend only references classes that
  actually exist

Run it from the repository root:

    python3 tools/verify_backend_refs.py

Exit code is non-zero when a real gap is found. ``--upstream <res dir>`` adds a
report of resources that exist in the upstream Pojav backend but not here,
which is the list to check when the backend is updated.
"""

from __future__ import annotations

import argparse
import os
import re
import sys
import xml.etree.ElementTree as ET
from collections import defaultdict

REPO_ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
APP = os.path.join(REPO_ROOT, "main_cryonixlauncher", "src", "main")
RES = os.path.join(APP, "res")
JAVA = os.path.join(APP, "java")
KOTLIN = os.path.join(APP, "kotlin")
MANIFEST = os.path.join(APP, "AndroidManifest.xml")
LAUNCHER_NAMESPACE = "com.cryonix.launcher"

FILE_TYPES = (
    "layout", "drawable", "mipmap", "font", "xml", "anim", "animator",
    "menu", "raw", "navigation", "transition", "interpolator",
)
VALUE_TYPES = (
    "string", "string-array", "array", "integer-array", "color", "style",
    "dimen", "bool", "integer", "plurals", "attr", "item", "fraction",
)
# Android collapses every XML array tag into R.array
ARRAY_ALIASES = ("array", "string-array", "integer-array")

# Resources that come from build dependencies or resValue declarations rather
# than from this module's res/ tree.
LIBRARY_DIMEN = re.compile(r"_\d+(?:\.\d+)?(?:sdp|ssp)$")
LIBRARY_STRINGS = {
    "application_package", "storageProviderAuthorities", "shareProviderAuthority",
    "curseforge_api_key",
}
LIBRARY_STYLES = re.compile(
    r"^(Theme|TextAppearance|Widget|Base|Platform)\.(AppCompat|Material|Theme)\b"
    r"|^Theme\.AppCompat|^Widget\.AppCompat|^TextAppearance\.AppCompat"
    r"|^PreferenceThemeOverlay"
)
LIBRARY_ATTRS = {
    "colorAccent", "editTextStyle", "progressBarStyleHorizontal",
    "seekBarPreferenceStyle", "colorPrimary", "colorPrimaryDark", "windowBackground",
}
LIBRARY_DRAWABLES = {
    "button_a", "button_b", "button_x", "button_y", "button_start", "button_select",
    "dpad_up", "dpad_down", "dpad_left", "dpad_right",
    "shoulder_left", "shoulder_right", "trigger_left", "trigger_right",
    "stick_left", "stick_right", "stick_left_click", "stick_right_click",
}
LIBRARY_COLORS = {"darker_gray", "transparent", "white", "black", "holo_blue_bright"}
LIBRARY_IDS = {"seekbar", "seekbar_value", "text1", "progress", "background"}
LIBRARY_LAYOUTS = {
    "simple_list_item_1", "simple_list_item_single_choice", "simple_spinner_item",
    "simple_spinner_dropdown_item", "support_simple_spinner_dropdown_item",
    "simple_expandable_list_item_1",
}

errors: list[str] = []
warnings: list[str] = []


def rel(path: str) -> str:
    return os.path.relpath(path, REPO_ROOT)


def read(path: str) -> str:
    with open(path, "r", encoding="utf-8", errors="replace") as handle:
        return handle.read()


def source_files(*roots: str):
    for root in roots:
        for dirpath, _dirnames, filenames in os.walk(root):
            for name in filenames:
                if name.endswith((".java", ".kt")):
                    yield os.path.join(dirpath, name)


def res_xml_files():
    for dirpath, _dirnames, filenames in os.walk(RES):
        for name in filenames:
            if name.endswith(".xml"):
                yield os.path.join(dirpath, name)


def is_library_resource(res_type: str, name: str) -> bool:
    if res_type == "dimen" and LIBRARY_DIMEN.match(name):
        return True
    if res_type == "string" and name in LIBRARY_STRINGS:
        return True
    if res_type == "style" and LIBRARY_STYLES.match(name):
        return True
    if res_type == "attr" and name in LIBRARY_ATTRS:
        return True
    if res_type == "drawable" and name in LIBRARY_DRAWABLES:
        return True
    if res_type == "color" and name in LIBRARY_COLORS:
        return True
    if res_type == "layout" and name in LIBRARY_LAYOUTS:
        return True
    if res_type == "id" and name in LIBRARY_IDS:
        return True
    return False


# Extensions that can appear on resource files. `.9.png` is checked before
# `.png` so a nine-patch defines the same resource name as a plain drawable.
RESOURCE_EXTENSIONS = (
    ".9.png", ".png", ".webp", ".jpg", ".jpeg", ".gif", ".bmp",
    ".ttf", ".otf", ".ttc", ".xml.json",
)


def resource_name(filename: str) -> str:
    """Resource name defined by `filename` (``foo.9.png`` -> ``foo``)."""
    lowered = filename.lower()
    for ext in RESOURCE_EXTENSIONS:
        if lowered.endswith(ext):
            return filename[: -len(ext)]
    return os.path.splitext(filename)[0]


# --------------------------------------------------------------------------
# resource definitions
# --------------------------------------------------------------------------

R_REF = re.compile(r"(?<![\w.])R\.([A-Za-z_][A-Za-z0-9_]*)\.([A-Za-z_][A-Za-z0-9_]*)")
# "@type/name", "@+id/name", plus framework references ("@android:type/name",
# "?android:attr/name") which are not ours to resolve.
XML_REF = re.compile(r"([@?])(android:)?([a-z]+)/([A-Za-z_][A-Za-z0-9_.]*)")
# "@+id/name" style declarations create resources instead of referencing them
ID_DECL = re.compile(r"@\+([a-z]+)/([A-Za-z_][A-Za-z0-9_]*)")


def collect_defined_resources():
    """Everything the module itself defines, keyed the way R.<type> sees it."""
    defined: dict[str, set[str]] = defaultdict(set)

    for path in res_xml_files():
        folder = os.path.basename(os.path.dirname(path)).split("-")[0]
        text = read(path)
        for match in ID_DECL.finditer(text):
            defined[match.group(1)].add(match.group(2))

    for dirpath, _dirnames, filenames in os.walk(RES):
        folder = os.path.basename(dirpath)
        base_folder = folder.split("-")[0]
        for name in filenames:
            full = os.path.join(dirpath, name)
            if base_folder == "values":
                if not name.endswith(".xml"):
                    continue
                try:
                    root = ET.parse(full).getroot()
                except ET.ParseError as exc:
                    errors.append(f"{rel(full)}: invalid XML ({exc})")
                    continue
                for child in root:
                    res_type = child.tag
                    if res_type == "item":
                        res_type = child.get("type") or "item"
                    res_name = child.get("name")
                    if not res_name:
                        continue
                    if res_type in ARRAY_ALIASES:
                        for alias in ARRAY_ALIASES:
                            defined[alias].add(res_name)
                        continue
                    defined[res_type].add(res_name)
                    if res_type == "declare-styleable":
                        # <attr> children of a styleable are also plain attrs
                        for attr in child.findall("attr"):
                            if attr.get("name"):
                                defined["attr"].add(attr.get("name"))
                continue
            if base_folder in FILE_TYPES:
                defined[base_folder].add(resource_name(name))
    return defined


# --------------------------------------------------------------------------
# references
# --------------------------------------------------------------------------



def collect_source_refs():
    refs = []
    for path in source_files(JAVA, KOTLIN):
        text = read(path)
        for match in R_REF.finditer(text):
            res_type, name = match.group(1), match.group(2)
            if res_type not in FILE_TYPES and res_type not in VALUE_TYPES and res_type != "id":
                continue
            line = text.count("\n", 0, match.start()) + 1
            refs.append((path, line, res_type, name))
    return refs


def collect_xml_refs():
    refs = []
    for path in res_xml_files():
        text = read(path)
        try:
            ET.parse(path)
        except ET.ParseError as exc:
            errors.append(f"{rel(path)}: invalid XML ({exc})")
            continue
        for match in XML_REF.finditer(text):
            marker, android_prefix, res_type, res_name = match.groups()
            if android_prefix is not None:
                continue  # framework resource
            if marker == "?":
                continue  # theme attribute reference
            if res_name.startswith("+"):
                continue
            line = text.count("\n", 0, match.start()) + 1
            refs.append((path, line, res_type, res_name))
    return refs


def check_resources(defined):
    unresolved = defaultdict(list)
    for path, line, res_type, name in collect_source_refs() + collect_xml_refs():
        if res_type not in FILE_TYPES and res_type not in VALUE_TYPES and res_type != "id":
            continue
        if name in defined.get(res_type, set()):
            continue
        if is_library_resource(res_type, name):
            continue
        if res_type == "id" and name == "content":
            continue  # android.R.id.content
        unresolved[(res_type, name)].append(f"{rel(path)}:{line}")

    for (res_type, name), where in sorted(unresolved.items()):
        errors.append(
            f"unresolved @{res_type}/{name} used by " + ", ".join(sorted(set(where))[:4])
        )
    return unresolved


# --------------------------------------------------------------------------
# classes
# --------------------------------------------------------------------------

def index_classes():
    classes = set()
    for path in source_files(JAVA, KOTLIN):
        text = read(path)
        match = re.search(r"^\s*package\s+([A-Za-z0-9_.]+)", text, re.MULTILINE)
        if not match:
            warnings.append(f"{rel(path)}: no package declaration")
            continue
        simple = os.path.splitext(os.path.basename(path))[0]
        classes.add(f"{match.group(1)}.{simple}")
    return classes


POJAV_CLASS_REF = re.compile(r"\bnet\.kdt\.pojavlaunch\.[A-Za-z0-9_.]+")


def check_backend_class_refs(classes):
    for path in source_files(JAVA, KOTLIN):
        if "/net/kdt/pojavlaunch/" in path:
            continue
        text = read(path)
        for match in POJAV_CLASS_REF.finditer(text):
            symbol = match.group(0).rstrip(".")
            if symbol.endswith(("R", "BuildConfig")):
                continue
            if symbol in classes or symbol.rsplit(".", 1)[0] in classes:
                continue
            if any(c.startswith(symbol + ".") for c in classes):
                continue
            line = text.count("\n", 0, match.start()) + 1
            errors.append(f"{rel(path)}:{line}: unknown backend class {symbol}")


# --------------------------------------------------------------------------
# manifest
# --------------------------------------------------------------------------

def check_manifest(classes):
    if not os.path.exists(MANIFEST):
        errors.append("AndroidManifest.xml is missing")
        return
    try:
        root = ET.parse(MANIFEST).getroot()
    except ET.ParseError as exc:
        errors.append(f"{rel(MANIFEST)}: invalid XML ({exc})")
        return

    android = "{http://schemas.android.com/apk/res/android}"
    application = root.find("application")
    if application is None:
        errors.append("AndroidManifest.xml has no <application>")
        return

    for tag in ("activity", "activity-alias", "service", "provider", "receiver"):
        for node in application.findall(tag):
            name = node.get(android + "name") or ""
            if not name:
                continue
            target = node.get(android + "targetActivity") if tag == "activity-alias" else None
            for candidate in (target or name,):
                fqcn = candidate
                if candidate.startswith("."):
                    fqcn = LAUNCHER_NAMESPACE + candidate
                elif "." not in candidate:
                    fqcn = LAUNCHER_NAMESPACE + "." + candidate
                if fqcn not in classes:
                    errors.append(f"AndroidManifest.xml: {tag} {fqcn} has no source file")

    process = application.get(android + "process")
    if process == ":launcher":
        warnings.append(
            "application runs in a :launcher process; the Pojav :game process "
            "must be declared on its own activities"
        )


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--upstream", help="upstream app_pojavlauncher/src/main/res directory")
    args = parser.parse_args()

    defined = collect_defined_resources()
    check_resources(defined)
    classes = index_classes()
    check_backend_class_refs(classes)
    check_manifest(classes)

    if args.upstream:
        upstream: dict[str, set[str]] = defaultdict(set)
        for dirpath, _dirnames, filenames in os.walk(args.upstream):
            folder = os.path.basename(dirpath).split("-")[0]
            for name in filenames:
                if folder == "values" and name.endswith(".xml"):
                    try:
                        root = ET.parse(os.path.join(dirpath, name)).getroot()
                    except ET.ParseError:
                        continue
                    for child in root:
                        res_type = child.tag
                        if res_type == "item":
                            # <item name="x" type="id" /> declares an id, not an item
                            res_type = child.get("type") or "item"
                        if res_type in ARRAY_ALIASES:
                            res_type = "array"
                        if child.get("name"):
                            upstream[res_type].add(child.get("name"))
                elif folder in FILE_TYPES:
                    upstream[folder].add(resource_name(name))
        gaps = [
            f"{res_type}/{name}"
            for res_type, names in upstream.items()
            for name in names
            if name not in defined.get(res_type, set())
            and not is_library_resource(res_type, name)
        ]
        print(f"backend resources not ported: {len(gaps)}")
        for gap in sorted(gaps):
            print(f"  - {gap}")

    for warning in warnings:
        print(f"warning: {warning}")
    if errors:
        print(f"\n{len(errors)} problem(s) found:\n")
        for problem in errors:
            print(f"  - {problem}")
        return 1
    print("backend reference check passed")
    return 0


if __name__ == "__main__":
    sys.exit(main())
