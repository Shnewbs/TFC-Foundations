"""Exercise real Git histories, including stacked and already-integrated work."""
from pathlib import Path
import subprocess
import tempfile
import unittest

from check_feature_branches import audit_branch, check, git, validate_registry


class FeatureBranchTests(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.repo = Path(self.temp.name)
        git(self.repo, 'init', '--initial-branch=26.1.2')
        git(self.repo, 'config', 'user.name', 'Branch Test')
        git(self.repo, 'config', 'user.email', 'branch-test@example.invalid')
        self.commit('base')
        git(self.repo, 'branch', 'feature/26.1.2/food')
        git(self.repo, 'branch', 'feature/26.1.2/animals')

    def commit(self, name):
        (self.repo / name).write_text(name)
        git(self.repo, 'add', name)
        git(self.repo, 'commit', '-m', name)

    def audit(self):
        return audit_branch(self.repo, '26.1.2', 'feature/26.1.2/food',
                            ['feature/26.1.2/animals'])

    def registry(self):
        return {'schema_version': 1, 'branch_pattern': 'feature/{target}/{system}',
                'integration_targets': ['26.1.2'],
                'systems': [{'id': name, 'requires_unmerged_features': []}
                            for name in ['food', 'animals']]}

    def test_independent_feature_commits_pass(self):
        git(self.repo, 'switch', 'feature/26.1.2/food')
        self.commit('food-change')
        git(self.repo, 'switch', 'feature/26.1.2/animals')
        self.commit('animal-change')
        self.assertEqual([], self.audit()['errors'])

    def test_stacked_sibling_is_rejected(self):
        git(self.repo, 'switch', 'feature/26.1.2/animals')
        self.commit('animal-change')
        git(self.repo, 'switch', 'feature/26.1.2/food')
        git(self.repo, 'merge', '--ff-only', 'feature/26.1.2/animals')
        self.assertTrue(any('Unmerged sibling' in error for error in self.audit()['errors']))

    def test_shared_change_landed_in_base_is_allowed(self):
        git(self.repo, 'switch', 'feature/26.1.2/animals')
        self.commit('shared-contract')
        git(self.repo, 'switch', '26.1.2')
        git(self.repo, 'merge', '--ff-only', 'feature/26.1.2/animals')
        git(self.repo, 'switch', 'feature/26.1.2/food')
        git(self.repo, 'merge', '--ff-only', '26.1.2')
        self.commit('food-change')
        self.assertEqual([], self.audit()['errors'])

    def test_stale_feature_requires_refresh(self):
        self.commit('new-core-contract')
        self.assertTrue(any('Refresh' in error for error in self.audit()['errors']))

    def test_unknown_version_or_system_is_rejected(self):
        for branch in ['feature/26.x/food', 'feature/26.1.2/unknown']:
            with self.assertRaises(ValueError):
                check(self.repo, self.registry(), branch=branch)

    def test_missing_feature_fails(self):
        git(self.repo, 'branch', '-D', 'feature/26.1.2/animals')
        self.assertTrue(all(result['errors'] for result in check(self.repo, self.registry())))

    def test_declared_sibling_dependency_is_rejected(self):
        registry = self.registry()
        registry['systems'][0]['requires_unmerged_features'] = ['animals']
        with self.assertRaises(ValueError):
            validate_registry(registry)

    def test_shallow_history_is_rejected(self):
        with tempfile.TemporaryDirectory() as dest:
            subprocess.run(['git', 'clone', '--depth=1', self.repo.as_uri(), dest],
                           check=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
            with self.assertRaisesRegex(ValueError, 'full history'):
                check(Path(dest), self.registry())


if __name__ == '__main__':
    unittest.main()
