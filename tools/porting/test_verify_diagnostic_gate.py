"""Regression tests for diagnostic compilation exit gates."""
import unittest
from verify_diagnostic_gate import verify


class DiagnosticGateTests(unittest.TestCase):
    def test_compilation_passes_only_with_sources_and_zero_exit(self):
        self.assertTrue(verify({'exit_code': 0, 'displayed_errors': 0, 'source_count': 5, 'timed_out': False})[0])

    def test_zero_diagnostics_on_failed_compiler_is_failure(self):
        self.assertFalse(verify({'exit_code': 1, 'displayed_errors': 0, 'source_count': 5, 'timed_out': False})[0])

    def test_zero_sources_is_failure(self):
        self.assertFalse(verify({'exit_code': 0, 'displayed_errors': 0, 'source_count': 0, 'timed_out': False})[0])

    def test_timeout_is_failure(self):
        self.assertFalse(verify({'exit_code': 0, 'displayed_errors': 0, 'source_count': 5, 'timed_out': True})[0])

    def test_errors_are_failure(self):
        self.assertFalse(verify({'exit_code': 0, 'displayed_errors': 1, 'source_count': 5, 'timed_out': False})[0])


if __name__ == '__main__':
    unittest.main()
