from __future__ import annotations

import shutil
from pathlib import Path

import pytest

from harness import coverage
from harness.cobol import Cobol, cobc_available

BUILD_DIR = Path(__file__).resolve().parent / ".build"
_cobol: Cobol | None = None


def pytest_addoption(parser):
    parser.addoption("--cobol-cov-fail-under", type=float, default=None,
                     help="fail if total COBOL statement coverage is below this percentage")


@pytest.fixture(scope="session")
def cobol() -> Cobol:
    global _cobol
    if not cobc_available():
        pytest.skip("GnuCOBOL (cobc) is not installed")
    if _cobol is None:
        shutil.rmtree(BUILD_DIR, ignore_errors=True)
        _cobol = Cobol(BUILD_DIR)
    return _cobol


def pytest_sessionfinish(session, exitstatus):
    if _cobol is None or not _cobol.traced_programs:
        return
    report = coverage.collect(_cobol.src_dir, _cobol.trace_dir, _cobol.traced_programs)
    coverage.write_json(report, BUILD_DIR / "coverage.json")
    session.config._cobol_cov_report = report
    threshold = session.config.getoption("--cobol-cov-fail-under")
    hit, total = coverage.totals(report)
    if threshold is not None and 100 * hit / max(total, 1) < threshold and exitstatus == 0:
        session.exitstatus = pytest.ExitCode.TESTS_FAILED


def pytest_terminal_summary(terminalreporter, exitstatus, config):
    report = getattr(config, "_cobol_cov_report", None)
    if report is None:
        return
    terminalreporter.write_sep("-", "COBOL statement coverage (app/cbl)")
    terminalreporter.write_line(coverage.format_table(report))
    terminalreporter.write_line(f"details: {BUILD_DIR / 'coverage.json'}")
    threshold = config.getoption("--cobol-cov-fail-under")
    hit, total = coverage.totals(report)
    pct = 100 * hit / max(total, 1)
    if threshold is not None and pct < threshold:
        terminalreporter.write_line(
            f"FAIL: COBOL statement coverage {pct:.1f}% is below {threshold}%", red=True)
