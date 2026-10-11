"""Regression tests for the compiler diagnostics triage utility."""
import unittest
from compiler_error_report import analyze


class CompilerReportTests(unittest.TestCase):
    def test_multiple_files_and_ordering(self):
        log = ('src/main/java/A.java:12: error: missing API\n'
               'src/main/java/B.java:20: error: type removed\n'
               'src/main/java/A.java:13: error: signature changed\n')
        result = analyze(log)
        self.assertEqual(result['total'], 3)
        self.assertEqual(result['files'][0]['file'], 'src/main/java/A.java')
        self.assertEqual(result['files'][0]['errors'], 2)

    def test_windows_path(self):
        log = 'C:' + chr(92) + 'source' + chr(92) + 'MyClass.java:44: error: cannot find symbol\n'
        result = analyze(log)
        self.assertEqual(result['total'], 1)
        self.assertEqual(result['errors'][0]['file'], 'C:/source/MyClass.java')
        self.assertEqual(result['errors'][0]['line'], 44)

    def test_ignores_warnings(self):
        self.assertEqual(analyze('src/Foo.java:7: warning: deprecated\n')['total'], 0)

    def test_repeated_errors_are_occurrences(self):
        log = 'src/Foo.java:7: error: missing\nsrc/Foo.java:7: error: missing\n'
        self.assertEqual(analyze(log)['total'], 2)

    def test_no_fabricated_success(self):
        self.assertEqual(analyze('error: javac unavailable\n')['total'], 0)


if __name__ == '__main__':
    unittest.main()
