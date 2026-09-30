#!/usr/bin/env python3
"""Turn the Gradle log of a failed CI build into GitHub annotations.

GitHub's job log endpoint is not reachable from every environment, so the
interesting parts of the build log are re-emitted as `::error::` annotations
(which *are* readable through the API) and the trimmed log is committed to the
`ci-logs/` directory of the branch that triggered the run.
"""
import os
import pathlib
import subprocess
import sys

INTERESTING = (
    "error:",
    "FAILURE",
    "What went wrong",
    "Execution failed",
    "Unresolved reference",
    "e: ",
    "Deprecated Gradle features",
    "> Task ",
    "Caused by",
    "Could not",
)

MAX_CHUNK = 3200
ANNOTATION_BUDGET = 20


def interesting_lines(lines):
    wanted = set()
    for index, line in enumerate(lines):
        if any(token in line for token in INTERESTING):
            wanted.update(range(max(0, index - 6), min(len(lines), index + 16)))
    if not wanted:
        wanted.update(range(max(0, len(lines) - 200), len(lines)))
    return [lines[i] for i in sorted(wanted)]


def emit_annotations(lines):
    chunk, size, announced = [], 0, 0
    for line in lines:
        if size + len(line) > MAX_CHUNK or announced >= ANNOTATION_BUDGET:
            if chunk:
                print(f"::error title=gradle failure {announced}::" + "\n".join(chunk))
                announced += 1
            chunk, size = [], 0
            if announced >= ANNOTATION_BUDGET:
                break
        chunk.append(line)
        size += len(line) + 1
    if chunk and announced < ANNOTATION_BUDGET:
        print(f"::error title=gradle failure {announced}::" + "\n".join(chunk))


def commit_log(path: pathlib.Path, text: str):
    branch = os.environ.get("GITHUB_REF_NAME", "")
    if not branch or os.environ.get("GITHUB_ACTIONS") != "true":
        return
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(text, encoding="utf-8", errors="replace")
    commands = [
        ["git", "add", "-f", str(path)],
        ["git", "-c", "user.name=github-actions[bot]",
         "-c", "user.email=41898282+github-actions[bot]@users.noreply.github.com",
         "commit", "-m", "ci: capture the failing gradle log [skip ci]"],
        ["git", "push", "origin", f"HEAD:refs/heads/{branch}"],
    ]
    for command in commands:
        result = subprocess.run(command, capture_output=True, text=True)
        if result.returncode != 0:
            print(f"::warning title=ci report::{' '.join(command)} failed: {result.stderr.strip()[:300]}")
            if command[1] == "push":
                break


def main():
    log = pathlib.Path("build.log")
    if not log.exists():
        print("::error title=gradle::the build produced no log file")
        return 0
    text = log.read_text(errors="replace")
    lines = text.splitlines()
    emit_annotations(interesting_lines(lines))
    # Keep the beginning (configuration errors) and the end (compiler errors).
    tail = "\n".join(lines[-1500:])
    commit_log(pathlib.Path("ci-logs/last-build.log"),
               "# build log of the failing run\n" + text[-400000:])
    sys.stderr.write(tail[-20000:] + "\n")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
